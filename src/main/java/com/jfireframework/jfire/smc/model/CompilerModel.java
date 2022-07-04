package com.jfireframework.jfire.smc.model;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompilerModel
{
    private final String             packageName  = "com.jfireframe.smc.output";
    private final String             className;
    private final StringBuilder      builder      = new StringBuilder();
    private Map<String, FieldModel>  fieldStore   = new HashMap<String, FieldModel>();
    private Map<Method, MethodModel> methodStore  = new HashMap<Method, MethodModel>();
    private Set<MethodModel>         shadowMethod = new HashSet<MethodModel>();
    private static final Logger      logger       = LoggerFactory.getLogger(CompilerModel.class);
    private final Class<?>           parentClass;
    
    public CompilerModel(String className, Class<?> parentClass)
    {
        this.className = className;
        this.parentClass = parentClass;
        builder.append("package ").append(packageName).append(';').append("\r\n")//
                .append("public class ").append(className).append(" extends ").append(parentClass.getName()).append(" {\r\n");
    }
    
    public MethodModel getMethodModel(Method method)
    {
        return methodStore.get(method);
    }
    
    public String fileName()
    {
        return className + ".java";
    }
    
    public String className()
    {
        return className;
    }
    
    public void addField(FieldModel... models)
    {
        for (FieldModel each : models)
        {
            fieldStore.put(each.getName(), each);
        }
    }
    
    public void putMethod(Method key, MethodModel value)
    {
        methodStore.put(key, value);
    }
    
    public Collection<Method> methods()
    {
        return methodStore.keySet();
    }
    
    public void putShadowMethodModel(MethodModel methodModel)
    {
        shadowMethod.add(methodModel);
    }
    
    public String output()
    {
        for (FieldModel each : fieldStore.values())
        {
            builder.append(each.toString());
        }
        for (MethodModel each : methodStore.values())
        {
            builder.append(each.toString());
        }
        for (MethodModel each : shadowMethod)
        {
            builder.append(each.toString());
        }
        builder.append("}");
        String source = builder.toString();
        logger.trace("为类:{}生成的代理类源代码:\r\n{}\r\n", parentClass.getName(), source);
        return source;
    }
    
}
