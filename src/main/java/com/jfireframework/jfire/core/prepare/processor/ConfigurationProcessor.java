package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.condition.Condition;
import com.jfireframework.jfire.core.prepare.annotation.condition.Conditional;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Bean;
import com.jfireframework.jfire.core.prepare.annotation.configuration.ConfigAfter;
import com.jfireframework.jfire.core.prepare.annotation.configuration.ConfigBefore;
import com.jfireframework.jfire.core.prepare.support.annotaion.AnnotationDatabase;
import com.jfireframework.jfire.core.prepare.support.annotaion.AnnotationInstance;
import com.jfireframework.jfire.core.resolver.impl.DefaultBeanInstanceResolver;
import com.jfireframework.jfire.core.resolver.impl.MethodBeanInstanceResolver;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

public class ConfigurationProcessor implements JfirePrepare
{
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationProcessor.class);

    @Override
    public void prepare(Environment environment)
    {
        List<String> list = new ArrayList<String>(environment.getCandidateConfiguration());
        logOrder(list);
        list = new SortList(list, environment.getAnnotationDatabase()).sort();
        logger.trace("traceId:{} 修正排序完毕", TRACEID.currentTraceId());
        logOrder(list);
        for (String each : list)
        {
            List<AnnotationInstance> conditionalAnnotations = environment.getAnnotationDatabase().getAnnotations(each, Conditional.class);
            if (!conditionalAnnotations.isEmpty())
            {
                boolean pass = true;
                for (AnnotationInstance instance : conditionalAnnotations)
                {
                    if (!matchCondition(environment, instance, environment.getAnnotationDatabase().getAnnotaionOnClass(each)))
                    {
                        pass = false;
                        break;
                    }
                }
                if (pass == false)
                {
                    continue;
                }
            }
            registerDeclaringClassBeanDefinition(each, environment);
            for (Method method : each.getMethods())
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

    private void logOrder(List<String> list)
    {
        if (logger.isDebugEnabled())
        {
            int    index   = 1;
            String traceId = TRACEID.currentTraceId();
            for (String each : list)
            {
                logger.trace("traceId:{} 顺序:{}为{}", traceId, index, each);
                index++;
            }
        }
    }

    class SortList
    {
        SortList.SortEntry head;
        AnnotationDatabase annotationDatabase;

        SortList(List<String> list, AnnotationDatabase annotationDatabase)
        {
            for (String each : list)
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

        List<String> sort()
        {
            SortList.SortEntry entry          = head;
            AnnotationUtil     annotationUtil = Utils.ANNOTATION_UTIL;
            logOrder();
            while (entry != null)
            {
                if (annotationDatabase.isAnnotationPresentOnClass(entry.value, ConfigBefore.class))
                {
                    AnnotationInstance configBefore = annotationDatabase.getAnnotation(entry.value, ConfigBefore.class);
                    SortList.SortEntry index        = entry.pre;
                    Class<?>           value        = (Class<?>) configBefore.getAttributes().get("value");
                    while (index != null && index.value != value.getName())
                    {
                        index = index.pre;
                    }
                    if (index != null)
                    {
                        logger.trace("traceId:{}将{}移动到{}前面", TRACEID.currentTraceId(), entry.value, index.value);
                        remove(entry);
                        addBefore(entry, index);
                        entry = head;
                        continue;
                    }
                }
                if (annotationDatabase.isAnnotationPresentOnClass(entry.value, ConfigAfter.class))
                {
                    AnnotationInstance configAfter = annotationDatabase.getAnnotation(entry.value, ConfigAfter.class);
                    SortList.SortEntry index       = entry.next;
                    Class<?>           value       = (Class<?>) configAfter.getAttributes().get("value");
                    while (index != null && index.value != value.getName())
                    {
                        index = index.next;
                    }
                    if (index != null)
                    {
                        logger.trace("traceId:{}将{}移动到{}前面", TRACEID.currentTraceId(), index.value, entry.value);
                        remove(index);
                        addBefore(index, entry);
                        entry = head;
                        continue;
                    }
                }
                entry = entry.next;
            }
            entry = head;
            List<String> list = new LinkedList<String>();
            while (entry != null)
            {
                list.add(entry.value);
                entry = entry.next;
            }
            return list;
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
                SortList.SortEntry pre  = entry.pre;
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
                SortList.SortEntry head    = this.head;
                String             traceId = TRACEID.currentTraceId();
                int                index   = 1;
                while (head != null)
                {
                    logger.trace("traceId:{} 顺序{}为{}", traceId, index, head.value);
                    head = head.next;
                    index++;
                }
            }
        }

        class SortEntry
        {
            SortList.SortEntry pre;
            SortList.SortEntry next;
            String             value;

            public SortEntry(String value)
            {
                this.value = value;
            }
        }
    }

    private boolean matchCondition(Environment environment, AnnotationInstance conditionalAnnotation, List<AnnotationInstance> annotationInstances)
    {
        boolean    match = true;
        Class<?>[] value = (Class<?>[]) conditionalAnnotation.getAttributes().get("value");
        for (Class<?> each : value)
        {
            try
            {
                Condition instance = (Condition) each.newInstance();
                if (instance.match(environment.readOnlyEnvironment(), annotationInstances) == false)
                {
                    match = false;
                    break;
                }
            }
            catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
        return match;
    }

    private void registerMethodBeanDefinition(Method method, Environment environment)
    {
        Bean           bean                 = Utils.ANNOTATION_UTIL.getAnnotation(Bean.class, method);
        String         beanName             = StringUtil.isNotBlank(bean.name()) ? bean.name() : method.getName();
        BeanDefinition methodBeanDefinition = new BeanDefinition(beanName, method.getReturnType(), bean.prototype());
        methodBeanDefinition.setBeanInstanceResolver(new MethodBeanInstanceResolver(method));
        environment.registerBeanDefinition(methodBeanDefinition);
        logger.debug("traceId:{} 注册方法Bean:{}", TRACEID.currentTraceId(), method.getDeclaringClass().getSimpleName() + "." + method.getName());
    }

    private void registerDeclaringClassBeanDefinition(Class<?> ckass, Environment environment)
    {
        BeanDefinition beanDefinition = new BeanDefinition(ckass.getName(), ckass, false);
        beanDefinition.setBeanInstanceResolver(new DefaultBeanInstanceResolver(ckass));
        environment.registerBeanDefinition(beanDefinition);
        logger.debug("traceId:{} 将配置类:{}注册为一个单例Bean", TRACEID.currentTraceId(), ckass.getName());
    }

    private static final String conditionalName = Conditional.class.getName().replace('.', '/');

    void findConditionalInstance(AnnotationMetadata annotationMetadata, List<AnnotationMetadata> list)
    {
        if (annotationMetadata.isAnnotation(conditionalName))
        {
            list.add(annotationMetadata);
        }
        for (AnnotationMetadata presentAnnotation : annotationMetadata.getPresentAnnotations())
        {
            findConditionalInstance(presentAnnotation, list);
        }
    }
}
