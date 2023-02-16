package com.jfireframework.jfire.core.prepare.annotation.condition.provide;

import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.annotation.condition.Conditional;
import com.jfireframework.jfire.core.prepare.annotation.condition.provide.ConditionOnAnnotation.OnAnnotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnAnnotation.class)
public @interface ConditionOnAnnotation
{
    Class<? extends Annotation>[] value();

    class OnAnnotation extends BaseCondition<ConditionOnAnnotation>
    {

        public OnAnnotation()
        {
            super(ConditionOnAnnotation.class);
        }

        @Override
        protected boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, ConditionOnAnnotation conditionOnAnnotation)
        {
            for (Class<? extends Annotation> type : conditionOnAnnotation.value())
            {
                if ( readOnlyEnvironment.isAnnotationPresent(type) == false )
                {
                    return false;
                }
            }
            return true;
        }

    }
}
