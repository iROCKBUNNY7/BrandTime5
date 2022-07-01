package com.jfireframework.jfire.smc.model;

public class FieldModel
{
    private final String   name;
    private final Class<?> type;
    
    protected String       outSource;
    
    public FieldModel(String name, Class<?> type)
    {
        this.name = name;
        this.type = type;
        StringBuilder builder = new StringBuilder();
        builder.append("public ").append(type.getName()).append(" ").append(name).append(";\r\n");
        outSource = builder.toString();
    }
    
    public String getName()
    {
        return name;
    }
    
    public Class<?> getType()
    {
        return type;
    }
    
    @Override
    public String toString()
    {
        return outSource;
    }
}
