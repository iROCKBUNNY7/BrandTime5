package com.jfireframework.jfire.core.aop.impl;

import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.smc.SmcHelper;
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
import com.jfireframework.jfire.core.aop.impl.transaction.TransactionManager;
import com.jfireframework.jfire.core.aop.impl.transaction.TransactionState;
import com.jfireframework.jfire.core.aop.notated.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class TransactionAopManager implements EnhanceManager
{
    private BeanDefinition transactionBeandefinition;

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
                if (annotationContext.isAnnotationPresent(Transactional.class))
                {
                    beanDefinition.addAopManager(this);
                    break;
                }
            }
        }
        transactionBeandefinition = applicationContext.getBeanDefinition(TransactionManager.class);
    }

    @Override
    public EnhanceCallbackForBeanInstance enhance(ClassModel classModel, Class<?> type, JfireContext applicationContext, String hostFieldName)
    {
        if (transactionBeandefinition == null)
        {
            return null;
        }
        classModel.addImport(ReflectUtil.class);
        String transFieldName = generateTransactionManagerField(classModel);
        generateSetTransactionManagerMethod(classModel, transFieldName);
        AnnotationContextFactory annotationContextFactory = applicationContext.getAnnotationContextFactory();
        ClassLoader              classLoader              = Thread.currentThread().getContextClassLoader();
        for (Method method : type.getMethods())
        {
            AnnotationContext annotationContext = annotationContextFactory.get(method, classLoader);
            if (Modifier.isFinal(method.getModifiers()))
            {
                continue;
            }
            if (annotationContext.isAnnotationPresent(Transactional.class) == false)
            {
                continue;
            }
            MethodModelKey key    = new MethodModelKey(method);
            MethodModel    origin = classModel.removeMethodModel(key);
            origin.setAccessLevel(AccessLevel.PRIVATE);
            origin.setMethodName(origin.getMethodName() + "_" + methodNameCounter.getAndIncrement());
            classModel.putMethodModel(origin);
            MethodModel   newOne               = new MethodModel(method, classModel);
            StringCache   cache                = new StringCache();
            Transactional transactional        = annotationContext.getAnnotation(Transactional.class);
            int           propagation          = transactional.propagation();
            String        transactionStateName = "transactionState_" + varNameCounter.getAndIncrement();
            cache.append(SmcHelper.getReferenceName(TransactionState.class, classModel)).append(" ").append(transactionStateName)//
                    .append(" = ").append(transFieldName).append(".beginTransAction(").append(propagation).append(");\r\n");
            cache.append("try\r\n{\r\n");
            if (method.getReturnType() != void.class)
            {
                cache.append(SmcHelper.getReferenceName(method.getReturnType(), classModel)).append(" result = ").append(origin.generateInvoke()).append(";\r\n");
                cache.append(transFieldName).append(".commit();\r\n");
                cache.append("return result;\r\n");
            }
            else
            {
                cache.append(origin.generateInvoke()).append(";\r\n");
                cache.append(transFieldName).append(".commit(").append(transactionStateName).append(");\r\n");
            }
            cache.append("}\r\n");
            cache.append("catch(java.lang.Throwable e)\r\n{\r\n");
            cache.append(transFieldName).append(".rollback(").append(transactionStateName).append(",e);\r\n");
            cache.append("ReflectUtil.throwException(e);\r\n");
            if (method.getReturnType() != void.class)
            {
                if (method.getReturnType().isPrimitive())
                {
                    Class<?> returnType = method.getReturnType();
                    if (returnType == boolean.class)
                    {
                        cache.append("return false;\r\n");
                    }
                    else
                    {
                        cache.append("return 0;\r\n");
                    }
                }
                else
                {
                    cache.append("return null;\r\n");
                }
            }
            cache.append("}\r\n");
            newOne.setBody(cache.toString());
            if (method.getGenericParameterTypes().length != 0)
            {
                boolean[] flags = new boolean[method.getParameterTypes().length];
                Arrays.fill(flags, true);
                newOne.setParamterFinals(flags);
            }
            classModel.putMethodModel(newOne);
        }
        return new EnhanceCallbackForBeanInstance()
        {
            @Override
            public void run(Object beanInstance)
            {
                ((SetTransactionManager) beanInstance).setTransactionManager((TransactionManager) transactionBeandefinition.getBean());
            }
        };
    }

    private String generateTransactionManagerField(ClassModel classModel)
    {
        String     transFieldName = "transactionManager_" + fieldNameCounter.getAndIncrement();
        FieldModel fieldModel     = new FieldModel(transFieldName, TransactionManager.class, classModel);
        classModel.addField(fieldModel);
        return transFieldName;
    }

    private void generateSetTransactionManagerMethod(ClassModel classModel, String transFieldName)
    {
        classModel.addInterface(SetTransactionManager.class);
        MethodModel methodModel = new MethodModel(classModel);
        methodModel.setAccessLevel(AccessLevel.PUBLIC);
        methodModel.setMethodName("setTransactionManager");
        methodModel.setParamterTypes(TransactionManager.class);
        methodModel.setReturnType(void.class);
        methodModel.setBody(transFieldName + " = $0;");
        classModel.putMethodModel(methodModel);
    }

    @Override
    public int order()
    {
        return TRANSACTION;
    }

    public interface SetTransactionManager
    {
        void setTransactionManager(TransactionManager transactionManager);
    }
}
