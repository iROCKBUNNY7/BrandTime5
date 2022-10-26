package com.jfireframework.jfire.support.JfirePrepared.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.BeanInstanceResolver;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.kernel.JfirePrepared;
import com.jfireframework.jfire.kernel.Order;
import com.jfireframework.jfire.support.BeanInstanceResolver.MethodBeanInstanceResolver;
import com.jfireframework.jfire.support.JfirePrepared.configuration.condition.Condition;
import com.jfireframework.jfire.support.JfirePrepared.configuration.condition.Conditional;
import com.jfireframework.jfire.support.constant.JfirePreparedConstant;

@Order(JfirePreparedConstant.CONFIGURATION_ORDER)
public class ProcessConfiguration implements JfirePrepared
{
	class ConfigurationOrder
	{
		int				order;
		Class<?>		type;
		List<Method>	methods	= new ArrayList<Method>();;
	}
	
	private Map<Class<? extends Condition>, Condition>	conditionImplStore	= new HashMap<Class<? extends Condition>, Condition>();
	private AnnotationUtil								annotationUtil		= new AnnotationUtil();
	private Comparator<Method>							comparator			= new Comparator<Method>() {
																				
																				@Override
																				public int compare(Method o1, Method o2)
																				{
																					int order1 = annotationUtil.isPresent(Order.class, o1) ? annotationUtil.getAnnotation(Order.class, o1).value() : 0;
																					int order2 = annotationUtil.isPresent(Order.class, o2) ? annotationUtil.getAnnotation(Order.class, o2).value() : 0;
																					return order1 > order2 ? 1 : order1 == order2 ? 0 : -1;
																				}
																			};
	
	@Override
	public void prepared(Environment environment)
	{
		List<ConfigurationOrder> configurationOrders = findConfigurationBeanDefinition(environment);
		processNoConditionMethod(environment, configurationOrders);
		processConditionMethod(environment, configurationOrders);
	}
	
	private void processConditionMethod(Environment environment, List<ConfigurationOrder> configurationOrders)
	{
		for (ConfigurationOrder each : configurationOrders)
		{
			Class<?> type = each.type;
			if (annotationUtil.isPresent(Conditional.class, type) && //
			        match(annotationUtil.getAnnotations(Conditional.class, type), type.getAnnotations(), environment) == false)
			{
				continue;
			}
			for (Method method : each.methods)
			{
				if (annotationUtil.isPresent(Conditional.class, method) && //
				        match(annotationUtil.getAnnotations(Conditional.class, method), method.getAnnotations(), environment) == false)
				{
					continue;
				}
				environment.registerBeanDefinition(generated(method, type, annotationUtil));
			}
		}
	}
	
	/*
	 * 处理没有条件相关的方法。 没有条件相关指的是该方法所在的bean没有被@Conditional注解，该方法没有被@Conditional注解.
	 * 处理完毕后，这些方法会从ConfigurationOrder.methods中被删除
	 */
	private void processNoConditionMethod(Environment environment, List<ConfigurationOrder> configurationOrders)
	{
		for (ConfigurationOrder each : configurationOrders)
		{
			Class<?> type = each.type;
			if (annotationUtil.isPresent(Conditional.class, type))
			{
				continue;
			}
			List<Method> needDeletes = new ArrayList<Method>();
			for (Method method : each.methods)
			{
				if (annotationUtil.isPresent(Conditional.class, method))
				{
					continue;
				}
				environment.registerBeanDefinition(generated(method, type, annotationUtil));
				needDeletes.add(method);
			}
			each.methods.removeAll(needDeletes);
		}
	}
	
	private List<ConfigurationOrder> findConfigurationBeanDefinition(Environment environment)
	{
		List<ConfigurationOrder> configurationOrders = new ArrayList<ProcessConfiguration.ConfigurationOrder>();
		for (BeanDefinition each : environment.getBeanDefinitions().values())
		{
			if (annotationUtil.isPresent(Configuration.class, each.getType()))
			{
				int order = annotationUtil.isPresent(Order.class, each.getType()) ? annotationUtil.getAnnotation(Order.class, each.getType()).value() : 0;
				ConfigurationOrder configurationOrder = new ConfigurationOrder();
				configurationOrder.type = each.getType();
				configurationOrder.order = order;
				for (Method method : each.getType().getDeclaredMethods())
				{
					if (annotationUtil.isPresent(Bean.class, method))
					{
						configurationOrder.methods.add(method);
					}
				}
				Collections.sort(configurationOrder.methods, comparator);
				configurationOrders.add(configurationOrder);
			}
		}
		Collections.sort(configurationOrders, new Comparator<ConfigurationOrder>() {
			
			@Override
			public int compare(ConfigurationOrder o1, ConfigurationOrder o2)
			{
				return o1.order - o2.order;
			}
		});
		return configurationOrders;
	}
	
	boolean match(Conditional[] conditionals, Annotation[] annotations, Environment environment)
	{
		for (Conditional conditional : conditionals)
		{
			for (Class<? extends Condition> type : conditional.value())
			{
				Condition condition = getCondition(type);
				if (condition.match(environment.readOnlyEnvironment(), annotations) == false)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	Condition getCondition(Class<? extends Condition> ckass)
	{
		Condition instance = conditionImplStore.get(ckass);
		if (instance == null)
		{
			try
			{
				instance = ckass.newInstance();
				conditionImplStore.put(ckass, instance);
			}
			catch (Exception e)
			{
				throw new JustThrowException(e);
			}
		}
		return instance;
	}
	
	private BeanDefinition generated(Method method, Class<?> type, AnnotationUtil annotationUtil)
	{
		BeanInstanceResolver resolver = new MethodBeanInstanceResolver(method);
		BeanDefinition beanDefinition = new BeanDefinition(method.getReturnType(), resolver);
		return beanDefinition;
	}
}
