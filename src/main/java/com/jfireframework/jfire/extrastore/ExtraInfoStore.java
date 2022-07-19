package com.jfireframework.jfire.extrastore;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class ExtraInfoStore
{
    private Map<Integer, Method>             store       = new HashMap<Integer, Method>();
    private IdentityHashMap<Method, Integer> sequenceMap = new IdentityHashMap<Method, Integer>();
    private int                              sequence    = 1;
    
    public int registerMethod(Method method)
    {
        if (sequenceMap.containsKey(method) == false)
        {
            sequenceMap.put(method, sequence);
            store.put(sequence, method);
            sequence += 1;
        }
        return sequence - 1;
    }
    
    public int methodSequence(Method method)
    {
        return sequenceMap.get(method);
    }
    
    public Method getMethod(int sequence)
    {
        return store.get(sequence);
    }
}
