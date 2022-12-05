package com.jfireframework.jfire.core.aop.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.PriorityQueue;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.FieldModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.baseutil.smc.model.MethodModel.MethodModelKey;
import com.jfireframework.jfire.Utils;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.aop.AopManager;
import com.jfireframework.jfire.core.aop.AopManagerNotated;
import com.jfireframework.jfire.core.aop.notated.After;
import com.jfireframework.jfire.core.aop.notated.Before;
import com.jfireframework.jfire.core.aop.notated.EnhanceClass;

@AopManagerNotated
public class DefaultAopManager implements AopManager
{
    class EnhanceInfo
    {
        Class<?>         type;
        String[]         fieldNames;
        BeanDefinition[] injects;
        Field[]          fields;
    }
    
    private IdentityHashMap<Class<?>, EnhanceInfo> enhanceInfos = new IdentityHashMap<Class<?>, DefaultAopManager.EnhanceInfo>();
    
    @Override
    public void scan(Environment environment)
    {
        AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
        for (BeanDefinition each : environment.beanDefinitions().values())
        {
            if (annotationUtil.isPresent(EnhanceClass.class, each.getType()))
            {
                String rule = annotationUtil.getAnnotation(EnhanceClass.class, each.getType()).value();
                for (BeanDefinition beanDefinition : environment.beanDefinitions().values())
                {
                    if (StringUtil.match(beanDefinition.getType().getName(), rule))
                    {
                        beanDefinition.addAopManager(this);
                    }
                }
            }
        }
    }
    
    @Override
    public void enhance(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName)
    {
        PriorityQueue<BeanDefinition> queue = findAspectClass(type, environment);
        List<String> fieldNames = new ArrayList<String>();
        List<BeanDefinition> injects = new ArrayList<BeanDefinition>();
        for (BeanDefinition each : queue)
        {
            String fieldName = "enhance_" + fieldNameCounter.getAndIncrement();
            fieldNames.add(fieldName);
            injects.add(each);
            FieldModel fieldModel = new FieldModel(fieldName, each.getType());
            classModel.addField(fieldModel);
            for (Method enhanceMethod : each.getType().getMethods())
            {
                if (Utils.ANNOTATION_UTIL.isPresent(Before.class, enhanceMethod))
                {
                    processBeforeAdvice(classModel, type, environment, hostFieldName, fieldName, enhanceMethod);
                }
                else if (Utils.ANNOTATION_UTIL.isPresent(After.class, enhanceMethod))
                {
                    processAfterAdvice(classModel, type, environment, hostFieldName, fieldName, enhanceMethod);
                }
            }
        }
        
    }
    
    private void processBeforeAdvice(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String rule = Utils.ANNOTATION_UTIL.getAnnotation(Before.class, enhanceMethod).value();
        for (Method method : type.getMethods())
        {
            if (match(rule, method))
            {
                MethodModelKey key = new MethodModelKey(method);
                MethodModel methodModel = classModel.getMethodModel(key);
                String originBody = methodModel.getBody();
                int sequence = environment.registerMethod(method);
                String pointName = "poine_" + fieldNameCounter.getAndIncrement();
                StringCache cache = new StringCache();
                cache.append("ProceedPointImpl ").append(pointName).append(" = new ProceedPointImpl();\r\n");
                cache.append(pointName).append(".setHost(").append(hostFieldName).append(");\r\n");
                cache.append(pointName).append(".setMethod(").append(Environment.ENVIRONMENT_FIELD_NAME).append(".getMethod(").append(sequence).append("));\r\n");
                if (method.getParameterTypes().length != 0)
                {
                    cache.append(pointName).append(".setParams(");
                    int length = method.getParameterTypes().length;
                    for (int i = 0; i < length; i++)
                    {
                        cache.append("$").append(i).append(",");
                    }
                    cache.deleteLast();
                    cache.append(");\r\n");
                }
                cache.append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
                cache.append(originBody);
                methodModel.setBody(cache.toString());
            }
        }
    }
    
    private void processAfterAdvice(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String rule = Utils.ANNOTATION_UTIL.getAnnotation(After.class, enhanceMethod).value();
        for (Method method : type.getMethods())
        {
            if (match(rule, method))
            {
                MethodModelKey key = new MethodModelKey(method);
                MethodModel methodModel = classModel.getMethodModel(key);
                String originBody = methodModel.getBody();
                int sequence = environment.registerMethod(method);
                String pointName = "poine_" + fieldNameCounter.getAndIncrement();
                StringCache cache = new StringCache();
                cache.append("try{\r\n").append(originBody).append("}\r\n").append("finally\r\n{\r\n");
                cache.append("ProceedPointImpl ").append(pointName).append(" = new ProceedPointImpl();\r\n");
                cache.append(pointName).append(".setHost(").append(hostFieldName).append(");\r\n");
                cache.append(pointName).append(".setMethod(").append(Environment.ENVIRONMENT_FIELD_NAME).append(".getMethod(").append(sequence).append("));\r\n");
                if (method.getParameterTypes().length != 0)
                {
                    cache.append(pointName).append(".setParams(");
                    int length = method.getParameterTypes().length;
                    for (int i = 0; i < length; i++)
                    {
                        cache.append("$").append(i).append(",");
                    }
                    cache.deleteLast();
                    cache.append(");\r\n");
                }
                cache.append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
                cache.append("}");
                methodModel.setBody(cache.toString());
            }
        }
    }
    
    private PriorityQueue<BeanDefinition> findAspectClass(Class<?> type, Environment environment)
    {
        PriorityQueue<BeanDefinition> queue = new PriorityQueue<BeanDefinition>(10, new Comparator<BeanDefinition>() {
            
            @Override
            public int compare(BeanDefinition o1, BeanDefinition o2)
            {
                int order1 = Utils.ANNOTATION_UTIL.getAnnotation(EnhanceClass.class, o1.getType()).order();
                int order2 = Utils.ANNOTATION_UTIL.getAnnotation(EnhanceClass.class, o2.getType()).order();
                return order1 - order2;
            }
        });
        for (BeanDefinition each : environment.beanDefinitions().values())
        {
            if (Utils.ANNOTATION_UTIL.isPresent(EnhanceClass.class, each.getType()))
            {
                String rule = Utils.ANNOTATION_UTIL.getAnnotation(EnhanceClass.class, each.getType()).value();
                if (StringUtil.match(type.getName(), rule))
                {
                    queue.add(each);
                }
            }
        }
        return queue;
    }
    
    private boolean match(String rule, Method method)
    {
        String methodNameRule = rule.substring(0, rule.indexOf('('));
        if (StringUtil.match(method.getName(), methodNameRule) == false)
        {
            return false;
        }
        String paramRule = rule.substring(rule.indexOf('(') + 1, rule.length() - 1);
        if ("*".equals(paramRule))
        {
            return true;
        }
        String[] split = paramRule.split(",");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < split.length; i++)
        {
            String literals = split[i].trim();
            if ("*".equals(literals))
            {
                continue;
            }
            if (literals.equals(parameterTypes[i].getName()) == false)
            {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void enhanceFinish(Class<?> type, Class<?> enhanceType, Environment environment)
    {
        EnhanceInfo enhanceInfo = enhanceInfos.get(type);
        Field[] fields = new Field[enhanceInfo.fieldNames.length];
        for (int i = 0; i < enhanceInfo.fieldNames.length; i++)
        {
            String fieldName = enhanceInfo.fieldNames[i];
            try
            {
                Field field = enhanceType.getDeclaredField(fieldName);
                field.setAccessible(true);
                fields[i] = field;
            }
            catch (Exception e)
            {
                throw new CannotFindEnhanceFieldException(e);
            }
        }
        enhanceInfo.fields = fields;
    }
    
    @Override
    public void fillBean(Object bean, Class<?> type)
    {
        EnhanceInfo enhanceInfo = enhanceInfos.get(type);
        Field[] fields = enhanceInfo.fields;
        BeanDefinition[] injects = enhanceInfo.injects;
        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];
            try
            {
                field.set(bean, injects[i].getBeanInstance());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public int order()
    {
        return DEFAULT;
    }
    
}
