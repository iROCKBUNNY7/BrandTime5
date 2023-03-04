package com.jfireframework.jfire.core.prepare.support.annotaion;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public interface AnnotationDatabase
{
    /**
     * 返回在类上的注解
     *
     * @param name 类的全限定名
     * @return
     */
    List<AnnotationInstance> getAnnotaionOnClass(String name);

    /**
     * 返回在方法上的注解
     *
     * @param method
     * @return
     */
    List<AnnotationInstance> getAnnotationOnMethod(Method method);

    /**
     * 类上是否存在指定的注解（该判断执行循环判断）
     *
     * @param className 类的全限定名
     * @param ckass     注解类
     * @return
     */
    boolean isAnnotationPresentOnClass(String className, Class<? extends Annotation> ckass);

    /**
     * 方法上是否存在指定的注解（该判断执行循环判断）
     *
     * @param ckass 注解类
     * @return
     */
    boolean isAnnotationPresentOnMethod(Method method, Class<? extends Annotation> ckass);

    /**
     * 返回类上的指定注解的实例信息
     *
     * @param className
     * @param ckass
     * @return
     */
    List<AnnotationInstance> getAnnotations(String className, Class<? extends Annotation> ckass);

    /**
     * 返回方法上的制定注解的实例信息
     * @param method
     * @param ckass
     * @return
     */
    List<AnnotationInstance> getAnnotations(Method method, Class<? extends Annotation> ckass);
}
