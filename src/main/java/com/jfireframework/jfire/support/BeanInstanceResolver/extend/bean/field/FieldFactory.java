package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.annotation.field.PropertyRead;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.dependency.DIField;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.dependency.DiFieldImpl;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.dependency.DiResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.dependency.impl.BaseDiResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.dependency.impl.InterfaceDiResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.dependency.impl.ListDiResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.dependency.impl.MapDiResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.UserDefinedResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.ParamField;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.ParamFieldImpl;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.ParamResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.BooleanResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.ByteArrayResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.ClassResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.EnumResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.FileResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.FloatResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.IntArrayResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.IntResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.IntegerResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.LongResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.SetResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.StringArrayResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.StringResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.WBooleanResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.WFloatResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl.WLongResolver;

public class FieldFactory
{
	
	/**
	 * 根据配置信息和field上的注解信息,返回该bean所有的依赖注入的field
	 * 
	 * @return
	 */
	public static List<DIField> buildDependencyFields(AnnotationUtil annotationUtil, Class<?> type, Map<String, BeanDefinition> beanDefinitions)
	{
		List<Field> fields = getAllFields(type);
		List<DIField> list = new LinkedList<DIField>();
		try
		{
			for (Field field : fields)
			{
				if (annotationUtil.isPresent(Resource.class, field))
				{
					list.add(buildDependencyField(annotationUtil, field, beanDefinitions));
				}
			}
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
		return list;
	}
	
	static List<Field> getAllFields(Class<?> ckass)
	{
		List<Field> list = new ArrayList<Field>();
		while (ckass != Object.class)
		{
			for (Field each : ckass.getDeclaredFields())
			{
				list.add(each);
			}
			ckass = ckass.getSuperclass();
		}
		return list;
	}
	
	private static DIField buildDependencyField(AnnotationUtil annotationUtil, Field field, Map<String, BeanDefinition> beanDefinitions) throws NoSuchMethodException, SecurityException
	{
		Class<?> type = field.getType();
		DiResolver resolver;
		if (type == List.class)
		{
			resolver = new ListDiResolver();
		}
		else if (type == Map.class)
		{
			resolver = new MapDiResolver();
		}
		else if (type.isInterface() || Modifier.isAbstract(type.getModifiers()))
		{
			resolver = new InterfaceDiResolver();
		}
		else
		{
			resolver = new BaseDiResolver();
		}
		resolver.initialize(field, annotationUtil, beanDefinitions);
		return new DiFieldImpl(resolver, field);
	}
	
	/**
	 * 根据配置文件，返回该bean所有的条件输入注入的field
	 * 
	 * @return
	 */
	public static List<ParamField> buildParamField(AnnotationUtil annotationUtil, Class<?> type, Map<String, String> properties, ClassLoader classLoader)
	{
		List<Field> fields = getAllFields(type);
		List<ParamField> list = new LinkedList<ParamField>();
		for (Field field : fields)
		{
			if (annotationUtil.isPresent(PropertyRead.class, field))
			{
				PropertyRead propertyRead = annotationUtil.getAnnotation(PropertyRead.class, field);
				String propertyName = propertyRead.value().equals("") ? field.getName() : propertyRead.value();
				String jvmPropertyValue = System.getProperty(propertyName);
				if (StringUtil.isNotBlank(jvmPropertyValue))
				{
					list.add(buildParamField(field, jvmPropertyValue, annotationUtil));
				}
				else if (properties.containsKey(propertyName))
				{
					list.add(buildParamField(field, properties.get(propertyName), annotationUtil));
				}
				else
				{
					continue;
				}
			}
		}
		return list;
	}
	
	private static Map<Class<?>, Class<? extends ParamResolver>> paramResolverTypes = new HashMap<Class<?>, Class<? extends ParamResolver>>();
	static
	{
		paramResolverTypes.put(String.class, StringResolver.class);
		paramResolverTypes.put(int[].class, IntArrayResolver.class);
		paramResolverTypes.put(Integer.class, IntegerResolver.class);
		paramResolverTypes.put(int.class, IntResolver.class);
		paramResolverTypes.put(Long.class, WLongResolver.class);
		paramResolverTypes.put(long.class, LongResolver.class);
		paramResolverTypes.put(Boolean.class, WBooleanResolver.class);
		paramResolverTypes.put(boolean.class, BooleanResolver.class);
		paramResolverTypes.put(float.class, FloatResolver.class);
		paramResolverTypes.put(Float.class, WFloatResolver.class);
		paramResolverTypes.put(String[].class, StringArrayResolver.class);
		paramResolverTypes.put(Set.class, SetResolver.class);
		paramResolverTypes.put(Class.class, ClassResolver.class);
		paramResolverTypes.put(File.class, FileResolver.class);
		paramResolverTypes.put(byte[].class, ByteArrayResolver.class);
		
	}
	
	private static ParamField buildParamField(Field field, String value, AnnotationUtil annotationUtil)
	{
		if (annotationUtil.isPresent(UserDefinedResolver.class, field))
		{
			UserDefinedResolver resolver = annotationUtil.getAnnotation(UserDefinedResolver.class, field);
			try
			{
				ParamResolver instance = resolver.value().newInstance();
				instance.initialize(value, field);
				return new ParamFieldImpl(field, instance);
			}
			catch (Exception e)
			{
				throw new JustThrowException(e);
			}
		}
		else
		{
			Class<?> fieldType = field.getType();
			ParamResolver resolver = null;
			if (Enum.class.isAssignableFrom(fieldType))
			{
				resolver = new EnumResolver();
			}
			else if (paramResolverTypes.containsKey(fieldType))
			{
				Class<? extends ParamResolver> resolverType = paramResolverTypes.get(fieldType);
				try
				{
					resolver = resolverType.newInstance();
				}
				catch (Exception e)
				{
					throw new JustThrowException(e);
				}
			}
			resolver.initialize(value, field);
			return new ParamFieldImpl(field, resolver);
		}
	}
	
}
