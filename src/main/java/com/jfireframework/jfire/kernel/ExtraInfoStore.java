package com.jfireframework.jfire.kernel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ExtraInfoStore
{
    private List<Method> methods  = new ArrayList<Method>();
    private int          sequence = 0;
    
    public int registerMethod(Method method)
    {
        int index = methods.indexOf(method);
        if (index != -1)
        {
            return index;
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
