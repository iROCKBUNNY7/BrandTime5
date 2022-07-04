package com.jfireframework.jfire.util;

import com.jfireframework.baseutil.collection.StringCache;

public class JfireTool
{
    public static String getTypeName(Class<?> type)
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
}
