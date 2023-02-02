package com.jfireframework.context.test.function.loader;

import com.jfireframework.jfire.core.resolver.impl.LoadByBeanInstanceResolver.BeanLoadFactory;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Resource(name = "allLoader")
public class AllLoader implements BeanLoadFactory
{
    private Class<?> ckass;
    private Map<Class, Object> holder = new HashMap<Class, Object>();

    public AllLoader()
    {
        holder.put(Person.class, new Person()
        {

            @Override
            public String getName()
            {
                return "name";
            }
        });
        holder.put(Home.class, new Home()
        {

            @Override
            public int getLength()
            {
                return 100;
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T load(Class<T> ckass)
    {
        return (T) holder.get(ckass);
    }

}
