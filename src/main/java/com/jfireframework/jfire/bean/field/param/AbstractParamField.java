package com.jfireframework.jfire.bean.field.param;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.encrypt.Base64Tool;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import sun.misc.Unsafe;

public abstract class AbstractParamField implements ParamField
{
    protected long         offset;
    protected Unsafe       unsafe = ReflectUtil.getUnsafe();
    protected Object       value;
    protected final String name;
    
    public AbstractParamField(Field field, String value)
    {
        offset = unsafe.objectFieldOffset(field);
        name = field.getName();
    }
    
    @Override
    public void setParam(Object entity)
    {
        unsafe.putObject(entity, offset, value);
    }
    
    @Override
    public String getName()
    {
        return name;
    }
    
    public static ParamField build(Field field, String value, ClassLoader classLoader)
    {
        Class<?> fieldType = field.getType();
        if (fieldType == String.class)
        {
            return new StringField(field, value);
        }
        else if (fieldType == int[].class)
        {
            return new IntArrayField(field, value);
        }
        else if (fieldType == Integer.class)
        {
            return new IntegerField(field, value);
        }
        else if (fieldType == int.class)
        {
            return new IntField(field, value);
        }
        else if (fieldType == Long.class)
        {
            return new WLongField(field, value);
        }
        else if (fieldType == long.class)
        {
            return new LongField(field, value);
        }
        else if (fieldType == Boolean.class)
        {
            return new WBooleanField(field, value);
        }
        else if (fieldType == boolean.class)
        {
            return new BooleanField(field, value);
        }
        else if (fieldType == float.class)
        {
            return new FloatField(field, value);
        }
        else if (fieldType == Float.class)
        {
            return new WFloatField(field, value);
        }
        else if (fieldType == String[].class)
        {
            return new StringArrayField(field, value);
        }
        else if (fieldType == Set.class)
        {
            return new SetField(field, value);
        }
        else if (fieldType == Class.class)
        {
            return new ClassField(field, value, classLoader);
        }
        else if (fieldType == File.class)
        {
            return new FileField(field, value);
        }
        else if (fieldType == byte[].class)
        {
            return new ByteArrayField(field, value);
        }
        else if (Enum.class.isAssignableFrom(fieldType))
        {
            return new EnumField(field, value);
        }
        else
        {
            throw new RuntimeException(StringUtil.format("属性类型{}还未支持，请联系框架作者eric@jfire.com", fieldType));
        }
    }
    
    static class IntArrayField extends AbstractParamField
    {
        
        private final int[] array;
        
        public IntArrayField(Field field, String value)
        {
            super(field, value);
            String[] tmp = value.split(",");
            array = new int[tmp.length];
            for (int i = 0; i < array.length; i++)
            {
                array[i] = Integer.valueOf(tmp[i]);
            }
            this.value = array;
        }
        
    }
    
    static class BooleanField extends AbstractParamField
    {
        private final boolean value;
        
        public BooleanField(Field field, String value)
        {
            super(field, value);
            this.value = Boolean.parseBoolean(value);
        }
        
        @Override
        public void setParam(Object entity)
        {
            unsafe.putBoolean(entity, offset, value);
        }
    }
    
    static class FloatField extends AbstractParamField
    {
        private final float value;
        
        public FloatField(Field field, String value)
        {
            super(field, value);
            this.value = Float.parseFloat(value);
        }
        
        @Override
        public void setParam(Object entity)
        {
            unsafe.putFloat(entity, offset, value);
        }
        
    }
    
    static class IntegerField extends AbstractParamField
    {
        
        public IntegerField(Field field, String value)
        {
            super(field, value);
            this.value = Integer.valueOf(value);
        }
    }
    
    static class IntField extends AbstractParamField
    {
        private final int value;
        
        public IntField(Field field, String value)
        {
            super(field, value);
            this.value = Integer.parseInt(value);
        }
        
        @Override
        public void setParam(Object entity)
        {
            unsafe.putInt(entity, offset, value);
        }
    }
    
    static class LongField extends AbstractParamField
    {
        private final long value;
        
        public LongField(Field field, String value)
        {
            super(field, value);
            this.value = Long.parseLong(value);
        }
        
        @Override
        public void setParam(Object entity)
        {
            unsafe.putLong(entity, offset, value);
        }
    }
    
    static class SetField extends AbstractParamField
    {
        
        public SetField(Field field, String value)
        {
            super(field, value);
            Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            if (type instanceof ParameterizedType)
            {
                type = ((ParameterizedType) type).getRawType();
            }
            if (type instanceof Class<?>)
            {
                if (type == String.class)
                {
                    Set<String> set = new HashSet<String>();
                    for (String each : value.split(","))
                    {
                        set.add(each);
                    }
                    this.value = set;
                }
                else if (type == Integer.class)
                {
                    Set<Integer> set = new HashSet<Integer>();
                    for (String each : value.split(","))
                    {
                        set.add(Integer.valueOf(each));
                    }
                    this.value = set;
                }
                else if (type == Long.class)
                {
                    Set<Long> set = new HashSet<Long>();
                    for (String each : value.split(","))
                    {
                        set.add(Long.valueOf(each));
                    }
                    this.value = set;
                }
                else if (type == Float.class)
                {
                    Set<Float> set = new HashSet<Float>();
                    for (String each : value.split(","))
                    {
                        set.add(Float.valueOf(each));
                    }
                    this.value = set;
                }
                else if (type == Double.class)
                {
                    Set<Double> set = new HashSet<Double>();
                    for (String each : value.split(","))
                    {
                        set.add(Double.valueOf(each));
                    }
                    this.value = set;
                }
                else if (type == Class.class)
                {
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    Set<Class<?>> set = new HashSet<Class<?>>();
                    for (String each : value.split(","))
                    {
                        try
                        {
                            set.add(classLoader.loadClass(each));
                        }
                        catch (ClassNotFoundException e)
                        {
                            throw new JustThrowException(e);
                        }
                    }
                    this.value = set;
                }
                else
                {
                    throw new UnSupportException("目前Set注入只支持String,Integer,Long,Float,Double,Class");
                }
            }
            else
            {
                throw new UnSupportException("Set注入，必须指明注入类型，而不能使用问号");
            }
        }
    }
    
    static class StringArrayField extends AbstractParamField
    {
        
        public StringArrayField(Field field, String value)
        {
            super(field, value);
            this.value = value.split(",");
        }
        
    }
    
    static class StringField extends AbstractParamField
    {
        
        public StringField(Field field, String value)
        {
            super(field, value);
            this.value = value;
        }
        
    }
    
    static class WBooleanField extends AbstractParamField
    {
        
        public WBooleanField(Field field, String value)
        {
            super(field, value);
            this.value = Boolean.valueOf(value);
        }
        
    }
    
    static class WFloatField extends AbstractParamField
    {
        
        public WFloatField(Field field, String value)
        {
            super(field, value);
            this.value = Float.valueOf(value);
        }
        
    }
    
    static class WLongField extends AbstractParamField
    {
        
        public WLongField(Field field, String value)
        {
            super(field, value);
            this.value = Long.valueOf(value);
        }
        
    }
    
    static class ClassField extends AbstractParamField
    {
        
        public ClassField(Field field, String value, ClassLoader classLoader)
        {
            super(field, value);
            try
            {
                this.value = classLoader.loadClass(value);
            }
            catch (ClassNotFoundException e)
            {
                throw new JustThrowException(e);
            }
        }
        
    }
    
    static class FileField extends AbstractParamField
    {
        
        public FileField(Field field, String value)
        {
            super(field, value);
            this.value = new File(value);
        }
        
    }
    
    static class EnumField extends AbstractParamField
    {
        
        @SuppressWarnings("unchecked")
        public EnumField(Field field, String value)
        {
            super(field, value);
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
    
    static class ByteArrayField extends AbstractParamField
    {
        
        public ByteArrayField(Field field, String value)
        {
            super(field, value);
            if (value.startsWith("0x"))
            {
                this.value = StringUtil.hexStringToBytes(value.substring(2));
            }
            else
            {
                this.value = Base64Tool.decode(value);
            }
        }
        
    }
}
