package com.jfireframework.jfire.support.JfireAwareContextInited;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.kernel.JfireAwareContextInited;
import com.jfireframework.jfire.kernel.Order;
import com.jfireframework.jfire.support.SupportConstant;
import com.jfireframework.jfire.support.JfireAwareContextInited.InstanceSingtonBeanAsap.InstanceSingtonBean;
import com.jfireframework.jfire.support.JfirePrepared.Import;

@Import(InstanceSingtonBean.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface InstanceSingtonBeanAsap
{
    @Order(SupportConstant.INSTANCE_SINGTON_BEAN_ORDER)
    class InstanceSingtonBean implements JfireAwareContextInited
    {
        
        @Override
        public void awareContextInited(ReadOnlyEnvironment readOnlyEnvironment)
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
