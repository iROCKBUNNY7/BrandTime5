package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl;

import java.util.Map;
import com.jfireframework.baseutil.reflect.ReflectUtil;

public class EnumResolver extends ObjectResolver
{
    
    @SuppressWarnings("unchecked")
    @Override
    protected void initialize(String value)
    {
        Map<String, ? extends Enum<?>> map = ReflectUtil.getAllEnumInstances((Class<? extends Enum<?>>) field.getType());
        try
        {
            int intValue = Integer.parseInt(value);
            for (Enum<?> each : map.values())
            {
                if (each.ordinal() == intValue)
                {
                    this.value = each;
                    break;
                }
            }
        }
        catch (NumberFormatException e)
        {
            Enum<?> instance = map.get(value);
            this.value = instance;
        }
    }
    
}