package com.jfireframework.jfire.core.prepare.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.condition.Condition;
import com.jfireframework.jfire.core.prepare.annotation.condition.Conditional;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Bean;
import com.jfireframework.jfire.core.prepare.annotation.configuration.ConfigAfter;
import com.jfireframework.jfire.core.prepare.annotation.configuration.ConfigBefore;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfireframework.jfire.core.resolver.impl.MethodBeanInstanceResolver;
import com.jfireframework.jfire.exception.ConditionCannotInstanceException;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

public class ConfigurationProcessor implements JfirePrepare
{
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationProcessor.class);
    
    @Override
    public void prepare(Environment environment)
    {
        List<BeanDefinition> list = findConfigurationBeanDefinition(environment);
        logOrder(list);
        list = new SortList(list).sort();
        logger.trace("traceId:{} 修正排序完毕", TRACEID.currentTraceId());
        logOrder(list);
        for (BeanDefinition beanDefinition : list)
        {
            if (Utils.ANNOTATION_UTIL.isPresent(Conditional.class, beanDefinition.getType()))
            {
                boolean match = matchCondition(environment, Utils.ANNOTATION_UTIL.getAnnotation(Conditional.class, beanDefinition.getType()), beanDefinition.getType().getAnnotations());
                if (match == false)
                {
                    continue;
                }
            }
            for (Method method : beanDefinition.getType().getMethods())
            {
                if (Utils.ANNOTATION_UTIL.isPresent(Bean.class, method) == false)
                {
                    continue;
                }
                // 没有包含条件的直接注册
                else if (Utils.ANNOTATION_UTIL.isPresent(Conditional.class, method) == false)
                {
                    registerMethodBeanDefinition(method, environment);
                }
                // 判断条件是否成功
                else if (matchCondition(environment, Utils.ANNOTATION_UTIL.getAnnotation(Conditional.class, method), method.getAnnotations()))
                {
                    registerMethodBeanDefinition(method, environment);
                }
            }
        }
    }
    
    @Override
    public int order()
    {
        return JfirePreparedConstant.CONFIGURATION_ORDER;
    }
    
    /**
     * @param environment
     * @return
     */
    private List<BeanDefinition> findConfigurationBeanDefinition(Environment environment)
    {
        String traceId = TRACEID.currentTraceId();
        List<BeanDefinition> list = new LinkedList<BeanDefinition>();
        for (BeanDefinition beanDefinition : environment.beanDefinitions().values())
        {
            if (Utils.ANNOTATION_UTIL.isPresent(Configuration.class, beanDefinition.getType()))
            {
                logger.debug("traceId:{} 发现配置类:{}", traceId, beanDefinition.getType());
                list.add(beanDefinition);
                continue;
            }
        }
        return list;
    }
    
    private void logOrder(List<BeanDefinition> list)
    {
        if (logger.isDebugEnabled())
        {
            int index = 1;
            String traceId = TRACEID.currentTraceId();
            for (BeanDefinition beanDefinition : list)
            {
                logger.trace("traceId:{} 顺序:{}为{}", traceId, index, beanDefinition.getType());
                index++;
            }
        }
    }
    
    class SortList
    {
        SortList.SortEntry head;
        
        SortList(List<BeanDefinition> list)
        {
            for (BeanDefinition each : list)
            {
                if (head == null)
                {
                    head = new SortList.SortEntry(each);
                }
                else
                {
                    SortList.SortEntry newHead = new SortList.SortEntry(each);
                    newHead.next = head;
                    head.pre = newHead;
                    head = newHead;
                }
            }
            
        }
        
        List<BeanDefinition> sort()
        {
            SortList.SortEntry entry = head;
            AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
            logOrder();
            while (entry != null)
            {
                if (annotationUtil.isPresent(ConfigBefore.class, entry.value.getType()))
                {
                    ConfigBefore configBefore = annotationUtil.getAnnotation(ConfigBefore.class, entry.value.getType());
                    SortList.SortEntry index = entry.pre;
                    while (index != null && index.value.getType() != configBefore.value())
                    {
                        index = index.pre;
                    }
                    if (index != null)
                    {
                        logger.trace("traceId:{}将{}移动到{}前面", TRACEID.currentTraceId(), entry.value.getType().getName(), index.value.getType().getName());
                        remove(entry);
                        addBefore(entry, index);
                        entry = head;
                        continue;
                    }
                }
                if (annotationUtil.isPresent(ConfigAfter.class, entry.value.getType()))
                {
                    ConfigAfter configAfter = annotationUtil.getAnnotation(ConfigAfter.class, entry.value.getType());
                    SortList.SortEntry index = entry.next;
                    while (index != null && index.value.getType() != configAfter.value())
                    {
                        index = index.next;
                    }
                    if (index != null)
                    {
                        logger.trace("traceId:{}将{}移动到{}前面", TRACEID.currentTraceId(), index.value.getType().getName(), entry.value.getType().getName());
                        remove(index);
                        addBefore(index, entry);
                        entry = head;
                        continue;
                    }
                }
                entry = entry.next;
            }
            entry = head;
            List<BeanDefinition> beanDefinitions = new LinkedList<BeanDefinition>();
            while (entry != null)
            {
                beanDefinitions.add(entry.value);
                entry = entry.next;
            }
            return beanDefinitions;
        }
        
        void remove(SortList.SortEntry entry)
        {
            if (entry.pre == null)
            {
                head = entry.next;
                head.pre = null;
            }
            else
            {
                SortList.SortEntry pre = entry.pre;
                SortList.SortEntry next = entry.next;
                pre.next = next;
                if (next != null)
                {
                    next.pre = pre;
                }
            }
            entry.pre = entry.next = null;
        }
        
        void addBefore(SortList.SortEntry add, SortList.SortEntry index)
        {
            SortList.SortEntry pre = index.pre;
            if (pre == null)
            {
                add.next = head;
                head.pre = add;
                head = add;
            }
            else
            {
                pre.next = add;
                add.pre = pre;
                add.next = index;
                index.pre = add;
            }
        }
        
        void logOrder()
        {
            if (logger.isDebugEnabled())
            {
                SortList.SortEntry head = this.head;
                String traceId = TRACEID.currentTraceId();
                int index = 1;
                while (head != null)
                {
                    logger.trace("traceId:{} 顺序{}为{}", traceId, index, head.value.getType().getName());
                    head = head.next;
                    index++;
                }
            }
        }
        
        class SortEntry
        {
            SortList.SortEntry pre;
            SortList.SortEntry next;
            BeanDefinition     value;
            
            public SortEntry(BeanDefinition value)
            {
                this.value = value;
            }
        }
    }
    
    /**
     * @param environment
     * @return
     */
    private boolean matchCondition(Environment environment, Conditional conditional, Annotation[] annotations)
    {
        boolean match = true;
        for (Class<? extends Condition> conditionClass : conditional.value())
        {
            try
            {
                Condition condition = conditionClass.newInstance();
                if (condition.match(environment.readOnlyEnvironment(), annotations) == false)
                {
                    match = false;
                    break;
                }
            }
            catch (Exception e)
            {
                throw new ConditionCannotInstanceException(conditionClass, e);
            }
        }
        return match;
    }
    
    private void registerMethodBeanDefinition(Method method, Environment environment)
    {
        Bean bean = Utils.ANNOTATION_UTIL.getAnnotation(Bean.class, method);
        String beanName = StringUtil.isNotBlank(bean.name()) ? bean.name() : method.getName();
        BeanDefinition methodBeanDefinition = new BeanDefinition(beanName, method.getReturnType(), bean.prototype());
        methodBeanDefinition.setBeanInstanceResolver(new MethodBeanInstanceResolver(method));
        environment.registerBeanDefinition(methodBeanDefinition);
        logger.debug("traceId:{} 注册方法Bean:{}", TRACEID.currentTraceId(), method.getDeclaringClass().getSimpleName() + "." + method.getName());
    }
}
