package com.jfireframework.jfire.core;

import java.util.List;

public interface ApplicationContext
{
    <E> E getBean(Class<E> ckass);

    <E> List<E> getBeans(Class<E> ckass);

    <E> E getBean(String beanName);
}
