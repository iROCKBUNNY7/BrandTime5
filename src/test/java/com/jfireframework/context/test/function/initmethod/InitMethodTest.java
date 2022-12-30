package com.jfireframework.context.test.function.initmethod;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.core.JfireBootstrap;
import com.jfireframework.jfire.core.prepare.impl.ComponentScan;
import com.jfireframework.jfire.core.prepare.impl.Configuration;

public class InitMethodTest
{
	@Configuration
	@ComponentScan("com.jfireframework.context.test.function.initmethod")
	public static class InitMethodTestScan
	{
		
	}
	
	@Test
	public void test()
	{
		JfireBootstrap config = new JfireBootstrap(InitMethodTestScan.class);
		Person person = config.start().getBean(Person.class);
		Assert.assertEquals(23, person.getAge());
		Assert.assertEquals("林斌", person.getName());
	}
	
}
