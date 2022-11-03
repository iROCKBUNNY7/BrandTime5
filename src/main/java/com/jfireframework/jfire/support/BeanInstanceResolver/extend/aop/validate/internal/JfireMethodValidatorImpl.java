package com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.validate.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import com.jfireframework.jfire.Utils;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.validate.JfireMethodValidator;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.validate.ValidateResult;

public class JfireMethodValidatorImpl implements JfireMethodValidator
{
	private Validator validator;
	
	@PostConstruct
	public void init()
	{
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}
	
	@Override
	public ValidateResult validate(Method method, Object[] params, Class<?>... groups)
	{
		for (Annotation[] annotations : method.getParameterAnnotations())
		{
			for (int i = 0; i < annotations.length; i++)
			{
				Annotation annotation = annotations[i];
				
			}
		}
		List<ConstraintViolation<?>> result = new ArrayList<ConstraintViolation<?>>();
		for (Object param : params)
		{
			Set<ConstraintViolation<Object>> set = validator.validate(param, groups);
			result.addAll(set);
		}
		ValidateResult validateResult = new ValidateResult(result);
		return validateResult;
	}
	
}
