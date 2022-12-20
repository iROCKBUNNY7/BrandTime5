package com.jfireframework.jfire.core.aop.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.jfireframework.jfire.core.aop.impl.CacheAopManager.Cache;

public class ConcurrentMapCache implements Cache
{
	class Element
	{
		private final Object	value;
		private final long		time;
		private final long		ttl;
		
		public Element(Object value, long time, long ttl)
		{
			this.value = value;
			this.time = time;
			this.ttl = ttl;
		}
		
	}
	
	private ConcurrentMap<String, Element> map = new ConcurrentHashMap<String, Element>();
	
	@Override
	public void put(String key, Object value, int timeToLive)
	{
		if (timeToLive == -1)
		{
			Element element = new Element(value, System.currentTimeMillis(), -1);
			map.put(key, element);
		}
		else
		{
			Element element = new Element(value, System.currentTimeMillis(), timeToLive * 1000);
			map.put(key, element);
		}
	}
	
	@Override
	public Object get(String key)
	{
		Element element = map.get(key);
		if (element != null)
		{
			if (element.ttl == -1)
			{
				return element.value;
			}
			else if (System.currentTimeMillis() - element.time > element.ttl)
			{
				map.remove(key, element);
				return null;
			}
			else
			{
				return element.value;
			}
		}
		else
		{
			return null;
		}
	}
	
	@Override
	public void remove(String key)
	{
		map.remove(key);
	}
	
	@Override
	public void clear()
	{
		map.clear();
	}
	
}
