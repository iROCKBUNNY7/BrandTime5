package com.jfireframework.context.test.function.cachetest;

import org.junit.Test;
import com.jfireframework.context.test.function.base.data.House;
import com.jfireframework.jfire.core.Jfire;
import com.jfireframework.jfire.core.JfireBootstrap;

public class NewCacheTest
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
		House house = cacheTarget.get(5);
		System.out.println(house);
		house = cacheTarget.get(5);
		System.out.println(house);
		
	}
}
