package com.jfireframework.jfire.core.prepare.impl;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.order.Order;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.JfirePreparedNotated;
import com.jfireframework.jfire.core.prepare.impl.Configuration.ProcessConfiguration.ConfigurationOrder;
import com.jfireframework.jfire.core.resolver.BeanInstanceResolver;
import com.jfireframework.jfire.support.JfirePrepared.configuration.condition.Conditional;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Resource
public @interface Configuration
{
    int Order() default 0;
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Bean
    {
        /**
         * bean的名称，如果不填写的话，默认为方法名
         * 
         * @return
         */
        String name() default "";
        
        int order() default 0;
        
        boolean prototype() default false;
        
    }
    
    @JfirePreparedNotated(order = JfirePreparedConstant.CONFIGURATION_ORDER)
    public class ProcessConfiguration implements JfirePrepare
    {
        
        private Comparator<Method> comparator = new Comparator<Method>() {
            
            @Override
            public int compare(Method o1, Method o2)
            {
                int order1 = Utils.ANNOTATION_UTIL.getAnnotation(Bean.class, o1).order();
                int order2 = Utils.ANNOTATION_UTIL.getAnnotation(Bean.class, o2).order();
                return order1 - order2;
            }
        };
        
        private void processConditionMethod(Environment environment, List<ConfigurationOrder> configurationOrders)
        {
            for (ConfigurationOrder each : configurationOrders)
            {
                Class<?> type = each.type;
                if (annotationUtil.isPresent(Conditional.class, type) && //
                        match(annotationUtil.getAnnotations(Conditional.class, type), type.getAnnotations(), environment) == false)
                {
                    continue;
                }
                for (Method method : each.methods)
                {
                    if (annotationUtil.isPresent(Conditional.class, method) && //
                            match(annotationUtil.getAnnotations(Conditional.class, method), method.getAnnotations(), environment) == false)
                    {
                        continue;
                    }
                    environment.registerBeanDefinition(generated(method));
                }
            }
        }
        
        /*
         * 处理没有条件相关的方法。
         * 没有条件相关指的是该方法所在的bean没有被@Conditional注解，该方法没有被@Conditional注解.
         * 处理完毕后，这些方法会从ConfigurationOrder.methods中被删除
         */
        private void processNoConditionMethod(Environment environment, List<BeanDefinition> beanDefinitions)
        {
            AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
            for (BeanDefinition each : beanDefinitions)
            {
                if (annotationUtil.isPresent(Conditional.class, each.getType()))
                {
                    continue;
                }
                for (Method method : each.getType().getMethods())
                {
                    if (annotationUtil.isPresent(Conditional.class, method) || annotationUtil.isPresent(Bean.class, method) == false)
                    {
                        continue;
                    }
                    Bean bean = annotationUtil.getAnnotation(Bean.class, method);
                    String beanName = StringUtil.isNotBlank(bean.name()) ? bean.name() : method.getName();
                }
            }
            for (ConfigurationOrder each : configurationOrders)
            {
                Class<?> type = each.type;
                if (annotationUtil.isPresent(Conditional.class, type))
                {
                    continue;
                }
                List<Method> needDeletes = new ArrayList<Method>();
                for (Method method : each.methods)
                {
                    if (annotationUtil.isPresent(Conditional.class, method))
                    {
                        continue;
                    }
                    environment.registerBeanDefinition(generated(method, type, annotationUtil));
                    needDeletes.add(method);
                }
                each.methods.removeAll(needDeletes);
            }
        }
        
        private List<ConfigurationOrder> findConfigurationBeanDefinition(Environment environment)
        {
            List<ConfigurationOrder> configurationOrders = new ArrayList<ProcessConfiguration.ConfigurationOrder>();
            for (BeanDefinition each : environment.getBeanDefinitions().values())
            {
                if (annotationUtil.isPresent(Configuration.class, each.getType()))
                {
                    int order = annotationUtil.isPresent(Order.class, each.getType()) ? annotationUtil.getAnnotation(Order.class, each.getType()).value() : 0;
                    ConfigurationOrder configurationOrder = new ConfigurationOrder();
                    configurationOrder.type = each.getType();
                    configurationOrder.order = order;
                    for (Method method : each.getType().getDeclaredMethods())
                    {
                        if (annotationUtil.isPresent(Bean.class, method))
                        {
                            configurationOrder.methods.add(method);
                        }
                    }
                    Collections.sort(configurationOrder.methods, comparator);
                    configurationOrders.add(configurationOrder);
                }
            }
            Collections.sort(configurationOrders, new Comparator<ConfigurationOrder>() {
                
                @Override
                public int compare(ConfigurationOrder o1, ConfigurationOrder o2)
                {
                    return o1.order - o2.order;
                }
            });
            return configurationOrders;
        }
        
        boolean match(List<Conditional> conditionals, Annotation[] annotations, Environment environment)
        {
            for (Conditional conditional : conditionals)
            {
                for (Class<? extends Condition> type : conditional.value())
                {
                    Condition condition = getCondition(type);
                    if (condition.match(environment.readOnlyEnvironment(), annotations) == false)
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        
        Condition getCondition(Class<? extends Condition> ckass)
        {
            Condition instance = conditionImplStore.get(ckass);
            if (instance == null)
            {
                try
                {
                    instance = ckass.newInstance();
                    conditionImplStore.put(ckass, instance);
                }
                catch (Exception e)
                {
                    throw new JustThrowException(e);
                }
            }
            return instance;
        }
        
        private BeanDefinition generated(Method method)
        {
            Bean bean = annotationUtil.getAnnotation(Bean.class, method);
            String beanName = StringUtil.isNotBlank(bean.name()) ? bean.name() : method.getName();
            BeanDefinition beanDefinition = new BeanDefinition(beanName, method.getReturnType(), bean.prototype());
            BeanInstanceResolver beanInstanceResolver = new MethodBeanInstanceResolver(method);
            beanDefinition.setBeanInstanceResolver(beanInstanceResolver);
            return beanDefinition;
        }
        
        @Override
        public void prepare(Environment environment)
        {
            List<BeanDefinition> list = new LinkedList<BeanDefinition>();
            for (BeanDefinition beanDefinition : environment.beanDefinitions().values())
            {
                if (Utils.ANNOTATION_UTIL.isPresent(Configuration.class, beanDefinition.getType()))
                {
                    list.add(beanDefinition);
                }
            }
            processNoConditionMethod(environment, configurationOrders);
            processConditionMethod(environment, configurationOrders);
        }
    }
}
