package com.jfireframework.jfire.core.prepare.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.JfirePreparedNotated;
import com.jfireframework.jfire.core.prepare.condition.Conditional;
import com.jfireframework.jfire.core.resolver.impl.MethodBeanInstanceResolver;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Resource
public @interface Configuration
{
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Bean
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
    public class ProcessConfiguration implements JfirePrepare
    {
        
        @Override
        public void prepare(Environment environment)
        {
            List<BeanDefinition> list = new LinkedList<BeanDefinition>();
            for (BeanDefinition beanDefinition : environment.beanDefinitions().values())
            {
                if (Utils.ANNOTATION_UTIL.isPresent(Configuration.class, beanDefinition.getType()))
                {
                    list.add(beanDefinition);
                    continue;
                }
                for (Method method : beanDefinition.getType().getMethods())
                {
                    if (Utils.ANNOTATION_UTIL.isPresent(Bean.class, method))
                    {
                        list.add(beanDefinition);
                        break;
                    }
                }
            }
            processNoConditionBeanMethod(environment, list);
        }

        private void processNoConditionBeanMethod(Environment environment, List<BeanDefinition> list)
        {
            for (BeanDefinition beanDefinition : list)
            {
                if (Utils.ANNOTATION_UTIL.isPresent(Conditional.class, beanDefinition.getType()))
                {
                    continue;
                }
                for (Method method : beanDefinition.getType().getMethods())
                {
                    if (Utils.ANNOTATION_UTIL.isPresent(Conditional.class, method) || Utils.ANNOTATION_UTIL.isPresent(Bean.class, method) == false)
                    {
                        continue;
                    }
                    Bean bean = Utils.ANNOTATION_UTIL.getAnnotation(Bean.class, method);
                    String beanName = StringUtil.isNotBlank(bean.name()) ? bean.name() : method.getName();
                    BeanDefinition methodBeanDefinition = new BeanDefinition(beanName, method.getReturnType(), bean.prototype());
                    methodBeanDefinition.setBeanInstanceResolver(new MethodBeanInstanceResolver(method));
                    environment.registerBeanDefinition(methodBeanDefinition);
                }
            }
        }
    }
}
