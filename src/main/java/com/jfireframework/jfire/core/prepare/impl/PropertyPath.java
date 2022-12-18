package com.jfireframework.jfire.core.prepare.impl;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.jfireframework.baseutil.IniReader.IniFile;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.JfirePreparedNotated;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
@Import(PropertyPath.PropertyPathImporter.class)
public @interface PropertyPath
{
	public String[] value();
	
	@JfirePreparedNotated(order = JfirePreparedConstant.DEFAULT_ORDER)
	public class PropertyPathImporter implements JfirePrepare
	{
		
		@Override
		public void prepare(Environment environment)
		{
			if (environment.isAnnotationPresent(PropertyPath.class))
			{
				for (PropertyPath propertyPath : environment.getAnnotations(PropertyPath.class))
				{
					for (String path : propertyPath.value())
					{
						IniFile iniFile = Utils.processPath(path);
						for (String each : iniFile.keySet())
						{
							environment.putProperty(each, iniFile.getValue(each));
						}
					}
				}
			}
		}
		
	}
	
}
