package com.jfireframework.jfire.support.JfirePrepared;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.kernel.JfirePrepared;
import com.jfireframework.jfire.kernel.Order;
import com.jfireframework.jfire.support.SupportConstant;

@Import(AddProperty.ImportProperty.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface AddProperty
{
	String[] value();
	
	@Order(SupportConstant.DEFAULT_ORDER)
	class ImportProperty implements JfirePrepared
	{
		
		@Override
		public void prepared(Environment environment)
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
