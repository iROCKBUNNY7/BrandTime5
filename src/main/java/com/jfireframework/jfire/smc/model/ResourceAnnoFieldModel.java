package com.jfireframework.jfire.smc.model;

public class ResourceAnnoFieldModel extends FieldModel
{
    
    public ResourceAnnoFieldModel(String name, Class<?> type)
    {
        super(name, type);
        StringBuilder builder = new StringBuilder();
        builder.append("@javax.annotation.Resource\r\n");
        builder.append("public ").append(type.getName()).append(" ").append(name).append(";\r\n");
        outSource = builder.toString();
    }
    
}
