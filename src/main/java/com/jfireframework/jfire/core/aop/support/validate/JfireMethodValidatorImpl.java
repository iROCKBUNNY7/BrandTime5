package com.jfireframework.jfire.core.aop.support.validate;

import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.jfire.core.aop.impl.ValidateAopManager.JfireMethodValidator;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Set;

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
        ExecutableValidator         executables = validator.forExecutables();
        Set<ConstraintViolation<T>> set         = executables.validateParameters(object, method, parameterValues, groups);
        if (set.isEmpty() == false)
        {
            StringCache cache = new StringCache();
            for (ConstraintViolation<T> each : set)
            {
                cache.append(each.getPropertyPath().toString()).appendComma();
            }
            throw new ValidationException(cache.toString());
        }
    }

    @Override
    public <T> void validateReturnValue(T object, Method method, Object returnValue, Class<?>... groups)
    {
        ExecutableValidator         executables = validator.forExecutables();
        Set<ConstraintViolation<T>> set         = executables.validateReturnValue(object, method, returnValue, groups);
        if (set.isEmpty() == false)
        {
            StringCache cache = new StringCache();
            for (ConstraintViolation<T> each : set)
            {
                cache.append(each.getPropertyPath().toString()).appendComma();
            }
            throw new ValidationException(cache.toString());
        }
    }
}
