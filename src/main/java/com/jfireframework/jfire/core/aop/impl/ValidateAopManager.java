package com.jfireframework.jfire.core.aop.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import javax.validation.Constraint;
import javax.validation.Valid;
import javax.validation.executable.ValidateOnExecution;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.FieldModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.baseutil.smc.model.MethodModel.AccessLevel;
import com.jfireframework.baseutil.smc.model.MethodModel.MethodModelKey;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.aop.AopManager;
import com.jfireframework.jfire.core.aop.AopManagerNotated;
import com.jfireframework.jfire.util.Utils;

@AopManagerNotated
public class ValidateAopManager implements AopManager
{
	private BeanDefinition validatorBeandefinition;
	
	@Override
	public void scan(Environment environment)
	{
		AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
		for (BeanDefinition beanDefinition : environment.beanDefinitions().values())
		{
			for (Method method : beanDefinition.getType().getMethods())
			{
				if (annotationUtil.isPresent(ValidateOnExecution.class, method))
				{
					beanDefinition.addAopManager(this);
					break;
				}
			}
		}
		List<BeanDefinition> list = environment.getBeanDefinitionByAbstract(JfireMethodValidator.class);
		if (list.isEmpty()==false)
		{
			validatorBeandefinition = list.get(0);
		}
	}
	
	@Override
	public void enhance(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName)
	{
		classModel.addInterface(SetJfireMethodValidator.class);
		AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
		String validateFieldName = generateValidatorField(classModel);
		generateSetJfireMethodValidatorMethod(classModel, validateFieldName);
		for (Method method : type.getMethods())
		{
			if (Modifier.isFinal(method.getModifiers()))
			{
				continue;
			}
			if (annotationUtil.isPresent(ValidateOnExecution.class, method))
			{
				if (hasConstraintBeforeMethodExecute(method))
				{
					processValidateParamter(classModel, environment, hostFieldName, validateFieldName, method);
				}
				if (hasConstraintOnReturnValue(method))
				{
					processValidateReturnValue(classModel, environment, hostFieldName, validateFieldName, method);
				}
			}
		}
	}
	
	/**
	 * @param classModel
	 * @param environment
	 * @param hostFieldName
	 * @param validateFieldName
	 * @param method
	 */
	private void processValidateReturnValue(ClassModel classModel, Environment environment, String hostFieldName, String validateFieldName, Method method)
	{
		MethodModelKey key = new MethodModelKey(method);
		MethodModel origin = classModel.removeMethodModel(key);
		origin.setMethodName(origin.getMethodName() + "_" + methodNameCounter.getAndIncrement());
		classModel.putMethodModel(origin);
		StringCache cache = new StringCache();
		int sequence = environment.registerMethod(method);
		cache.append(method.getReturnType().getSimpleName()).append(" result = ").append(origin.generateInvoke()).append(";\r\n");
		cache.append(validateFieldName).append(".validateReturnValue(").append(hostFieldName).append(",")//
		        .append(Environment.ENVIRONMENT_FIELD_NAME).append(".getMethod(").append(sequence).append("),result);\r\n")//
		        .append("return result;\r\n");
		MethodModel methodModel = new MethodModel(method);
		methodModel.setBody(cache.toString());
		classModel.putMethodModel(methodModel);
	}
	
	/**
	 * @param classModel
	 * @param environment
	 * @param hostFieldName
	 * @param validateFieldName
	 * @param method
	 */
	private void processValidateParamter(ClassModel classModel, Environment environment, String hostFieldName, String validateFieldName, Method method)
	{
		StringCache cache = new StringCache();
		int sequence = environment.registerMethod(method);
		cache.append(validateFieldName).append(".validateParameters(").append(hostFieldName).append(",")//
		        .append(Environment.ENVIRONMENT_FIELD_NAME).append(".getMethod(").append(sequence).append("),")//
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
		MethodModelKey key = new MethodModelKey(method);
		MethodModel methodModel = classModel.getMethodModel(key);
		methodModel.setBody(cache.toString() + methodModel.getBody());
	}
	
	/**
	 * @param classModel
	 * @return
	 */
	private String generateValidatorField(ClassModel classModel)
	{
		String fieldName = "validator_" + fieldNameCounter.getAndIncrement();
		FieldModel fieldModel = new FieldModel(fieldName, JfireMethodValidator.class);
		classModel.addField(fieldModel);
		return fieldName;
	}
	
	/**
	 * @param classModel
	 * @param fieldName
	 */
	private void generateSetJfireMethodValidatorMethod(ClassModel classModel, String fieldName)
	{
		MethodModel methodModel = new MethodModel();
		methodModel.setAccessLevel(AccessLevel.PUBLIC);
		methodModel.setMethodName("setJfireMethodValidator");
		methodModel.setParamterTypes(JfireMethodValidator.class);
		methodModel.setReturnType(void.class);
		methodModel.setBody(fieldName + " = $0;");
		classModel.putMethodModel(methodModel);
	}
	
	private static boolean hasConstraintBeforeMethodExecute(Method method)
	{
		AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
		if (annotationUtil.isPresent(Constraint.class, method))
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
			}
			if (annotationUtil.isPresent(Constraint.class, parameterAnnotations))
			{
				return true;
			}
		}
		return false;
	}
	
	private static boolean hasConstraintOnReturnValue(Method method)
	{
		if (method.getReturnType() == void.class || method.getReturnType().isPrimitive() || method.isAnnotationPresent(Valid.class) == false)
		{
			return false;
		}
		return true;
	}
	
	@Override
	public void enhanceFinish(Class<?> type, Class<?> enhanceType, Environment environment)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void fillBean(Object bean, Class<?> type)
	{
		((SetJfireMethodValidator) bean).setJfireMethodValidator((JfireMethodValidator) validatorBeandefinition.getBeanInstance());
	}
	
	@Override
	public int order()
	{
		return VALIDATE;
	}
	
	public static interface JfireMethodValidator
	{
		<T> void validateParameters(T object, Method method, Object[] parameterValues, Class<?>... groups);
		
		<T> void validateReturnValue(T object, Method method, Object returnValue, Class<?>... groups);
	}
	
	public static interface SetJfireMethodValidator
	{
		void setJfireMethodValidator(JfireMethodValidator validator);
	}
}
