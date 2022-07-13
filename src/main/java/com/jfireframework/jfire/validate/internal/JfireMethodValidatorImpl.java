package com.jfireframework.jfire.validate.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.groups.Default;
import com.jfireframework.jfire.validate.JfireMethodValidator;
import com.jfireframework.jfire.validate.ValidateResult;
import com.jfireframework.jfire.validate.ValidateResult.ValidateResultDetail;
import com.jfireframework.validator.engine.internal.JfireValidator;

public class JfireMethodValidatorImpl implements JfireMethodValidator
{
    private JfireValidator jfireValidator;
    
    @PostConstruct
    public void init()
    {
        jfireValidator = (JfireValidator) Validation.buildDefaultValidatorFactory().getValidator();
    }
    
    abstract class Helper
    {
        private Class<?>[] groups;
        
        ValidateResult doValidate(Object... beans)
        {
            ValidateResult result = new ValidateResult();
            for (Object each : beans)
            {
                Set<ConstraintViolation<Object>> set = jfireValidator.validate(each, groups);
                if (set.size() > 0)
                {
                    List<ValidateResultDetail> details = new ArrayList<ValidateResult.ValidateResultDetail>();
                    for (ConstraintViolation<Object> constraintViolation : set)
                    {
                        ValidateResultDetail detail = new ValidateResultDetail();
                        detail.setInValidatedValue(constraintViolation.getInvalidValue());
                        detail.setMessage(constraintViolation.getMessage());
                        detail.setMessageTemplate(constraintViolation.getMessageTemplate());
                        detail.setPath(constraintViolation.getPropertyPath().toString());
                        details.add(detail);
                    }
                    result.setDetails(Collections.unmodifiableList(details));
                    return result;
                }
            }
            result.setDetails(Collections.unmodifiableList(new LinkedList<ValidateResultDetail>()));
            return result;
        }
        
        Class<?>[] getGroups()
        {
            groups = groups.length == 0 ? new Class<?>[] { Default.class } : groups;
            return groups;
        }
    }
    
    @Override
    public ValidateResult validateParams(Object[] params, Class<?>... groups)
    {
        Helper helper = new Helper() {};
        helper.groups = groups;
        return helper.doValidate(params);
    }
    
    @Override
    public ValidateResult validateParam(Object param, Class<?>... groups)
    {
        Helper helper = new Helper() {};
        helper.groups = groups;
        return helper.doValidate(param);
    }
    
}
