package com.jfireframework.jfire.core.prepare.condition.provide;

import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.condition.Conditional;
import com.jfireframework.jfire.core.prepare.condition.provide.ConditionOnProperty.OnProperty;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnProperty.class)
public @interface ConditionOnProperty
{
    /**
     * 需要存在的属性名称
     *
     * @return
     */
    String[] value();

    class OnProperty extends BaseCondition<ConditionOnProperty>
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
                if ( readOnlyEnvironment.hasProperty(each) == false )
                {
                    return false;
                }
            }
            return true;
        }

    }
}
