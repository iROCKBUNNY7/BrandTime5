package com.jfireframework.jfire.condition.provide;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.condition.Conditional;
import com.jfireframework.jfire.condition.provide.ConditionOnProperty.OnProperty;
import com.jfireframework.jfire.config.environment.Environment.ReadOnlyEnvironment;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnProperty.class)
public @interface ConditionOnProperty
{
    /**
     * 需要存在的属性名称
     * 
     * @return
     */
    public String[] value();
    
    public static class OnProperty extends BaseCondition<ConditionOnProperty>
    {
        
        public OnProperty()
        {
            super(ConditionOnProperty.class);
        }
        
        @Override
        protected boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, ConditionOnProperty conditionOnProperty)
        {
            for (String each : conditionOnProperty.value())
            {
                if (readOnlyEnvironment.hasProperty(each) == false)
                {
                    return false;
                }
            }
            return true;
        }
        
    }
}
