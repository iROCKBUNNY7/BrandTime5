package com.jfirer.jfire.test.function.loader;

import com.jfirer.jfire.core.BeanFactory;
import com.jfirer.jfire.core.beandescriptor.BeanDescriptor;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Resource(name = "allLoader")
public class AllLoader implements BeanFactory
{
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

    @Override
    public <E> E getBean(BeanDescriptor beanDescriptor)
    {
        return (E) holder.get(beanDescriptor.getDescriptorClass());
    }
}
