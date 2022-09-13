package com.jfireframework.jfire.condition.provide;

import java.lang.annotation.Annotation;
import com.jfireframework.jfire.condition.Condition;
import com.jfireframework.jfire.kernel.Environment.ReadOnlyEnvironment;

public abstract class BaseCondition<T extends Annotation> implements Condition
{
    protected final Class<T> selectAnnoType;
    
    public BaseCondition(Class<T> selectAnnoType)
    {
        this.selectAnnoType = selectAnnoType;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean match(ReadOnlyEnvironment readOnlyEnvironment, Annotation[] annotations)
    {
        for (Annotation each : annotations)
        {
            if (each.annotationType() == selectAnnoType)
            {
                return handleSelectAnnoType(readOnlyEnvironment, (T) each);
            }
        }
        throw new NullPointerException();
    }
    
    protected abstract boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, T annotation);
}
