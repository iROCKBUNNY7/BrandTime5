package com.jfireframework.jfire.core.inject.impl;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.encrypt.Base64Tool;
import com.jfireframework.baseutil.reflect.ValueAccessor;
import com.jfireframework.jfire.core.EnvironmentTmp;
import com.jfireframework.jfire.core.inject.InjectHandler;
import com.jfireframework.jfire.core.inject.notated.PropertyRead;
import com.jfireframework.jfire.exception.InjectTypeException;
import com.jfireframework.jfire.exception.InjectValueException;
import com.jfireframework.jfire.util.Utils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DefaultPropertyInjectHandler implements InjectHandler
{
    private ValueAccessor valueAccessor;
    private String        propertyValue;
    private Inject        inject;

    @Override
    public void init(Field field, EnvironmentTmp environment)
    {
        valueAccessor = new ValueAccessor(field);
        PropertyRead propertyRead = Utils.ANNOTATION_UTIL.getAnnotation(PropertyRead.class, field);
        String       propertyName = StringUtil.isNotBlank(propertyRead.value()) ? propertyRead.value() : field.getName();
        if (StringUtil.isNotBlank(System.getProperty(propertyName)))
        {
            propertyValue = System.getProperty(propertyName);
        }
        else if (StringUtil.isNotBlank(environment.getProperty(propertyName)))
        {
            propertyValue = environment.getProperty(propertyName);
        }
        else
        {
        }
        if (propertyValue != null)
        {
            Class<?> type = field.getType();
            if (type == int.class || type == Integer.class)
            {
                inject = new IntInject();
            }
            else if (type == short.class || type == Short.class)
            {
                inject = new ShortInject();
            }
            else if (type == long.class || type == Long.class)
            {
                inject = new LongInject();
            }
            else if (type == float.class || type == Float.class)
            {
                inject = new FloatInject();
            }
            else if (type == double.class || type == Double.class)
            {
                inject = new DoubleInject();
            }
            else if (type == boolean.class || type == Boolean.class)
            {
                inject = new BooleanInject();
            }
            else if (type == byte[].class)
            {
                inject = new ByteArrayInject();
            }
            else if (type == Class.class)
            {
                inject = new ClassInject();
            }
            else if (type == File.class)
            {
                inject = new FileInject();
            }
            else if (type == int[].class)
            {
                inject = new IntArrayInject();
            }
            else if (type == String.class)
            {
                inject = new StringInject();
            }
            else if (type == String[].class)
            {
                inject = new StringArrayInject();
            }
            else if (type == Set.class)
            {
                inject = new SetStringInject();
            }
            else if (type.isEnum())
            {
                inject = new EnumInject(field);
            }
            else
            {
                throw new InjectTypeException("无法识别的参数注入类型，请检查" + field.toGenericString());
            }
        }
    }

    @Override
    public void inject(Object instance)
    {
        if (inject == null)
        {
            return;
        }
        inject.inject(instance);
    }

    interface Inject
    {
        void inject(Object instance);
    }

    abstract class AbstractInject implements Inject
    {
        protected Object value;

        @Override
        public void inject(Object instance)
        {
            try
            {
                valueAccessor.setObject(instance, value);
            } catch (Exception e)
            {
                throw new InjectValueException(e);
            }
        }
    }

    class BooleanInject extends AbstractInject
    {
        BooleanInject()
        {
            value = Boolean.valueOf(propertyValue);
        }
    }

    class IntInject extends AbstractInject
    {
        IntInject()
        {
            value = Integer.valueOf(propertyValue);
        }
    }

    class LongInject extends AbstractInject
    {
        LongInject()
        {
            value = Long.valueOf(propertyValue);
        }
    }

    class ShortInject extends AbstractInject
    {
        ShortInject()
        {
            value = Short.valueOf(propertyValue);
        }
    }

    class FloatInject extends AbstractInject
    {
        FloatInject()
        {
            value = Float.valueOf(propertyValue);
        }
    }

    class DoubleInject extends AbstractInject
    {
        DoubleInject()
        {
            value = Double.valueOf(propertyValue);
        }
    }

    class ByteArrayInject extends AbstractInject
    {
        ByteArrayInject()
        {
            if (propertyValue.startsWith("0x"))
            {
                value = StringUtil.hexStringToBytes(propertyValue.substring(2));
            }
            else
            {
                value = Base64Tool.decode(propertyValue);
            }
        }
    }

    class ClassInject extends AbstractInject
    {
        ClassInject()
        {
            try
            {
                value = this.getClass().getClassLoader().loadClass(propertyValue);
            } catch (Exception e)
            {
                throw new InjectValueException(e);
            }
        }
    }

    class FileInject extends AbstractInject
    {
        FileInject()
        {
            value = new File(propertyValue);
        }
    }

    class IntArrayInject extends AbstractInject
    {
        IntArrayInject()
        {
            String[] tmp   = propertyValue.split(",");
            int[]    array = new int[tmp.length];
            for (int i = 0; i < array.length; i++)
            {
                array[i] = Integer.valueOf(tmp[i]);
            }
            value = array;
        }
    }

    class StringInject extends AbstractInject
    {
        StringInject()
        {
            value = propertyValue;
        }
    }

    class StringArrayInject extends AbstractInject
    {
        StringArrayInject()
        {
            value = propertyValue.split(",");
        }
    }

    class SetStringInject extends AbstractInject
    {
        SetStringInject()
        {
            Set<String> set = new HashSet<String>();
            Collections.addAll(set, propertyValue.split(","));
            value = set;
        }
    }

    class EnumInject extends AbstractInject
    {

        @SuppressWarnings({"unchecked", "rawtypes"})
        EnumInject(Field field)
        {
            value = Enum.valueOf((Class<Enum>) field.getType(), propertyValue);
        }
    }
}
