package com.jfireframework.jfire.extrastore;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ExtraInfoStore
{
    private List<Method> methods  = new ArrayList<Method>();
    private int          sequence = 0;
    
    public int registerMethod(Method method)
    {
        for (int i = 0; i < sequence; i++)
        {
            if (method == methods.get(i))
            {
                return i;
            }
        }
        methods.add(method);
        sequence += 1;
        return sequence - 1;
    }
    
    public Method getMethod(int sequence)
    {
        return methods.get(sequence);
    }
}
