package com.jfireframework.jfire.smc;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.aop.EnhanceAnnoInfo;
import com.jfireframework.jfire.aop.annotation.Transaction;
import com.jfireframework.jfire.cache.annotation.CacheDelete;
import com.jfireframework.jfire.cache.annotation.CacheGet;
import com.jfireframework.jfire.cache.annotation.CachePut;
import com.jfireframework.jfire.smc.el.SmcEl;
import com.jfireframework.jfire.smc.model.CompilerModel;
import com.jfireframework.jfire.smc.model.MethodModel;
import com.jfireframework.jfire.tx.TransactionIsolate;

public class SmcHelper
{
    
    private static final AtomicInteger count     = new AtomicInteger(0);
    private static final AtomicInteger typeCount = new AtomicInteger(0);
    
    public static CompilerModel createClientClass(Class<?> type)
    {
        CompilerModel compilerModle = new CompilerModel(type.getSimpleName() + "$Smc$" + typeCount.incrementAndGet(), type);
        for (Method method : ReflectUtil.getAllMehtods(type))
        {
            if (canHaveChildMethod(method.getModifiers()))
            {
                MethodModel methodModel = new MethodModel(method);
                if (method.getReturnType() == void.class)
                {
                    methodModel.setBody("super." + methodModel.getInvokeInfo() + ";");
                }
                else
                {
                    methodModel.setBody("return super." + methodModel.getInvokeInfo() + ";");
                }
                compilerModle.putMethod(method, methodModel);
            }
        }
        return compilerModle;
    }
    
    private static boolean canHaveChildMethod(int moditifer)
    {
        if ((Modifier.isPublic(moditifer) || Modifier.isProtected(moditifer)) //
                && Modifier.isFinal(moditifer) == false //
                && Modifier.isNative(moditifer) == false //
                && Modifier.isStatic(moditifer) == false)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static void enhanceBefore(CompilerModel model, Method method, EnhanceAnnoInfo enhanceAnnoInfo)
    {
        MethodModel pred = model.getMethodModel(method);
        String rename = method.getName() + "_" + count.incrementAndGet();
        pred.setMethodName(rename);
        model.putShadowMethodModel(pred);
        MethodModel insert = new MethodModel(method);
        String body = "com.jfireframework.jfire.aop.ProceedPointImpl point = new com.jfireframework.jfire.aop.ProceedPointImpl();\r\n";
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
        String body = "com.jfireframework.jfire.aop.ProceedPointImpl point = new com.jfireframework.jfire.aop.ProceedPointImpl();\r\n";
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
        String body = "com.jfireframework.jfire.aop.ProceedPointImpl point = new com.jfireframework.jfire.aop.ProceedPointImpl();\r\n";
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
                    + "throw e" + index + ";}\r\n";
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
        String body = "com.jfireframework.jfire.aop.ProceedPointImpl point = new com.jfireframework.jfire.aop.ProceedPointImpl(){\r\n"//
                + " public Object invoke()  {\r\n";
        if (method.getReturnType() == void.class)
        {
            body += pred.getInvokeInfo() + ";\r\n"//
                    + "return null;\r\n";
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
                    + "throw e;\r\n" + "}\r\n"//
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
                    + "throw e;\r\n" + "}\r\n"//
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
                    + "throw e;\r\n" + "}\r\n"//
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
                    + "throw e;\r\n" + "}\r\n"//
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
            String body = " com.jfireframework.jfire.cache.Cache _cache = " + fieldName + ".get(\"" + cacheGet.cacheName() + "\");\r\n" //
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
                    + "throw e;\r\n" + "}\r\n";//
            insert.setBody(body);
            compilerModel.putMethod(method, insert);
        }
        else
        {
            String condition = SmcEl.createIf(cacheGet.condition(), method.getParameterTypes());
            String key = SmcEl.createValue(cacheGet.value(), method.getParameterTypes());
            String body = "if(" + condition + "){\r\n"//
                    + " com.jfireframework.jfire.cache.Cache _cache = " + fieldName + ".get(\"" + cacheGet.cacheName() + "\");\r\n" //
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
                    + "throw e;\r\n" + "}\r\n";//
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
            String body = " com.jfireframework.jfire.cache.Cache _cache = " + fieldName + ".get(\"" + cachePut.cacheName() + "\");\r\n" //
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
            body += " com.jfireframework.jfire.cache.Cache _cache = " + fieldName + ".get(\"" + cachePut.cacheName() + "\");\r\n" //
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
            String body = " com.jfireframework.jfire.cache.Cache _cache = " + fieldName + ".get(\"" + cacheDelete.cacheName() + "\");\r\n"; //
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
                    + " com.jfireframework.jfire.cache.Cache _cache = " + fieldName + ".get(\"" + cacheDelete.cacheName() + "\");\r\n"; //
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
