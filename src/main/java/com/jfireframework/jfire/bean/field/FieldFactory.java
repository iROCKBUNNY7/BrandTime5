package com.jfireframework.jfire.bean.field;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.bean.annotation.field.CanBeNull;
import com.jfireframework.jfire.bean.annotation.field.MapKey;
import com.jfireframework.jfire.bean.annotation.field.PropertyRead;
import com.jfireframework.jfire.bean.field.dependency.DIFieldInfo;
import com.jfireframework.jfire.bean.field.param.AbstractParamField;
import com.jfireframework.jfire.bean.field.param.ParamField;
import com.jfireframework.jfire.util.EnvironmentUtil;

public class FieldFactory
{
    
    /**
     * 根据配置信息和field上的注解信息,返回该bean所有的依赖注入的field
     * 
     * @param bean
     * @param beans
     * @param beanConfig
     * @return
     */
    public static List<DIFieldInfo> buildDependencyField(BeanDefinition beanInfo, Map<String, BeanDefinition> beanDefinitions)
    {
        Field[] fields = ReflectUtil.getAllFields(beanInfo.getType());
        List<DIFieldInfo> list = new LinkedList<DIFieldInfo>();
        Map<String, String> dependencyMap = beanInfo.getDependencies();
        AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
        // 优先以配置中的为准
        try
        {
            for (Field field : fields)
            {
                if (dependencyMap.containsKey(field.getName()))
                {
                    String dependencyStr = dependencyMap.get(field.getName());
                    list.add(buildDependFieldsByConfig(field, dependencyStr, beanDefinitions));
                }
                else if (annotationUtil.isPresent(Resource.class, field))
                {
                    list.add(buildDependencyFieldByAnno(field, beanDefinitions));
                }
            }
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        return list;
    }
    
    /**
     * 使用配置信息为bean生成依赖注入信息
     * 
     * @param bean
     * @param beans
     * @param dependencyMap
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    private static DIFieldInfo buildDependFieldsByConfig(Field field, String dependencyStr, Map<String, BeanDefinition> beanDefinitions) throws NoSuchMethodException, SecurityException
    {
        Class<?> type = field.getType();
        if (List.class == type)
        {
            return buildListByConfig(field, dependencyStr, beanDefinitions);
        }
        else if (Map.class == type)
        {
            return buildMapFieldByConfig(field, dependencyStr, beanDefinitions);
        }
        else
        {
            return buildDefaultFieldByConfig(field, dependencyStr, beanDefinitions);
        }
    }
    
    private static DIFieldInfo buildDependencyFieldByAnno(Field field, Map<String, BeanDefinition> beanDefinitions) throws NoSuchMethodException, SecurityException
    {
        Class<?> type = field.getType();
        if (type == List.class)
        {
            return buildListFieldByAnno(field, beanDefinitions);
        }
        else if (type == Map.class)
        {
            return buildMapFieldByAnno(field, beanDefinitions);
        }
        else if (type.isInterface() || Modifier.isAbstract(type.getModifiers()))
        {
            return buildInterfaceField(field, beanDefinitions);
        }
        else
        {
            return buildDefaultField(field, beanDefinitions);
        }
    }
    
    private static DIFieldInfo buildListByConfig(Field field, String dependencyStr, Map<String, BeanDefinition> beanDefinitions)
    {
        String[] dependencyBeanNames = dependencyStr.split(";");
        BeanDefinition[] injects = new BeanDefinition[dependencyBeanNames.length];
        Class<?> beanInterface = (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        for (int i = 0; i < injects.length; i++)
        {
            BeanDefinition dependencyBean = beanDefinitions.get(dependencyBeanNames[i]);
            Verify.exist(dependencyBean, "配置文件中注入配置{}中的beanName:{}不存在", dependencyStr, dependencyBeanNames[i]);
            Verify.True(beanInterface.isAssignableFrom(dependencyBean.getType()), "配置文件中注入配置{}中的beanName:{}没有实现接口:{}", dependencyStr, dependencyBeanNames[i], beanInterface);
            injects[i] = dependencyBean;
        }
        DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.LIST);
        diFieldInfo.setBeanDefinitions(injects);
        return diFieldInfo;
    }
    
    private static DIFieldInfo buildMapFieldByConfig(Field field, String dependencyStr, Map<String, BeanDefinition> beanDefinitions) throws NoSuchMethodException, SecurityException
    {
        Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        Verify.matchType(types[0], Class.class, "map依赖字段，要求key必须指明类型，而当前类型是{}", types[0]);
        Verify.matchType(types[1], Class.class, "map依赖字段，要求value必须指明类型，而当前类型是{}", types[1]);
        Class<?> keyClass = (Class<?>) (types[0]);
        Class<?> valueClass = (Class<?>) (types[1]);
        if (dependencyStr.startsWith("version1!"))
        {
            dependencyStr = dependencyStr.substring(9);
            String methodName = dependencyStr.split(":")[0];
            String[] dependencyBeanNames = dependencyStr.split(":")[1].split(";");
            BeanDefinition[] beans = new BeanDefinition[dependencyBeanNames.length];
            for (int i = 0; i < beans.length; i++)
            {
                BeanDefinition dependencyBean = beanDefinitions.get(dependencyBeanNames[i]);
                Verify.exist(dependencyBean, "配置文件中注入配置{}中的beanName:{}不存在", dependencyStr, dependencyBeanNames[i]);
                Verify.True(valueClass.isAssignableFrom(dependencyBean.getType()), "配置文件中注入配置{}中的beanName:{}和类:{}类型不符", dependencyStr, dependencyBeanNames[i], valueClass);
                beans[i] = dependencyBean;
            }
            DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.METHOD_MAP);
            Method method = valueClass.getDeclaredMethod(methodName);
            Verify.notNull(method, "执行Map注入分析发现类:{}不存在方法:{},因此无法完成Map注入", valueClass.getName(), methodName);
            Verify.True(keyClass.isAssignableFrom(method.getReturnType()), "执行Map注入分析发现类:{}的方法:{}返回值不是:{}类型,不能完成Map注入", valueClass.getName(), methodName, keyClass.getName());
            diFieldInfo.setBeanDefinitions(beans);
            diFieldInfo.setMethod_map_method(ReflectUtil.fastMethod(method));
            return diFieldInfo;
        }
        else if (dependencyStr.startsWith("version2!"))
        {
            dependencyStr = dependencyStr.substring(9);
            String[] keyAndValues = dependencyStr.split("\\|");
            BeanDefinition[] beans = new BeanDefinition[keyAndValues.length];
            Object[] keys = new Object[keyAndValues.length];
            for (int i = 0; i < beans.length; i++)
            {
                String key = keyAndValues[i].split(":")[0];
                String value = keyAndValues[i].split(":")[1];
                BeanDefinition dependencyBean = beanDefinitions.get(value);
                Verify.exist(dependencyBean, "属性{}.{}进行map注入，配置信息{}中指定的bean{}不存在", field.getDeclaringClass(), field.getName(), dependencyStr, value);
                beans[i] = dependencyBean;
                if (keyClass == Integer.class)
                {
                    keys[i] = Integer.valueOf(key);
                }
                else if (keyClass == String.class)
                {
                    keys[i] = key;
                }
                else if (keyClass == Long.class)
                {
                    keys[i] = Long.valueOf(key);
                }
                else
                {
                    throw new UnsupportedOperationException("不识别的类型");
                }
            }
            DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.VALUE_MAP);
            diFieldInfo.setBeanDefinitions(beans);
            diFieldInfo.setValue_map_values(keys);
            return diFieldInfo;
        }
        else if (dependencyStr.startsWith("version3!"))
        {
            dependencyStr = dependencyStr.substring(9);
            String[] beanNames = dependencyStr.split(",");
            BeanDefinition[] beans = new BeanDefinition[beanNames.length];
            Verify.True(keyClass.equals(String.class), "只用Resource注解进行map注入时，key就是注入的bean的名称，所以要求key是String类型。请检查{}.{}", field.getDeclaringClass(), field.getName());
            for (int i = 0; i < beanNames.length; i++)
            {
                beans[i] = beanDefinitions.get(beanNames[i]);
            }
            DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.BEAN_NAME_MAP);
            diFieldInfo.setBeanDefinitions(beans);
            return diFieldInfo;
        }
        else
        {
            throw new UnSupportException(StringUtil.format("属性{}.{}进行map注入，本质信息不正确。请检查{}", field.getDeclaringClass(), field.getName(), dependencyStr));
        }
    }
    
    private static DIFieldInfo buildDefaultFieldByConfig(Field field, String dependencyStr, Map<String, BeanDefinition> beanDefinitions)
    {
        BeanDefinition dependencyBean = beanDefinitions.get(dependencyStr);
        Verify.notNull(dependencyBean, "配置文件中注入配置{}的bean不存在", dependencyStr);
        Class<?> type = field.getType();
        Verify.True(type.isAssignableFrom(dependencyBean.getType()), "配置文件中注入的bean:{}不是接口:{}的实现", dependencyStr, type);
        DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.DEFAULT);
        diFieldInfo.setBeanDefinition(dependencyBean);
        return diFieldInfo;
    }
    
    private static DIFieldInfo buildListFieldByAnno(Field field, Map<String, BeanDefinition> beanDefinitions)
    {
        Type type = ((java.lang.reflect.ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        Class<?> beanInterface = null;
        if (type instanceof Class)
        {
            beanInterface = (Class<?>) type;
        }
        else if (type instanceof ParameterizedType)
        {
            beanInterface = (Class<?>) ((ParameterizedType) type).getRawType();
        }
        else
        {
            throw new IllegalArgumentException();
        }
        List<BeanDefinition> tmp = new LinkedList<BeanDefinition>();
        for (BeanDefinition each : beanDefinitions.values())
        {
            if (beanInterface.isAssignableFrom(each.getType()))
            {
                tmp.add(each);
            }
        }
        DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.LIST);
        diFieldInfo.setBeanDefinitions(tmp.toArray(new BeanDefinition[tmp.size()]));
        return diFieldInfo;
    }
    
    private static DIFieldInfo buildMapFieldByAnno(Field field, Map<String, BeanDefinition> beanNameMap) throws NoSuchMethodException, SecurityException
    {
        Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        Verify.matchType(types[0], Class.class, "map依赖字段，要求key必须指明类型，而当前类型是{}", types[0]);
        Verify.matchType(types[1], Class.class, "map依赖字段，要求value必须指明类型，而当前类型是{}", types[1]);
        Class<?> valueClass = (Class<?>) (types[1]);
        List<BeanDefinition> tmp = new LinkedList<BeanDefinition>();
        for (BeanDefinition each : beanNameMap.values())
        {
            if (valueClass.isAssignableFrom(each.getType()))
            {
                tmp.add(each);
            }
        }
        BeanDefinition[] beans = tmp.toArray(new BeanDefinition[tmp.size()]);
        AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
        if (annotationUtil.isPresent(MapKey.class, field))
        {
            String methodName = annotationUtil.getAnnotation(MapKey.class, field).value();
            Method method = valueClass.getDeclaredMethod(methodName);
            Verify.notNull(method, "执行Map注入分析发现类:{}不存在方法:{},因此无法完成Map注入", valueClass.getName(), methodName);
            DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.METHOD_MAP);
            diFieldInfo.setMethod_map_method(ReflectUtil.fastMethod(method));
            diFieldInfo.setBeanDefinitions(beans);
            return diFieldInfo;
            
        }
        else
        {
            DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.BEAN_NAME_MAP);
            diFieldInfo.setBeanDefinitions(beans);
            return diFieldInfo;
        }
    }
    
    /**
     * 注入一个bean，首先按照名称来寻找，无法找到的情况下使用接口类型来寻找匹配。再找不到报错
     * 
     * @param field
     * @param beanNameMap
     * @return
     */
    private static DIFieldInfo buildInterfaceField(Field field, Map<String, BeanDefinition> beanNameMap)
    {
        AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
        Resource resource = annotationUtil.getAnnotation(Resource.class, field);
        Class<?> type = field.getType();
        if (resource.name().equals("") == false)
        {
            BeanDefinition nameBean = beanNameMap.get(resource.name());
            if (annotationUtil.isPresent(CanBeNull.class, field))
            {
                if (nameBean == null)
                {
                    return new DIFieldInfo(field, DIFieldInfo.NONE);
                }
                else
                {
                    Verify.True(type.isAssignableFrom(nameBean.getType()), "bean:{}不是接口:{}的实现", nameBean.getType().getName(), type.getName());
                    DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.DEFAULT);
                    diFieldInfo.setBeanDefinition(nameBean);
                    return diFieldInfo;
                }
            }
            else
            {
                Verify.exist(nameBean, "属性{}.{}指定需要bean:{}注入，但是该bean不存在，请检查", field.getDeclaringClass().getName(), field.getName(), resource.name());
                Verify.True(type.isAssignableFrom(nameBean.getType()), "bean:{}不是接口:{}的实现", nameBean.getType().getName(), type.getName());
                DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.DEFAULT);
                diFieldInfo.setBeanDefinition(nameBean);
                return diFieldInfo;
            }
        }
        else
        {
            // 寻找实现了该接口的bean,如果超过1个,则抛出异常
            int find = 0;
            BeanDefinition implBean = null;
            for (BeanDefinition each : beanNameMap.values())
            {
                if (type.isAssignableFrom(each.getType()))
                {
                    find++;
                    implBean = each;
                }
            }
            if (find != 0)
            {
                Verify.True(find == 1, "接口或抽象类{}的实现多于一个,无法自动注入{}.{},请在resource注解上注明需要注入的bean的名称", type.getName(), field.getDeclaringClass().getName(), field.getName());
                DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.DEFAULT);
                diFieldInfo.setBeanDefinition(implBean);
                return diFieldInfo;
            }
            else if (annotationUtil.isPresent(CanBeNull.class, field))
            {
                return new DIFieldInfo(field, DIFieldInfo.NONE);
            }
            else
            {
                throw new NullPointerException(StringUtil.format("属性{}.{}没有可以注入的bean,属性类型为{}", field.getDeclaringClass().getName(), field.getName(), field.getType().getName()));
            }
        }
    }
    
    /**
     * 构建默认情况的注入bean。首先按照bean的名称来寻找，如果找不到，则按照类型来寻找。再找不到，则报错
     * 
     * @param field
     * @param beanNameMap
     * @return
     */
    private static DIFieldInfo buildDefaultField(Field field, Map<String, BeanDefinition> beanNameMap)
    {
        AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
        Resource resource = annotationUtil.getAnnotation(Resource.class, field);
        if (resource.name().equals("") == false)
        {
            BeanDefinition nameBean = beanNameMap.get(resource.name());
            if (nameBean != null)
            {
                Verify.True(field.getType() == nameBean.getType(), "bean:{}不是类:{}的实例", nameBean.getBeanName(), field.getType().getName());
                DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.DEFAULT);
                diFieldInfo.setBeanDefinition(nameBean);
                return diFieldInfo;
            }
            else
            {
                if (annotationUtil.isPresent(CanBeNull.class, field))
                {
                    return new DIFieldInfo(field, DIFieldInfo.NONE);
                }
                else
                {
                    throw new NullPointerException(StringUtil.format("无法注入{}.{},没有任何可以注入的内容", field.getDeclaringClass().getName(), field.getName()));
                }
            }
        }
        else
        {
            String beanName = field.getType().getName();
            BeanDefinition nameBean = beanNameMap.get(beanName);
            if (nameBean != null)
            {
                Verify.True(field.getType() == nameBean.getType(), "bean:{}不是类:{}的实例", nameBean.getBeanName(), field.getType().getName());
                DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.DEFAULT);
                diFieldInfo.setBeanDefinition(nameBean);
                return diFieldInfo;
            }
            else
            {
                Class<?> fieldType = field.getType();
                for (BeanDefinition each : beanNameMap.values())
                {
                    if (fieldType.isAssignableFrom(each.getType()))
                    {
                        DIFieldInfo diFieldInfo = new DIFieldInfo(field, DIFieldInfo.DEFAULT);
                        diFieldInfo.setBeanDefinition(each);
                        return diFieldInfo;
                    }
                }
                if (annotationUtil.isPresent(CanBeNull.class, field))
                {
                    return new DIFieldInfo(field, DIFieldInfo.NONE);
                }
                else
                {
                    throw new NullPointerException(StringUtil.format("无法注入{}.{},没有任何可以注入的内容", field.getDeclaringClass().getName(), field.getName()));
                }
            }
        }
    }
    
    /**
     * 根据配置文件，返回该bean所有的条件输入注入的field
     * 
     * @param bean
     * @param beanConfig
     * @return
     */
    public static List<ParamField> buildParamField(BeanDefinition beanDefinition, Map<String, String> params, Map<String, String> properties, ClassLoader classLoader)
    {
        Field[] fields = ReflectUtil.getAllFields(beanDefinition.getType());
        List<ParamField> list = new LinkedList<ParamField>();
        AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
        for (Field field : fields)
        {
            if (params.containsKey(field.getName()))
            {
                list.add(buildParamField(field, params.get(field.getName()), classLoader));
            }
            else if (annotationUtil.isPresent(PropertyRead.class, field))
            {
                PropertyRead propertyRead = annotationUtil.getAnnotation(PropertyRead.class, field);
                String propertyName = propertyRead.value();
                if (properties.containsKey(propertyName))
                {
                    list.add(buildParamField(field, properties.get(propertyName), classLoader));
                }
                else
                {
                    continue;
                }
            }
        }
        return list;
    }
    
    private static ParamField buildParamField(Field field, String value, ClassLoader classLoader)
    {
        return AbstractParamField.build(field, value, classLoader);
    }
}
