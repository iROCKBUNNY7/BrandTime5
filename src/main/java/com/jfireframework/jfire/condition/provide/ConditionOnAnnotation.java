package com.jfireframework.jfire.condition.provide;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.condition.Conditional;
import com.jfireframework.jfire.condition.provide.ConditionOnAnnotation.OnAnnotation;
import com.jfireframework.jfire.config.environment.Environment.ReadOnlyEnvironment;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnAnnotation.class)
public @interface ConditionOnAnnotation
{
    public Class<? extends Annotation>[] value();
    
    public static class OnAnnotation extends BaseCondition
    {
        
        public OnAnnotation()
        {
            super(ConditionOnAnnotation.class);
        }
        
        @Override
        public boolean match(ReadOnlyEnvironment readOnlyEnvironment, Annotation[] annotations)
        {
            for (Annotation each : annotations)
            {
                if (each.annotationType() == ConditionOnAnnotation.class)
                {
                    for (Class<? extends Annotation> type : ((ConditionOnAnnotation) each).value())
                    {
                        if (readOnlyEnvironment.isAnnotationPresent(type) == false)
                        {
                            return false;
                        }
                    }
                    return true;
                }
            }
            throw new NullPointerException();
        }
        
        @Override
        protected boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, Annotation annotation)
        {
            for (Class<? extends Annotation> type : ((ConditionOnAnnotation) annotation).value())
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
