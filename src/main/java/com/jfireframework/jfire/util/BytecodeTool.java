package com.jfireframework.jfire.util;

import com.jfireframework.baseutil.bytecode.ClassFile;
import com.jfireframework.baseutil.bytecode.ClassFileParser;
import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.bytecode.structure.Attribute.AttributeInfo;
import com.jfireframework.baseutil.bytecode.structure.Attribute.CodeAttriInfo;
import com.jfireframework.baseutil.bytecode.structure.Attribute.LocalVariableTableAttriInfo;
import com.jfireframework.baseutil.bytecode.structure.MethodInfo;
import com.jfireframework.baseutil.bytecode.util.BytecodeUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;

import javax.annotation.Resource;
import javax.print.attribute.standard.MediaSize;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class BytecodeTool
{
    public static final String ConfigurationName = Configuration.class.getName().replace('.', '/');
    public static final String ResourceName      = Resource.class.getName().replace('.', '/');
    public static final String RetentionName     = Retention.class.getName().replace('.', '/');
    public static final String DocumentedName    = Documented.class.getName().replace('.', '/');
    public static final String TargetName        = Target.class.getName().replace('.', '/');

    /**
     * 在annotations中以及其级联的层次中是否具备名称为name的注解实例信息
     *
     * @param annotations
     * @param name
     * @return
     */
    public static boolean isAnnotationed(List<AnnotationMetadata> annotations, String name)
    {
        for (AnnotationMetadata annotation : annotations)
        {
            if (annotation.isAnnotation(RetentionName) || annotation.isAnnotation(DocumentedName) || annotation.isAnnotation(TargetName))
            {
                continue;
            }
            if (annotation.isAnnotation(name) || isAnnotationed(annotation.getPresentAnnotations(), name))
            {
                return true;
            }
        }
        return false;
    }

    public static byte[] loadBytecode(Class<?> ckass, ClassLoader classLoader)
    {
        String replace = ckass.getName().replace('.', '/');
        return BytecodeUtil.loadBytecode(classLoader, replace);
    }

    /**
     * 返回查找的类的字节码。
     *
     * @param name 类的资源名称，格式为aa/bb/cc
     * @param classLoader
     * @return
     */
    public static byte[] loadBytecode(String name, ClassLoader classLoader)
    {
        return BytecodeUtil.loadBytecode(classLoader, name);
    }

    /**
     * 找到在类上的注解
     *
     * @param name 类的资源名称，格式为aa/bb/cc
     * @param loader
     * @return
     */
    public static List<AnnotationMetadata> findAnnotationsOnClass(String name, ClassLoader loader)
    {
        return BytecodeUtil.findAnnotationOnClass(name, loader);
    }

    /**
     * 找到在方法上的注解
     * @param method
     * @param loader
     * @return
     */
    public static List<AnnotationMetadata> findAnnotationsOnMethod(Method method, ClassLoader loader)
    {
        return BytecodeUtil.findAnnotationOnMethod(method, loader);
    }
}
