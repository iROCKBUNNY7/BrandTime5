package com.jfireframework.jfire.aware.provider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.aware.JfireAwareConstructBeanFinished;
import com.jfireframework.jfire.aware.provider.InstanceSingtonBeanAsap.instanceSingtonBean;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.config.annotation.Import;
import com.jfireframework.jfire.config.environment.Environment.ReadOnlyEnvironment;

@Import(instanceSingtonBean.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface InstanceSingtonBeanAsap
{
    class instanceSingtonBean implements JfireAwareConstructBeanFinished
    {
        
        @Override
        public void awareConstructBeanFinished(ReadOnlyEnvironment readOnlyEnvironment)
        {
            for (BeanDefinition beanDefinition : readOnlyEnvironment.beanDefinitions())
            {
                if (beanDefinition.isPrototype() == false)
                {
                    beanDefinition.getInstance();
                }
            }
        }
        
    }
}
