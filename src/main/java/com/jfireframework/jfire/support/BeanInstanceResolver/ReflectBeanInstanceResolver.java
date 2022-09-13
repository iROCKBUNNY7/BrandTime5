package com.jfireframework.jfire.support.BeanInstanceResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.Constraint;
import javax.validation.Valid;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.baseutil.order.AescComparator;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.CompilerModel;
import com.jfireframework.baseutil.smc.model.ResourceAnnoFieldModel;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.jfire.Utils;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.kernel.ExtraInfoStore;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.DynamicCodeTool;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.EnhanceAnnoInfo;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.AfterEnhance;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.AroundEnhance;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.AutoResource;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.BeforeEnhance;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.EnhanceClass;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.ThrowEnhance;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.Transaction;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.CacheManager;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.annotation.CacheDelete;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.annotation.CacheGet;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.annotation.CachePut;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.tx.RessourceManager;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.tx.TransactionManager;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.validate.JfireMethodValidator;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.annotation.LazyInitUniltFirstInvoke;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.FieldFactory;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.dependency.DIField;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.ParamField;

public class ReflectBeanInstanceResolver extends BaseBeanInstanceResolver
{
    private final ClassLoader         classLoader;
    private final ExtraInfoStore      extraInfoStore;
    private final Map<String, String> properties;
    private String                    postConstructMethod;
    private Method                    _postConstructMethod;
    private String                    closeMethod;
    private List<DIField>             diFields;
    private List<ParamField>          paramFields;
    private Class<?>                  enhanceType;
    private static AtomicInteger      enhanceCount = new AtomicInteger(0);
    
    public ReflectBeanInstanceResolver(String beanName, Class<?> type, boolean prototype, Environment environment)
    {
        classLoader = environment.getClassLoader();
        extraInfoStore = environment.getExtraInfoStore();
        properties = environment.getProperties();
        AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
        lazyInitUntilFirstInvoke = annotationUtil.isPresent(LazyInitUniltFirstInvoke.class, type);
        baseInitialize(beanName, type, prototype, lazyInitUntilFirstInvoke);
    }
    
    @Override
    protected Object buildInstance(Map<String, Object> beanInstanceMap)
    {
        try
        {
            Object instance = enhanceType.newInstance();
            beanInstanceMap.put(beanName, instance);
            for (DIField each : diFields)
            {
                each.inject(instance, beanInstanceMap);
            }
            for (ParamField each : paramFields)
            {
                each.setParam(instance);
            }
            if (postConstructMethod != null)
            {
                _postConstructMethod.invoke(instance);
            }
            return instance;
        }
        catch (Exception e)
        {
            throw new UnSupportException(StringUtil.format("初始化bean实例错误，实例名称:{},对象类名:{}", beanName, type.getName()), e);
        }
    }
    
    @Override
    public void initialize(Map<String, BeanDefinition> definitions)
    {
        findAnnoedPostAndPreDestoryMethod();
        enhanceType = enhance(definitions);
        AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
        diFields = FieldFactory.buildDependencyFields(annotationUtil, enhanceType, definitions);
        paramFields = FieldFactory.buildParamField(annotationUtil, enhanceType, properties, classLoader);
        if (postConstructMethod != null)
        {
            _postConstructMethod = getMethod(postConstructMethod, enhanceType);
            _postConstructMethod.setAccessible(true);
        }
        if (closeMethod != null)
        {
            preDestoryMethod = getMethod(closeMethod, enhanceType);
            preDestoryMethod.setAccessible(true);
        }
    }
    
    Method getMethod(String name, Class<?> type)
    {
        while (type != Object.class)
        {
            try
            {
                Method method = type.getDeclaredMethod(name);
                return method;
            }
            catch (NoSuchMethodException e)
            {
                continue;
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        throw new NullPointerException();
    }
    
    void findAnnoedPostAndPreDestoryMethod()
    {
        AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
        for (Method method : ReflectUtil.getAllMehtods(type))
        {
            if (annotationUtil.isPresent(PostConstruct.class, method))
            {
                postConstructMethod = method.getName();
            }
            if (annotationUtil.isPresent(PreDestroy.class, method))
            {
                closeMethod = method.getName();
            }
        }
    }
    
    Class<?> enhance(Map<String, BeanDefinition> definitions)
    {
        BeanAopDefinition beanAopDefinition = scanForEmbedEnhanceMethods();
        scanForAspect(definitions, beanAopDefinition);
        if (beanAopDefinition.autoResourceMethods.isEmpty() && //
                beanAopDefinition.cacheMethods.isEmpty() && //
                beanAopDefinition.txMethods.isEmpty() && //
                beanAopDefinition.enhanceAnnoInfos.isEmpty() && //
                beanAopDefinition.validateMethods.isEmpty())
        {
            return type;
        }
        try
        {
            return enhance(beanAopDefinition);
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    private Class<?> enhance(BeanAopDefinition beanAopDefinition) throws ClassNotFoundException
    {
        CompilerModel compilerModel = DynamicCodeTool.createClientClass(type);
        if (beanAopDefinition.getTxMethods().size() > 0)
        {
            String txFieldName = "tx$smc";
            compilerModel.addField(new ResourceAnnoFieldModel(txFieldName, TransactionManager.class));
            addTxToMethod(compilerModel, txFieldName, beanAopDefinition.getTxMethods());
        }
        if (beanAopDefinition.getAutoResourceMethods().size() > 0)
        {
            String resFieldName = "ac$smc";
            compilerModel.addField(new ResourceAnnoFieldModel(resFieldName, RessourceManager.class));
            addResToMethod(compilerModel, resFieldName, beanAopDefinition.getAutoResourceMethods());
        }
        if (beanAopDefinition.getCacheMethods().size() > 0)
        {
            String cacheFieldName = "cache$smc";
            compilerModel.addField(new ResourceAnnoFieldModel(cacheFieldName, CacheManager.class));
            addCacheToMethod(compilerModel, cacheFieldName, beanAopDefinition.getCacheMethods());
        }
        // 验证应该是在内部的最外层
        if (beanAopDefinition.getValidateMethods().size() > 0)
        {
            String validateFieldName = "validate$smc";
            String extraInfoStoreFieldName = "extraInfoStore$smc";
            compilerModel.addField(new ResourceAnnoFieldModel(extraInfoStoreFieldName, ExtraInfoStore.class));
            compilerModel.addField(new ResourceAnnoFieldModel(validateFieldName, JfireMethodValidator.class));
            addValidateToMethod(compilerModel, validateFieldName, extraInfoStoreFieldName, beanAopDefinition.getValidateMethods());
        }
        if (beanAopDefinition.getEnhanceAnnoInfos().size() > 0)
        {
            /**
             * aop增强,采用的是将增强类作为目标类的属性注入到目标类中.所以在开始增强前,需要确定注入属性的名称.
             * 由于一个增强类中的多个EnhanceAnnoInfo的共用同一个属性名，所以在这里进行去重。只要放入一个即可
             */
            HashSet<String> enHanceNameSet = new HashSet<String>();
            for (EnhanceAnnoInfo info : beanAopDefinition.getEnhanceAnnoInfos())
            {
                if (enHanceNameSet.contains(info.getEnhanceFieldName()) == false)
                {
                    compilerModel.addField(new ResourceAnnoFieldModel(info.getEnhanceFieldName(), info.getEnhanceBeanType()));
                    enHanceNameSet.add(info.getEnhanceFieldName());
                }
            }
        }
        Collections.sort(beanAopDefinition.getEnhanceAnnoInfos(), new AescComparator());
        for (Method each : compilerModel.methods())
        {
            logger.trace("准备检查方法:{}", each.getName());
            for (EnhanceAnnoInfo enhanceAnnoInfo : beanAopDefinition.getEnhanceAnnoInfos())
            {
                switch (enhanceAnnoInfo.getType())
                {
                    case EnhanceAnnoInfo.BEFORE:
                        if (enhanceAnnoInfo.match(each))
                        {
                            DynamicCodeTool.enhanceBefore(compilerModel, each, enhanceAnnoInfo);
                        }
                        break;
                    case EnhanceAnnoInfo.AFTER:
                        if (enhanceAnnoInfo.match(each))
                        {
                            DynamicCodeTool.enhanceAfter(compilerModel, each, enhanceAnnoInfo);
                        }
                        break;
                    case EnhanceAnnoInfo.AROUND:
                        if (enhanceAnnoInfo.match(each))
                        {
                            DynamicCodeTool.enhanceAround(compilerModel, each, enhanceAnnoInfo);
                        }
                        break;
                    case EnhanceAnnoInfo.THROW:
                        if (enhanceAnnoInfo.match(each))
                        {
                            DynamicCodeTool.enhanceException(compilerModel, each, enhanceAnnoInfo);
                        }
                        break;
                }
            }
        }
        JavaStringCompiler compiler = new JavaStringCompiler();
        try
        {
            logger.trace("生成类:{}的源代码:\r\n{}\r\n", compilerModel.className(), compilerModel.toString());
            return compiler.compile(compilerModel, classLoader);
        }
        catch (Exception e)
        {
            logger.error("生成代理过程异常", e);
            throw new JustThrowException(e);
        }
    }
    
    /**
     * 为事务方法增加上事务的开启，提交和回滚
     * 
     * @param targetCc 事务方法所在的类
     * @param txFieldName 事务管理器在这个类中的属性名
     * @param txMethods 事务方法
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private void addTxToMethod(CompilerModel compilerModel, String txFieldName, List<Method> txMethods)
    {
        for (Method method : txMethods)
        {
            DynamicCodeTool.addTxToMethod(compilerModel, txFieldName, method);
        }
    }
    
    private void addValidateToMethod(CompilerModel compilerModel, String validateFieldName, String extraInfoStoreFieldName, Set<Method> validateMethods)
    {
        for (Method method : validateMethods)
        {
            int sequence = extraInfoStore.registerMethod(method);
            DynamicCodeTool.addValidateToMethod(compilerModel, validateFieldName, extraInfoStoreFieldName, method, sequence);
        }
    }
    
    /**
     * 为自动关闭方法加上资源关闭的调用
     * 
     * @param targetCc 自动关闭方法所在的类
     * @param resFieldName 自动关闭管理器在这个类中的属性名
     * @param txMethods 自动关闭方法
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private void addResToMethod(CompilerModel compilerModel, String resFieldName, List<Method> resMethods)
    {
        for (Method method : resMethods)
        {
            DynamicCodeTool.addAutoResourceToMethod(compilerModel, resFieldName, method);
        }
    }
    
    private void addCacheToMethod(CompilerModel compilerModel, String cacheFieldName, List<Method> cacheMethods)
    {
        AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
        for (Method each : cacheMethods)
        {
            try
            {
                if (annotationUtil.isPresent(CacheGet.class, each))
                {
                    DynamicCodeTool.addCacheGetToMethod(compilerModel, cacheFieldName, each);
                }
                if (annotationUtil.isPresent(CachePut.class, each))
                {
                    DynamicCodeTool.addCachePutToMethod(compilerModel, cacheFieldName, each);
                }
                if (annotationUtil.isPresent(CacheDelete.class, each))
                {
                    DynamicCodeTool.addCacheDeleteToMethod(compilerModel, cacheFieldName, each);
                }
                
            }
            catch (Exception e)
            {
                throw new UnSupportException(StringUtil.format("构造缓存方法异常，请检查{}.{}", each.getDeclaringClass().getName(), each.getName()), e);
            }
        }
    }
    
    public int getParamNameIndex(String inject, String[] paramNames)
    {
        for (int i = 0; i < paramNames.length; i++)
        {
            if (paramNames[i].equals(inject))
            {
                return i;
            }
        }
        throw new RuntimeException("给定的参数" + inject + "不在参数列表中");
    }
    
    private BeanAopDefinition scanForEmbedEnhanceMethods()
    {
        class Helper
        {
            
            boolean hasValidateEnhance(Method method)
            {
                AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
                if (annotationUtil.isPresent(Constraint.class, method))
                {
                    return true;
                }
                for (Annotation[] annotations : method.getParameterAnnotations())
                {
                    for (Annotation each : annotations)
                    {
                        if (each.annotationType() == Valid.class)
                        {
                            return true;
                        }
                    }
                    if (annotationUtil.isPresent(Constraint.class, annotations))
                    {
                        return true;
                    }
                }
                return false;
            }
            
            boolean hasTransactionEnhance(Method method)
            {
                AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
                if (annotationUtil.isPresent(Transaction.class, method))
                {
                    Verify.False(annotationUtil.isPresent(AutoResource.class, method), "同一个方法上不能同时有事务注解和自动关闭注解，请检查{}.{}", method.getDeclaringClass(), method.getName());
                    Verify.True(Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers()), "方法{}.{}有事务注解,访问类型必须是public或protected", method.getDeclaringClass(), method.getName());
                    return true;
                }
                else
                {
                    return false;
                }
            }
            
            boolean hasAutoresourceEnhance(Method method)
            {
                AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
                if (annotationUtil.isPresent(AutoResource.class, method))
                {
                    Verify.True(Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers()), "方法{}.{}有自动关闭注解,访问类型必须是public或protected", method.getDeclaringClass(), method.getName());
                    return true;
                }
                else
                {
                    return false;
                }
            }
            
            boolean hasCacheEnhance(Method method)
            {
                AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
                if (annotationUtil.isPresent(CachePut.class, method) || annotationUtil.isPresent(CacheGet.class, method) || annotationUtil.isPresent(CacheDelete.class, method))
                {
                    Verify.True(Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers()), "方法{}.{}有缓存注解,访问类型必须是public或protected", method.getDeclaringClass(), method.getName());
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        Helper helper = new Helper();
        BeanAopDefinition beanAopDefinition = new BeanAopDefinition();
        // 由于增强是采用子类来实现的,所以事务注解只对当前的类有效.如果当前类的父类也有事务注解,在本次增强中就无法起作用
        for (Method method : ReflectUtil.getAllMehtods(type))
        {
            if (helper.hasValidateEnhance(method))
            {
                beanAopDefinition.addValidateMethod(method);
                logger.trace("发现需要验证增强的方法:{}", method.toString());
            }
            if (helper.hasTransactionEnhance(method))
            {
                beanAopDefinition.addTxMethod(method);
                logger.trace("发现事务增强方法:{}", method.toString());
            }
            else if (helper.hasAutoresourceEnhance(method))
            {
                beanAopDefinition.addAutoResourceMethod(method);
                logger.trace("发现资源关闭增强方法:{}", method.toString());
            }
            if (helper.hasCacheEnhance(method))
            {
                beanAopDefinition.addCacheMethod(method);
                logger.trace("发现缓存增强方法:{}", method.toString());
            }
        }
        return beanAopDefinition;
    }
    
    private void scanForAspect(Map<String, BeanDefinition> beanDefinitions, BeanAopDefinition beanAopDefinition)
    {
        AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
        for (BeanDefinition enhanceBeanDefinition : beanDefinitions.values())
        {
            if (annotationUtil.isPresent(EnhanceClass.class, enhanceBeanDefinition.getOriginType()))
            {
                String rule = annotationUtil.getAnnotation(EnhanceClass.class, enhanceBeanDefinition.getOriginType()).value();
                if (StringUtil.match(type.getName(), rule))
                {
                    beanAopDefinition.resolveEnhanceBeanDefinition(enhanceBeanDefinition);
                }
            }
        }
    }
    
    class BeanAopDefinition
    {
        private Set<Method>           validateMethods     = new HashSet<Method>(8);
        private List<Method>          autoResourceMethods = new ArrayList<Method>(8);
        private List<Method>          txMethods           = new ArrayList<Method>(8);
        private List<Method>          cacheMethods        = new ArrayList<Method>(8);
        private List<EnhanceAnnoInfo> enhanceAnnoInfos    = new ArrayList<EnhanceAnnoInfo>(8);
        
        void addValidateMethod(Method method)
        {
            validateMethods.add(method);
        }
        
        void addAutoResourceMethod(Method method)
        {
            autoResourceMethods.add(method);
        }
        
        void addTxMethod(Method method)
        {
            txMethods.add(method);
        }
        
        void addCacheMethod(Method method)
        {
            cacheMethods.add(method);
        }
        
        void resolveEnhanceBeanDefinition(BeanDefinition enhanceBeanDefinition)
        {
            AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
            String enhanceBeanfieldName = "jfireinvoker$" + enhanceCount.incrementAndGet();
            String path;
            int order;
            for (Method each : enhanceBeanDefinition.getOriginType().getDeclaredMethods())
            {
                if (annotationUtil.isPresent(AfterEnhance.class, each))
                {
                    AfterEnhance afterEnhance = annotationUtil.getAnnotation(AfterEnhance.class, each);
                    path = afterEnhance.value().equals("") ? each.getName() + "(*)" : afterEnhance.value();
                    order = afterEnhance.order();
                }
                else if (annotationUtil.isPresent(AroundEnhance.class, each))
                {
                    AroundEnhance aroundEnhance = annotationUtil.getAnnotation(AroundEnhance.class, each);
                    path = aroundEnhance.value().equals("") ? each.getName() + "(*)" : aroundEnhance.value();
                    order = aroundEnhance.order();
                }
                else if (annotationUtil.isPresent(BeforeEnhance.class, each))
                {
                    BeforeEnhance beforeEnhance = annotationUtil.getAnnotation(BeforeEnhance.class, each);
                    path = beforeEnhance.value().equals("") ? each.getName() + "(*)" : beforeEnhance.value();
                    order = beforeEnhance.order();
                }
                else if (annotationUtil.isPresent(ThrowEnhance.class, each))
                {
                    ThrowEnhance throwEnhance = annotationUtil.getAnnotation(ThrowEnhance.class, each);
                    path = throwEnhance.value().equals("") ? each.getName() + "(*)" : throwEnhance.value();
                    order = throwEnhance.order();
                    EnhanceAnnoInfo enhanceAnnoInfo = new EnhanceAnnoInfo(annotationUtil, enhanceBeanDefinition.getBeanName(), enhanceBeanDefinition.getOriginType(), enhanceBeanfieldName, path, order, each);
                    enhanceAnnoInfo.setThrowtype(throwEnhance.type());
                    enhanceAnnoInfos.add(enhanceAnnoInfo);
                    continue;
                }
                else
                {
                    continue;
                }
                enhanceAnnoInfos.add(new EnhanceAnnoInfo(annotationUtil, enhanceBeanDefinition.getBeanName(), enhanceBeanDefinition.getOriginType(), enhanceBeanfieldName, path, order, each));
            }
        }
        
        public List<Method> getAutoResourceMethods()
        {
            return autoResourceMethods;
        }
        
        public List<Method> getTxMethods()
        {
            return txMethods;
        }
        
        public List<Method> getCacheMethods()
        {
            return cacheMethods;
        }
        
        public List<EnhanceAnnoInfo> getEnhanceAnnoInfos()
        {
            return enhanceAnnoInfos;
        }
        
        Set<Method> getValidateMethods()
        {
            return validateMethods;
        }
    }
    
}
