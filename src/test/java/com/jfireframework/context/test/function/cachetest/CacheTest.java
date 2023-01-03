package com.jfireframework.context.test.function.cachetest;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.context.test.function.base.data.House;
import com.jfireframework.context.test.function.base.data.MutablePerson;
import com.jfireframework.jfire.core.Jfire;
import com.jfireframework.jfire.core.JfireBootstrap;

public class CacheTest
{
	@Test
	public void test()
	{
		JfireBootstrap config = new JfireBootstrap();
		config.register(CacheTarget.class);
		config.register(DemoCache.class);
		config.register(CacheManagerTest.class);
		Jfire jfire = config.start();
		CacheTarget cacheTarget = jfire.getBean(CacheTarget.class);
		House house = cacheTarget.get(1);
		House second = cacheTarget.get(1);
		Assert.assertFalse(house == second);
		house = cacheTarget.get(5);
		second = cacheTarget.get(5);
		Assert.assertTrue(house == second);
		cacheTarget.put(5);
		second = cacheTarget.get(5);
		Assert.assertFalse(house == second);
		house = cacheTarget.get(5);
		second = cacheTarget.get(5);
		Assert.assertTrue(house == second);
		cacheTarget.remove(5);
		second = cacheTarget.get(5);
		Assert.assertFalse(house == second);
		String first = cacheTarget.get();
		String seconde = cacheTarget.get();
		Assert.assertTrue(first == seconde);
		cacheTarget.put();
		seconde = cacheTarget.get();
		Assert.assertFalse(first == seconde);
		first = cacheTarget.get();
		Assert.assertTrue(first == seconde);
	}
	
	/**
	 * 测试覆盖补全
	 */
	@Test
	public void test_2()
	{
		JfireBootstrap config = new JfireBootstrap();
		config.register(CacheTarget.class);
		config.register(DemoCache.class);
		config.register(CacheManagerTest.class);
		Jfire jfire = config.start();
		CacheTarget cacheTarget = jfire.getBean(CacheTarget.class);
		House house = cacheTarget.get2(6);
		House second = cacheTarget.get2(6);
		Assert.assertTrue(house == second);
	}
	
	/**
	 * 测试覆盖补全
	 */
	@Test
	public void test_3()
	{
		JfireBootstrap config = new JfireBootstrap();
		config.register(CacheTarget.class);
		config.register(DemoCache.class);
		config.register(CacheManagerTest.class);
		Jfire jfire = config.start();
		CacheTarget cacheTarget = jfire.getBean(CacheTarget.class);
		House house = cacheTarget.get3(6);
		House second = cacheTarget.get3(6);
		Assert.assertTrue(house == second);
	}
	
	/**
	 * 测试覆盖补全
	 */
	@Test
	public void test_4()
	{
		JfireBootstrap config = new JfireBootstrap();
		config.register(CacheTarget.class);
		config.register(DemoCache.class);
		config.register(CacheManagerTest.class);
		Jfire jfire = config.start();
		CacheTarget cacheTarget = jfire.getBean(CacheTarget.class);
		MutablePerson person = new MutablePerson();
		person.setAge(18);
		House house = cacheTarget.get4(person);
		House second = cacheTarget.get4(person);
		Assert.assertTrue(house == second);
	}
}
