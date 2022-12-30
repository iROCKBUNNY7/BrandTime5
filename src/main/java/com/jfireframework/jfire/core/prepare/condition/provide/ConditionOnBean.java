package com.jfireframework.jfire.core.prepare.condition.provide;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.condition.Conditional;
import com.jfireframework.jfire.core.prepare.condition.provide.ConditionOnBean.OnBean;

@Conditional(OnBean.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionOnBean
{
	public Class<?>[] value();
	
	public static class OnBean extends BaseCondition<ConditionOnBean>
	{
		
		public OnBean()
		{
			super(ConditionOnBean.class);
		}
		
		@Override
		protected boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, ConditionOnBean annotation)
		{
			Class<?>[] beanTypes = annotation.value();
			for (Class<?> each : beanTypes)
			{
				boolean miss = true;
				for (BeanDefinition beanDefinition : readOnlyEnvironment.beanDefinitions())
				{
					if (each.isAssignableFrom(beanDefinition.getType()))
					{
						miss = false;
						break;
					}
				}
				if (miss)
				{
					return false;
				}
			}
			return true;
		}
		
	}
}
