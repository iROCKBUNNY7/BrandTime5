package com.jfireframework.jfire.condition.provide;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.condition.Conditional;
import com.jfireframework.jfire.condition.provide.ConditionOnMissBeanType.OnMissBeanType;
import com.jfireframework.jfire.config.environment.Environment.ReadOnlyEnvironment;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnMissBeanType.class)
public @interface ConditionOnMissBeanType
{
    public Class<?>[] value();
    
    public static class OnMissBeanType extends BaseCondition<ConditionOnMissBeanType>
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
                if (readOnlyEnvironment.isBeanDefinitionExist(each))
                {
                    return false;
                }
            }
            return true;
        }
        
    }
}
