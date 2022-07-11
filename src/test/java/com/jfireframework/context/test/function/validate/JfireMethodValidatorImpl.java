package com.jfireframework.context.test.function.validate;

import javax.annotation.Resource;
import com.jfireframework.jfire.validate.JfireMethodValidator;
import com.jfireframework.jfire.validate.ValidateResult;
import com.jfireframework.jfire.validate.ValidateResult.ValidateResultDetail;

@Resource
public class JfireMethodValidatorImpl implements JfireMethodValidator
{
    
    @Override
    public ValidateResult validateParam(Object object, Class<?>... groups)
    {
        System.out.println("asdas");
        ValidateResult result = new ValidateResult();
        ValidateResultDetail detail = new ValidateResultDetail();
        detail.setMessage("测试");
        result.setDetails(new ValidateResultDetail[] { detail });
        return result;
    }
    
    @Override
    public ValidateResult validateParams(Object[] params, Class<?>... groups)
    {
        System.out.println("asdas");
        ValidateResult result = new ValidateResult();
        ValidateResultDetail detail = new ValidateResultDetail();
        detail.setMessage("测试");
        result.setDetails(new ValidateResultDetail[] { detail });
        return result;
    }
    
}
