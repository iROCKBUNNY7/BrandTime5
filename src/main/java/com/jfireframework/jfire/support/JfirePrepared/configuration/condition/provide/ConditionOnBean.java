package com.jfireframework.jfire.support.JfirePrepared.configuration.condition.provide;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.support.JfirePrepared.configuration.condition.Conditional;
import com.jfireframework.jfire.support.JfirePrepared.configuration.condition.provide.ConditionOnBean.OnBean;

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
				if (miss == false)
				{
					return false;
				}
			}
			return true;
		}
		
	}
}
