package com.jfireframework.jfire.core.aop.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.PriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.FieldModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.baseutil.smc.model.MethodModel.AccessLevel;
import com.jfireframework.baseutil.smc.model.MethodModel.MethodModelKey;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.aop.AopManager;
import com.jfireframework.jfire.core.aop.AopManagerNotated;
import com.jfireframework.jfire.core.aop.notated.After;
import com.jfireframework.jfire.core.aop.notated.AfterReturning;
import com.jfireframework.jfire.core.aop.notated.AfterThrowable;
import com.jfireframework.jfire.core.aop.notated.Around;
import com.jfireframework.jfire.core.aop.notated.Before;
import com.jfireframework.jfire.core.aop.notated.EnhanceClass;
import com.jfireframework.jfire.exception.CannotFindEnhanceFieldException;
import com.jfireframework.jfire.util.Utils;

@AopManagerNotated
public class DefaultAopManager implements AopManager
{
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultAopManager.class);
	
	class EnhanceInfo
	{
		Class<?>			type;
		String[]			fieldNames;
		BeanDefinition[]	injects;
		Field[]				fields;
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
			FieldModel fieldModel = new FieldModel(fieldName, each.getType(), classModel);
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
				else if (Utils.ANNOTATION_UTIL.isPresent(AfterReturning.class, enhanceMethod))
				{
					processAfterReturningAdvice(classModel, type, environment, hostFieldName, fieldName, enhanceMethod);
				}
				else if (Utils.ANNOTATION_UTIL.isPresent(AfterThrowable.class, enhanceMethod))
				{
					processAfterThrowableAdvice(classModel, type, environment, hostFieldName, fieldName, enhanceMethod);
				}
				else if (enhanceMethod.getReturnType() != void.class && Utils.ANNOTATION_UTIL.isPresent(Around.class, enhanceMethod))
				{
					processAroundAdvice(classModel, type, environment, hostFieldName, fieldName, enhanceMethod);
				}
			}
		}
		EnhanceInfo enhanceInfo = new EnhanceInfo();
		enhanceInfo.fieldNames = fieldNames.toArray(new String[fieldNames.size()]);
		enhanceInfo.injects = injects.toArray(new BeanDefinition[injects.size()]);
		enhanceInfo.type = type;
		enhanceInfos.put(type, enhanceInfo);
	}
	
	private void processBeforeAdvice(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName, String fieldName, Method enhanceMethod)
	{
		String traceId = TRACEID.currentTraceId();
		String rule = Utils.ANNOTATION_UTIL.getAnnotation(Before.class, enhanceMethod).value();
		for (Method method : type.getMethods())
		{
			if (match(rule, method))
			{
				logger.debug("traceId:{} 规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
				MethodModelKey key = new MethodModelKey(method);
				MethodModel methodModel = classModel.getMethodModel(key);
				String originBody = methodModel.getBody();
				String pointName = "point_" + fieldNameCounter.getAndIncrement();
				StringCache cache = new StringCache();
				generateProceedPointImpl(environment, hostFieldName, method, pointName, cache);
				generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
				cache.append(originBody);
				methodModel.setBody(cache.toString());
			}
		}
	}
	
	private void generateProceedPointImpl(Environment environment, String hostFieldName, Method method, String pointName, StringCache cache)
	{
		int sequence = environment.registerMethod(method);
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
	}
	
	private void processAfterAdvice(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName, String fieldName, Method enhanceMethod)
	{
		String traceId = TRACEID.currentTraceId();
		String rule = Utils.ANNOTATION_UTIL.getAnnotation(After.class, enhanceMethod).value();
		for (Method method : type.getMethods())
		{
			if (match(rule, method))
			{
				logger.debug("traceId:{} 规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
				MethodModelKey key = new MethodModelKey(method);
				MethodModel methodModel = classModel.getMethodModel(key);
				String originBody = methodModel.getBody();
				String pointName = "point_" + fieldNameCounter.getAndIncrement();
				StringCache cache = new StringCache();
				cache.append("try{\r\n").append(originBody).append("}\r\n").append("finally\r\n{\r\n");
				generateProceedPointImpl(environment, hostFieldName, method, pointName, cache);
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
	
	private void processAfterReturningAdvice(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName, String fieldName, Method enhanceMethod)
	{
		String traceId = TRACEID.currentTraceId();
		String rule = Utils.ANNOTATION_UTIL.getAnnotation(AfterReturning.class, enhanceMethod).value();
		for (Method method : type.getMethods())
		{
			if (method.getReturnType() != void.class && match(rule, method))
			{
				logger.debug("traceId:{} 规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
				MethodModelKey key = new MethodModelKey(method);
				MethodModel origin = classModel.removeMethodModel(key);
				MethodModel newOne = new MethodModel(method, classModel);
				origin.setAccessLevel(AccessLevel.PRIVATE);
				origin.setMethodName(origin.getMethodName() + "_" + methodNameCounter.getAndIncrement());
				classModel.putMethodModel(origin);
				StringCache cache = new StringCache();
				cache.append(SmcHelper.getReferenceName(method.getReturnType(), classModel)).append(" result = ").append(origin.generateInvoke()).append(";\r\n");
				String pointName = generatePointName();
				generateProceedPointImpl(environment, hostFieldName, method, pointName, cache);
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
		String pointName = "point_" + fieldNameCounter.getAndIncrement();
		return pointName;
	}
	
	private void processAfterThrowableAdvice(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName, String fieldName, Method enhanceMethod)
	{
		String traceId = TRACEID.currentTraceId();
		String rule = Utils.ANNOTATION_UTIL.getAnnotation(AfterThrowable.class, enhanceMethod).value();
		for (Method method : type.getMethods())
		{
			if (match(rule, method))
			{
				logger.debug("traceId:{} 规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
				MethodModelKey key = new MethodModelKey(method);
				MethodModel methodModel = classModel.getMethodModel(key);
				String body = methodModel.getBody();
				StringCache cache = new StringCache();
				cache.append("try\r\n{\r\n").append(body).append("}\r\ncatch(java.lang.Throwable e)\r\n{");
				String pointName = generatePointName();
				generateProceedPointImpl(environment, hostFieldName, method, pointName, cache);
				cache.append(pointName).append(".setE(e);\r\n");
				generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
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
	
	private void processAroundAdvice(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName, String fieldName, Method enhanceMethod)
	{
		String traceId = TRACEID.currentTraceId();
		String rule = Utils.ANNOTATION_UTIL.getAnnotation(Around.class, enhanceMethod).value();
		for (Method method : type.getMethods())
		{
			if (match(rule, method))
			{
				logger.debug("traceId:{} 规则匹配成功，规则:{},方法:{},通知方法:{}", traceId, rule, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
				MethodModelKey key = new MethodModelKey(method);
				MethodModel methodModel = classModel.getMethodModel(key);
				boolean[] flags = new boolean[methodModel.getParamterTypes().length];
				Arrays.fill(flags, true);
				methodModel.setParamterFinals(flags);
				String body = methodModel.getBody();
				String pointName = generatePointName();
				int sequence = environment.registerMethod(method);
				StringCache cache = new StringCache();
				cache.append("ProceedPointImpl ").append(pointName).append(" = new ProceedPointImpl(){\r\n");
				cache.append("public Object invoke(){ ").append(body).append(" }\r\n};\r\n");
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
				Class<?> returnType = method.getReturnType();
				if (returnType == void.class)
				{
					generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
				}
				else if (returnType.isPrimitive())
				{
					if (returnType == int.class)
					{
						classModel.addImport(Integer.class);
						cache.append("return (Integer)").append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
					}
					else if (returnType == short.class)
					{
						classModel.addImport(Short.class);
						cache.append("return (Short)").append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
					}
					else if (returnType == long.class)
					{
						classModel.addImport(Long.class);
						cache.append("return (Long)").append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
					}
					else if (returnType == float.class)
					{
						classModel.addImport(Float.class);
						cache.append("return (Float)").append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
					}
					else if (returnType == double.class)
					{
						classModel.addImport(Double.class);
						cache.append("return (Double)").append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
					}
					else if (returnType == byte.class)
					{
						classModel.addImport(Byte.class);
						cache.append("return (Byte)").append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
					}
					else if (returnType == char.class)
					{
						classModel.addImport(Character.class);
						cache.append("return (Character)").append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
					}
					else if (returnType == boolean.class)
					{
						classModel.addImport(Boolean.class);
						cache.append("return (Boolean)").append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
					}
					else
					{
						throw new UnsupportedOperationException();
					}
				}
				else
				{
					classModel.addImport(returnType);
					cache.append("return (").append(returnType.getSimpleName()).append(")").append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
				}
				methodModel.setBody(cache.toString());
				classModel.putMethodModel(methodModel);
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
			if (method.getParameterTypes().length == 0)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		String[] split = paramRule.split(",");
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
