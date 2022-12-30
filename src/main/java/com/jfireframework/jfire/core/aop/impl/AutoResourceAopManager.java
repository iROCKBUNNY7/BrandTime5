package com.jfireframework.jfire.core.aop.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
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
import com.jfireframework.jfire.core.aop.notated.AutoResourceable;
import com.jfireframework.jfire.util.Utils;

@AopManagerNotated
public class AutoResourceAopManager implements AopManager
{
	private BeanDefinition autoResourceBeanDefinition;
	
	@Override
	public void scan(Environment environment)
	{
		AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
		for (BeanDefinition beanDefinition : environment.beanDefinitions().values())
		{
			for (Method method : beanDefinition.getType().getMethods())
			{
				if (annotationUtil.isPresent(AutoResourceable.class, method))
				{
					beanDefinition.addAopManager(this);
					break;
				}
			}
		}
		List<BeanDefinition> list = environment.getBeanDefinitionByAbstract(AutoResource.class);
		if (list.isEmpty() == false)
		{
			autoResourceBeanDefinition = list.get(0);
		}
	}
	
	@Override
	public void enhance(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName)
	{
		AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
		String autoResourceFieldName = generateAutoResourceField(classModel);
		generateSetAutoResourceMethod(classModel, autoResourceFieldName);
		for (Method method : type.getMethods())
		{
			if (Modifier.isFinal(method.getModifiers()))
			{
				continue;
			}
			if (annotationUtil.isPresent(AutoResourceable.class, method))
			{
				MethodModelKey key = new MethodModelKey(method);
				MethodModel methodModel = classModel.getMethodModel(key);
				StringCache cache = new StringCache();
				cache.append(autoResourceFieldName).append(".open();\r\n");
				cache.append("try\r\n{\r\n");
				cache.append(methodModel.getBody());
				cache.append("}\r\nfinally\r\n{\r\n").append(autoResourceFieldName).append(".close();\r\n}");
				methodModel.setBody(cache.toString());
				classModel.putMethodModel(methodModel);
			}
		}
	}
	
	private void generateSetAutoResourceMethod(ClassModel classModel, String autoResourceFieldName)
	{
		MethodModel methodModel = new MethodModel();
		methodModel.setAccessLevel(AccessLevel.PUBLIC);
		methodModel.setMethodName("setAutoResource");
		methodModel.setParamterTypes(AutoResource.class);
		methodModel.setReturnType(void.class);
		methodModel.setBody(autoResourceFieldName + " = $0;\r\n");
		classModel.putMethodModel(methodModel);
		classModel.addInterface(SetAutoResource.class);
	}
	
	private String generateAutoResourceField(ClassModel classModel)
	{
		String autoResourceFieldName = "autoResource_" + fieldNameCounter.getAndIncrement();
		FieldModel fieldModel = new FieldModel(autoResourceFieldName, AutoResource.class);
		classModel.addField(fieldModel);
		return autoResourceFieldName;
	}
	
	@Override
	public void enhanceFinish(Class<?> type, Class<?> enhanceType, Environment environment)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void fillBean(Object bean, Class<?> type)
	{
		((SetAutoResource) bean).setAutoResource((AutoResource) autoResourceBeanDefinition.getBeanInstance());
	}
	
	@Override
	public int order()
	{
		return AUTORESOURCE;
	}
	
	public static interface AutoResource
	{
		void open();
		
		void close();
	}
	
	public static interface SetAutoResource
	{
		void setAutoResource(AutoResource autoResource);
	}
	
}
