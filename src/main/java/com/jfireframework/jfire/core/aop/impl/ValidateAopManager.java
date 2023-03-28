package com.jfireframework.jfire.core.aop.impl;

import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.FieldModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.baseutil.smc.model.MethodModel.AccessLevel;
import com.jfireframework.baseutil.smc.model.MethodModel.MethodModelKey;
import com.jfireframework.jfire.core.ApplicationContext;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.JfireContext;
import com.jfireframework.jfire.core.aop.EnhanceCallbackForBeanInstance;
import com.jfireframework.jfire.core.aop.EnhanceManager;

import javax.validation.Constraint;
import javax.validation.Valid;
import javax.validation.executable.ValidateOnExecution;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ValidateAopManager implements EnhanceManager
{
    private BeanDefinition validatorBeandefinition;

    @Override
    public void scan(JfireContext applicationContext)
    {
        AnnotationContextFactory annotationContextFactory = applicationContext.getAnnotationContextFactory();
        ClassLoader              classLoader              = Thread.currentThread().getContextClassLoader();
        for (BeanDefinition beanDefinition : applicationContext.getAllBeanDefinitions())
        {
            for (Method method : beanDefinition.getType().getMethods())
            {
                AnnotationContext annotationContext = annotationContextFactory.get(method, classLoader);
                if (annotationContext.isAnnotationPresent(ValidateOnExecution.class))
                {
                    beanDefinition.addAopManager(this);
                    break;
                }
            }
        }
        validatorBeandefinition = applicationContext.getBeanDefinition(JfireMethodValidator.class);
    }

    @Override
    public EnhanceCallbackForBeanInstance enhance(ClassModel classModel, Class<?> type, JfireContext applicationContext, String hostFieldName)
    {
        classModel.addInterface(SetJfireMethodValidator.class);
        String validateFieldName = generateValidatorField(classModel);
        String methodMapField    = generateMethodMapField(classModel);
        generateSetJfireMethodValidatorMethod(classModel, validateFieldName, methodMapField);
        AnnotationContextFactory  annotationContextFactory = applicationContext.getAnnotationContextFactory();
        ClassLoader               classLoader              = Thread.currentThread().getContextClassLoader();
        final Map<String, Method> methodMap                = new HashMap<String, Method>();
        for (Method method : type.getMethods())
        {
            if (Modifier.isFinal(method.getModifiers()))
            {
                continue;
            }
            AnnotationContext annotationContext = annotationContextFactory.get(method, classLoader);
            if (annotationContext.isAnnotationPresent(ValidateOnExecution.class))
            {
                if (hasConstraintBeforeMethodExecute(method, annotationContext))
                {
                    String methodName = processValidateParamter(classModel, applicationContext, hostFieldName, validateFieldName, method, methodMapField);
                    methodMap.put(methodName, method);
                }
                if (hasConstraintOnReturnValue(method))
                {
                    String methodName = processValidateReturnValue(classModel, applicationContext, hostFieldName, validateFieldName, method, methodMapField);
                    methodMap.put(methodName, method);
                }
            }
        }
        return new EnhanceCallbackForBeanInstance()
        {
            @Override
            public void run(Object beanInstance)
            {
                JfireMethodValidator jfireMethodValidator = (JfireMethodValidator) validatorBeandefinition.getBean();
                System.out.println(jfireMethodValidator.getClass());
                for (Map.Entry<String, Method> each : methodMap.entrySet())
                {
                    System.out.println(each.getKey() + ":" + each.getValue());
                }
                ((SetJfireMethodValidator) beanInstance).setJfireMethodValidator(jfireMethodValidator, methodMap);
            }
        };
    }

    private String generateMethodMapField(ClassModel classModel)
    {
        String     fieldName  = "validator_" + fieldNameCounter.getAndIncrement();
        FieldModel fieldModel = new FieldModel(fieldName, Map.class, classModel);
        classModel.addField(fieldModel);
        return fieldName;
    }

    /**
     * @param classModel
     * @param hostFieldName
     * @param validateFieldName
     * @param method
     * @return
     */
    private String processValidateReturnValue(ClassModel classModel, ApplicationContext applicationContext, String hostFieldName, String validateFieldName, Method method, String methodMapField)
    {
        MethodModelKey key    = new MethodModelKey(method);
        MethodModel    origin = classModel.removeMethodModel(key);
        origin.setMethodName(origin.getMethodName() + "_" + methodNameCounter.getAndIncrement());
        classModel.putMethodModel(origin);
        StringCache cache      = new StringCache();
        String      mehtodName = "validateMethod_$" + fieldNameCounter.getAndIncrement();
        cache.append(method.getReturnType().getSimpleName()).append(" result = ").append(origin.generateInvoke()).append(";\r\n");
        cache.append(validateFieldName).append(".validateReturnValue(").append(hostFieldName).append(",")//
                .append(methodMapField).append(".get(\"" + mehtodName + "\"),result);\r\n")//
                .append("return result;\r\n");
        MethodModel methodModel = new MethodModel(method, classModel);
        methodModel.setBody(cache.toString());
        classModel.putMethodModel(methodModel);
        return mehtodName;
    }

    /**
     * @param classModel
     * @param hostFieldName
     * @param validateFieldName
     * @param method
     */
    private String processValidateParamter(ClassModel classModel, ApplicationContext applicationContext, String hostFieldName, String validateFieldName, Method method, String methodMapField)
    {
        StringCache cache      = new StringCache();
        String      methodName = "validateMethod_$" + fieldNameCounter.getAndIncrement();
        cache.append(validateFieldName).append(".validateParameters(").append(hostFieldName).append(",(java.lang.reflect.Method)")//
                .append(methodMapField).append(".get(\"" + methodName + "\"),")//
                .append("new Object[]{");
        int length = method.getParameterTypes().length;
        for (int i = 0; i < length; i++)
        {
            cache.append("$").append(i).appendComma();
        }
        if (cache.isCommaLast())
        {
            cache.deleteLast();
        }
        cache.append("});\r\n");
        MethodModelKey key         = new MethodModelKey(method);
        MethodModel    methodModel = classModel.getMethodModel(key);
        methodModel.setBody(cache.toString() + methodModel.getBody());
        return methodName;
    }

    /**
     * @param classModel
     * @return
     */
    private String generateValidatorField(ClassModel classModel)
    {
        String     fieldName  = "validator_" + fieldNameCounter.getAndIncrement();
        FieldModel fieldModel = new FieldModel(fieldName, JfireMethodValidator.class, classModel);
        classModel.addField(fieldModel);
        return fieldName;
    }

    /**
     * @param classModel
     * @param validateFieldName
     */
    private void generateSetJfireMethodValidatorMethod(ClassModel classModel, String validateFieldName, String methodMapField)
    {
        MethodModel methodModel = new MethodModel(classModel);
        methodModel.setAccessLevel(AccessLevel.PUBLIC);
        methodModel.setMethodName("setJfireMethodValidator");
        methodModel.setParamterTypes(JfireMethodValidator.class, Map.class);
        methodModel.setReturnType(void.class);
        methodModel.setBody(validateFieldName + " = $0;\r\n" + methodMapField + " = $1;\r\n");
        classModel.putMethodModel(methodModel);
    }

    private static boolean hasConstraintBeforeMethodExecute(Method method, AnnotationContext annotationContext)
    {
        if (annotationContext.isAnnotationPresent(Constraint.class))
        {
            return true;
        }
        for (Annotation[] parameterAnnotations : method.getParameterAnnotations())
        {
            for (Annotation annotation : parameterAnnotations)
            {
                if (annotation.annotationType() == Valid.class)
                {
                    return true;
                }
                if (annotation.annotationType().isAnnotationPresent(Constraint.class))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasConstraintOnReturnValue(Method method)
    {
        return method.getReturnType() != void.class && !method.getReturnType().isPrimitive() && method.isAnnotationPresent(Valid.class) != false;
    }

    @Override
    public int order()
    {
        return VALIDATE;
    }

    public interface JfireMethodValidator
    {
        <T> void validateParameters(T object, Method method, Object[] parameterValues, Class<?>... groups);

        <T> void validateReturnValue(T object, Method method, Object returnValue, Class<?>... groups);
    }

    public interface SetJfireMethodValidator
    {
        void setJfireMethodValidator(JfireMethodValidator validator, Map<String, Method> methodMap);
    }
}
