package com.jfireframework.jfire;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import com.jfireframework.baseutil.PackageScan;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.baseutil.code.CodeLocation;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.baseutil.order.AescComparator;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.simplelog.ConsoleLogFactory;
import com.jfireframework.baseutil.simplelog.Logger;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.jfire.aop.AopUtil;
import com.jfireframework.jfire.bean.Bean;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.bean.field.FieldFactory;
import com.jfireframework.jfire.bean.field.dependency.DIField;
import com.jfireframework.jfire.bean.field.dependency.DIFieldInfo;
import com.jfireframework.jfire.bean.field.dependency.impl.BeanNameMapField;
import com.jfireframework.jfire.bean.field.dependency.impl.DefaultBeanField;
import com.jfireframework.jfire.bean.field.dependency.impl.ListField;
import com.jfireframework.jfire.bean.field.dependency.impl.MethodMapField;
import com.jfireframework.jfire.bean.field.dependency.impl.ValueMapField;
import com.jfireframework.jfire.bean.field.param.ParamField;
import com.jfireframework.jfire.bean.impl.AnnotationConfigBean;
import com.jfireframework.jfire.bean.impl.BaseBean;
import com.jfireframework.jfire.bean.impl.DefaultBean;
import com.jfireframework.jfire.bean.impl.LoadByBean;
import com.jfireframework.jfire.bean.impl.OuterEntityBean;
import com.jfireframework.jfire.bean.load.LoadBy;
import com.jfireframework.jfire.config.Condition;
import com.jfireframework.jfire.config.Environment;
import com.jfireframework.jfire.config.ImportBeanDefinition;
import com.jfireframework.jfire.config.JfireInitializationCfg;
import com.jfireframework.jfire.config.annotation.BeanDefinitions;
import com.jfireframework.jfire.config.annotation.ComponentScan;
import com.jfireframework.jfire.config.annotation.Conditional;
import com.jfireframework.jfire.config.annotation.Configuration;
import com.jfireframework.jfire.config.annotation.Import;
import com.jfireframework.jfire.config.annotation.PropertyPaths;
import com.jfireframework.jfire.util.EnvironmentUtil;
import sun.reflect.MethodAccessor;

public class JfireConfig
{
    protected Map<String, BeanDefinition> beanDefinitions = new HashMap<String, BeanDefinition>();
    protected List<String>                classNames      = new LinkedList<String>();
    protected ClassLoader                 classLoader     = JfireConfig.class.getClassLoader();
    protected Map<String, String>         properties      = new HashMap<String, String>();
    protected Environment                 environment     = new Environment(beanDefinitions, properties);
    protected static final Logger         logger          = ConsoleLogFactory.getLogger();
    
    public JfireConfig()
    {
    }
    
    public JfireConfig(JfireInitializationCfg cfg)
    {
        readConfig(cfg);
    }
    
    public JfireConfig(Class<?> configClass)
    {
        AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
        if (annotationUtil.isPresent(Configuration.class, configClass))
        {
            environment.addConfigClass(configClass);
            processComponentScan(configClass);
            registerBeanDefinition(configClass);
        }
    }
    
    public JfireConfig(Class<?> configClass, JfireInitializationCfg cfg)
    {
        readConfig(cfg);
        AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
        if (annotationUtil.isPresent(Configuration.class, configClass))
        {
            environment.addConfigClass(configClass);
            processComponentScan(configClass);
            registerBeanDefinition(configClass);
        }
    }
    
    private void addScanPackageNames(String... scanPackageNames)
    {
        if (scanPackageNames.length == 0)
        {
            return;
        }
        Verify.notNull(scanPackageNames, "添加扫描的包名有误,不能为null.请检查{}", CodeLocation.getCodeLocation(2));
        List<String> classNames = new LinkedList<String>();
        for (String each : scanPackageNames)
        {
            if (each == null)
            {
                continue;
            }
            for (String var : PackageScan.scan(each))
            {
                classNames.add(var);
            }
        }
        this.classNames.addAll(classNames);
    }
    
    private void readConfig(JfireInitializationCfg cfg)
    {
        addScanPackageNames(cfg.getScanPackageNames());
        for (BeanDefinition each : cfg.getBeanDefinitions())
        {
            each.enablePrototype(each.getCfgPrototype());
            each.switchDefault();
        }
        registerBeanDefinition(cfg.getBeanDefinitions());
        resolvePropertyFile(cfg.getPropertyPaths());
        properties.putAll(cfg.getProperties());
    }
    
    private void resolvePropertyFile(String[] paths)
    {
        for (String path : paths)
        {
            InputStream inputStream = null;
            try
            {
                if (path.startsWith("classpath:"))
                {
                    path = path.substring(10);
                    if (this.getClass().getClassLoader().getResource(path) == null)
                    {
                        continue;
                    }
                    inputStream = this.getClass().getClassLoader().getResourceAsStream(path);
                }
                else if (path.startsWith("file:"))
                {
                    path = path.substring(5);
                    if (new File(path).exists() == false)
                    {
                        continue;
                    }
                    inputStream = new FileInputStream(new File(path));
                }
                else
                {
                    continue;
                }
                Properties properties = new Properties();
                properties.load(inputStream);
                addProperties(properties);
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
            finally
            {
                try
                {
                    if (inputStream != null)
                    {
                        inputStream.close();
                        inputStream = null;
                    }
                }
                catch (IOException e)
                {
                    ;
                }
            }
        }
    }
    
    public JfireConfig registerBeanDefinition(Class<?>... srcs)
    {
        AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
        for (Class<?> src : srcs)
        {
            if (annotationUtil.isPresent(Resource.class, src))
            {
                Resource resource = annotationUtil.getAnnotation(Resource.class, src);
                String beanName = "".equals(resource.name()) ? src.getName() : resource.name();
                boolean prototype = (resource.shareable() == false);
                BeanDefinition definition = new BeanDefinition();
                definition.setBeanName(beanName);
                definition.setClassName(src.getName());
                definition.setOriginType(src);
                definition.setType(src);
                definition.enablePrototype(prototype);
                if (annotationUtil.isPresent(LoadBy.class, src))
                {
                    definition.switchLoadBy();
                    definition.setLoadByFactoryName(annotationUtil.getAnnotation(LoadBy.class, src).factoryBeanName());
                }
                else
                {
                    definition.switchDefault();
                }
                if (annotationUtil.isPresent(Configuration.class, src))
                {
                    definition.enableConfiguration();
                    environment.addConfigClass(src);
                }
                putBeanDefinition(definition);
            }
        }
        return this;
    }
    
    public JfireConfig registerBeanDefinition(String resourceName, boolean prototype, Class<?> src)
    {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanName(resourceName);
        beanDefinition.setType(src);
        beanDefinition.enablePrototype(prototype);
        beanDefinition.switchDefault();
        putBeanDefinition(beanDefinition);
        return this;
    }
    
    public JfireConfig registerBeanDefinition(BeanDefinition... beanInfos)
    {
        for (BeanDefinition definition : beanInfos)
        {
            putBeanDefinition(definition);
        }
        return this;
    }
    
    protected void initJfire(Jfire jfire)
    {
        EnvironmentUtil.getAnnoUtil().clear();
        Plugin[] plugins = new Plugin[] { //
                new PreparationPlugin(jfire), //
                new ResolveClassNamesPlugin(), //
                new ComplementedBeanDefinition(), //
                new ResolveBeanConfigurationAnnoPlugin(), //
                new FindAnnoatateBeanDefinitionPlugin(), //
                new ProcessPlaceHolderPlugin(), //
                new FindAnnoedPostAndPreDestoryMethod(), //
                new EnhancePlugin(), //
                new InitDependencyAndParamFieldsPlugin(), //
                new ConstructBeanPlugin(), //
                new InitSingletonBeanPlugin(), //
                new DetectJfireInitFinishInterfacePlugin(), //
                new TriggerJfireInitFinishPlugin()
        };
        for (Plugin plugin : plugins)
        {
            logger.debug("当前执行插件:{}", plugin.getClass().getSimpleName());
            plugin.process();
        }
        EnvironmentUtil.getAnnoUtil().clear();
    }
    
    public JfireConfig setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
        Thread.currentThread().setContextClassLoader(classLoader);
        return this;
    }
    
    public JfireConfig addProperties(Properties... properties)
    {
        for (Properties each : properties)
        {
            for (Entry<Object, Object> entry : each.entrySet())
            {
                this.properties.put((String) entry.getKey(), (String) entry.getValue());
            }
        }
        return this;
    }
    
    public JfireConfig registerSingletonEntity(String beanName, Object entity)
    {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.switchOutter();
        beanDefinition.setBeanName(beanName);
        beanDefinition.enablePrototype(false);
        beanDefinition.setType(entity.getClass());
        beanDefinition.setClassName(entity.getClass().getName());
        beanDefinition.setOriginType(entity.getClass());
        beanDefinition.switchOutter();
        beanDefinition.setOutterEntity(entity);
        putBeanDefinition(beanDefinition);
        return this;
    }
    
    interface AnnoValueProcessor<T extends Annotation>
    {
        void process(T annotation) throws Exception;
    }
    
    private <T extends Annotation> void processAnnoValue(Class<?> ckass, Class<T> annoType, AnnoValueProcessor<T> processor)
    {
        AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
        if (annotationUtil.isPresent(annoType, ckass))
        {
            try
            {
                for (T each : annotationUtil.getAnnotations(annoType, ckass))
                {
                    processor.process(each);
                }
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
    }
    
    private void processComponentScan(Class<?> ckass)
    {
        processAnnoValue(ckass, ComponentScan.class, //
                new AnnoValueProcessor<ComponentScan>() {
                    
                    @Override
                    public void process(ComponentScan componentScan)
                    {
                        addScanPackageNames(componentScan.value());
                    }
                });
    }
    
    private void putBeanDefinition(BeanDefinition definition)
    {
        BeanDefinition exist = beanDefinitions.get(definition.getBeanName());
        if (exist == null)
        {
            beanDefinitions.put(definition.getBeanName(), definition);
        }
        else
        {
            Verify.equal(definition.mode(), exist.mode(), "bean:{}的模式在不同的配置中存在不同，该不同无法兼容", definition.getBeanName());
            if (exist.getClassName() != null && definition.getClassName() != null && exist.getClassName().equals(definition.getClassName()) == false)
            {
                throw new UnsupportedOperationException(StringUtil.format("bean:{}的className在不同的配置中分别存在:{}和{}。无法兼容", exist.getBeanName(), exist.getClassName(), definition.getClassName()));
            }
            // 可能出现两个ClassName都是null，该情况无妨，可以继续融合
            String className = exist.getClassName() != null ? exist.getClassName() : definition.getClassName();
            if (exist.getOriginType() != null && definition.getOriginType() != null && exist.getOriginType() != definition.getOriginType())
            {
                throw new UnsupportedOperationException(StringUtil.format("bean:{}的originType在不同的配置中分别存在:{}和{},无法兼容", exist.getOriginType(), definition.getOriginType()));
            }
            Class<?> originType = exist.getOriginType() != null ? exist.getOriginType() : definition.getOriginType();
            if (exist.getType() != null && definition.getType() != null && exist.getType() != definition.getType())
            {
                throw new UnsupportedOperationException(StringUtil.format("bean:{}的type在不同的配置中分别存在:{}和{},无法兼容", exist.getType(), definition.getType()));
            }
            Class<?> type = exist.getType() != null ? exist.getType() : definition.getType();
            exist.getDependencies().putAll(definition.getDependencies());
            Map<String, String> dependencies = exist.getDependencies();
            exist.getParams().putAll(definition.getParams());
            Map<String, String> params = exist.getParams();
            if (exist.getPostConstructMethod() != null && definition.getPostConstructMethod() != null && exist.getPostConstructMethod().equals(definition.getPostConstructMethod()) == false)
            {
                throw new UnsupportedOperationException(StringUtil.format("bean:{}的postConstructMethod在不同的配置中分别存在:{}和{},无法兼容", exist.getPostConstructMethod(), definition.getPostConstructMethod()));
            }
            String postConstructMethod = exist.getPostConstructMethod() != null ? exist.getPostConstructMethod() : definition.getPostConstructMethod();
            if (exist.getCloseMethod() != null && definition.getCloseMethod() != null && exist.getCloseMethod().equals(definition.getCloseMethod()) == false)
            {
                throw new UnsupportedOperationException(StringUtil.format("bean:{}的postConstructMethod在不同的配置中分别存在:{}和{},无法兼容", exist.getCloseMethod(), definition.getCloseMethod()));
            }
            String closeMethod = exist.getCloseMethod() != null ? exist.getCloseMethod() : definition.getCloseMethod();
            if (exist.getLoadByFactoryName() != null && definition.getLoadByFactoryName() != null && exist.getLoadByFactoryName().equals(definition.getLoadByFactoryName()) == false)
            {
                throw new UnsupportedOperationException(StringUtil.format("bean:{}的loadByFactoryName在不同的配置中分别存在:{}和{},无法兼容", exist.getLoadByFactoryName(), definition.getLoadByFactoryName()));
            }
            String loadByFactoryName = exist.getCloseMethod() != null ? exist.getLoadByFactoryName() : definition.getLoadByFactoryName();
            if (exist.getOutterEntity() != null && definition.getOutterEntity() != null && exist.getOutterEntity() != definition.getOutterEntity())
            {
                throw new UnsupportedOperationException(StringUtil.format("bean:{}的outterEntity在不同的配置中分别存在,无法兼容"));
            }
            Object outterEntity = exist.getOutterEntity() != null ? exist.getOutterEntity() : definition.getOutterEntity();
            if (exist.getConstructedBean() != null && definition.getConstructedBean() != null && exist.getConstructedBean() != definition.getConstructedBean())
            {
                throw new UnsupportedOperationException(StringUtil.format("bean:{}的constructedBean在不同的配置中分别存在,无法兼容"));
            }
            Bean constructedBean = exist.getConstructedBean() != null ? exist.getConstructedBean() : definition.getConstructedBean();
            if (exist.getAnnotationEnvironment() != null && definition.getAnnotationEnvironment() != null && exist.getAnnotationEnvironment() != definition.getAnnotationEnvironment())
            {
                throw new UnsupportedOperationException(StringUtil.format("bean:{}的annotationEnvironment在不同的配置中分别存在,无法兼容"));
            }
            Environment annotationEnvironment = exist.getAnnotationEnvironment() != null ? exist.getAnnotationEnvironment() : definition.getAnnotationEnvironment();
            int schema = exist.schema() | definition.schema();
            if (schema == 0)
            {
                System.out.println("sadas");
            }
            Map<String, DIFieldInfo> set1 = new HashMap<String, DIFieldInfo>();
            for (DIFieldInfo each : exist.getDiFieldInfos())
            {
                set1.put(each.getFieldName(), each);
            }
            for (DIFieldInfo each : definition.getDiFieldInfos())
            {
                set1.put(each.getFieldName(), each);
            }
            Map<String, ParamField> set2 = new HashMap<String, ParamField>();
            for (ParamField each : exist.getParamFields())
            {
                set2.put(each.getName(), each);
            }
            for (ParamField each : definition.getParamFields())
            {
                set2.put(each.getName(), each);
            }
            exist.setClassName(className);
            exist.setOriginType(originType);
            exist.setType(type);
            exist.setParams(params);
            exist.setDependencies(dependencies);
            exist.setPostConstructMethod(postConstructMethod);
            exist.setCloseMethod(closeMethod);
            exist.setLoadByFactoryName(loadByFactoryName);
            exist.setOutterEntity(outterEntity);
            exist.setConstructedBean(constructedBean);
            exist.setAnnotationEnvironment(annotationEnvironment);
            exist.setSchema(schema);
            exist.getDiFieldInfos().clear();
            exist.getDiFieldInfos().addAll(set1.values());
            exist.getParamFields().clear();
            exist.getParamFields().addAll(set2.values());
        }
    }
    
    interface Plugin
    {
        void process();
    }
    
    class ResolveClassNamesPlugin implements Plugin
    {
        
        @Override
        public void process()
        {
            for (String each : classNames)
            {
                Class<?> res = null;
                try
                {
                    res = classLoader.loadClass(each);
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException("对应的类不存在", e);
                }
                // 如果本身是一个注解或者没有使用resource注解，则忽略
                if (res.isAnnotation() || EnvironmentUtil.getAnnoUtil().isPresent(Resource.class, res) == false)
                {
                    continue;
                }
                AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
                Resource resource = annotationUtil.getAnnotation(Resource.class, res);
                String beanName = resource.name().equals("") ? res.getName() : resource.name();
                BeanDefinition beanDefinition = beanDefinitions.get(beanName);
                beanDefinition = new BeanDefinition();
                beanDefinition.setBeanName(beanName);
                beanDefinition.enablePrototype(resource.shareable() == false);
                beanDefinition.setOriginType(res);
                beanDefinition.setType(res);
                beanDefinition.setClassName(res.getName());
                beanDefinition.switchDefault();
                if (annotationUtil.isPresent(LoadBy.class, res))
                {
                    LoadBy loadBy = annotationUtil.getAnnotation(LoadBy.class, res);
                    beanDefinition.switchLoadBy();
                    beanDefinition.setLoadByFactoryName(loadBy.factoryBeanName());
                }
                else if (res.isInterface() == false)
                {
                    ;
                }
                else
                {
                    throw new UnSupportException(StringUtil.format("在接口上只有Resource注解是无法实例化bean的.请检查{}", res.getName()));
                }
                if (annotationUtil.isPresent(Configuration.class, res))
                {
                    beanDefinition.enableConfiguration();
                    environment.addConfigClass(res);
                }
                putBeanDefinition(beanDefinition);
            }
        }
        
    }
    
    class ResolveBeanConfigurationAnnoPlugin implements Plugin
    {
        
        @Override
        public void process()
        {
            List<Class<?>> configurations = new ArrayList<Class<?>>();
            for (BeanDefinition each : beanDefinitions.values())
            {
                if (each.isConfiguration())
                {
                    configurations.add(each.getType());
                }
            }
            for (Class<?> each : configurations)
            {
                processBeanDefinition(each);
                processProperties(each);
                processPropertyPaths(each);
                processBeanDefinitions(each);
                processImport(each);
            }
        }
        
        private void processPropertyPaths(Class<?> ckass)
        {
            processAnnoValue(ckass, PropertyPaths.class, //
                    new AnnoValueProcessor<PropertyPaths>() {
                        
                        @Override
                        public void process(PropertyPaths annotation)
                        {
                            resolvePropertyFile(annotation.value());
                        }
                    });
        }
        
        private void processBeanDefinition(Class<?> ckass)
        {
            processAnnoValue(ckass, com.jfireframework.jfire.config.annotation.BeanDefinition.class, //
                    new AnnoValueProcessor<com.jfireframework.jfire.config.annotation.BeanDefinition>() {
                        
                        @Override
                        public void process(com.jfireframework.jfire.config.annotation.BeanDefinition annotation) throws Exception
                        {
                            generateBeanDefinitionFromAnno(annotation);
                        }
                    });
        }
        
        private void processBeanDefinitions(Class<?> ckass)
        {
            processAnnoValue(ckass, BeanDefinitions.class, //
                    new AnnoValueProcessor<BeanDefinitions>() {
                        
                        @Override
                        public void process(BeanDefinitions beanDefinitions) throws ClassNotFoundException
                        {
                            for (com.jfireframework.jfire.config.annotation.BeanDefinition each : beanDefinitions.value())
                            {
                                generateBeanDefinitionFromAnno(each);
                            }
                        }
                    });
            
        }
        
        private void generateBeanDefinitionFromAnno(com.jfireframework.jfire.config.annotation.BeanDefinition anno) throws ClassNotFoundException
        {
            BeanDefinition beanDefinition = JfireConfig.this.beanDefinitions.containsKey(anno.beanName()) ? JfireConfig.this.beanDefinitions.get(anno.beanName()) : new BeanDefinition();
            beanDefinition.setBeanName(anno.beanName());
            beanDefinition.enablePrototype(anno.prototype());
            if (StringUtil.isNotBlank(anno.className()))
            {
                beanDefinition.setType(classLoader.loadClass(anno.className()));
                beanDefinition.setClassName(anno.className());
            }
            if (StringUtil.isNotBlank(anno.postConstructMethod()))
            {
                beanDefinition.setPostConstructMethod(anno.postConstructMethod());
            }
            if (StringUtil.isNotBlank(anno.closeMethod()))
            {
                beanDefinition.setCloseMethod(anno.closeMethod());
            }
            if (anno.dependencies().length != 0)
            {
                Map<String, String> map = new HashMap<String, String>();
                for (String depend : anno.dependencies())
                {
                    int index = depend.indexOf("=");
                    map.put(depend.substring(0, index), depend.substring(index + 1));
                }
                beanDefinition.setDependencies(map);
            }
            if (anno.params().length != 0)
            {
                Map<String, String> map = new HashMap<String, String>();
                for (String param : anno.params())
                {
                    int index = param.indexOf("=");
                    map.put(param.substring(0, index), param.substring(index + 1));
                }
                beanDefinition.setParams(map);
            }
            putBeanDefinition(beanDefinition);
        }
        
        private void processImport(final Class<?> ckass)
        {
            processAnnoValue(ckass, Import.class, new AnnoValueProcessor<Import>() {
                
                @Override
                public void process(Import anno)
                {
                    for (Class<?> each : anno.value())
                    {
                        if (beanDefinitions.containsKey(each.getName()) == false)
                        {
                            registerBeanDefinition(each);
                        }
                        else
                        {
                            BeanDefinition definition = beanDefinitions.get(each.getName());
                            definition.enableConfiguration();
                            environment.addConfigClass(each);
                            if (ImportBeanDefinition.class.isAssignableFrom(definition.getOriginType()))
                            {
                                definition.setAnnotationEnvironment(environment);
                                definition.enableImportBean();
                            }
                        }
                        processBeanDefinition(each);
                        processProperties(each);
                        processPropertyPaths(each);
                        processBeanDefinitions(each);
                        processImport(each);
                    }
                }
            });
        }
        
        private void processProperties(Class<?> ckass)
        {
            processAnnoValue(ckass, com.jfireframework.jfire.config.annotation.Properties.class, //
                    new AnnoValueProcessor<com.jfireframework.jfire.config.annotation.Properties>() {
                        
                        @Override
                        public void process(com.jfireframework.jfire.config.annotation.Properties annotation)
                        {
                            for (String kvPair : annotation.value())
                            {
                                int index = kvPair.indexOf("=");
                                properties.put(kvPair.substring(0, index), kvPair.substring(index + 1));
                            }
                        }
                    });
        }
    }
    
    class ComplementedBeanDefinition implements Plugin
    {
        
        @Override
        public void process()
        {
            AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
            for (BeanDefinition each : beanDefinitions.values())
            {
                try
                {
                    if (each.getType() == null && each.getClassName() != null)
                    {
                        each.setType(classLoader.loadClass(each.getClassName()));
                    }
                    if (each.getOriginType() == null)
                    {
                        each.setOriginType(each.getType());
                    }
                    if (each.getOriginType() != null && JfireInitFinish.class.isAssignableFrom(each.getOriginType()))
                    {
                        each.enableJfireInitFinish();
                    }
                    if (each.getOriginType() != null && annotationUtil.isPresent(Configuration.class, each.getOriginType()))
                    {
                        each.enableConfiguration();
                    }
                }
                catch (Exception e)
                {
                    throw new JustThrowException(e);
                }
            }
        }
        
    }
    
    class FindAnnoedPostAndPreDestoryMethod implements Plugin
    {
        @Override
        public void process()
        {
            AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
            for (BeanDefinition each : beanDefinitions.values())
            {
                Verify.notNull(each.getOriginType(), "bean:{}没有原始类型", each.getBeanName());
                for (Method method : each.getOriginType().getDeclaredMethods())
                {
                    if (annotationUtil.isPresent(PostConstruct.class, method))
                    {
                        each.setPostConstructMethod(method.getName());
                    }
                    if (annotationUtil.isPresent(PreDestroy.class, method))
                    {
                        each.setCloseMethod(method.getName());
                    }
                }
            }
        }
    }
    
    class EnhancePlugin implements Plugin
    {
        
        @Override
        public void process()
        {
            AopUtil aopUtil = new AopUtil(classLoader);
            aopUtil.enhance(beanDefinitions);
        }
    }
    
    class ProcessPlaceHolderPlugin implements Plugin
    {
        
        @Override
        public void process()
        {
            resolvePlaceholderOfProperties();
            resolvePlaceholderOfBeanInfo();
        }
        
        /**
         * 替换Bean配置信息中存在的占位符表达。
         */
        private void resolvePlaceholderOfBeanInfo()
        {
            for (BeanDefinition config : beanDefinitions.values())
            {
                for (Entry<String, String> entry : config.getParams().entrySet())
                {
                    String value = entry.getValue();
                    if (value.startsWith("${"))
                    {
                        entry.setValue(resolveplaceholder(value, properties));
                    }
                }
                for (Entry<String, String> entry : config.getDependencies().entrySet())
                {
                    String value = entry.getValue();
                    if (value.startsWith("${"))
                    {
                        entry.setValue(resolveplaceholder(value, properties));
                    }
                }
            }
        }
        
        /**
         * 解析配置文件properties中的占位符表达式
         */
        private void resolvePlaceholderOfProperties()
        {
            Iterator<Entry<String, String>> it = properties.entrySet().iterator();
            while (it.hasNext())
            {
                Entry<String, String> entry = it.next();
                String value = entry.getValue();
                if (value.startsWith("${"))
                {
                    entry.setValue(resolveplaceholder(value, properties));
                }
            }
        }
        
        /**
         * 解析${a}格式或者${a}||b格式的占位符表达式,并返回表达式的值。 规则<br/>
         * 格式${x}意味着需要返回holderProvider中x的值<br/>
         * 格式${a}||b意味着需要返回holderProvider中的a的值，如果a不存在于holderProvider，则返回默认值b
         * 
         * @return
         */
        private String resolveplaceholder(String placeholder, Map<String, String> holderProvider)
        {
            int end = placeholder.indexOf("}||");
            if (end != -1)
            {
                String name = placeholder.substring(2, end);
                String value = holderProvider.get(name);
                if (value != null)
                {
                    return value;
                }
                else
                {
                    String defaultValue = placeholder.substring(end + 3);
                    return defaultValue;
                }
            }
            else
            {
                String name = placeholder.substring(2, placeholder.length() - 1);
                String value = holderProvider.get(name);
                if (value != null)
                {
                    return value;
                }
                else
                {
                    throw new NullPointerException("属性值中不存在" + name);
                }
            }
        }
    }
    
    class InitDependencyAndParamFieldsPlugin implements Plugin
    {
        
        @Override
        public void process()
        {
            for (BeanDefinition candidate : beanDefinitions.values())
            {
                if (candidate.isDefault())
                {
                    candidate.addDIFieldInfos(FieldFactory.buildDependencyField(candidate, beanDefinitions), true);
                    candidate.addParamFields(FieldFactory.buildParamField(candidate, candidate.getParams(), properties, classLoader), true);
                }
            }
        }
        
    }
    
    class ConstructBeanPlugin implements Plugin
    {
        @Override
        public void process()
        {
            for (BeanDefinition candidate : beanDefinitions.values())
            {
                constructBean(candidate);
            }
            logger.debug("装配bean完毕");
        }
        
        private Bean constructBean(BeanDefinition beanDefinition)
        {
            Bean bean = beanDefinition.getConstructedBean();
            if (bean != null)
            {
                return bean;
            }
            if (beanDefinition.isDefault())
            {
                bean = new DefaultBean(beanDefinition.getType(), beanDefinition.getBeanName(), beanDefinition.isPrototype(), generateDiFields(beanDefinition), beanDefinition.getParamFields().toArray(new ParamField[beanDefinition.getParamFields().size()]));
            }
            else if (beanDefinition.isLoadBy())
            {
                bean = new LoadByBean(beanDefinition.getType(), beanDefinition.getBeanName(), constructBean(beanDefinitions.get(beanDefinition.getLoadByFactoryName())));
            }
            else if (beanDefinition.isOutter())
            {
                bean = new OuterEntityBean(beanDefinition.getBeanName(), beanDefinition.getOutterEntity());
            }
            else if (beanDefinition.isAnnotationConfig())
            {
                try
                {
                    MethodAccessor methodAccessor = ReflectUtil.fastMethod(beanDefinitions.get(beanDefinition.getHostBeanName()).getType().getDeclaredMethod(beanDefinition.getBeanAnnotatedMethod()));
                    bean = new AnnotationConfigBean(constructBean(beanDefinitions.get(beanDefinition.getHostBeanName())), methodAccessor, beanDefinition.getType(), beanDefinition.getBeanName(), beanDefinition.isPrototype());
                }
                catch (Exception e)
                {
                    throw new JustThrowException(e);
                }
            }
            else
            {
                throw new NullPointerException();
            }
            beanDefinition.setConstructedBean(bean);
            if (beanDefinition.getPostConstructMethod() != null)
            {
                try
                {
                    Method method = beanDefinition.getType().getDeclaredMethod(beanDefinition.getPostConstructMethod());
                    ((BaseBean) bean).setPostConstructMethod(ReflectUtil.fastMethod(method));
                }
                catch (Exception e)
                {
                    throw new JustThrowException(e);
                }
            }
            if (beanDefinition.getCloseMethod() != null)
            {
                try
                {
                    Method method = beanDefinition.getType().getDeclaredMethod(beanDefinition.getCloseMethod());
                    ((BaseBean) bean).setPreDestoryMethod(ReflectUtil.fastMethod(method));
                }
                catch (Exception e)
                {
                    throw new JustThrowException(e);
                }
            }
            logger.debug("构建bean:{}完毕", beanDefinition.getBeanName());
            return bean;
        }
        
        private DIField[] generateDiFields(BeanDefinition beanDefinition)
        {
            List<DIField> diFields = new ArrayList<DIField>();
            for (DIFieldInfo diFieldInfo : beanDefinition.getDiFieldInfos())
            {
                switch (diFieldInfo.mode())
                {
                    case DIFieldInfo.DEFAULT:
                    {
                        DIField diField = new DefaultBeanField(diFieldInfo.getField(), diFieldInfo.getBeanDefinition());
                        diFields.add(diField);
                        break;
                    }
                    case DIFieldInfo.LIST:
                    {
                        DIField diField = new ListField(diFieldInfo.getField(), diFieldInfo.getBeanDefinitions());
                        diFields.add(diField);
                        break;
                    }
                    case DIFieldInfo.BEAN_NAME_MAP:
                    {
                        List<String> beanNames = new ArrayList<String>();
                        for (BeanDefinition each : diFieldInfo.getBeanDefinitions())
                        {
                            beanNames.add(each.getBeanName());
                        }
                        DIField diField = new BeanNameMapField(diFieldInfo.getField(), diFieldInfo.getBeanDefinitions(), beanNames.toArray(new String[beanNames.size()]));
                        diFields.add(diField);
                        break;
                    }
                    case DIFieldInfo.METHOD_MAP:
                    {
                        DIField diField = new MethodMapField(diFieldInfo.getField(), diFieldInfo.getBeanDefinitions(), diFieldInfo.getMethod_map_method());
                        diFields.add(diField);
                        break;
                    }
                    case DIFieldInfo.NONE:
                        break;
                    case DIFieldInfo.VALUE_MAP:
                    {
                        DIField diField = new ValueMapField(diFieldInfo.getField(), diFieldInfo.getBeanDefinitions(), diFieldInfo.getValue_map_values());
                        diFields.add(diField);
                        break;
                    }
                    default:
                        break;
                }
            }
            return diFields.toArray(new DIField[diFields.size()]);
        }
    }
    
    class InitSingletonBeanPlugin implements Plugin
    {
        
        @Override
        public void process()
        {
            for (BeanDefinition beanDefinition : beanDefinitions.values())
            {
                if (beanDefinition.isPrototype() == false)
                {
                    beanDefinition.getInstance();
                }
            }
        }
        
    }
    
    class TriggerJfireInitFinishPlugin implements Plugin
    {
        
        @Override
        public void process()
        {
            List<JfireInitFinish> tmp = new LinkedList<JfireInitFinish>();
            for (BeanDefinition beanDefinition : beanDefinitions.values())
            {
                if (beanDefinition.isJfireInitFinish())
                {
                    tmp.add((JfireInitFinish) beanDefinition.getConstructedBean().getInstance());
                }
            }
            Collections.sort(tmp, new AescComparator());
            for (JfireInitFinish each : tmp)
            {
                logger.trace("准备执行方法{}.afterContextInit", each.getClass().getName());
                try
                {
                    each.afterContextInit();
                }
                catch (Exception e)
                {
                    logger.error("执行方法{}.afterContextInit发生异常", each.getClass().getName(), e);
                    throw new JustThrowException(e);
                }
            }
        }
        
    }
    
    class PreparationPlugin implements Plugin
    {
        private final Jfire jfire;
        
        public PreparationPlugin(Jfire jfire)
        {
            this.jfire = jfire;
        }
        
        @Override
        public void process()
        {
            registerSingletonEntity(Jfire.class.getName(), jfire);
            registerSingletonEntity(ClassLoader.class.getName(), classLoader);
            registerSingletonEntity(Environment.class.getName(), environment);
        }
        
    }
    
    class DetectJfireInitFinishInterfacePlugin implements Plugin
    {
        
        @Override
        public void process()
        {
            for (BeanDefinition each : beanDefinitions.values())
            {
                if (JfireInitFinish.class.isAssignableFrom(each.getType()))
                {
                    each.enableJfireInitFinish();
                }
            }
        }
        
    }
    
    class FindAnnoatateBeanDefinitionPlugin implements Plugin
    {
        private BeanDefinition generated(Method method, BeanDefinition host, AnnotationUtil annotationUtil)
        {
            com.jfireframework.jfire.config.annotation.Bean annotatedBean = annotationUtil.getAnnotation(com.jfireframework.jfire.config.annotation.Bean.class, method);
            String beanName = "".equals(annotatedBean.name()) ? method.getName() : annotatedBean.name();
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanName(beanName);
            beanDefinition.setType(method.getReturnType());
            beanDefinition.setOriginType(method.getReturnType());
            beanDefinition.enablePrototype(annotatedBean.prototype());
            beanDefinition.setClassName(beanDefinition.getType().getName());
            beanDefinition.setHostBeanName(host.getBeanName());
            beanDefinition.setBeanAnnotatedMethod(method.getName());
            beanDefinition.switchAnnotationConfig();
            if ("".equals(annotatedBean.destroyMethod()) == false)
            {
                beanDefinition.setCloseMethod(annotatedBean.destroyMethod());
            }
            if (JfireInitFinish.class.isAssignableFrom(method.getReturnType()))
            {
                beanDefinition.enableJfireInitFinish();
            }
            return beanDefinition;
        }
        
        @Override
        public void process()
        {
            AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
            List<BeanDefinition> newDefinitions = new ArrayList<BeanDefinition>();
            for (BeanDefinition each : beanDefinitions.values())
            {
                if (each.isConfiguration())
                {
                    for (Method method : each.getOriginType().getDeclaredMethods())
                    {
                        if (annotationUtil.isPresent(com.jfireframework.jfire.config.annotation.Bean.class, method) //
                                && annotationUtil.isPresent(Conditional.class, method) == false)
                        {
                            newDefinitions.add(generated(method, each, annotationUtil));
                        }
                    }
                }
            }
            for (BeanDefinition each : newDefinitions)
            {
                putBeanDefinition(each);
            }
            newDefinitions.clear();
            Map<Class<? extends Condition>, Condition> conditions = new HashMap<Class<? extends Condition>, Condition>();
            for (BeanDefinition each : beanDefinitions.values())
            {
                if (each.isConfiguration())
                {
                    for (Method method : each.getOriginType().getDeclaredMethods())
                    {
                        if (annotationUtil.isPresent(com.jfireframework.jfire.config.annotation.Bean.class, method) //
                                && annotationUtil.isPresent(Conditional.class, method))
                        {
                            
                            if (match(method, annotationUtil, conditions))
                            {
                                newDefinitions.add(generated(method, each, annotationUtil));
                            }
                        }
                    }
                }
            }
            for (BeanDefinition each : newDefinitions)
            {
                putBeanDefinition(each);
            }
        }
        
        boolean match(Method method, AnnotationUtil annotationUtil, Map<Class<? extends Condition>, Condition> conditions)
        {
            boolean match = true;
            if (annotationUtil.isPresent(Conditional.class, method))
            {
                for (Conditional conditional : annotationUtil.getAnnotations(Conditional.class, method))
                {
                    Condition condition = conditions.get(conditional.value());
                    if (condition == null)
                    {
                        try
                        {
                            condition = conditional.value().newInstance();
                        }
                        catch (Exception e)
                        {
                            throw new JustThrowException(e);
                        }
                        conditions.put(conditional.value(), condition);
                    }
                    if (condition.match(environment, method, annotationUtil))
                    {
                        match = true;
                    }
                    else
                    {
                        match = false;
                    }
                }
            }
            else
            {
                match = true;
            }
            return match;
        }
    }
    
}
