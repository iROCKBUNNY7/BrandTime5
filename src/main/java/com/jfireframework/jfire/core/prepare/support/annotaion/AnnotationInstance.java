package com.jfireframework.jfire.core.prepare.support.annotaion;

import com.jfireframework.baseutil.bytecode.structure.AnnotationInfo;

import java.util.List;
import java.util.Map;

public interface AnnotationInstance
{
    /**
     * 返回该注解实例的所有值，以Map的形式。 object的实际类型可能为基本类型的包装类，String，Class，Enum，Map ，以及以上元素的数组
     *
     * @return
     */
    Map<String, Object> getAttributes();

    List<AnnotationInstance> getPresentAnnotaions();

    /**
     * 如果是注解实例或被该注解标注（循环判断），返回true
     *
     * @param annotationResourceName 注解类的资源名称，格式为aa/bb/cc
     * @return
     */
    boolean isAnnotationPresent(String annotationResourceName);

    /**
     * 返回指定注解的实例。该方法会以循环的方式不断查询直到找到
     *
     * @param annotationResourceName 注解类的资源名称，格式为aa/bb/cc
     * @return
     */
    AnnotationInstance getAnnotation(String annotationResourceName);

    /**
     * 将指定注解的实例放入list中
     *
     * @param annotationResourceName 注解类的资源名称，格式为aa/bb/cc
     * @param list
     */
    void getAnnotations(String annotationResourceName, List<AnnotationInstance> list);
}
