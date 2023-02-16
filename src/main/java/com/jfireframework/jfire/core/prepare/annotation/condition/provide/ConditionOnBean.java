package com.jfireframework.jfire.core.prepare.annotation.condition.provide;

import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.annotation.condition.Conditional;
import com.jfireframework.jfire.core.prepare.annotation.condition.provide.ConditionOnBean.OnBean;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Conditional(OnBean.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionOnBean
{
    Class<?>[] value();

    class OnBean extends BaseCondition<ConditionOnBean>
    {

        public OnBean()
        {
            super(ConditionOnBean.class);
        }

        @Override
        protected boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, ConditionOnBean annotation)
        {
            Class<?>[] beanTypes = annotation.value();
            for (Class<?> each : beanTypes)
            {
                boolean miss = true;
                for (BeanDefinition beanDefinition : readOnlyEnvironment.beanDefinitions())
                {
                    if ( each.isAssignableFrom(beanDefinition.getType()) )
                    {
                        miss = false;
                        break;
                    }
                }
                if ( miss )
                {
                    return false;
                }
            }
            return true;
        }

    }
}
