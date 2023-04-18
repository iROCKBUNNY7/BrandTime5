package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.TRACEID;
import com.jfirer.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfirer.baseutil.bytecode.annotation.ValuePair;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.BeanDefinition;
import com.jfirer.jfire.core.beandescriptor.InstanceDescriptor;
import com.jfirer.jfire.core.beandescriptor.MethodInvokeInstanceDescriptor;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.condition.Condition;
import com.jfirer.jfire.core.prepare.annotation.condition.Conditional;
import com.jfirer.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfirer.jfire.core.prepare.annotation.configuration.Bean;
import com.jfirer.jfire.core.prepare.annotation.configuration.ConfigAfter;
import com.jfirer.jfire.core.prepare.annotation.configuration.ConfigBefore;
import com.jfirer.jfire.util.PrepareConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ConfigurationProcessor implements ContextPrepare
{
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationProcessor.class);

    @Override
    public ApplicationContext.NeedRefresh prepare(ApplicationContext context)
    {
        List<Class<?>> list = new ArrayList<Class<?>>(context.getConfigurationClassSet());
        logOrder(list);
        list = new SortList(list, context.getAnnotationContextFactory()).sort();
        logger.trace("traceId:{} 修正排序完毕", TRACEID.currentTraceId());
        logOrder(list);
        ErrorMessage             errorMessage             = new ErrorMessage();
        AnnotationContextFactory annotationContextFactory = context.getAnnotationContextFactory();
        for (Class<?> each : list)
        {
            errorMessage.clear();
            AnnotationContext        annotationContextOnClass = annotationContextFactory.get(each);
            List<AnnotationMetadata> conditionalAnnotations   = annotationContextOnClass.getAnnotationMetadatas(Conditional.class);
            if (!conditionalAnnotations.isEmpty())
            {
                boolean pass = true;
                for (AnnotationMetadata conditional : conditionalAnnotations)
                {
                    if (!matchCondition(context, conditional, annotationContextOnClass, errorMessage))
                    {
                        pass = false;
                        break;
                    }
                }
                if (pass == false)
                {
                    for (String error : errorMessage.getList())
                    {
                        logger.debug("traceId:{} 配置类:{}不符合条件:{}", TRACEID.currentTraceId(), each, error);
                    }
                    continue;
                }
            }
            Class<?> ckass = each;
            for (Method method : ckass.getDeclaredMethods())
            {
                AnnotationContext annotationContextOnMethod = annotationContextFactory.get(method);
                if (annotationContextOnMethod.isAnnotationPresent(Bean.class) == false)
                {
                    continue;
                }
                // 没有包含条件的直接注册
                else if (annotationContextOnMethod.isAnnotationPresent(Conditional.class) == false)
                {
                    registerMethodBeanDefinition(method, context, annotationContextOnMethod);
                }
                // 判断条件是否成功
                else
                {
                    boolean pass = true;
                    for (AnnotationMetadata conditional : annotationContextOnMethod.getAnnotationMetadatas(Conditional.class))
                    {
                        if (matchCondition(context, conditional, annotationContextOnMethod, errorMessage) == false)
                        {
                            pass = false;
                            break;
                        }
                    }
                    if (pass)
                    {
                        registerMethodBeanDefinition(method, context, annotationContextOnMethod);
                    }
                    else
                    {
                        for (String error : errorMessage.getList())
                        {
                            logger.debug("traceId:{} 配置类:{}不符合条件:{}", TRACEID.currentTraceId(), each, error);
                        }
                    }
                }
            }
        }
        return ApplicationContext.NeedRefresh.NO;
    }

    @Override
    public int order()
    {
        return PrepareConstant.CONFIGURATION_ORDER;
    }

    private void logOrder(List<Class<?>> list)
    {
        if (logger.isDebugEnabled())
        {
            int    index   = 1;
            String traceId = TRACEID.currentTraceId();
            for (Class<?> each : list)
            {
                logger.trace("traceId:{} 顺序:{}为{}", traceId, index, each.getName());
                index++;
            }
        }
    }

    class SortList
    {
        SortList.SortEntry       head;
        AnnotationContextFactory annotationContextFactory;

        SortList(List<Class<?>> list, AnnotationContextFactory annotationContextFactory)
        {
            this.annotationContextFactory = annotationContextFactory;
            for (Class<?> each : list)
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

        List<Class<?>> sort()
        {
            SortList.SortEntry entry = head;
            logOrder();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            while (entry != null)
            {
                AnnotationContext annotationContext = annotationContextFactory.get(entry.value, classLoader);
                if (annotationContext.isAnnotationPresent(ConfigBefore.class))
                {
                    AnnotationMetadata configBefore = annotationContext.getAnnotationMetadata(ConfigBefore.class);
                    SortList.SortEntry index        = entry.pre;
                    String             targetClass  = configBefore.getAttribyte("value").getClassName();
                    while (index != null && index.value.getName().equals(targetClass) == false)
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
                if (annotationContext.isAnnotationPresent(ConfigAfter.class))
                {
                    AnnotationMetadata configAfter = annotationContext.getAnnotationMetadata(ConfigAfter.class);
                    SortList.SortEntry index       = entry.next;
                    String             targetClass = configAfter.getAttribyte("value").getClassName();
                    while (index != null && index.value.getName().equals(targetClass) == false)
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
            List<Class<?>> list = new LinkedList<Class<?>>();
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
            Class<?>           value;

            public SortEntry(Class<?> value)
            {
                this.value = value;
            }
        }
    }

    /**
     * 判断条件注解中的条件是否被符合
     *
     * @param context
     * @param conditional
     * @param annotationContext 在元素上的注解集合。元素可能为class也可能为member
     * @param errorMessage
     * @return
     */
    private boolean matchCondition(ApplicationContext context, AnnotationMetadata conditional, AnnotationContext annotationContext, ErrorMessage errorMessage)
    {
        boolean     match       = true;
        ValuePair[] value       = conditional.getAttribyte("value").getArray();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (ValuePair each : value)
        {
            try
            {
                Condition instance = (Condition) (classLoader.loadClass(each.getClassName())).newInstance();
                if (instance.match(context, annotationContext, errorMessage) == false)
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

    private void registerMethodBeanDefinition(Method method, ApplicationContext context, AnnotationContext annotationContextOnMethod)
    {
        Bean               bean                 = annotationContextOnMethod.getAnnotation(Bean.class);
        String             beanName             = StringUtil.isNotBlank(bean.name()) ? bean.name() : method.getName();
        InstanceDescriptor instanceDescriptor   = new MethodInvokeInstanceDescriptor(method);
        BeanDefinition     methodBeanDefinition = new BeanDefinition(beanName, method.getReturnType(), bean.prototype(), instanceDescriptor);
        context.registerBeanDefinition(methodBeanDefinition);
        logger.debug("traceId:{} 注册方法Bean:{}", TRACEID.currentTraceId(), method.getDeclaringClass().getSimpleName() + "." + method.getName());
    }
}
