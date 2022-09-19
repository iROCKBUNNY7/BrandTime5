package com.jfireframework.jfire.support.JfirePrepared;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.BeanInstanceResolver;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.kernel.JfirePrepared;
import com.jfireframework.jfire.kernel.Order;
import com.jfireframework.jfire.support.BeanInstanceResolver.MethodBeanInstanceResolver;
import com.jfireframework.jfire.support.JfirePrepared.condition.Condition;
import com.jfireframework.jfire.support.JfirePrepared.condition.Conditional;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Resource
public @interface Configuration
{
    @Retention(RetentionPolicy.RUNTIME)
    @interface Bean
    {
        /**
         * bean的名称，如果不填写的话，默认为方法名
         * 
         * @return
         */
        String name() default "";
        
        boolean prototype() default false;
        
        String destroyMethod() default "";
    }
    
    @Order(20)
    class ProcessConfiguration implements JfirePrepared
    {
        class ConfigurationOrder
        {
            int          order;
            Class<?>     type;
            List<Method> methods = new ArrayList<Method>();;
        }
        
        @Override
        public void prepared(Environment environment)
        {
            final AnnotationUtil annotationUtil = new AnnotationUtil();
            Comparator<Method> comparator = new Comparator<Method>() {
                
                @Override
                public int compare(Method o1, Method o2)
                {
                    int order1 = annotationUtil.isPresent(Order.class, o1) ? annotationUtil.getAnnotation(Order.class, o1).value() : 0;
                    int order2 = annotationUtil.isPresent(Order.class, o2) ? annotationUtil.getAnnotation(Order.class, o2).value() : 0;
                    return order1 - order2;
                }
            };
            List<ConfigurationOrder> configurationOrders = new ArrayList<Configuration.ProcessConfiguration.ConfigurationOrder>();
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
            Set<Method> handleds = new HashSet<Method>();
            /** 先将没有条件的Bean注解处理完成 **/
            for (ConfigurationOrder each : configurationOrders)
            {
                Class<?> type = each.type;
                if (annotationUtil.isPresent(Conditional.class, type))
                {
                    continue;
                }
                for (Method method : each.methods)
                {
                    if (annotationUtil.isPresent(Conditional.class, method))
                    {
                        continue;
                    }
                    environment.registerBeanDefinition(generated(method, type, annotationUtil));
                    handleds.add(method);
                }
            }
            /** 先将没有条件的Bean注解处理完成 **/
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
                    if (handleds.contains(method))
                    {
                        continue;
                    }
                    if (annotationUtil.isPresent(Conditional.class, method) && //
                            match(annotationUtil.getAnnotations(Conditional.class, method), method.getAnnotations(), environment) == false)
                    {
                        continue;
                    }
                    environment.registerBeanDefinition(generated(method, type, annotationUtil));
                }
            }
        }
        
        boolean match(Conditional[] conditionals, Annotation[] annotations, Environment environment)
        {
            for (Conditional conditional : conditionals)
            {
                for (Class<? extends Condition> type : conditional.value())
                {
                    Condition condition = environment.getCondition(type);
                    if (condition.match(environment.readOnlyEnvironment(), annotations) == false)
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        
        private BeanDefinition generated(Method method, Class<?> type, AnnotationUtil annotationUtil)
        {
            BeanInstanceResolver resolver = new MethodBeanInstanceResolver(method);
            BeanDefinition beanDefinition = new BeanDefinition(method.getReturnType(), resolver);
            return beanDefinition;
        }
    }
    
}
