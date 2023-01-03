package com.jfireframework.jfire.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.FieldModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.baseutil.smc.model.MethodModel.AccessLevel;
import com.jfireframework.jfire.core.aop.AopManager;
import com.jfireframework.jfire.core.aop.AopManager.SetHost;
import com.jfireframework.jfire.core.aop.ProceedPoint;
import com.jfireframework.jfire.core.aop.ProceedPointImpl;
import com.jfireframework.jfire.core.inject.InjectHandler;
import com.jfireframework.jfire.core.inject.InjectHandler.CustomInjectHanlder;
import com.jfireframework.jfire.core.inject.impl.DefaultDependencyInjectHandler;
import com.jfireframework.jfire.core.inject.impl.DefaultPropertyInjectHandler;
import com.jfireframework.jfire.core.inject.notated.PropertyRead;
import com.jfireframework.jfire.core.resolver.BeanInstanceResolver;
import com.jfireframework.jfire.exception.EnhanceException;
import com.jfireframework.jfire.exception.IncompleteBeanDefinitionException;
import com.jfireframework.jfire.exception.NewBeanInstanceException;
import com.jfireframework.jfire.exception.PostConstructMethodException;
import com.jfireframework.jfire.util.Utils;

public class BeanDefinition
{
	// 多实例标记
	private static final int								PROTOTYPE			= 1 << 0;
	private static final int								AWARE_CONTEXT_INIT	= 1 << 1;
	// 如果是单例的情况，后续只会使用该实例
	private volatile Object									cachedSingtonInstance;
	/******/
	// 该Bean配置的状态
	private int												state				= 0;
	// 该Bean的类
	private Class<?>										type;
	// 增强后的类，如果没有增强标记，该属性为空
	private Class<?>										enhanceType;
	private Set<AopManager>									aopManagers			= new HashSet<AopManager>();
	private AopManager[]									orderedAopManagers;
	private String											beanName;
	// 标注@PostConstruct的方法
	private Method											postConstructMethod;
	private BeanInstanceResolver							resolver;
	private InjectHandler[]									injectHandlers;
	private Environment										environment;
	public static final ThreadLocal<Map<String, Object>>	tmpBeanInstanceMap	= new ThreadLocal<Map<String, Object>>() {
																					@Override
																					protected java.util.Map<String, Object> initialValue()
																					{
																						return new HashMap<String, Object>();
																					};
																				};
	
	public BeanDefinition(String beanName, Class<?> type, boolean prototype)
	{
		this.beanName = beanName;
		this.type = type;
		setPrototype(prototype);
		if (JfireAwareContextInited.class.isAssignableFrom(type))
		{
			setAwareContextInit();
		}
	}
	
	public void init(Environment environment)
	{
		this.environment = environment;
		initPostConstructMethod();
		initInjectHandlers(environment);
		initEnvironmentForResolver(environment);
		initEnhance(environment);
		initAwareContextInit();
	}
	
	private void initAwareContextInit()
	{
		if (JfireAwareContextInited.class.isAssignableFrom(type))
		{
			setAwareContextInit();
		}
	}
	
	private void initEnhance(Environment environment)
	{
		if (aopManagers.size() == 0)
		{
			orderedAopManagers = new AopManager[0];
			return;
		}
		orderedAopManagers = aopManagers.toArray(new AopManager[aopManagers.size()]);
		Arrays.sort(orderedAopManagers, new Comparator<AopManager>() {
			
			@Override
			public int compare(AopManager o1, AopManager o2)
			{
				return o1.order() - o2.order();
			}
		});
		ClassModel classModel = new ClassModel(type.getSimpleName() + "$AOP$" + AopManager.classNameCounter.getAndIncrement(), type);
		classModel.addImport(ProceedPointImpl.class);
		classModel.addImport(ProceedPoint.class);
		classModel.addImport(Object.class);
		addEnvironmentField(classModel);
		String hostFieldName = "host_" + AopManager.fieldNameCounter.getAndIncrement();
		addHostField(classModel, hostFieldName);
		addSetAopHostMethod(classModel, hostFieldName);
		addInvokeHostPublicMethod(classModel, hostFieldName);
		for (AopManager aopManager : orderedAopManagers)
		{
			aopManager.enhance(classModel, type, environment, hostFieldName);
		}
		JavaStringCompiler compiler = environment.getCompiler();
		try
		{
			enhanceType = compiler.compile(classModel);
			for (AopManager aopManager : orderedAopManagers)
			{
				aopManager.enhanceFinish(type, enhanceType, environment);
			}
		}
		catch (Throwable e)
		{
			throw new EnhanceException(e);
		}
	}
	
	private void addEnvironmentField(ClassModel classModel)
	{
		FieldModel fieldModel = new FieldModel(Environment.ENVIRONMENT_FIELD_NAME, Environment.class);
		classModel.addField(fieldModel);
	}
	
	private void addInvokeHostPublicMethod(ClassModel classModel, String hostFieldName)
	{
		for (Method each : type.getMethods())
		{
			if (Modifier.isFinal(each.getModifiers()))
			{
				continue;
			}
			MethodModel methodModel = new MethodModel(each);
			StringCache cache = new StringCache();
			if (each.getReturnType() != void.class)
			{
				cache.append("return ");
			}
			cache.append(hostFieldName).append(".").append(each.getName()).append("(");
			for (int i = 0; i < methodModel.getParamterTypes().length; i++)
			{
				cache.append("$").append(i).appendComma();
			}
			if (cache.isCommaLast())
			{
				cache.deleteLast();
			}
			cache.append(");");
			methodModel.setBody(cache.toString());
			classModel.putMethodModel(methodModel);
		}
	}
	
	private void addSetAopHostMethod(ClassModel compilerModel, String hostFieldName)
	{
		MethodModel setAopHost = new MethodModel();
		setAopHost.setAccessLevel(AccessLevel.PUBLIC);
		setAopHost.setMethodName("setAopHost");
		setAopHost.setParamterTypes(Object.class, Environment.class);
		setAopHost.setReturnType(void.class);
		setAopHost.setBody(hostFieldName + " = (" + SmcHelper.getTypeName(type) + ")$0;\r\n" + Environment.ENVIRONMENT_FIELD_NAME + "=$1;\r\n");
		compilerModel.putMethodModel(setAopHost);
	}
	
	private void addHostField(ClassModel compilerModel, String hostFieldName)
	{
		FieldModel hostField = new FieldModel(hostFieldName, type);
		compilerModel.addField(hostField);
		compilerModel.addInterface(SetHost.class);
	}
	
	/**
	 * 初始化bean实例的初始化方法
	 */
	private void initPostConstructMethod()
	{
		if (type.isInterface() == false)
		{
			Class<?> type = this.type;
			boolean find = false;
			while (type != Object.class)
			{
				for (Method each : type.getDeclaredMethods())
				{
					if (each.getParameterTypes().length == 0)
					{
						if (Utils.ANNOTATION_UTIL.isPresent(PostConstruct.class, each))
						{
							postConstructMethod = each;
							postConstructMethod.setAccessible(true);
							find = true;
							break;
						}
					}
				}
				if (find)
				{
					break;
				}
				else
				{
					type = type.getSuperclass();
				}
			}
		}
	}
	
	/**
	 * 初始化属性注入处理器
	 * 
	 * @param environment
	 */
	private void initInjectHandlers(Environment environment)
	{
		if (type.isInterface() == false)
		{
			List<InjectHandler> list = new LinkedList<InjectHandler>();
			AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
			for (Field each : ReflectUtil.getAllFields(type))
			{
				if (annotationUtil.isPresent(CustomInjectHanlder.class, each))
				{
					CustomInjectHanlder customDiHanlder = annotationUtil.getAnnotation(CustomInjectHanlder.class, each);
					try
					{
						InjectHandler newInstance = customDiHanlder.value().newInstance();
						newInstance.init(each, environment);
						list.add(newInstance);
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
				}
				else if (annotationUtil.isPresent(Resource.class, each))
				{
					DefaultDependencyInjectHandler injectHandler = new DefaultDependencyInjectHandler();
					injectHandler.init(each, environment);
					list.add(injectHandler);
				}
				else if (annotationUtil.isPresent(PropertyRead.class, each))
				{
					DefaultPropertyInjectHandler injectHandler = new DefaultPropertyInjectHandler();
					injectHandler.init(each, environment);
					list.add(injectHandler);
				}
			}
			injectHandlers = list.toArray(new InjectHandler[list.size()]);
		}
		else
		{
			injectHandlers = new InjectHandler[0];
		}
	}
	
	private void initEnvironmentForResolver(Environment environment)
	{
		resolver.init(environment);
	}
	
	public BeanDefinition setBeanName(String beanName)
	{
		this.beanName = beanName;
		return this;
	}
	
	public BeanDefinition setType(Class<?> type)
	{
		this.type = type;
		return this;
	}
	
	public BeanDefinition setBeanInstanceResolver(BeanInstanceResolver resolver)
	{
		this.resolver = resolver;
		return this;
	}
	
	/**
	 * 返回Bean的实例
	 * 
	 * @return
	 */
	public Object getBeanInstance()
	{
		if (isPropertype())
		{
			return buildInstance();
		}
		else if (cachedSingtonInstance != null)
		{
			return cachedSingtonInstance;
		}
		else
		{
			synchronized (this)
			{
				if (cachedSingtonInstance != null)
				{
					return cachedSingtonInstance;
				}
				cachedSingtonInstance = buildInstance();
				return cachedSingtonInstance;
			}
		}
	}
	
	private Object buildInstance()
	{
		Map<String, Object> map = tmpBeanInstanceMap.get();
		boolean cleanMark = map.isEmpty();
		Object instance = map.get(beanName);
		if (instance != null)
		{
			return instance;
		}
		if (orderedAopManagers.length != 0)
		{
			try
			{
				SetHost newInstance = (SetHost) enhanceType.newInstance();
				newInstance.setAopHost(resolver.buildInstance(), environment);
				instance = newInstance;
				map.put(beanName, instance);
				for (AopManager each : orderedAopManagers)
				{
					each.fillBean(instance, type);
				}
			}
			catch (Throwable e)
			{
				throw new NewBeanInstanceException(e);
			}
		}
		if (instance == null)
		{
			instance = resolver.buildInstance();
			map.put(beanName, instance);
		}
		if (injectHandlers.length != 0)
		{
			for (InjectHandler each : injectHandlers)
			{
				each.inject(instance);
			}
		}
		if (postConstructMethod != null)
		{
			try
			{
				postConstructMethod.invoke(instance);
			}
			catch (Exception e)
			{
				throw new PostConstructMethodException(e);
			}
		}
		if (cleanMark)
		{
			map.clear();
		}
		return instance;
	}
	
	public void setPrototype(boolean flag)
	{
		state = flag ? state | PROTOTYPE : state & (~PROTOTYPE);
	}
	
	private void setAwareContextInit()
	{
		state |= AWARE_CONTEXT_INIT;
	}
	
	private boolean isPropertype()
	{
		return (state & PROTOTYPE) != 0;
	}
	
	public boolean isAwareContextInit()
	{
		return (state & AWARE_CONTEXT_INIT) != 0;
	}
	
	public String getBeanName()
	{
		return beanName;
	}
	
	public Class<?> getType()
	{
		return type;
	}
	
	public void addAopManager(AopManager aopManager)
	{
		aopManagers.add(aopManager);
	}
	
	public void check()
	{
		if (beanName == null)
		{
			throw new IncompleteBeanDefinitionException("beanName没有赋值");
		}
		if (resolver == null)
		{
			throw new IncompleteBeanDefinitionException("BeanInstanceResolver没有赋值");
		}
		if (type == null)
		{
			throw new IncompleteBeanDefinitionException("类型没有赋值");
		}
	}
}
