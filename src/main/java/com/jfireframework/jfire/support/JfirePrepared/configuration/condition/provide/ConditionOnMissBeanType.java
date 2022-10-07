package com.jfireframework.jfire.support.JfirePrepared.configuration.condition.provide;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.kernel.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.support.JfirePrepared.configuration.condition.Conditional;
import com.jfireframework.jfire.support.JfirePrepared.configuration.condition.provide.ConditionOnMissBeanType.OnMissBeanType;

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
