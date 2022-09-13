package com.jfireframework.jfire.support.JfirePrepared.condition.provide;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.kernel.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.support.JfirePrepared.condition.Conditional;
import com.jfireframework.jfire.support.JfirePrepared.condition.provide.ConditionOnAnnotation.OnAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnAnnotation.class)
public @interface ConditionOnAnnotation
{
    public Class<? extends Annotation>[] value();
    
    public static class OnAnnotation extends BaseCondition<ConditionOnAnnotation>
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
                if (readOnlyEnvironment.isAnnotationPresent(type) == false)
                {
                    return false;
                }
            }
            return true;
        }
        
    }
}
