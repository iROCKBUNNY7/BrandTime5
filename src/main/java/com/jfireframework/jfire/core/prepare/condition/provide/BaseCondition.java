package com.jfireframework.jfire.core.prepare.condition.provide;

import java.lang.annotation.Annotation;
import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.condition.Condition;

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
