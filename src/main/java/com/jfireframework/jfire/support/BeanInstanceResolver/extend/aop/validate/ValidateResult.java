package com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.validate;

import java.util.List;
import javax.validation.ConstraintViolation;
import com.jfireframework.baseutil.collection.StringCache;

public class ValidateResult
{
	
	private final List<ConstraintViolation<?>> violations;
	
	public ValidateResult(List<ConstraintViolation<?>> violations)
	{
		this.violations = violations;
	}
	
	public boolean isInValid()
	{
		return violations.isEmpty() == false;
	}
	
	public List<ConstraintViolation<?>> getViolations()
	{
		return violations;
	}
	
	@Override
	public String toString()
	{
		StringCache cache = new StringCache();
		for (ConstraintViolation<?> each : violations)
		{
			cache.append("{");
			cache.append(each.getPropertyPath()).append(" : ").append(each.getMessage()).append("}").appendComma();
		}
		if (cache.isCommaLast())
		{
			cache.deleteLast();
		}
		return cache.toString();
	}
	
}
