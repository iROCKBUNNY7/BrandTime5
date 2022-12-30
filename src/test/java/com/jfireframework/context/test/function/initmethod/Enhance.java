package com.jfireframework.context.test.function.initmethod;

import javax.annotation.Resource;
import com.jfireframework.jfire.core.aop.ProceedPoint;
import com.jfireframework.jfire.core.aop.notated.Before;
import com.jfireframework.jfire.core.aop.notated.EnhanceClass;

@Resource
@EnhanceClass("com.jfire.*.init*")
public class Enhance
{
	@Before("initage()")
	public void initage(ProceedPoint point)
	{
		System.out.println("dads");
	}
}
