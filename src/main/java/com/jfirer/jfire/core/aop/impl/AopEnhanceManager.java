package com.jfirer.jfire.core.aop.impl;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.TRACEID;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.smc.SmcHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.FieldModel;
import com.jfirer.baseutil.smc.model.MethodModel;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.BeanDefinition;
import com.jfirer.jfire.core.aop.EnhanceCallbackForBeanInstance;
import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.aop.ProceedPoint;
import com.jfirer.jfire.core.aop.notated.*;
import com.jfirer.jfire.exception.CannotFindEnhanceFieldException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class AopEnhanceManager implements EnhanceManager
{

    private static final Logger logger = LoggerFactory.getLogger(AopEnhanceManager.class);

    class EnhanceInfo
    {
        Class<?>         type;
        String[]         fieldNames;
        BeanDefinition[] injects;
        volatile Field[] fields;
    }

    @Override
    public void scan(ApplicationContext context)
    {
        AnnotationContextFactory annotationContextFactory = context.getAnnotationContextFactory();
        ClassLoader              classLoader              = Thread.currentThread().getContextClassLoader();
        for (BeanDefinition each : context.getAllBeanDefinitions())
        {
            AnnotationContext annotationContext = annotationContextFactory.get(each.getType(), classLoader);
            if (annotationContext.isAnnotationPresent(EnhanceClass.class))
            {
                String rule = annotationContext.getAnnotationMetadata(EnhanceClass.class).getAttribyte("value").getStringValue();
                for (BeanDefinition beanDefinition : context.getAllBeanDefinitions())
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
    public EnhanceCallbackForBeanInstance enhance(ClassModel classModel, final Class<?> type, ApplicationContext applicationContext, String hostFieldName)
    {
        PriorityQueue<BeanDefinition> queue                    = findAspectClass(type, applicationContext);
        List<String>                  fieldNames               = new ArrayList<String>();
        List<BeanDefinition>          injects                  = new ArrayList<BeanDefinition>();
        AnnotationContextFactory      annotationContextFactory = applicationContext.getAnnotationContextFactory();
        ClassLoader                   classLoader              = Thread.currentThread().getContextClassLoader();
        for (BeanDefinition each : queue)
        {
            String fieldName = "enhance_" + fieldNameCounter.getAndIncrement();
            fieldNames.add(fieldName);
            injects.add(each);
            FieldModel fieldModel = new FieldModel(fieldName, each.getType(), classModel);
            classModel.addField(fieldModel);
            for (Method enhanceMethod : each.getType().getMethods())
            {
                AnnotationContext annotationContext = annotationContextFactory.get(enhanceMethod, classLoader);
                if (annotationContext.isAnnotationPresent(Before.class))
                {
                    processBeforeAdvice(classModel, type, annotationContext, hostFieldName, fieldName, enhanceMethod);
                }
                else if (annotationContext.isAnnotationPresent(After.class))
                {
                    processAfterAdvice(classModel, type, annotationContext, hostFieldName, fieldName, enhanceMethod);
                }
                else if (annotationContext.isAnnotationPresent(AfterReturning.class))
                {
                    processAfterReturningAdvice(classModel, type, annotationContext, hostFieldName, fieldName, enhanceMethod);
                }
                else if (annotationContext.isAnnotationPresent(AfterThrowable.class))
                {
                    processAfterThrowableAdvice(classModel, type, annotationContext, hostFieldName, fieldName, enhanceMethod);
                }
                else if (annotationContext.isAnnotationPresent(Around.class))
                {
                    processAroundAdvice(classModel, type, annotationContext, hostFieldName, fieldName, enhanceMethod);
                }
            }
        }
        final EnhanceInfo enhanceInfo = new EnhanceInfo();
        enhanceInfo.fieldNames = fieldNames.toArray(new String[fieldNames.size()]);
        enhanceInfo.injects = injects.toArray(new BeanDefinition[injects.size()]);
        enhanceInfo.type = type;
        return new EnhanceCallbackForBeanInstance()
        {

            @Override
            public void run(Object beanInstance)
            {
                if (enhanceInfo.fields == null)
                {
                    synchronized (enhanceInfo)
                    {
                        if (enhanceInfo.fields == null)
                        {
                            Field[]  fields      = new Field[enhanceInfo.fieldNames.length];
                            Class<?> enhanceType = beanInstance.getClass();
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
                    }
                }
                Field[]          fields  = enhanceInfo.fields;
                BeanDefinition[] injects = enhanceInfo.injects;
                for (int i = 0; i < fields.length; i++)
                {
                    Field field = fields[i];
                    try
                    {
                        field.set(beanInstance, injects[i].getBean());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private void processBeforeAdvice(ClassModel classModel, Class<?> type, AnnotationContext annotationContext, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String traceId = TRACEID.currentTraceId();
        String rule    = getRule(annotationContext, enhanceMethod, Before.class);
        for (Method method : type.getMethods())
        {
            if (match(rule, method))
            {
                logger.debug("traceId:{} 前置通知规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModel.MethodModelKey key         = new MethodModel.MethodModelKey(method);
                MethodModel                methodModel = classModel.getMethodModel(key);
                String                     originBody  = methodModel.getBody();
                String                     pointName   = "point_" + fieldNameCounter.getAndIncrement();
                StringBuilder              cache       = new StringBuilder();
                generateProceedPointImpl(classModel, hostFieldName, method, pointName, cache, false);
                generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
                cache.append(originBody);
                methodModel.setBody(cache.toString());
            }
        }
    }

    private String getRule(AnnotationContext annotationContext, Method enhanceMethod, Class<? extends Annotation> type)
    {
        return annotationContext.getAnnotationMetadata(type).getAttribyte("value").getStringValue();
    }

    private void generateProceedPointImplWithInvokeinternal(ClassModel classModel, String hostFieldName, Method method, String pointName, StringBuilder cache, String origin)
    {
        cache.append("ProceedPointImpl ").append(pointName).append(" = new ProceedPointImpl(){\r\n");
        if (method.getReturnType() == void.class)
        {
            cache.append("public Object invokeInternel(){ ").append(origin).append(" return null;\r\n}\r\n};\r\n");
        }
        else
        {
            cache.append("public Object invokeInternel(){ ").append(origin).append(" }\r\n};\r\n");
        }
        generateProceedPointImpl(classModel, hostFieldName, method, pointName, cache, true);
    }

    private void generateProceedPointImpl(ClassModel classModel, String hostFieldName, Method method, String pointName, StringBuilder cache, boolean hasProceedPoint)
    {
        if (hasProceedPoint == false)
        {
            cache.append("ProceedPointImpl ").append(pointName).append(" = new ProceedPointImpl();\r\n");
        }
        cache.append(pointName).append(".setHost(").append(hostFieldName).append(");\r\n");
        String _MethodDescriptionName = "_MethodDescription_" + fieldNameCounter.getAndIncrement();
        classModel.addImport(ProceedPoint.MethodDescription.class);
        cache.append("MethodDescription " + _MethodDescriptionName + " = new MethodDescription(\"").append(method.getName()).append("\",new Class[]{");
        if (method.getParameterTypes().length != 0)
        {
            for (Class<?> each : method.getParameterTypes())
            {
                cache.append(SmcHelper.getReferenceName(each, classModel)).append(".class,");
            }
            cache.setLength(cache.length() - 1);
        }
        cache.append("});\r\n");
        cache.append(pointName).append(".setMethodDescription(" + _MethodDescriptionName + ");\r\n");
        if (method.getParameterTypes().length != 0)
        {
            cache.append(pointName).append(".setParams(new Object[]{");
            int length = method.getParameterTypes().length;
            for (int i = 0; i < length; i++)
            {
                cache.append("$").append(i).append(',');
            }
            cache.setLength(cache.length() - 1);
            cache.append("});\r\n");
        }
    }

    private void processAfterAdvice(ClassModel classModel, Class<?> type, AnnotationContext annotationContext, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String traceId = TRACEID.currentTraceId();
        String rule    = getRule(annotationContext, enhanceMethod, After.class);
        for (Method method : type.getMethods())
        {
            if (match(rule, method))
            {
                logger.debug("traceId:{} 后置通知规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModel.MethodModelKey key         = new MethodModel.MethodModelKey(method);
                MethodModel                methodModel = classModel.getMethodModel(key);
                String                     originBody  = methodModel.getBody();
                String                     pointName   = "point_" + fieldNameCounter.getAndIncrement();
                StringBuilder              cache       = new StringBuilder();
                cache.append("try{\r\n").append(originBody).append("}\r\n").append("finally\r\n{\r\n");
                generateProceedPointImpl(classModel, hostFieldName, method, pointName, cache, false);
                generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
                cache.append("}");
                methodModel.setBody(cache.toString());
            }
        }
    }

    private void generateEnhanceMethodInvoke(String fieldName, Method enhanceMethod, String pointName, StringBuilder cache)
    {
        cache.append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
    }

    private void processAfterReturningAdvice(ClassModel classModel, Class<?> type, AnnotationContext annotationContext, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String traceId = TRACEID.currentTraceId();
        String rule    = getRule(annotationContext, enhanceMethod, AfterReturning.class);
        for (Method method : type.getMethods())
        {
            if (method.getReturnType() != void.class && match(rule, method))
            {
                logger.debug("traceId:{} 返回通知规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModel.MethodModelKey key    = new MethodModel.MethodModelKey(method);
                MethodModel                origin = classModel.removeMethodModel(key);
                MethodModel                newOne = new MethodModel(method, classModel);
                origin.setAccessLevel(MethodModel.AccessLevel.PRIVATE);
                origin.setMethodName(origin.getMethodName() + "_" + methodNameCounter.getAndIncrement());
                classModel.putMethodModel(origin);
                StringBuilder cache = new StringBuilder();
                cache.append(SmcHelper.getReferenceName(method.getReturnType(), classModel)).append(" result = ").append(origin.generateInvoke()).append(";\r\n");
                String pointName = generatePointName();
                generateProceedPointImpl(classModel, hostFieldName, method, pointName, cache, false);
                cache.append(pointName).append(".setResult(result);\r\n");
                generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
                cache.append("return result;\r\n");
                newOne.setBody(cache.toString());
                classModel.putMethodModel(newOne);
            }
        }
    }

    private String generatePointName()
    {
        return "point_" + fieldNameCounter.getAndIncrement();
    }

    private void processAfterThrowableAdvice(ClassModel classModel, Class<?> type, AnnotationContext annotationContext, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String traceId = TRACEID.currentTraceId();
        String rule    = getRule(annotationContext, enhanceMethod, AfterThrowable.class);
        classModel.addImport(ReflectUtil.class);
        for (Method method : type.getMethods())
        {
            if (match(rule, method))
            {
                logger.debug("traceId:{} 规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModel.MethodModelKey key         = new MethodModel.MethodModelKey(method);
                MethodModel                methodModel = classModel.getMethodModel(key);
                String                     body        = methodModel.getBody();
                StringBuilder              cache       = new StringBuilder();
                cache.append("try\r\n{\r\n").append(body).append("}\r\ncatch(java.lang.Throwable e)\r\n{");
                String pointName = generatePointName();
                generateProceedPointImpl(classModel, hostFieldName, method, pointName, cache, false);
                cache.append(pointName).append(".setE(e);\r\n");
                generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
                cache.append("ReflectUtil.throwException(e);\r\n");
                if (method.getReturnType().isPrimitive())
                {
                    if (method.getReturnType() == boolean.class)
                    {
                        cache.append("return false;\r\n");
                    }
                    else if (method.getReturnType() != void.class)
                    {
                        cache.append("return 0;\r\n");
                    }
                }
                else
                {
                    cache.append("return null;\r\n");
                }
                cache.append("}\r\n");
                methodModel.setBody(cache.toString());
                classModel.putMethodModel(methodModel);
            }
        }
    }

    private void processAroundAdvice(ClassModel classModel, Class<?> type, AnnotationContext annotationContext, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String traceId = TRACEID.currentTraceId();
        String rule    = getRule(annotationContext, enhanceMethod, Around.class);
        for (Method method : type.getMethods())
        {
            if (match(rule, method))
            {
                logger.debug("traceId:{} 环绕通知规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModel.MethodModelKey key         = new MethodModel.MethodModelKey(method);
                MethodModel                methodModel = classModel.getMethodModel(key);
                boolean[]                  flags       = new boolean[methodModel.getParamterTypes().length];
                Arrays.fill(flags, true);
                methodModel.setParamterFinals(flags);
                String        body      = methodModel.getBody();
                String        pointName = generatePointName();
                StringBuilder cache     = new StringBuilder();
                generateProceedPointImplWithInvokeinternal(classModel, hostFieldName, method, pointName, cache, body);
                Class<?> returnType = method.getReturnType();
                generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
                if (returnType == void.class)
                {
                }
                else if (returnType.isPrimitive())
                {
                    if (returnType == int.class)
                    {
                        classModel.addImport(Integer.class);
                        cache.append("return (Integer)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == short.class)
                    {
                        classModel.addImport(Short.class);
                        cache.append("return (Short)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == long.class)
                    {
                        classModel.addImport(Long.class);
                        cache.append("return (Long)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == float.class)
                    {
                        classModel.addImport(Float.class);
                        cache.append("return (Float)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == double.class)
                    {
                        classModel.addImport(Double.class);
                        cache.append("return (Double)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == byte.class)
                    {
                        classModel.addImport(Byte.class);
                        cache.append("return (Byte)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == char.class)
                    {
                        classModel.addImport(Character.class);
                        cache.append("return (Character)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == boolean.class)
                    {
                        classModel.addImport(Boolean.class);
                        cache.append("return (Boolean)").append(pointName).append(".getResult();\r\n");
                    }
                    else
                    {
                        throw new UnsupportedOperationException();
                    }
                }
                else
                {
                    cache.append("return (").append(SmcHelper.getReferenceName(returnType, classModel)).append(")").append(pointName).append(".getResult();\r\n");
                }
                methodModel.setBody(cache.toString());
                classModel.putMethodModel(methodModel);
            }
        }
    }

    private PriorityQueue<BeanDefinition> findAspectClass(Class<?> type, ApplicationContext applicationContext)
    {
        final ClassLoader              classLoader              = Thread.currentThread().getContextClassLoader();
        final AnnotationContextFactory annotationContextFactory = applicationContext.getAnnotationContextFactory();
        PriorityQueue<BeanDefinition> queue = new PriorityQueue<BeanDefinition>(10, new Comparator<BeanDefinition>()
        {

            @Override
            public int compare(BeanDefinition o1, BeanDefinition o2)
            {
                int order1 = annotationContextFactory.get(o1.getType(), classLoader).getAnnotation(EnhanceClass.class).order();
                int order2 = annotationContextFactory.get(o2.getType(), classLoader).getAnnotation(EnhanceClass.class).order();
                return order1 - order2;
            }
        });
        for (BeanDefinition each : applicationContext.getAllBeanDefinitions())
        {
            AnnotationContext annotationContext = annotationContextFactory.get(each.getType(), classLoader);
            if (annotationContext.isAnnotationPresent(EnhanceClass.class))
            {
                String rule = annotationContext.getAnnotation(EnhanceClass.class).value();
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
        String traceId = TRACEID.currentTraceId();
        logger.trace("traceId:{} 准备匹配AOP方法拦截，规则:{},方法:{}", traceId, rule, method.toGenericString());
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
        if ("".equals(paramRule))
        {
            return method.getParameterTypes().length == 0;
        }
        String[]   split          = paramRule.split(",");
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (split.length != parameterTypes.length)
        {
            return false;
        }
        for (int i = 0; i < split.length; i++)
        {
            String literals = split[i].trim();
            if ("*".equals(literals))
            {
                continue;
            }
            if (literals.equals(parameterTypes[i].getSimpleName()) == false && literals.equals(parameterTypes[i].getName()) == false)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int order()
    {
        return DEFAULT;
    }
}
