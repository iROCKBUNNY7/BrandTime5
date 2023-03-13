package com.jfireframework.jfire.core.prepare.support.annotaion;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;

public interface AnnotationDatabase
{

    /**
     * 返回在类上的注解
     *
     * @param className 类的全限定名
     * @return
     */
    List<AnnotationMetadata> getAnnotaionOnClass(String className);

    /**
     * 返回在方法上的注解
     *
     * @param method
     * @return
     */
    List<AnnotationMetadata> getAnnotationOnMethod(Method method);

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
     * @param className 类的全限定名
     * @param ckass
     * @return
     */
    List<AnnotationMetadata> getAnnotations(String className, Class<? extends Annotation> ckass);

    /**
     * 返回方法上的制定注解的实例信息
     *
     * @param method
     * @param ckass
     * @return
     */
    List<AnnotationMetadata> getAnnotations(Method method, Class<? extends Annotation> ckass);

    /**
     * 判断一个类是否是注解
     *
     * @param className 类的全限定名
     * @return
     */
    boolean isAnnotation(String className);
}
