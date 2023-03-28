package com.jfireframework.jfire.core;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public interface Environment
{
    void putProperty(String property, String value);

    void addProperties(Properties properties);

    String getProperty(String propertyName);

    class EnvironmentImpl implements Environment
    {
        private Map<String, String> store = new HashMap<String, String>();

        @Override
        public void putProperty(String property, String value)
        {
            store.put(property, value);
        }

        @Override
        public void addProperties(Properties properties)
        {
            Enumeration<?> enumeration = properties.propertyNames();
            while (enumeration.hasMoreElements())
            {
                String name = (String) enumeration.nextElement();
                store.put(name, (String) properties.get(name));
            }
        }

        @Override
        public String getProperty(String propertyName)
        {
            return store.get(propertyName);
        }
    }
}
