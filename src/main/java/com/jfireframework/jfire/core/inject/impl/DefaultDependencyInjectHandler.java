package com.jfireframework.jfire.core.inject.impl;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.baseutil.reflect.ValueAccessor;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.JfireContext;
import com.jfireframework.jfire.core.inject.InjectHandler;
import com.jfireframework.jfire.core.inject.notated.CanBeNull;
import com.jfireframework.jfire.core.inject.notated.MapKeyMethodName;
import com.jfireframework.jfire.exception.BeanDefinitionCanNotFindException;
import com.jfireframework.jfire.exception.InjectTypeException;
import com.jfireframework.jfire.exception.InjectValueException;
import com.jfireframework.jfire.exception.MapKeyMethodCanNotFindException;

import javax.annotation.Resource;
import java.lang.reflect.*;
import java.util.*;

public class DefaultDependencyInjectHandler implements InjectHandler
{
    private JfireContext  context;
    private Inject        inject;
    private ValueAccessor valueAccessor;

    @Override
    public void init(Field field, JfireContext context)
    {
        if (field.getType().isPrimitive())
        {
            throw new UnsupportedOperationException("基础类型无法执行注入操作");
        }
        this.context = context;
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
            Field                    field                    = valueAccessor.getField();
            AnnotationContextFactory annotationContextFactory = context.getAnnotationContextFactory();
            AnnotationContext        annotationContext        = annotationContextFactory.get(field, Thread.currentThread().getContextClassLoader());
            Resource                 resource                 = annotationContext.getAnnotation(Resource.class);
            String                   beanName                 = StringUtil.isNotBlank(resource.name()) ? resource.name() : field.getType().getName();
            beanDefinition = context.getBeanDefinition(beanName);
            if (beanDefinition == null && annotationContext.isAnnotationPresent(CanBeNull.class) == false)
            {
                throw new InjectValueException("无法找到属性:" + field.getDeclaringClass().getSimpleName() + "." + field.getName() + "可以注入的bean，需要的bean名称:" + beanName);
            }
        }

        public void inject(Object instance)
        {
            Object value = beanDefinition.getBean();
            try
            {
                valueAccessor.setObject(instance, value);
            }
            catch (Exception e)
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
            Field                    field                    = valueAccessor.getField();
            Class<?>                 fieldType                = field.getType();
            AnnotationContextFactory annotationContextFactory = context.getAnnotationContextFactory();
            AnnotationContext        annotationContext        = annotationContextFactory.get(field, Thread.currentThread().getContextClassLoader());
            Resource                 resource                 = annotationContext.getAnnotation(Resource.class);
            // 如果定义了名称，就寻找特定名称的Bean
            if (StringUtil.isNotBlank(resource.name()))
            {
                beanDefinition = context.getBeanDefinition(resource.name());
                if (beanDefinition == null && annotationContext.isAnnotationPresent(CanBeNull.class) == false)
                {
                    throw new BeanDefinitionCanNotFindException(resource.name());
                }
            }
            else
            {
                List<BeanDefinition> list = context.getBeanDefinitions(fieldType);
                if (list.size() > 1)
                {
                    throw new BeanDefinitionCanNotFindException(list, fieldType);
                }
                else if (list.size() == 1)
                {
                    beanDefinition = list.get(0);
                }
                else if (annotationContext.isAnnotationPresent(CanBeNull.class))
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
                Object value = beanDefinition.getBean();
                try
                {
                    valueAccessor.setObject(instance, value);
                }
                catch (Exception e)
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
            List<BeanDefinition> list    = context.getBeanDefinitions(rawType);
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
                    value.add(each.getBean());
                }
            }
            catch (InjectValueException e)
            {
                throw e;
            }
            catch (Exception e)
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
            List<BeanDefinition> list    = context.getBeanDefinitions(rawType);
            beanDefinitions = list.toArray(new BeanDefinition[list.size()]);
            AnnotationContext annotationContext = context.getAnnotationContextFactory().get(field, Thread.currentThread().getContextClassLoader());
            if (annotationContext.isAnnotationPresent(MapKeyMethodName.class))
            {
                mapKeyType = MapKeyType.METHOD;
                String methodName = annotationContext.getAnnotation(MapKeyMethodName.class).value();
                try
                {
                    method = rawType.getMethod(methodName);
                }
                catch (Exception e)
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
                            Object entryValue = each.getBean();
                            Object entryKey   = method.invoke(entryValue);
                            value.put(entryKey, entryValue);
                        }
                        break;
                    case BEAN_NAME:
                        for (BeanDefinition each : beanDefinitions)
                        {
                            Object entryValue = each.getBean();
                            String entryKey   = each.getBeanName();
                            value.put(entryKey, entryValue);
                        }
                        break;
                    default:
                        break;
                }
            }
            catch (Exception e)
            {
                throw new InjectValueException(e);
            }
        }
    }
}
