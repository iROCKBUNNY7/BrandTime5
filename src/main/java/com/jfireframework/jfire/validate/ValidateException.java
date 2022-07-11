package com.jfireframework.jfire.validate;

public class ValidateException extends RuntimeException
{
    
    /**
     * 
     */
    private static final long    serialVersionUID = 1L;
    private final ValidateResult result;
    
    public ValidateException(ValidateResult result)
    {
        super(result.getDetails()[0].getMessage());
        this.result = result;
    }
    
    public ValidateResult getResult()
    {
        return result;
    }
    
}
