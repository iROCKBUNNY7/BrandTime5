package com.jfireframework.jfire.validate;

public class ValidateResult
{
    private ValidateResultDetail[] details;
    
    public ValidateResultDetail[] getDetails()
    {
        return details;
    }
    
    public void setDetails(ValidateResultDetail[] details)
    {
        this.details = details;
    }
    
    public static class ValidateResultDetail
    {
        private String path;
        private Object inValidatedValue;
        private String message;
        private String messageTemplate;
        
        public String getMessageTemplate()
        {
            return messageTemplate;
        }
        
        public void setMessageTemplate(String messageTemplate)
        {
            this.messageTemplate = messageTemplate;
        }
        
        public String getPath()
        {
            return path;
        }
        
        public void setPath(String path)
        {
            this.path = path;
        }
        
        public Object getInValidatedValue()
        {
            return inValidatedValue;
        }
        
        public void setInValidatedValue(Object inValidatedValue)
        {
            this.inValidatedValue = inValidatedValue;
        }
        
        public String getMessage()
        {
            return message;
        }
        
        public void setMessage(String message)
        {
            this.message = message;
        }
        
    }
}
