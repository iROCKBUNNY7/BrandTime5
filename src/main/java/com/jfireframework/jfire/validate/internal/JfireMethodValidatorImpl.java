package com.jfireframework.jfire.validate.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import com.jfireframework.jfire.validate.JfireMethodValidator;
import com.jfireframework.jfire.validate.ValidateResult;
import com.jfireframework.jfire.validate.ValidateResult.ValidateResultDetail;
import com.jfireframework.validator.engine.JfireValidator;

public class JfireMethodValidatorImpl implements JfireMethodValidator
{
    private JfireValidator jfireValidator;
    
    @PostConstruct
    public void init()
    {
        jfireValidator = (JfireValidator) Validation.buildDefaultValidatorFactory().getValidator();
    }
    
    @Override
    public ValidateResult validate(Method method, Object[] params, Class<?>... groups)
    {
        Set<ConstraintViolation<Method>> constraintViolations = jfireValidator.validateMethod(method, params, groups);
        ValidateResult result = new ValidateResult();
        List<ValidateResultDetail> details = new ArrayList<ValidateResult.ValidateResultDetail>();
        for (ConstraintViolation<Method> each : constraintViolations)
        {
            ValidateResultDetail detail = new ValidateResultDetail();
            detail.setInValidatedValue(each.getInvalidValue());
            detail.setMessage(each.getMessage());
            detail.setMessageTemplate(each.getMessageTemplate());
            detail.setPath(each.getPropertyPath().toString());
            details.add(detail);
        }
        result.setDetails(Collections.unmodifiableList(details));
        return result;
    }
    
}
