package com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.el.SmcEl;
import com.jfireframework.baseutil.smc.model.CompilerModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.Transaction;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.Cache;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.annotation.CacheDelete;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.annotation.CacheGet;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.annotation.CachePut;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.tx.TransactionIsolate;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.validate.Validate;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.validate.ValidateResult;

public class DynamicCodeTool
{
	
	private static final AtomicInteger count = new AtomicInteger(0);
	
	public static CompilerModel createClientClass(Class<?> type)
	{
		return SmcHelper.createClientClass(type);
	}
	
	public static void enhanceBefore(CompilerModel model, Method method, EnhanceAnnoInfo enhanceAnnoInfo)
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
	
	public static void enhanceAfter(CompilerModel model, Method method, EnhanceAnnoInfo enhanceAnnoInfo)
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
	
	public static void enhanceException(CompilerModel model, Method method, EnhanceAnnoInfo enhanceAnnoInfo)
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
	
	public static void enhanceAround(CompilerModel model, Method method, EnhanceAnnoInfo enhanceAnnoInfo)
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
	
	public static void addValidateToMethod(final CompilerModel compilerModel, final String validateFieldName, final String extraInfoStoreFieldName, final Method method, final int methodSequence)
	{
		final int length = method.getParameterTypes().length;
		if (length == 0)
		{
			return;
		}
		final AnnotationUtil annotationUtil = new AnnotationUtil();
		class Helper
		{
			void addGroups(StringCache cache, Method method)
			{
				if (annotationUtil.isPresent(Validate.class, method))
				{
					Validate validate = annotationUtil.getAnnotation(Validate.class, method);
					Class<?>[] groups = validate.groups();
					cache.append("new Class[]{");
					for (Class<?> each : groups)
					{
						cache.append(each.getName()).appendComma();
					}
					if (cache.isCommaLast())
					{
						cache.deleteLast();
					}
					cache.append("}");
				}
				else
				{
					cache.append("new Class[0]");
				}
			}
			
			String buildBody(String invokeStr)
			{
				StringCache cache = new StringCache();
				String variableName = "validateResult_smc_" + count.incrementAndGet();
				cache.append(ValidateResult.class.getName()).append(" ").append(variableName).append(" = ")//
				        .append(validateFieldName).append(".validate(").append(extraInfoStoreFieldName).append(".getMethod(" + methodSequence + "),new Object[]{");
				for (int i = 0; i < length; i++)
				{
					cache.append("$").append(i).appendComma();
				}
				cache.deleteLast().append("},");
				addGroups(cache, method);
				cache.append(");\r\n");
				addValidateResultJudge(cache, variableName);
				addEnd(cache, invokeStr, method);
				return cache.toString();
			}
			
			void addEnd(StringCache cache, String invokeStr, Method method)
			{
				if (method.getReturnType() != void.class)
				{
					cache.append("return ");
				}
				cache.append(invokeStr).append(";\r\n");
			}
			
			void addValidateResultJudge(StringCache cache, String variableName)
			{
				cache.append("if(").append(variableName).append(".getDetails().size()>0){\r\n");
				cache.append("throw new javax.validation.ValidationException(").append(variableName).append(".toString());\r\n");
				cache.append("}\r\n");
			}
			
			String processPredMethod()
			{
				MethodModel pred = compilerModel.getMethodModel(method);
				String rename = method.getName() + "_" + count.incrementAndGet();
				pred.setMethodName(rename);
				compilerModel.putShadowMethodModel(pred);
				return pred.getInvokeInfo();
			}
		}
		Helper helper = new Helper();
		String invokeStr = helper.processPredMethod();
		MethodModel validatedMethod = new MethodModel(method);
		String body = helper.buildBody(invokeStr);
		validatedMethod.setBody(body);
		compilerModel.putMethod(method, validatedMethod);
		
	}
	
	public static void addTxToMethod(CompilerModel compilerModel, String txFieldName, Method method)
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
	
	public static void addAutoResourceToMethod(CompilerModel compilerModel, String fieldName, Method method)
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
	
	public static void addCacheGetToMethod(CompilerModel compilerModel, String fieldName, Method method)
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
	
	public static void addCachePutToMethod(CompilerModel compilerModel, String fieldName, Method method)
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
	
	public static void addCacheDeleteToMethod(CompilerModel compilerModel, String fieldName, Method method)
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
