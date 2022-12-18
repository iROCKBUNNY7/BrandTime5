package com.jfireframework.jfire.core.prepare.impl;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.JfirePreparedNotated;
import com.jfireframework.jfire.core.resolver.impl.DefaultBeanInstanceResolver;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

/**
 * 用来引入其他的类配置.
 *
 * @author linbin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import
{
	Class<?>[] value();
	
	@JfirePreparedNotated(order = JfirePreparedConstant.IMPORT_ORDER)
	class ImportProcessor implements JfirePrepare
	{
		private static final Logger logger = LoggerFactory.getLogger(ImportProcessor.class);
		
		@Override
		public void prepare(Environment environment)
		{
			AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
			List<Class<?>> tmp = new ArrayList<Class<?>>();
			for (BeanDefinition each : environment.beanDefinitions().values())
			{
				if (annotationUtil.isPresent(Import.class, each.getType()))
				{
					tmp.add(each.getType());
				}
			}
			for (Class<?> each : tmp)
			{
				processImport(each, environment, annotationUtil);
			}
		}
		
		private void processImport(final Class<?> ckass, Environment environment, AnnotationUtil annotationUtil)
		{
			if (annotationUtil.isPresent(Import.class, ckass))
			{
				String traceId = TRACEID.currentTraceId();
				for (Import annotation : annotationUtil.getAnnotations(Import.class, ckass))
				{
					for (Class<?> each : annotation.value())
					{
						logger.debug("traceId:{} 导入类:{}", traceId, each.getName());
						BeanDefinition beanDefinition = new BeanDefinition(each.getName(), each, false);
						beanDefinition.setBeanInstanceResolver(new DefaultBeanInstanceResolver(each));
						environment.registerBeanDefinition(beanDefinition);
						processImport(each, environment, annotationUtil);
					}
				}
			}
		}
		
	}
}
