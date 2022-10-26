package com.jfireframework.jfire.support.JfirePrepared;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.jfireframework.baseutil.IniReader.IniFile;
import com.jfireframework.jfire.Utils;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.kernel.JfirePrepared;
import com.jfireframework.jfire.kernel.Order;
import com.jfireframework.jfire.support.constant.JfirePreparedConstant;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
@Import(PropertyPath.PropertyPathImporter.class)
public @interface PropertyPath
{
	public String[] value();
	
	@Order(JfirePreparedConstant.DEFAULT_ORDER)
	public class PropertyPathImporter implements JfirePrepared
	{
		@Override
		public void prepared(Environment environment)
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
