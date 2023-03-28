package com.jfireframework.jfire.core;

import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.baseutil.smc.compiler.CompileHelper;
import com.jfireframework.jfire.core.aop.EnhanceManager;
import com.jfireframework.jfire.core.beandescriptor.BeanDescriptor;
import com.jfireframework.jfire.core.prepare.JfirePrepare;

import javax.tools.JavaCompiler;
import java.util.Collection;
import java.util.List;

public interface ApplicationContext
{
    <E> E getBean(Class<E> ckass);

    <E> List<E> getBeans(Class<E> ckass);

    <E> E getBean(String beanName);

    /**
     * 注册一个类，框架会分析导入类的类型：<br/>
     * 1. 如果实现了JfirePrepare接口，则调用registerJfirePrepare接口进行注册,并且返回1。<br/>
     * 2. 如果类上标记为了Configuration注解，则调用registerConfiguration接口进行注册，并且返回2。<br/>
     * 3. 如果类实现了EnhanceManager接口，则调用registerEnhanceManager接口进行注册，并且返回3。<br/>
     * 4. 如果不是抽象类或者接口，则注册为一个Bean。返回4。<br/>
     * <br/>
     * 如果没有任何步骤生效，则返回-1.
     *
     * @param ckass
     */
    int registerClass(Class<?> ckass);
    ////
}
