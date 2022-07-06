package com.jfireframework.jfire.condition.provide;

import java.lang.annotation.Annotation;
import com.jfireframework.jfire.condition.Condition;
import com.jfireframework.jfire.config.environment.Environment.ReadOnlyEnvironment;

public abstract class BaseCondition implements Condition
{
    protected final Class<? extends Annotation> selectAnnoType;
    
    public BaseCondition(Class<? extends Annotation> selectAnnoType)
    {
        this.selectAnnoType = selectAnnoType;
    }
    
    @Override
    public boolean match(ReadOnlyEnvironment readOnlyEnvironment, Annotation[] annotations)
    {
        for (Annotation each : annotations)
        {
            if (each.annotationType() == selectAnnoType)
            {
                return handleSelectAnnoType(readOnlyEnvironment, each);
            }
        }
        throw new NullPointerException();
    }
    
    protected abstract boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, Annotation annotation);
}
