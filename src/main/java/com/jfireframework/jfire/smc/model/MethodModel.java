package com.jfireframework.jfire.smc.model;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import com.jfireframework.baseutil.collection.StringCache;

public class MethodModel
{
    private final String modifier;
    private String       methodName;
    private String       body;
    private final String returnInfo;
    private final String argsInfo;
    private final int    sumOfarg;
    
    @Override
    public String toString()
    {
        StringCache cache = new StringCache();
        cache.append(modifier).append(' ').append(returnInfo).append(' ')//
                .append(methodName).append("(").append(argsInfo).append(")\r\n{\r\n")//
                .append(body).append("\r\n}\r\n");
        return cache.toString();
    }
    
    public MethodModel(Method method)
    {
        sumOfarg = method.getParameterTypes().length;
        if (Modifier.isPublic(method.getModifiers()))
        {
            modifier = "public";
        }
        else if (Modifier.isProtected(method.getModifiers()))
        {
            modifier = "protected";
        }
        else
        {
            throw new UnsupportedOperationException();
        }
        if (method.getReturnType() == void.class)
        {
            returnInfo = "void";
        }
        else
        {
            returnInfo = getTypeName(method.getReturnType());
        }
        methodName = method.getName();
        StringCache cache = new StringCache();
        int index = 0;
        for (Class<?> each : method.getParameterTypes())
        {
            cache.append(getTypeName(each)).append(" $").append(index).append(",");
            index += 1;
        }
        if (cache.isCommaLast())
        {
            cache.deleteLast();
        }
        argsInfo = cache.toString();
    }
    
    private String getTypeName(Class<?> type)
    {
        if (type.isArray() == false)
        {
            return type.getName();
        }
        else
        {
            StringCache cache = new StringCache();
            while (type.isArray())
            {
                cache.append("[]");
                type = type.getComponentType();
            }
            return type.getName() + cache.toString();
        }
    }
    
    public String getInvokeInfo()
    {
        StringCache cache = new StringCache();
        cache.clear().append(methodName).append('(');
        for (int i = 0; i < sumOfarg; i++)
        {
            cache.append('$').append(i).append(',');
        }
        if (cache.isCommaLast())
        {
            cache.deleteLast();
        }
        cache.append(')');
        return cache.toString();
    }
    
    public String getMethodName()
    {
        return methodName;
    }
    
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }
    
    public String getBody()
    {
        return body;
    }
    
    public void setBody(String body)
    {
        this.body = body;
    }
    
    public String getReturnInfo()
    {
        return returnInfo;
    }
    
    public String getArgsInfo()
    {
        return argsInfo;
    }
    
    public int getSumOfarg()
    {
        return sumOfarg;
    }
    
    public String getModifier()
    {
        return modifier;
    }
    
}
