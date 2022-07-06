package com.jfireframework.jfire.condition.provide;

import java.lang.annotation.Annotation;
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
    
    public static class OnMissBeanType extends BaseCondition
    {
        
        public OnMissBeanType()
        {
            super(ConditionOnMissBeanType.class);
        }
        
        @Override
        protected boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, Annotation annotation)
        {
            for (Class<?> each : ((ConditionOnMissBeanType) annotation).value())
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
