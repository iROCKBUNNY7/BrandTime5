package com.jfireframework.jfire.core.prepare.condition.provide;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.kernel.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.support.JfirePrepared.configuration.condition.Conditional;
import com.jfireframework.jfire.support.JfirePrepared.configuration.condition.provide.ConditionOnProperty.OnProperty;

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
