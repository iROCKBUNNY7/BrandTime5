package com.jfireframework.jfire.support.jfireprepared;

import com.jfireframework.baseutil.PackageScan;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.kernel.Environment;
import javax.annotation.Resource;
import java.lang.annotation.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 用来填充配置文件中packageNames的值
 *
 * @author linbin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
@Import(ComponentScan.ComponentScanImporter.class)
public @interface ComponentScan
{
	String[] value();
	
	class ComponentScanImporter implements SelectImport
	{
		
		@Override
		public void selectImport(Environment environment)
		{
			if (environment.isAnnotationPresent(ComponentScan.class))
			{
				List<String>    classNames = new LinkedList<String>();
				ComponentScan[] scans      = environment.getAnnotations(ComponentScan.class);
				for (ComponentScan componentScan : scans)
				{
					for (String each : componentScan.value())
					{
						Collections.addAll(classNames, PackageScan.scan(each));
					}
				}
				ClassLoader    classLoader    = environment.getClassLoader();
				AnnotationUtil annotationUtil = environment.getAnnotationUtil();
				for (String each : classNames)
				{
					Class<?> ckass;
					try
					{
						ckass = classLoader.loadClass(each);
					}
					catch (ClassNotFoundException e)
					{
						throw new RuntimeException("对应的类不存在", e);
					}
					// 如果本身是一个注解或者没有使用resource注解，则忽略
					if (ckass.isAnnotation() || annotationUtil.isPresent(Resource.class, ckass) == false)
					{
						continue;
					}
					environment.registerBeanDefinition(ckass);
				}
			}
		}
		
	}
	
}
