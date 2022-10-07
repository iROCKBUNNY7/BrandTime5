package com.jfireframework.context.test.function;

import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.kernel.Jfire;
import com.jfireframework.jfire.kernel.JfirePrepared;
import com.jfireframework.jfire.kernel.Order;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.annotation.field.PropertyRead;
import com.jfireframework.jfire.support.JfirePrepared.Import;
import com.jfireframework.jfire.support.JfirePrepared.configuration.Configuration;

public class ImportOrderTest
{
	/**
	 * 测试Order注解在ImportSelecter上是否生效
	 */
	@Test
	public void test()
	{
		JfireConfig jfireConfig = new JfireConfig();
		jfireConfig.registerBeanDefinition(Order1.class);
		Jfire jfire = jfireConfig.build();
		Order1 order1 = jfire.getBean(Order1.class);
		Assert.assertEquals("1,2", order1.getResult());
	}
	
	@Resource
	@Configuration
	@Import({ Import2.class, Import1.class })
	public static class Order1
	{
		@PropertyRead("result")
		private String result;
		
		public String getResult()
		{
			return result;
		}
		
		public void setResult(String result)
		{
			this.result = result;
		}
		
	}
	
	@Order(1)
	public static class Import1 implements JfirePrepared
	{
		
		@Override
		public void prepared(Environment environment)
		{
			String result = environment.getProperty("result");
			if (result == null)
			{
				result = "1";
			}
			else
			{
				result = result + ",1";
			}
			environment.putProperty("result", result);
		}
		
	}
	
	@Order(2)
	public static class Import2 implements JfirePrepared
	{
		
		@Override
		public void prepared(Environment environment)
		{
			String result = environment.getProperty("result");
			if (result == null)
			{
				result = "2";
			}
			else
			{
				result = result + ",2";
			}
			environment.putProperty("result", result);
		}
		
	}
}
