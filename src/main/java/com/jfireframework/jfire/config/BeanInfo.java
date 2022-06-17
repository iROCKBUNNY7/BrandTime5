package com.jfireframework.jfire.config;

import java.util.HashMap;
import java.util.Map;

public class BeanInfo
{
    private String              beanName;
    private String              className;
    private boolean             prototype    = false;
    private Map<String, String> params       = new HashMap<String, String>();
    private Map<String, String> dependencies = new HashMap<String, String>();
    private String              postConstructMethod;
    private String              closeMethod;
    
    public String getCloseMethod()
    {
        return closeMethod;
    }
    
    public void setCloseMethod(String closeMethod)
    {
        this.closeMethod = closeMethod;
    }
    
    public String getBeanName()
    {
        return beanName;
    }
    
    public void setBeanName(String beanName)
    {
        this.beanName = beanName;
    }
    
    public String getClassName()
    {
        return className;
    }
    
    public void setClassName(String className)
    {
        this.className = className;
    }
    
    public boolean isPrototype()
    {
        return prototype;
    }
    
    public void setPrototype(boolean prototype)
    {
        this.prototype = prototype;
    }
    
    public Map<String, String> getParams()
    {
        return params;
    }
    
    public void setParams(Map<String, String> params)
    {
        this.params = params;
    }
    
    public Map<String, String> getDependencies()
    {
        return dependencies;
    }
    
    public void setDependencies(Map<String, String> dependencies)
    {
        this.dependencies = dependencies;
    }
    
    public String getPostConstructMethod()
    {
        return postConstructMethod;
    }
    
    public void setPostConstructMethod(String postConstructMethod)
    {
        this.postConstructMethod = postConstructMethod;
    }
    
    public void putParam(String key, String value)
    {
        params.put(key, value);
    }
}
