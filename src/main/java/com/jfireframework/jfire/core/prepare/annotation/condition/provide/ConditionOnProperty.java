package com.jfireframework.jfire.core.prepare.annotation.condition.provide;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.bytecode.annotation.ValuePair;
import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.annotation.condition.Conditional;
import com.jfireframework.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfireframework.jfire.core.prepare.annotation.condition.provide.ConditionOnProperty.OnProperty;

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

    class OnProperty extends BaseCondition
    {

        public OnProperty()
        {
            super(ConditionOnProperty.class);
        }

        @Override
        protected boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, AnnotationMetadata annotation, ErrorMessage errorMessage)
        {
            for (ValuePair each : annotation.getAttribyte("value").getArray())
            {
                if (readOnlyEnvironment.hasProperty(each.getStringValue()) == false)
                {
                    errorMessage.addErrorMessage("缺少属性:" + each);
                    return false;
                }
            }
            return true;
        }
    }
}
