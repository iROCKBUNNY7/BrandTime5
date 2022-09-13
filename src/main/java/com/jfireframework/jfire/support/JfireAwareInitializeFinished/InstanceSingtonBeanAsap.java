package com.jfireframework.jfire.support.JfireAwareInitializeFinished;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.JfireAwareInitializeFinished;
import com.jfireframework.jfire.kernel.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.support.JfireAwareInitializeFinished.InstanceSingtonBeanAsap.instanceSingtonBean;
import com.jfireframework.jfire.support.JfirePrepared.Import;

@Import(instanceSingtonBean.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface InstanceSingtonBeanAsap
{
    class instanceSingtonBean implements JfireAwareInitializeFinished
    {
        
        @Override
        public void awareInitializeFinished(ReadOnlyEnvironment readOnlyEnvironment)
        {
            Map<String, Object> beanInstanceMap = new HashMap<String, Object>();
            for (BeanDefinition beanDefinition : readOnlyEnvironment.beanDefinitions())
            {
                if (beanDefinition.isPrototype() == false)
                {
                    beanDefinition.getBeanInstanceResolver().getInstance(beanInstanceMap);
                }
            }
        }
        
    }
}
