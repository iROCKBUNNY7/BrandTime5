package com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.validate.internal;

import java.lang.reflect.Method;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.validate.JfireMethodValidator;

public class JfireMethodValidatorImpl implements JfireMethodValidator
{
	private Validator validator;
	
	@PostConstruct
	protected void initialize()
	{
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}
	
	@Override
	public <T> void validateParameters(T object, Method method, Object[] parameterValues, Class<?>... groups)
	{
		ExecutableValidator executables = validator.forExecutables();
		Set<ConstraintViolation<T>> set = executables.validateParameters(object, method, parameterValues, groups);
		if (set.isEmpty() == false)
		{
			StringCache cache = new StringCache();
			for (ConstraintViolation<T> each : set)
			{
				cache.append(each.getPropertyPath().toString()).appendComma();
			}
			ValidationException validationException = new ValidationException(cache.toString());
			throw validationException;
		}
	}
	
	@Override
	public <T> void validateReturnValue(T object, Method method, Object returnValue, Class<?>... groups)
	{
		ExecutableValidator executables = validator.forExecutables();
		Set<ConstraintViolation<T>> set = executables.validateReturnValue(object, method, returnValue, groups);
		if (set.isEmpty() == false)
		{
			StringCache cache = new StringCache();
			for (ConstraintViolation<T> each : set)
			{
				cache.append(each.getPropertyPath().toString()).appendComma();
			}
			ValidationException validationException = new ValidationException(cache.toString());
			throw validationException;
		}
	}
	
}
