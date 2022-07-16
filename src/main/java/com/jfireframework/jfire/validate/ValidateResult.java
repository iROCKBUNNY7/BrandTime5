package com.jfireframework.jfire.validate;

import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;

public class ValidateResult
{
    private List<ValidateResultDetail> details;
    
    public List<ValidateResultDetail> getDetails()
    {
        return details;
    }
    
    public void setDetails(List<ValidateResultDetail> details)
    {
        this.details = details;
    }
    
    @Override
    public String toString()
    {
        StringCache cache = new StringCache();
        for (ValidateResultDetail each : details)
        {
            cache.append("{");
            cache.append(each.getPath()).append(" : ").append(each.getMessage()).append("}").appendComma();
        }
        if (cache.isCommaLast())
        {
            cache.deleteLast();
        }
        return cache.toString();
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
