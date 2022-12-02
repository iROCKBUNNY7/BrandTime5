package com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import javax.validation.Constraint;
import javax.validation.Valid;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.el.SmcEl;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.jfire.Utils;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.Transaction;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.Cache;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.annotation.CacheDelete;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.annotation.CacheGet;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.annotation.CachePut;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.tx.TransactionIsolate;

public class DynamicCodeTool
{
	
	private static final AtomicInteger count = new AtomicInteger(0);
	
	public static ClassModel createClientClass(Class<?> type)
	{
		return SmcHelper.createClientClass(type);
	}
	
	public static void enhanceBefore(ClassModel model, Method method, EnhanceAnnoInfo enhanceAnnoInfo)
	{
		MethodModel pred = model.getMethodModel(method);
		String rename = method.getName() + "_" + count.incrementAndGet();
		pred.setMethodName(rename);
		model.putShadowMethodModel(pred);
		MethodModel insert = new MethodModel(method);
		String body = ProceedPointImpl.class.getName() + " point = new " + ProceedPointImpl.class.getName() + "();\r\n";
		if (insert.getSumOfarg() == 0)
		{
			body += "point.setParams();\r\n";
		}
		else
		{
			body += "point.setParams(";
			StringCache cache = new StringCache();
			for (int i = 0; i < insert.getSumOfarg(); i++)
			{
				cache.append("$").append(i).appendComma();
			}
			cache.deleteLast();
			body += cache.toString() + ");\r\n";
		}
		body += enhanceAnnoInfo.getEnhanceFieldName() + "." + enhanceAnnoInfo.getEnhanceMethodName() + "(point);\r\n";
		if (method.getReturnType() == void.class)
		{
			body += pred.getInvokeInfo() + ";\r\n";
		}
		else
		{
			body += "return " + pred.getInvokeInfo() + ";\r\n";
		}
		insert.setBody(body);
		model.putMethod(method, insert);
	}
	
	public static void enhanceAfter(ClassModel model, Method method, EnhanceAnnoInfo enhanceAnnoInfo)
	{
		MethodModel pred = model.getMethodModel(method);
		String rename = method.getName() + "_" + count.incrementAndGet();
		pred.setMethodName(rename);
		model.putShadowMethodModel(pred);
		MethodModel insert = new MethodModel(method);
		String body = ProceedPointImpl.class.getName() + " point = new " + ProceedPointImpl.class.getName() + "();\r\n";
		if (insert.getSumOfarg() == 0)
		{
			body += "point.setParams();\r\n";
		}
		else
		{
			body += "point.setParams(";
			StringCache cache = new StringCache();
			for (int i = 0; i < insert.getSumOfarg(); i++)
			{
				cache.append("$").append(i).appendComma();
			}
			cache.deleteLast();
			body += cache.toString() + ");\r\n";
		}
		if (method.getReturnType() == void.class)
		{
			body += pred.getInvokeInfo() + ";\r\n";
		}
		else
		{
			body += insert.getReturnInfo() + " result =" + pred.getInvokeInfo() + ";\r\n";
			body += "point.setResult(result);\r\n";
		}
		if (method.getReturnType() == void.class)
		{
			body += enhanceAnnoInfo.getEnhanceFieldName() + "." + enhanceAnnoInfo.getEnhanceMethodName() + "(point);\r\n";
		}
		else
		{
			body += enhanceAnnoInfo.getEnhanceFieldName() + "." + enhanceAnnoInfo.getEnhanceMethodName() + "(point);\r\n"//
			        + "return result;\r\n";
		}
		insert.setBody(body);
		model.putMethod(method, insert);
	}
	
	public static void enhanceException(ClassModel model, Method method, EnhanceAnnoInfo enhanceAnnoInfo)
	{
		MethodModel pred = model.getMethodModel(method);
		String rename = method.getName() + "_" + count.incrementAndGet();
		pred.setMethodName(rename);
		model.putShadowMethodModel(pred);
		MethodModel insert = new MethodModel(method);
		String body = ProceedPointImpl.class.getName() + " point = new " + ProceedPointImpl.class.getName() + "();\r\n";
		if (insert.getSumOfarg() == 0)
		{
			body += "point.setParams();\r\n";
		}
		else
		{
			body += "point.setParams(";
			StringCache cache = new StringCache();
			for (int i = 0; i < insert.getSumOfarg(); i++)
			{
				cache.append("$").append(i).appendComma();
			}
			cache.deleteLast();
			body += cache.toString() + ");\r\n";
		}
		body += "try{\r\n";
		if (method.getReturnType() == void.class)
		{
			body += pred.getInvokeInfo() + ";\r\n";
		}
		else
		{
			body += "return " + pred.getInvokeInfo() + ";\r\n";
		}
		body += "}\r\n";
		int index = 0;
		for (Class<?> each : enhanceAnnoInfo.getThrowtype())
		{
			body += "catch(" + each.getName() + " e" + index + "){"//
			        + "point.setE(e" + index + ");\r\n"//
			        + enhanceAnnoInfo.getEnhanceFieldName() + "." + enhanceAnnoInfo.getEnhanceMethodName() + "(point);\r\n"//
			        + "throw new java.lang.RuntimeException( e" + index + ");}\r\n";
			index += 1;
		}
		insert.setBody(body);
		model.putMethod(method, insert);
	}
	
	public static void enhanceAround(ClassModel model, Method method, EnhanceAnnoInfo enhanceAnnoInfo)
	{
		MethodModel pred = model.getMethodModel(method);
		String rename = method.getName() + "_" + count.incrementAndGet();
		pred.setMethodName(rename);
		model.putShadowMethodModel(pred);
		MethodModel insert = new MethodModel(method);
		modifyParamsToFinal(method, insert);
		String body = ProceedPointImpl.class.getName() + " point = new " + ProceedPointImpl.class.getName() + "(){\r\n"//
		        + " public Object invoke()  {\r\n";
		if (method.getReturnType() == void.class)
		{
			body += pred.getInvokeInfo() + ";\r\n" + "return null;\r\n";
		}
		else
		{
			body += "return " + pred.getInvokeInfo() + ";\r\n";
		}
		body += "}};\r\n";
		if (insert.getSumOfarg() == 0)
		{
			body += "point.setParams();\r\n";
		}
		else
		{
			body += "point.setParams(";
			StringCache cache = new StringCache();
			for (int i = 0; i < insert.getSumOfarg(); i++)
			{
				cache.append("$").append(i).appendComma();
			}
			cache.deleteLast();
			body += cache.toString() + ");\r\n";
		}
		if (method.getReturnType() == void.class)
		{
			body += enhanceAnnoInfo.getEnhanceFieldName() + "." + enhanceAnnoInfo.getEnhanceMethodName() + "(point);\r\n";
		}
		else
		{
			body += "return " + enhanceAnnoInfo.getEnhanceFieldName() + "." + enhanceAnnoInfo.getEnhanceMethodName() + "(point);\r\n";
		}
		insert.setBody(body);
		model.putMethod(method, insert);
	}
	
	private static void modifyParamsToFinal(Method method, MethodModel insert)
	{
		StringCache cache = new StringCache();
		int index = 0;
		for (Class<?> each : method.getParameterTypes())
		{
			cache.append("final ").append(SmcHelper.getTypeName(each)).append(" $").append(index).append(",");
			index += 1;
		}
		if (cache.isCommaLast())
		{
			cache.deleteLast();
		}
		insert.resetArgsInfo(cache.toString());
	}
	
	public static void addValidateToMethod(final ClassModel compilerModel, final String validateFieldName, final String extraInfoStoreFieldName, final Method method, final int methodSequence)
	{
		String invokeStr = renameOriginMethod(compilerModel, method);
		MethodModel validatedMethod = new MethodModel(method);
		String body = buildBody(invokeStr, validateFieldName, extraInfoStoreFieldName, methodSequence, method);
		validatedMethod.setBody(body);
		compilerModel.putMethod(method, validatedMethod);
		
	}
	
	private static String renameOriginMethod(ClassModel compilerModel, Method method)
	{
		MethodModel pred = compilerModel.getMethodModel(method);
		String rename = method.getName() + "_" + count.incrementAndGet();
		pred.setMethodName(rename);
		compilerModel.putShadowMethodModel(pred);
		return pred.getInvokeInfo();
	}
	
	private static String buildBody(String invokeStr, String validateFieldName, String extraInfoStoreFieldName, Integer methodSequence, Method method)
	{
		StringCache cache = new StringCache();
		if (hasConstraintBeforeMethodExecute(method))
		{
			int length = method.getParameterTypes().length;
			cache.append(validateFieldName).append(".validateParameters(").append("this,").append(extraInfoStoreFieldName).append(".getMethod(" + methodSequence + "),new Object[]{");
			for (int i = 0; i < length; i++)
			{
				cache.append("$").append(i).appendComma();
			}
			cache.deleteLast().append("});\r\n");
		}
		if (hasConstraintOnReturnValue(method))
		{
			String returnValueField = "returnValue_" + count.incrementAndGet();
			cache.append("Object ").append(returnValueField).append(" = ").append(invokeStr).append(";\r\n");
			cache.append(validateFieldName).append(".validateReturnValue(").append("this,").append(extraInfoStoreFieldName).append(".getMethod(" + methodSequence + "),").append(returnValueField).append(");\r\n");
			cache.append("return ").append(returnValueField).append(";\r\n");
		}
		else
		{
			if (method.getReturnType() != void.class)
			{
				cache.append("return ");
			}
			cache.append(invokeStr).append(";\r\n");
		}
		return cache.toString();
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
		if (method.getReturnType() == void.class || method.isAnnotationPresent(Valid.class) == false)
		{
			return false;
		}
		return true;
	}
	
	public static void addTxToMethod(ClassModel compilerModel, String txFieldName, Method method)
	{
		AnnotationUtil annotationUtil = new AnnotationUtil();
		MethodModel methodModel = compilerModel.getMethodModel(method);
		String rename = method.getName() + "_" + count.incrementAndGet();
		methodModel.setMethodName(rename);
		compilerModel.putShadowMethodModel(methodModel);
		MethodModel insert = new MethodModel(method);
		int isolateLevel = 0;
		Transaction transaction = annotationUtil.getAnnotation(Transaction.class, method);
		TransactionIsolate isolate = transaction.isolate();
		switch (isolate)
		{
			case USE_DB_SETING:
				isolateLevel = -1;
				break;
			case READ_COMMITTED:
				isolateLevel = 2;
				break;
			case REPEATABLE_READ:
				isolateLevel = 4;
				break;
			case SERIALIZABLE:
				isolateLevel = 8;
				break;
		}
		if (method.getReturnType() == void.class)
		{
			String body = txFieldName + ".buildCurrentSession();\r\n" + //
			        txFieldName + ".beginTransAction(" + isolateLevel + ");\r\n"//
			        + "try{\r\n"//
			        + methodModel.getInvokeInfo() + ";\r\n"//
			        + txFieldName + ".commit();"//
			        + "}\r\n"//
			        + "catch(java.lang.Throwable e){\r\n"//
			        + txFieldName + ".rollback(e);\r\n"//
			        + "throw new java.lang.RuntimeException(e);\r\n" + "}\r\n"//
			        + "finally{\r\n"//
			        + txFieldName + ".closeCurrentSession();\r\n"//
			        + "}\r\n";
			insert.setBody(body);
		}
		else
		{
			String body = txFieldName + ".buildCurrentSession();\r\n" + //
			        txFieldName + ".beginTransAction(" + isolateLevel + ");\r\n"//
			        + "try{\r\n"//
			        + insert.getReturnInfo() + " result = " + methodModel.getInvokeInfo() + ";\r\n"//
			        + txFieldName + ".commit();"//
			        + "return result;\r\n"//
			        + "}\r\n"//
			        + "catch(java.lang.Throwable e){\r\n"//
			        + txFieldName + ".rollback(e);\r\n"//
			        + "throw new java.lang.RuntimeException(e);\r\n" + "}\r\n"//
			        + "finally{\r\n"//
			        + txFieldName + ".closeCurrentSession();\r\n"//
			        + "}\r\n";
			insert.setBody(body);
		}
		compilerModel.putMethod(method, insert);
	}
	
	public static void addAutoResourceToMethod(ClassModel compilerModel, String fieldName, Method method)
	{
		MethodModel methodModel = compilerModel.getMethodModel(method);
		String rename = method.getName() + "_" + count.incrementAndGet();
		methodModel.setMethodName(rename);
		compilerModel.putShadowMethodModel(methodModel);
		MethodModel insert = new MethodModel(method);
		if (method.getReturnType() == void.class)
		{
			
			String body = fieldName + ".open();\r\n" //
			        + "try{\r\n"//
			        + methodModel.getInvokeInfo() + ";\r\n"//
			        + "}\r\n"//
			        + "catch(java.lang.Throwable e){\r\n"//
			        + "throw new java.lang.RuntimeException(e);\r\n" + "}\r\n"//
			        + "finally{\r\n"//
			        + fieldName + ".close();\r\n"//
			        + "}\r\n";
			insert.setBody(body);
		}
		else
		{
			String body = fieldName + ".open();\r\n" //
			        + "try{\r\n"//
			        + insert.getReturnInfo() + " result = " + methodModel.getInvokeInfo() + ";\r\n"//
			        + "return result;\r\n"//
			        + "}\r\n"//
			        + "catch(java.lang.Throwable e){\r\n"//
			        + "throw new java.lang.RuntimeException(e);\r\n" + "}\r\n"//
			        + "finally{\r\n"//
			        + fieldName + ".close();\r\n"//
			        + "}\r\n";
			insert.setBody(body);
		}
		compilerModel.putMethod(method, insert);
	}
	
	public static void addCacheGetToMethod(ClassModel compilerModel, String fieldName, Method method)
	{
		AnnotationUtil annotationUtil = new AnnotationUtil();
		MethodModel methodModel = compilerModel.getMethodModel(method);
		String rename = method.getName() + "_" + count.incrementAndGet();
		methodModel.setMethodName(rename);
		compilerModel.putShadowMethodModel(methodModel);
		MethodModel insert = new MethodModel(method);
		CacheGet cacheGet = annotationUtil.getAnnotation(CacheGet.class, method);
		if ("".equals(cacheGet.condition()))
		{
			String key = SmcEl.createValue(cacheGet.value(), method.getParameterTypes());
			String body = Cache.class.getName() + " _cache = " + fieldName + ".get(\"" + cacheGet.cacheName() + "\");\r\n" //
			        + "try{\r\n"//
			        + "Object cacheResult = _cache.get(" + key + ");\r\n"//
			        + "if(cacheResult!=null){return(" + getWrapperType(method.getReturnType()).getName() + " )cacheResult;}\r\n"//
			        + "else{\r\n" + getWrapperType(method.getReturnType()).getName() + " result = " + methodModel.getInvokeInfo() + ";\r\n"//
			        + "if(result==null){return null;}\r\n"//
			        + "else{\r\n";//
			if (cacheGet.timeToLive() == -1)
			{
				body = body + "_cache.put(" + key + ",result);\r\nreturn result;\r\n}\r\n}\r\n";
			}
			else
			{
				body = body + "_cache.put(" + key + ",result," + cacheGet.timeToLive() + ");\r\nreturn result;\r\n}\r\n}\r\n";
			}
			body = body + "}\r\n"//
			        + "catch(java.lang.Throwable e){\r\n"//
			        + "throw new java.lang.RuntimeException(e);\r\n" + "}\r\n";//
			insert.setBody(body);
			compilerModel.putMethod(method, insert);
		}
		else
		{
			String condition = SmcEl.createIf(cacheGet.condition(), method.getParameterTypes());
			String key = SmcEl.createValue(cacheGet.value(), method.getParameterTypes());
			String body = "if(" + condition + "){\r\n"//
			        + Cache.class.getName() + " _cache = " + fieldName + ".get(\"" + cacheGet.cacheName() + "\");\r\n" //
			        + "try{\r\n"//
			        + "Object cacheResult = _cache.get(" + key + ");\r\n"//
			        + "if(cacheResult!=null){return(" + getWrapperType(method.getReturnType()).getName() + " )cacheResult;}\r\n"//
			        + "else{\r\n" + getWrapperType(method.getReturnType()).getName() + " result = " + methodModel.getInvokeInfo() + ";\r\n"//
			        + "if(result==null){return null;}\r\n"//
			        + "else{\r\n";//
			if (cacheGet.timeToLive() == -1)
			{
				body = body + "_cache.put(" + key + ",result);\r\nreturn result;\r\n}\r\n}\r\n";
			}
			else
			{
				body = body + "_cache.put(" + key + ",result," + cacheGet.timeToLive() + ");\r\nreturn result;\r\n}\r\n}\r\n";
			}
			body = body + "}\r\n"//
			        + "catch(Throwable e){\r\n"//
			        + "throw new java.lang.RuntimeException(e);\r\n" + "}\r\n";//
			body += "}\r\n";
			body += "else{return " + methodModel.getInvokeInfo() + ";}\r\n";// ;
			insert.setBody(body);
			compilerModel.putMethod(method, insert);
		}
	}
	
	public static void addCachePutToMethod(ClassModel compilerModel, String fieldName, Method method)
	{
		AnnotationUtil annotationUtil = new AnnotationUtil();
		MethodModel methodModel = compilerModel.getMethodModel(method);
		String rename = method.getName() + "_" + count.incrementAndGet();
		methodModel.setMethodName(rename);
		compilerModel.putShadowMethodModel(methodModel);
		MethodModel insert = new MethodModel(method);
		CachePut cachePut = annotationUtil.getAnnotation(CachePut.class, method);
		if ("".equals(cachePut.condition()))
		{
			String key = SmcEl.createValue(cachePut.value(), method.getParameterTypes());
			String body = Cache.class.getName() + " _cache = " + fieldName + ".get(\"" + cachePut.cacheName() + "\");\r\n" //
			        + insert.getReturnInfo() + " result = " + methodModel.getInvokeInfo() + ";\r\n";//
			if (cachePut.timeToLive() == -1)
			{
				body = body + "_cache.put(" + key + ",result);\r\nreturn result;\r\n";
			}
			else
			{
				body = body + "_cache.put(" + key + ",result," + cachePut.timeToLive() + ");\r\nreturn result;\r\n";
			}
			insert.setBody(body);
			compilerModel.putMethod(method, insert);
		}
		else
		{
			String condition = SmcEl.createIf(cachePut.condition(), method.getParameterTypes());
			String key = SmcEl.createValue(cachePut.value(), method.getParameterTypes());
			String body = "if(" + condition + "){\r\n";//
			body += Cache.class.getName() + " _cache = " + fieldName + ".get(\"" + cachePut.cacheName() + "\");\r\n" //
			        + insert.getReturnInfo() + " result = " + methodModel.getInvokeInfo() + ";\r\n";//
			if (cachePut.timeToLive() == -1)
			{
				body = body + "_cache.put(" + key + ",result);\r\nreturn result;\r\n";
			}
			else
			{
				body = body + "_cache.put(" + key + ",result," + cachePut.timeToLive() + ");\r\nreturn result;\r\n";
			}
			body += "}\r\n" + "else{return " + methodModel.getInvokeInfo() + ";}\r\n";
			insert.setBody(body);
			compilerModel.putMethod(method, insert);
		}
	}
	
	public static void addCacheDeleteToMethod(ClassModel compilerModel, String fieldName, Method method)
	{
		AnnotationUtil annotationUtil = new AnnotationUtil();
		MethodModel methodModel = compilerModel.getMethodModel(method);
		String rename = method.getName() + "_" + count.incrementAndGet();
		methodModel.setMethodName(rename);
		compilerModel.putShadowMethodModel(methodModel);
		MethodModel insert = new MethodModel(method);
		CacheDelete cacheDelete = annotationUtil.getAnnotation(CacheDelete.class, method);
		if ("".equals(cacheDelete.condition()))
		{
			String key = SmcEl.createValue(cacheDelete.value(), method.getParameterTypes());
			String body = Cache.class.getName() + " _cache = " + fieldName + ".get(\"" + cacheDelete.cacheName() + "\");\r\n"; //
			if (method.getReturnType() == void.class)
			{
				body += methodModel.getInvokeInfo() + ";\r\n_cache.remove(" + key + ");\r\n";
			}
			else
			{
				body += insert.getReturnInfo() + " result = " + methodModel.getInvokeInfo() + ";\r\n"//
				        + "_cache.remove(" + key + ");\r\n"//
				        + "return result;\r\n";//
			}
			insert.setBody(body);
			compilerModel.putMethod(method, insert);
		}
		else
		{
			String condition = SmcEl.createIf(cacheDelete.condition(), method.getParameterTypes());
			String key = SmcEl.createValue(cacheDelete.value(), method.getParameterTypes());
			String body = "if(" + condition + "){"//
			        + Cache.class.getName() + " _cache = " + fieldName + ".get(\"" + cacheDelete.cacheName() + "\");\r\n"; //
			if (method.getReturnType() == void.class)
			{
				body += methodModel.getInvokeInfo() + ";\r\n_cache.remove(" + key + ");\r\n";
			}
			else
			{
				body += insert.getReturnInfo() + " result = " + methodModel.getInvokeInfo() + ";\r\n"//
				        + "_cache.remove(" + key + ");\r\n"//
				        + "return result;\r\n";//
			}
			body += "}\r\nelse{\r\n";
			if (method.getReturnType() == void.class)
			{
				body += methodModel.getInvokeInfo() + ";\r\n}\r\n";
			}
			else
			{
				
				body += "return " + methodModel.getInvokeInfo() + ";\r\n}\r\n";
			}
			insert.setBody(body);
			compilerModel.putMethod(method, insert);
		}
	}
	
	private static Class<?> getWrapperType(Class<?> type)
	{
		if (type.isPrimitive() == false)
		{
			return type;
		}
		else if (type == int.class)
		{
			return Integer.class;
		}
		else if (type == boolean.class)
		{
			return Boolean.class;
		}
		else if (type == char.class)
		{
			return Character.class;
		}
		else if (type == short.class)
		{
			return Short.class;
		}
		else if (type == long.class)
		{
			return Long.class;
		}
		else if (type == float.class)
		{
			return Float.class;
		}
		else if (type == Double.class)
		{
			return Double.class;
		}
		else if (type == byte.class)
		{
			return Byte.class;
		}
		else
		{
			return type;
		}
	}
}
