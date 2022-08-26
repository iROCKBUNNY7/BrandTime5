package com.jfireframework.jfire;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.baseutil.order.AescComparator;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.jfire.aop.AopUtil;
import com.jfireframework.jfire.bean.Bean;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.bean.annotation.LazyInitUniltFirstInvoke;
import com.jfireframework.jfire.bean.field.FieldFactory;
import com.jfireframework.jfire.bean.field.dependency.DIField;
import com.jfireframework.jfire.bean.field.dependency.DIFieldInfo;
import com.jfireframework.jfire.bean.field.dependency.impl.BeanNameMapField;
import com.jfireframework.jfire.bean.field.dependency.impl.DefaultBeanField;
import com.jfireframework.jfire.bean.field.dependency.impl.ListField;
import com.jfireframework.jfire.bean.field.dependency.impl.MethodMapField;
import com.jfireframework.jfire.bean.field.param.ParamField;
import com.jfireframework.jfire.bean.impl.BaseBean;
import com.jfireframework.jfire.bean.impl.DefaultBean;
import com.jfireframework.jfire.bean.impl.LoadByBean;
import com.jfireframework.jfire.bean.impl.MethodConfigBean;
import com.jfireframework.jfire.bean.impl.OuterEntityBean;
import com.jfireframework.jfire.bean.load.LoadBy;
import com.jfireframework.jfire.condition.Condition;
import com.jfireframework.jfire.condition.Conditional;
import com.jfireframework.jfire.config.annotation.Configuration;
import com.jfireframework.jfire.config.annotation.Import;
import com.jfireframework.jfire.config.annotation.Order;
import com.jfireframework.jfire.config.environment.Environment;
import com.jfireframework.jfire.extrastore.ExtraInfoStore;
import com.jfireframework.jfire.importer.ImportSelecter;

public class JfireConfig
{
    protected Map<String, BeanDefinition> beanDefinitions = new HashMap<String, BeanDefinition>();
    protected ClassLoader                 classLoader     = JfireConfig.class.getClassLoader();
    protected Map<String, String>         properties      = new HashMap<String, String>();
    protected Environment                 environment     = new Environment(beanDefinitions, properties, this);
    protected ExtraInfoStore              extraInfoStore  = new ExtraInfoStore();
    protected AnnotationUtil              annotationUtil  = environment.getAnnotationUtil();
    protected static final Logger         logger          = LoggerFactory.getLogger(JfireConfig.class);
    
    public JfireConfig()
    {
    }
    
    public JfireConfig(Class<?> configClass)
    {
        if (annotationUtil.isPresent(Configuration.class, configClass))
        {
            environment.addConfigClass(configClass);
        }
        if (annotationUtil.isPresent(Resource.class, configClass))
        {
            registerBeanDefinition(configClass);
        }
    }
    
    public Environment getEnvironment()
    {
        return environment;
    }
    
    public JfireConfig registerBeanDefinition(Class<?>... ckasses)
    {
        for (Class<?> ckass : ckasses)
        {
            mergeBeanDefinition(buildBeanDefinition(ckass));
        }
        return this;
    }
    
    public JfireConfig registerConfiurationBeanDefinition(Class<?>... ckasses)
    {
        for (Class<?> ckass : ckasses)
        {
            BeanDefinition definition = buildBeanDefinition(ckass);
            definition.enableConfiguration();
            environment.addConfigClass(ckass);
            mergeBeanDefinition(definition);
        }
        return this;
    }
    
    public JfireConfig registerBeanDefinition(String resourceName, boolean prototype, Class<?> src)
    {
        mergeBeanDefinition(buildBeanDefinition(resourceName, prototype, src));
        return this;
    }
    
    public JfireConfig registerBeanDefinition(BeanDefinition... definitions)
    {
        for (BeanDefinition definition : definitions)
        {
            mergeBeanDefinition(definition);
        }
        return this;
    }
    
    protected void initJfire(Jfire jfire)
    {
        Plugin[] plugins = new Plugin[] { //
                new PreparationPlugin(jfire), //
                new ResolveImportAnnotationPlugin(), //
                new TriggerImporterPlugin(), //
                new ResolveConfigBeanDefinitionPlugin(), //
                new FindAnnoedPostAndPreDestoryMethod(), //
                new EnhancePlugin(), //
                new InitDependencyAndParamFieldsPlugin(), //
                new ConstructBeanPlugin(), //
                new TriggerJfireInitFinishPlugin()
        };
        for (Plugin plugin : plugins)
        {
            plugin.process();
        }
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
        mergeBeanDefinition(beanDefinition);
        return this;
    }
    
    interface AnnoValueProcessor<T extends Annotation>
    {
        void process(T annotation) throws Exception;
    }
    
    private <T extends Annotation> void processAnnoValue(Class<?> ckass, Class<T> annoType, AnnoValueProcessor<T> processor)
    {
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
    
    private BeanDefinition buildBeanDefinition(String beanName, boolean prototype, Class<?> ckass)
    {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanName(beanName);
        beanDefinition.enablePrototype(prototype);
        beanDefinition.setOriginType(ckass);
        beanDefinition.setType(ckass);
        beanDefinition.setClassName(ckass.getName());
        beanDefinition.switchDefault();
        if (annotationUtil.isPresent(LoadBy.class, ckass))
        {
            LoadBy loadBy = annotationUtil.getAnnotation(LoadBy.class, ckass);
            beanDefinition.switchLoadBy();
            beanDefinition.setLoadByFactoryName(loadBy.factoryBeanName());
        }
        else if (ckass.isInterface() == false)
        {
            ;
        }
        else
        {
            throw new UnSupportException(StringUtil.format("在接口上只有Resource注解是无法实例化bean的.请检查{}", ckass.getName()));
        }
        if (annotationUtil.isPresent(Configuration.class, ckass))
        {
            beanDefinition.enableConfiguration();
            environment.addConfigClass(ckass);
        }
        if (annotationUtil.isPresent(LazyInitUniltFirstInvoke.class, ckass))
        {
            beanDefinition.enableLazyInitUntilFirstInvoke();
        }
        return beanDefinition;
    }
    
    private BeanDefinition buildBeanDefinition(Class<?> ckass)
    {
        Resource resource = annotationUtil.getAnnotation(Resource.class, ckass);
        String beanName;
        boolean prototype;
        if (resource == null)
        {
            prototype = false;
            beanName = ckass.getName();
        }
        else
        {
            prototype = resource.shareable() == false;
            beanName = resource.name().equals("") ? ckass.getName() : resource.name();
        }
        return buildBeanDefinition(beanName, prototype, ckass);
    }
    
    private void mergeBeanDefinition(BeanDefinition definition)
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
            int schema = exist.schema() | definition.schema();
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
    
    class ResolveImportAnnotationPlugin implements Plugin
    {
        
        @Override
        public void process()
        {
            List<BeanDefinition> tmp = new LinkedList<BeanDefinition>();
            for (BeanDefinition each : beanDefinitions.values())
            {
                if (each.isConfiguration())
                {
                    tmp.add(each);
                }
            }
            for (BeanDefinition each : tmp)
            {
                processImport(each.getOriginType());
            }
        }
        
        private void processImport(final Class<?> ckass)
        {
            
            processAnnoValue(ckass, Import.class, new AnnoValueProcessor<Import>() {
                
                @Override
                public void process(Import anno)
                {
                    for (Class<?> each : anno.value())
                    {
                        registerConfiurationBeanDefinition(each);
                        processImport(each);
                    }
                }
            });
        }
        
    }
    
    class FindAnnoedPostAndPreDestoryMethod implements Plugin
    {
        @Override
        public void process()
        {
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
            AopUtil aopUtil = new AopUtil(classLoader, annotationUtil, extraInfoStore);
            aopUtil.enhance(beanDefinitions);
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
                    candidate.addDIFieldInfos(FieldFactory.buildDependencyFields(annotationUtil, candidate, beanDefinitions), true);
                    candidate.addParamFields(FieldFactory.buildParamField(annotationUtil, candidate, candidate.getParams(), properties, classLoader), true);
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
                bean = new DefaultBean(beanDefinition.getType(), beanDefinition.getBeanName(), beanDefinition.isPrototype(), generateDiFields(beanDefinition), beanDefinition.getParamFields().toArray(new ParamField[beanDefinition.getParamFields().size()]), beanDefinition.isLazyInitUntilFirstInvoke());
            }
            else if (beanDefinition.isLoadBy())
            {
                bean = new LoadByBean(beanDefinition.getType(), beanDefinition.isPrototype(), beanDefinition.getBeanName(), constructBean(beanDefinitions.get(beanDefinition.getLoadByFactoryName())), beanDefinition.isLazyInitUntilFirstInvoke());
            }
            else if (beanDefinition.isOutter())
            {
                bean = new OuterEntityBean(beanDefinition.getBeanName(), beanDefinition.getOutterEntity());
            }
            else if (beanDefinition.isMethodBeanConfig())
            {
                try
                {
                    Method method = beanDefinition.getBeanAnnotatedMethod();
                    List<Bean> paramBeans = new ArrayList<Bean>();
                    next: //
                    for (Class<?> type : method.getParameterTypes())
                    {
                        for (BeanDefinition definition : beanDefinitions.values())
                        {
                            if (type.isAssignableFrom(definition.getOriginType()))
                            {
                                paramBeans.add(constructBean(definition));
                                continue next;
                            }
                        }
                    }
                    bean = new MethodConfigBean(constructBean(beanDefinitions.get(beanDefinition.getHostBeanName())), paramBeans, method, beanDefinition.getType(), beanDefinition.getBeanName(), beanDefinition.isPrototype(), beanDefinition.isLazyInitUntilFirstInvoke());
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
                    default:
                        break;
                }
            }
            return diFields.toArray(new DIField[diFields.size()]);
        }
    }
    
    class TriggerJfireInitFinishPlugin implements Plugin
    {
        
        @Override
        public void process()
        {
            List<JfireInitFinish> tmp = new LinkedList<JfireInitFinish>();
            for (BeanDefinition each : beanDefinitions.values())
            {
                if (JfireInitFinish.class.isAssignableFrom(each.getType()))
                {
                    tmp.add((JfireInitFinish) each.getConstructedBean().getInstance());
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
            environment.setClassLoader(classLoader);
            registerSingletonEntity(ExtraInfoStore.class.getName(), extraInfoStore);
            registerSingletonEntity(Jfire.class.getName(), jfire);
            registerSingletonEntity(ClassLoader.class.getName(), classLoader);
            registerSingletonEntity(Environment.class.getName(), environment);
        }
        
    }
    
    class ResolveConfigBeanDefinitionPlugin implements Plugin
    {
        class OrderInfo
        {
            int            order;
            BeanDefinition beanDefinition;
            List<Method>   methods = new ArrayList<Method>();
        }
        
        @Override
        public void process()
        {
            Comparator<Method> comparator = new Comparator<Method>() {
                
                @Override
                public int compare(Method o1, Method o2)
                {
                    int order1 = annotationUtil.isPresent(Order.class, o1) ? annotationUtil.getAnnotation(Order.class, o1).value() : 0;
                    int order2 = annotationUtil.isPresent(Order.class, o2) ? annotationUtil.getAnnotation(Order.class, o2).value() : 0;
                    return order1 - order2;
                }
            };
            List<OrderInfo> orderInfos = new ArrayList<JfireConfig.ResolveConfigBeanDefinitionPlugin.OrderInfo>();
            for (BeanDefinition each : beanDefinitions.values())
            {
                if (each.isConfiguration())
                {
                    int order = annotationUtil.isPresent(Order.class, each.getOriginType()) ? annotationUtil.getAnnotation(Order.class, each.getOriginType()).value() : 0;
                    OrderInfo newInfo = new OrderInfo();
                    newInfo.order = order;
                    newInfo.beanDefinition = each;
                    for (Method method : each.getOriginType().getDeclaredMethods())
                    {
                        if (annotationUtil.isPresent(com.jfireframework.jfire.config.annotation.Bean.class, method))
                        {
                            newInfo.methods.add(method);
                        }
                    }
                    Collections.sort(newInfo.methods, comparator);
                    orderInfos.add(newInfo);
                }
            }
            Collections.sort(orderInfos, new Comparator<OrderInfo>() {
                
                @Override
                public int compare(OrderInfo o1, OrderInfo o2)
                {
                    return o1.order - o2.order;
                }
            });
            Set<Method> handleds = new HashSet<Method>();
            /** 先将没有条件的Bean注解处理完成 **/
            for (OrderInfo each : orderInfos)
            {
                BeanDefinition beanDefinition = each.beanDefinition;
                if (annotationUtil.isPresent(Conditional.class, beanDefinition.getOriginType()))
                {
                    continue;
                }
                for (Method method : each.methods)
                {
                    if (annotationUtil.isPresent(Conditional.class, method))
                    {
                        continue;
                    }
                    mergeBeanDefinition(generated(method, beanDefinition, annotationUtil));
                    handleds.add(method);
                }
            }
            /** 先将没有条件的Bean注解处理完成 **/
            for (OrderInfo each : orderInfos)
            {
                BeanDefinition beanDefinition = each.beanDefinition;
                if (annotationUtil.isPresent(Conditional.class, beanDefinition.getOriginType()) && //
                        match(annotationUtil.getAnnotations(Conditional.class, beanDefinition.getOriginType()), beanDefinition.getOriginType().getAnnotations()) == false)
                {
                    continue;
                }
                for (Method method : each.methods)
                {
                    if (handleds.contains(method))
                    {
                        continue;
                    }
                    if (annotationUtil.isPresent(Conditional.class, method) && //
                            match(annotationUtil.getAnnotations(Conditional.class, method), method.getAnnotations()) == false)
                    {
                        continue;
                    }
                    mergeBeanDefinition(generated(method, beanDefinition, annotationUtil));
                }
            }
        }
        
        boolean match(Conditional[] conditionals, Annotation[] annotations)
        {
            for (Conditional conditional : conditionals)
            {
                for (Class<? extends Condition> type : conditional.value())
                {
                    Condition condition = environment.getCondition(type);
                    if (condition.match(environment.readOnlyEnvironment(), annotations) == false)
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        
        private BeanDefinition generated(Method method, BeanDefinition host, AnnotationUtil annotationUtil)
        {
            com.jfireframework.jfire.config.annotation.Bean annotatedBean = annotationUtil.getAnnotation(com.jfireframework.jfire.config.annotation.Bean.class, method);
            String beanName = "".equals(annotatedBean.name()) ? method.getName() : annotatedBean.name();
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanName(beanName);
            if (annotationUtil.isPresent(LazyInitUniltFirstInvoke.class, method))
            {
                beanDefinition.enableLazyInitUntilFirstInvoke();
            }
            if (method.getGenericReturnType() instanceof Class)
            {
                beanDefinition.setType(method.getReturnType());
                beanDefinition.setOriginType(method.getReturnType());
                beanDefinition.enablePrototype(annotatedBean.prototype());
                beanDefinition.setClassName(beanDefinition.getType().getName());
                beanDefinition.setHostBeanName(host.getBeanName());
                beanDefinition.setBeanAnnotatedMethod(method);
                beanDefinition.switchMethodBeanConfig();
                if ("".equals(annotatedBean.destroyMethod()) == false)
                {
                    beanDefinition.setCloseMethod(annotatedBean.destroyMethod());
                }
                return beanDefinition;
            }
            else
            {
                Type returnType = method.getGenericReturnType();
                if (returnType instanceof ParameterizedType)
                {
                    returnType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
                    beanDefinition.setType((Class<?>) returnType);
                    beanDefinition.setOriginType((Class<?>) returnType);
                    beanDefinition.enablePrototype(annotatedBean.prototype());
                    beanDefinition.setClassName(beanDefinition.getType().getName());
                    beanDefinition.switchDefault();
                    if ("".equals(annotatedBean.destroyMethod()) == false)
                    {
                        beanDefinition.setCloseMethod(annotatedBean.destroyMethod());
                    }
                    return beanDefinition;
                }
                else
                {
                    throw new UnsupportedOperationException(StringUtil.format("不支持配置，请检查方法:{}", method.toGenericString()));
                }
            }
        }
    }
    
    class TriggerImporterPlugin implements Plugin
    {
        class Entry
        {
            private int            order;
            private BeanDefinition beanDefinition;
            
            public Entry(int order, BeanDefinition beanDefinition)
            {
                this.order = order;
                this.beanDefinition = beanDefinition;
            }
            
            public int getOrder()
            {
                return order;
            }
            
            public void setOrder(int order)
            {
                this.order = order;
            }
            
            public BeanDefinition getBeanDefinition()
            {
                return beanDefinition;
            }
            
            public void setBeanDefinition(BeanDefinition beanDefinition)
            {
                this.beanDefinition = beanDefinition;
            }
            
            @Override
            public int hashCode()
            {
                return beanDefinition.hashCode();
            }
            
            @Override
            public boolean equals(Object o)
            {
                if (o == null)
                {
                    return false;
                }
                if (o instanceof Entry)
                {
                    if (beanDefinition == ((Entry) o).beanDefinition)
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    return false;
                }
            }
            
        }
        
        @Override
        public void process()
        {
            ArrayList<Entry> set = new ArrayList<Entry>();
            for (BeanDefinition definition : beanDefinitions.values())
            {
                if (definition.getOriginType() != null)
                {
                    if (ImportSelecter.class.isAssignableFrom(definition.getOriginType()))
                    {
                        Entry entry;
                        if (definition.getOriginType().isAnnotationPresent(Order.class))
                        {
                            Order order = definition.getOriginType().getAnnotation(Order.class);
                            entry = new Entry(order.value(), definition);
                        }
                        else
                        {
                            entry = new Entry(0, definition);
                        }
                        set.add(entry);
                    }
                }
            }
            Collections.sort(set, new Comparator<Entry>() {
                
                @Override
                public int compare(Entry o1, Entry o2)
                {
                    return o1.order - o2.order;
                }
            });
            try
            {
                for (Entry each : set)
                {
                    ((ImportSelecter) each.beanDefinition.getOriginType().newInstance()).importSelect(environment);
                }
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        
    }
    
}
