package com.jfireframework.context.test.function.validate;

import java.util.LinkedList;
import java.util.List;
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
        ValidateResult result = new ValidateResult();
        ValidateResultDetail detail = new ValidateResultDetail();
        detail.setMessage("测试");
        List<ValidateResultDetail> details = new LinkedList<ValidateResult.ValidateResultDetail>();
        details.add(detail);
        result.setDetails(details);
        return result;
    }
    
    @Override
    public ValidateResult validateParams(Object[] params, Class<?>... groups)
    {
        ValidateResult result = new ValidateResult();
        ValidateResultDetail detail = new ValidateResultDetail();
        detail.setMessage("测试");
        List<ValidateResultDetail> details = new LinkedList<ValidateResult.ValidateResultDetail>();
        details.add(detail);
        result.setDetails(details);
        return result;
    }
    
}
