package com.jfireframework.context.test.function;

import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.core.Jfire;
import com.jfireframework.jfire.core.JfireBootstrap;
import com.jfireframework.jfire.core.inject.notated.PropertyRead;
import com.jfireframework.jfire.core.prepare.impl.Configuration;
import com.jfireframework.jfire.core.prepare.impl.PropertyPath;

@Resource
public class PropertyPathImporterTest
{
	@PropertyRead
	private int age;
	
	@Configuration
	@PropertyPath("classpath:propertiestest.properties")
	public static class Test1
	{
		
	}
	
	@Configuration
	@PropertyPath("file:src/test/resources/propertiestest.properties")
	public static class Test2
	{
		
	}
	
	/**
	 * 使用classpath路径读取
	 */
	@Test
	public void test()
	{
		JfireBootstrap jfireConfig = new JfireBootstrap(Test1.class);
		jfireConfig.register(PropertyPathImporterTest.class);
		Jfire jfire = jfireConfig.start();
		PropertyPathImporterTest test = jfire.getBean(PropertyPathImporterTest.class);
		Assert.assertEquals(12, test.age);
	}
	
	/**
	 * 使用文件路径读取
	 */
	@Test
	public void test2()
	{
		JfireBootstrap jfireConfig = new JfireBootstrap(Test2.class);
		jfireConfig.register(PropertyPathImporterTest.class);
		Jfire jfire = jfireConfig.start();
		PropertyPathImporterTest test = jfire.getBean(PropertyPathImporterTest.class);
		Assert.assertEquals(12, test.age);
	}
}
