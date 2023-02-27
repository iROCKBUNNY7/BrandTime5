package com.jfireframework.jfire.core.inject.impl;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.reflect.ValueAccessor;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.inject.InjectHandler;
import com.jfireframework.jfire.core.inject.notated.CanBeNull;
import com.jfireframework.jfire.core.inject.notated.MapKeyMethodName;
import com.jfireframework.jfire.exception.BeanDefinitionCanNotFindException;
import com.jfireframework.jfire.exception.InjectTypeException;
import com.jfireframework.jfire.exception.InjectValueException;
import com.jfireframework.jfire.exception.MapKeyMethodCanNotFindException;
import com.jfireframework.jfire.util.Utils;

import javax.annotation.Resource;
import java.lang.reflect.*;
import java.util.*;

public class DefaultDependencyInjectHandler implements InjectHandler
{
    private Environment   environment;
    private Inject        inject;
    private ValueAccessor valueAccessor;

    @Override
    public void init(Field field, Environment environment)
    {
        if (field.getType().isPrimitive())
        {
            throw new UnsupportedOperationException("基础类型无法执行注入操作");
        }
        this.environment = environment;
        valueAccessor = new ValueAccessor(field);
        Class<?> fieldType = field.getType();
        if (Map.class.isAssignableFrom(fieldType))
        {
            inject = new MapInject();
        }
        else if (Collection.class.isAssignableFrom(fieldType))
        {
            inject = new CollectionInject();
        }
        else if (fieldType.isInterface() || Modifier.isAbstract(fieldType.getModifiers()))
        {
            inject = new AbstractInject();
        }
        else
        {
            inject = new InstacenInject();
        }
    }

    @Override
    public void inject(Object instance)
    {
        inject.inject(instance);
    }

    interface Inject
    {
        void inject(Object instance);
    }

    class InstacenInject implements Inject
    {
        private BeanDefinition beanDefinition;

        InstacenInject()
        {
            Field          field          = valueAccessor.getField();
            AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
            Resource       resource       = annotationUtil.getAnnotation(Resource.class, field);
            String         beanName       = StringUtil.isNotBlank(resource.name()) ? resource.name() : field.getType().getName();
            beanDefinition = environment.getBeanDefinition(beanName);
            if (beanDefinition == null)
            {
                throw new InjectValueException("无法找到属性:" + field.getDeclaringClass().getSimpleName() + "." + field.getName() + "可以注入的bean，需要的bean名称:" + beanName);
            }
        }

        public void inject(Object instance)
        {
            Object value = beanDefinition.getBeanInstance();
            try
            {
                valueAccessor.setObject(instance, value);
            } catch (Exception e)
            {
                throw new InjectValueException(e);
            }
        }
    }

    class AbstractInject implements Inject
    {
        BeanDefinition beanDefinition;

        AbstractInject()
        {
            Field    field     = valueAccessor.getField();
            Class<?> fieldType = field.getType();
            Resource resource  = Utils.ANNOTATION_UTIL.getAnnotation(Resource.class, field);
            // 如果定义了名称，就寻找特定名称的Bean
            if (StringUtil.isNotBlank(resource.name()))
            {
                beanDefinition = environment.getBeanDefinition(resource.name());
                if (beanDefinition == null && Utils.ANNOTATION_UTIL.isPresent(CanBeNull.class, field) == false)
                {
                    throw new BeanDefinitionCanNotFindException(resource.name());
                }
            }
            else
            {
                List<BeanDefinition> list = environment.getBeanDefinitionByAbstract(fieldType);
                if (list.size() > 1)
                {
                    throw new BeanDefinitionCanNotFindException(list, fieldType);
                }
                else if (list.size() == 1)
                {
                    beanDefinition = list.get(0);
                }
                else if (Utils.ANNOTATION_UTIL.isPresent(CanBeNull.class, field))
                {
                    //可为空，允许
                    return;
                }
                else
                {
                    throw new BeanDefinitionCanNotFindException(list, fieldType);
                }
            }
        }

        @Override
        public void inject(Object instance)
        {
            if (beanDefinition != null)
            {
                Object value = beanDefinition.getBeanInstance();
                try
                {
                    valueAccessor.setObject(instance, value);
                } catch (Exception e)
                {
                    throw new InjectValueException(e);
                }
            }
        }
    }

    class CollectionInject implements Inject
    {
        private BeanDefinition[] beanDefinitions;
        private int              listOrSet = 0;

        private static final int LIST = 1;
        private static final int SET  = 2;

        CollectionInject()
        {
            Field field       = valueAccessor.getField();
            Type  genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType == false)
            {
                throw new InjectTypeException(field.toGenericString() + "不是泛型定义，无法找到需要注入的Bean类型");
            }
            Class<?>             rawType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
            List<BeanDefinition> list    = environment.getBeanDefinitionByAbstract(rawType);
            beanDefinitions = list.toArray(new BeanDefinition[list.size()]);
            if (List.class.isAssignableFrom(field.getType()))
            {
                listOrSet = LIST;
            }
            else if (Set.class.isAssignableFrom(field.getType()))
            {
                listOrSet = SET;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void inject(Object instance)
        {
            try
            {
                Collection<Object> value = (Collection<Object>) valueAccessor.get(instance);
                if (value == null)
                {
                    if (listOrSet == LIST)
                    {
                        value = new LinkedList<Object>();
                        valueAccessor.setObject(instance, value);
                    }
                    else if (listOrSet == SET)
                    {
                        value = new HashSet<Object>();
                        valueAccessor.setObject(instance, value);
                    }
                    else
                    {
                        throw new InjectValueException("无法识别类型:" + valueAccessor.getField().getType().getName() + "，无法生成其对应的实例");
                    }
                }
                for (BeanDefinition each : beanDefinitions)
                {
                    value.add(each.getBeanInstance());
                }
            } catch (InjectValueException e)
            {
                throw e;
            } catch (Exception e)
            {
                throw new InjectValueException(e);
            }
        }
    }

    enum MapKeyType
    {
        BEAN_NAME, METHOD
    }

    class MapInject implements Inject
    {
        MapKeyType       mapKeyType;
        BeanDefinition[] beanDefinitions;
        Method           method;

        MapInject()
        {
            Field field       = valueAccessor.getField();
            Type  genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType == false)
            {
                throw new InjectTypeException(field.toGenericString() + "不是泛型定义，无法找到需要注入的Bean类型");
            }
            Class<?>             rawType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[1];
            List<BeanDefinition> list    = environment.getBeanDefinitionByAbstract(rawType);
            beanDefinitions = list.toArray(new BeanDefinition[list.size()]);
            if (Utils.ANNOTATION_UTIL.isPresent(MapKeyMethodName.class, field))
            {
                mapKeyType = MapKeyType.METHOD;
                String methodName = Utils.ANNOTATION_UTIL.getAnnotation(MapKeyMethodName.class, field).value();
                try
                {
                    method = rawType.getMethod(methodName);
                } catch (Exception e)
                {
                    throw new MapKeyMethodCanNotFindException(methodName, rawType, e);
                }
            }
            else
            {
                mapKeyType = MapKeyType.BEAN_NAME;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void inject(Object instance)
        {
            try
            {
                Map<Object, Object> value = (Map<Object, Object>) valueAccessor.get(instance);
                if (value == null)
                {
                    value = new HashMap<Object, Object>();
                    valueAccessor.setObject(instance, value);
                }
                switch (mapKeyType)
                {
                    case METHOD:
                        for (BeanDefinition each : beanDefinitions)
                        {
                            Object entryValue = each.getBeanInstance();
                            Object entryKey   = method.invoke(entryValue);
                            value.put(entryKey, entryValue);
                        }
                        break;
                    case BEAN_NAME:
                        for (BeanDefinition each : beanDefinitions)
                        {
                            Object entryValue = each.getBeanInstance();
                            String entryKey   = each.getBeanName();
                            value.put(entryKey, entryValue);
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e)
            {
                throw new InjectValueException(e);
            }
        }
    }
}
