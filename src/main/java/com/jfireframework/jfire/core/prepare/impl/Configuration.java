package com.jfireframework.jfire.core.prepare.impl;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.JfirePreparedNotated;
import com.jfireframework.jfire.core.prepare.condition.Condition;
import com.jfireframework.jfire.core.prepare.condition.Conditional;
import com.jfireframework.jfire.core.resolver.impl.MethodBeanInstanceResolver;
import com.jfireframework.jfire.exception.ConditionCannotInstanceException;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
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

    }

    @JfirePreparedNotated(order = JfirePreparedConstant.CONFIGURATION_ORDER)
    class ConfigurationProcessor implements JfirePrepare
    {
        private static final Logger logger = LoggerFactory.getLogger(ConfigurationProcessor.class);

        @Override
        public void prepare(Environment environment)
        {
            List<BeanDefinition> list = findConfigurationBeanDefinition(environment);
            processNoConditionBeanMethod(environment, list);
            for (BeanDefinition beanDefinition : list)
            {
                if ( Utils.ANNOTATION_UTIL.isPresent(Conditional.class, beanDefinition.getType()) )
                {
                    boolean match = matchCondition(environment, Utils.ANNOTATION_UTIL.getAnnotation(Conditional.class, beanDefinition.getType()), beanDefinition.getType().getAnnotations());
                    if ( match == false )
                    {
                        continue;
                    }
                    for (Method method : beanDefinition.getType().getMethods())
                    {
                        if ( Utils.ANNOTATION_UTIL.isPresent(Bean.class, method) == false )
                        {
                            continue;
                        }
                        // 没有包含条件的直接注册
                        else if ( Utils.ANNOTATION_UTIL.isPresent(Conditional.class, method) == false )
                        {
                            registerMethodBeanDefinition(method, environment);
                        }
                        // 判断条件是否成功
                        else if ( matchCondition(environment, Utils.ANNOTATION_UTIL.getAnnotation(Conditional.class, method), method.getAnnotations()) )
                        {
                            registerMethodBeanDefinition(method, environment);
                        }
                    }
                }
                else
                {
                    // 寻找有条件注解的方法执行注册
                    for (Method method : beanDefinition.getType().getMethods())
                    {
                        if ( Utils.ANNOTATION_UTIL.isPresent(Bean.class, method) == false || Utils.ANNOTATION_UTIL.isPresent(Conditional.class, method) == false )
                        {
                            continue;
                        }
                        if ( matchCondition(environment, Utils.ANNOTATION_UTIL.getAnnotation(Conditional.class, method), method.getAnnotations()) )
                        {
                            registerMethodBeanDefinition(method, environment);
                        }
                    }

                }
            }
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
                if ( Utils.ANNOTATION_UTIL.isPresent(Configuration.class, beanDefinition.getType()) )
                {
                    logger.debug("traceId:{} 发现配置类:{}", traceId, beanDefinition.getType());
                    list.add(beanDefinition);
                    continue;
                }
            }
            return list;
        }

        /**
         * @param environment
         * @param beanDefinition
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
                    if ( condition.match(environment.readOnlyEnvironment(), annotations) == false )
                    {
                        match = false;
                        break;
                    }
                } catch (Exception e)
                {
                    throw new ConditionCannotInstanceException(conditionClass, e);
                }
            }
            return match;
        }

        private void processNoConditionBeanMethod(Environment environment, List<BeanDefinition> list)
        {
            for (BeanDefinition beanDefinition : list)
            {
                if ( Utils.ANNOTATION_UTIL.isPresent(Conditional.class, beanDefinition.getType()) )
                {
                    continue;
                }
                for (Method method : beanDefinition.getType().getMethods())
                {
                    if ( Utils.ANNOTATION_UTIL.isPresent(Conditional.class, method) || Utils.ANNOTATION_UTIL.isPresent(Bean.class, method) == false )
                    {
                        continue;
                    }
                    registerMethodBeanDefinition(method, environment);
                }
            }
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
}
