package com.jfireframework.jfire.core.prepare.impl;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.JfirePreparedNotated;
import com.jfireframework.jfire.util.JfirePreparedConstant;

@Retention(RetentionPolicy.RUNTIME)
public @interface AddProperty
{
	String[] value();
	
	@JfirePreparedNotated(order = JfirePreparedConstant.DEFAULT_ORDER)
	class AddPropertyProcessor implements JfirePrepare
	{
		
		@Override
		public void prepare(Environment environment)
		{
			if (environment.isAnnotationPresent(AddProperty.class))
			{
				AddProperty[] addProperties = environment.getAnnotations(AddProperty.class);
				for (AddProperty addProperty : addProperties)
				{
					for (String each : addProperty.value())
					{
						int index = each.indexOf('=');
						if (index != -1)
						{
							String property = each.substring(0, index).trim();
							String vlaue = each.substring(index + 1).trim();
							environment.putProperty(property, vlaue);
						}
					}
				}
			}
		}
		
	}
}
