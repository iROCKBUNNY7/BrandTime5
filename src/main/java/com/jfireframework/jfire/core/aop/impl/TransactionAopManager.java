package com.jfireframework.jfire.core.aop.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
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
import com.jfireframework.jfire.core.aop.notated.Transaction;
import com.jfireframework.jfire.exception.TransactionException;
import com.jfireframework.jfire.util.Utils;

@AopManagerNotated()
public class TransactionAopManager implements AopManager
{
	private BeanDefinition transactionBeandefinition;
	
	@Override
	public void scan(Environment environment)
	{
		for (BeanDefinition beanDefinition : environment.beanDefinitions().values())
		{
			for (Method method : beanDefinition.getType().getMethods())
			{
				if (Utils.ANNOTATION_UTIL.isPresent(Transaction.class, method))
				{
					beanDefinition.addAopManager(this);
					break;
				}
			}
		}
		List<BeanDefinition> list = environment.getBeanDefinitionByAbstract(TransactionManager.class);
		if (list.isEmpty() == false)
		{
			transactionBeandefinition = list.get(0);
		}
	}
	
	@Override
	public void enhance(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName)
	{
		if (transactionBeandefinition == null)
		{
			return;
		}
		classModel.addImport(TransactionException.class);
		String transFieldName = generateTransactionManagerField(classModel);
		generateSetTransactionManagerMethod(classModel, transFieldName);
		for (Method method : type.getMethods())
		{
			if (Modifier.isFinal(method.getModifiers()))
			{
				continue;
			}
			if (Utils.ANNOTATION_UTIL.isPresent(Transaction.class, method) == false)
			{
				continue;
			}
			MethodModelKey key = new MethodModelKey(method);
			MethodModel origin = classModel.removeMethodModel(key);
			origin.setAccessLevel(AccessLevel.PRIVATE);
			origin.setMethodName(origin.getMethodName() + "_" + methodNameCounter.getAndIncrement());
			classModel.putMethodModel(origin);
			MethodModel newOne = new MethodModel(method);
			StringCache cache = new StringCache();
			cache.append("try\r\n{\r\n");
			cache.append(transFieldName).append(".beginTransAction();\r\n");
			if (method.getReturnType() != void.class)
			{
				cache.append(SmcHelper.getTypeName(method.getReturnType())).append(" result = ").append(origin.generateInvoke()).append(";\r\n");
				cache.append(transFieldName).append(".commit();\r\n");
				cache.append("return result;\r\n");
			}
			else
			{
				cache.append(origin.generateInvoke()).append(";\r\n");
				cache.append(transFieldName).append(".commit();\r\n");
			}
			cache.append("}\r\n");
			cache.append("catch(java.lang.Throwable e)\r\n{\r\n");
			cache.append(transFieldName).append(".rollback(e);\r\n");
			cache.append("throw new TransactionException(e);\r\n");
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
	}
	
	private String generateTransactionManagerField(ClassModel classModel)
	{
		String transFieldName = "transactionManager_" + fieldNameCounter.getAndIncrement();
		FieldModel fieldModel = new FieldModel(transFieldName, TransactionManager.class);
		classModel.addField(fieldModel);
		return transFieldName;
	}
	
	private void generateSetTransactionManagerMethod(ClassModel classModel, String transFieldName)
	{
		classModel.addInterface(SetTransactionManager.class);
		MethodModel methodModel = new MethodModel();
		methodModel.setAccessLevel(AccessLevel.PUBLIC);
		methodModel.setMethodName("setTransactionManager");
		methodModel.setParamterTypes(TransactionManager.class);
		methodModel.setReturnType(void.class);
		methodModel.setBody(transFieldName + " = $0;");
		classModel.putMethodModel(methodModel);
	}
	
	@Override
	public void enhanceFinish(Class<?> type, Class<?> enhanceType, Environment environment)
	{
		
	}
	
	@Override
	public void fillBean(Object bean, Class<?> type)
	{
		((SetTransactionManager) bean).setTransactionManager((TransactionManager) transactionBeandefinition.getBeanInstance());
	}
	
	@Override
	public int order()
	{
		return TRANSACTION;
	}
	
	public static interface TransactionManager
	{
		
		/**
		 * 开启事务.
		 */
		void beginTransAction();
		
		/**
		 * 提交事务,但是并不关闭连接
		 */
		void commit();
		
		/**
		 * 事务回滚,但是并不关闭连接
		 */
		void rollback(Throwable e);
		
	}
	
	public static interface SetTransactionManager
	{
		void setTransactionManager(TransactionManager transactionManager);
	}
}
