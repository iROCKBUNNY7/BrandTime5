package com.jfireframework.jfire.core.prepare.condition.provide;

import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.condition.Conditional;
import com.jfireframework.jfire.core.prepare.condition.provide.ConditionOnMissBeanType.OnMissBeanType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnMissBeanType.class)
public @interface ConditionOnMissBeanType
{
    Class<?>[] value();

    class OnMissBeanType extends BaseCondition<ConditionOnMissBeanType>
    {

        public OnMissBeanType()
        {
            super(ConditionOnMissBeanType.class);
        }

        @Override
        protected boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, ConditionOnMissBeanType conditionOnMissBeanType)
        {
            for (Class<?> each : conditionOnMissBeanType.value())
            {
                boolean match = false;
                for (BeanDefinition beanDefinition : readOnlyEnvironment.beanDefinitions())
                {
                    if ( each.isAssignableFrom(beanDefinition.getType()) )
                    {
                        match = true;
                        break;
                    }
                }
                if ( match )
                {
                    return false;
                }
            }
            return true;
        }

    }
}
