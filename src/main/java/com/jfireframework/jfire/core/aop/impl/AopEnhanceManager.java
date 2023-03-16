package com.jfireframework.jfire.core.aop.impl;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.FieldModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.baseutil.smc.model.MethodModel.AccessLevel;
import com.jfireframework.baseutil.smc.model.MethodModel.MethodModelKey;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.EnvironmentTmp;
import com.jfireframework.jfire.core.aop.EnhanceCallbackForBeanInstance;
import com.jfireframework.jfire.core.aop.EnhanceManager;
import com.jfireframework.jfire.core.aop.ProceedPoint;
import com.jfireframework.jfire.core.aop.notated.*;
import com.jfireframework.jfire.core.prepare.support.annotaion.AnnotationDatabase;
import com.jfireframework.jfire.exception.CannotFindEnhanceFieldException;
import com.jfireframework.jfire.util.Utils;
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
    public void scan(EnvironmentTmp environment)
    {
        AnnotationDatabase annotationDatabase = environment.getAnnotationDatabase();
        for (BeanDefinition each : environment.beanDefinitions().values())
        {
            if (annotationDatabase.isAnnotationPresentOnClass(each.getType().getName(), EnhanceClass.class))
            {
                List<AnnotationMetadata> annotations = annotationDatabase.getAnnotations(each.getType().getName(), EnhanceClass.class);
                String                   rule        = annotations.get(0).getAttribyte("value").getStringValue();
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
    public EnhanceCallbackForBeanInstance enhance(ClassModel classModel, final Class<?> type, EnvironmentTmp environment, String hostFieldName)
    {
        PriorityQueue<BeanDefinition> queue              = findAspectClass(type, environment);
        List<String>                  fieldNames         = new ArrayList<String>();
        List<BeanDefinition>          injects            = new ArrayList<BeanDefinition>();
        AnnotationDatabase            annotationDatabase = environment.getAnnotationDatabase();
        for (BeanDefinition each : queue)
        {
            String fieldName = "enhance_" + fieldNameCounter.getAndIncrement();
            fieldNames.add(fieldName);
            injects.add(each);
            FieldModel fieldModel = new FieldModel(fieldName, each.getType(), classModel);
            classModel.addField(fieldModel);
            for (Method enhanceMethod : each.getType().getMethods())
            {
                if (annotationDatabase.isAnnotationPresentOnMethod(enhanceMethod, Before.class))
                {
                    processBeforeAdvice(classModel, type, annotationDatabase, hostFieldName, fieldName, enhanceMethod);
                }
                else if (annotationDatabase.isAnnotationPresentOnMethod(enhanceMethod, After.class))
                {
                    processAfterAdvice(classModel, type, annotationDatabase, hostFieldName, fieldName, enhanceMethod);
                }
                else if (annotationDatabase.isAnnotationPresentOnMethod(enhanceMethod, AfterReturning.class))
                {
                    processAfterReturningAdvice(classModel, type, annotationDatabase, hostFieldName, fieldName, enhanceMethod);
                }
                else if (annotationDatabase.isAnnotationPresentOnMethod(enhanceMethod, AfterThrowable.class))
                {
                    processAfterThrowableAdvice(classModel, type, annotationDatabase, hostFieldName, fieldName, enhanceMethod);
                }
                else if (annotationDatabase.isAnnotationPresentOnMethod(enhanceMethod, Around.class))
                {
                    processAroundAdvice(classModel, type, annotationDatabase, hostFieldName, fieldName, enhanceMethod);
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

    private void processBeforeAdvice(ClassModel classModel, Class<?> type, AnnotationDatabase annotationDatabase, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String traceId = TRACEID.currentTraceId();
        String rule    = getRule(annotationDatabase, enhanceMethod, Before.class);
        for (Method method : type.getMethods())
        {
            if (match(rule, method))
            {
                logger.debug("traceId:{} 前置通知规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModelKey key         = new MethodModelKey(method);
                MethodModel    methodModel = classModel.getMethodModel(key);
                String         originBody  = methodModel.getBody();
                String         pointName   = "point_" + fieldNameCounter.getAndIncrement();
                StringCache    cache       = new StringCache();
                generateProceedPointImpl(classModel, hostFieldName, method, pointName, cache, false);
                generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
                cache.append(originBody);
                methodModel.setBody(cache.toString());
            }
        }
    }

    private String getRule(AnnotationDatabase annotationDatabase, Method enhanceMethod, Class<? extends Annotation> type)
    {
        return annotationDatabase.getAnnotations(enhanceMethod, type).get(0).getAttribyte("value").getStringValue();
    }

    private void generateProceedPointImplWithInvokeinternal(ClassModel classModel, String hostFieldName, Method method, String pointName, StringCache cache, String origin)
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

    private void generateProceedPointImpl(ClassModel classModel, String hostFieldName, Method method, String pointName, StringCache cache, boolean hasProceedPoint)
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
            cache.deleteLast();
        }
        cache.append("});\r\n");
        cache.append(pointName).append(".setMethodDescription(" + _MethodDescriptionName + ");\r\n");
        if (method.getParameterTypes().length != 0)
        {
            cache.append(pointName).append(".setParams(new Object[]{");
            int length = method.getParameterTypes().length;
            for (int i = 0; i < length; i++)
            {
                cache.append("$").append(i).appendComma();
            }
            cache.deleteLast();
            cache.append("});\r\n");
        }
    }

    private void processAfterAdvice(ClassModel classModel, Class<?> type, AnnotationDatabase annotationDatabase, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String traceId = TRACEID.currentTraceId();
        String rule    = getRule(annotationDatabase, enhanceMethod, After.class);
        for (Method method : type.getMethods())
        {
            if (match(rule, method))
            {
                logger.debug("traceId:{} 后置通知规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModelKey key         = new MethodModelKey(method);
                MethodModel    methodModel = classModel.getMethodModel(key);
                String         originBody  = methodModel.getBody();
                String         pointName   = "point_" + fieldNameCounter.getAndIncrement();
                StringCache    cache       = new StringCache();
                cache.append("try{\r\n").append(originBody).append("}\r\n").append("finally\r\n{\r\n");
                generateProceedPointImpl(classModel, hostFieldName, method, pointName, cache, false);
                generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
                cache.append("}");
                methodModel.setBody(cache.toString());
            }
        }
    }

    private void generateEnhanceMethodInvoke(String fieldName, Method enhanceMethod, String pointName, StringCache cache)
    {
        cache.append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
    }

    private void processAfterReturningAdvice(ClassModel classModel, Class<?> type, AnnotationDatabase annotationDatabase, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String traceId = TRACEID.currentTraceId();
        String rule    = getRule(annotationDatabase, enhanceMethod, AfterReturning.class);
        for (Method method : type.getMethods())
        {
            if (method.getReturnType() != void.class && match(rule, method))
            {
                logger.debug("traceId:{} 返回通知规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModelKey key    = new MethodModelKey(method);
                MethodModel    origin = classModel.removeMethodModel(key);
                MethodModel    newOne = new MethodModel(method, classModel);
                origin.setAccessLevel(AccessLevel.PRIVATE);
                origin.setMethodName(origin.getMethodName() + "_" + methodNameCounter.getAndIncrement());
                classModel.putMethodModel(origin);
                StringCache cache = new StringCache();
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

    private void processAfterThrowableAdvice(ClassModel classModel, Class<?> type, AnnotationDatabase annotationDatabase, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String traceId = TRACEID.currentTraceId();
        String rule    = getRule(annotationDatabase, enhanceMethod, AfterThrowable.class);
        classModel.addImport(ReflectUtil.class);
        for (Method method : type.getMethods())
        {
            if (match(rule, method))
            {
                logger.debug("traceId:{} 规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModelKey key         = new MethodModelKey(method);
                MethodModel    methodModel = classModel.getMethodModel(key);
                String         body        = methodModel.getBody();
                StringCache    cache       = new StringCache();
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

    private void processAroundAdvice(ClassModel classModel, Class<?> type, AnnotationDatabase annotationDatabase, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String traceId = TRACEID.currentTraceId();
        String rule    = getRule(annotationDatabase, enhanceMethod, Around.class);
        for (Method method : type.getMethods())
        {
            if (match(rule, method))
            {
                logger.debug("traceId:{} 环绕通知规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModelKey key         = new MethodModelKey(method);
                MethodModel    methodModel = classModel.getMethodModel(key);
                boolean[]      flags       = new boolean[methodModel.getParamterTypes().length];
                Arrays.fill(flags, true);
                methodModel.setParamterFinals(flags);
                String      body      = methodModel.getBody();
                String      pointName = generatePointName();
                StringCache cache     = new StringCache();
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

    private PriorityQueue<BeanDefinition> findAspectClass(Class<?> type, EnvironmentTmp environment)
    {
        PriorityQueue<BeanDefinition> queue = new PriorityQueue<BeanDefinition>(10, new Comparator<BeanDefinition>()
        {

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
