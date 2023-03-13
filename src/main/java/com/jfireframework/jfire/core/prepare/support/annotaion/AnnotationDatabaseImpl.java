package com.jfireframework.jfire.core.prepare.support.annotaion;

import com.jfireframework.baseutil.bytecode.ClassFile;
import com.jfireframework.baseutil.bytecode.ClassFileParser;
import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.bytecode.util.BytecodeUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationDatabaseImpl implements AnnotationDatabase
{
    private Map<String, List<AnnotationInstance>> annotationOnClass  = new HashMap<String, List<AnnotationInstance>>();
    private Map<Method, List<AnnotationInstance>> annotationOnMethod = new HashMap<Method, List<AnnotationInstance>>();
    private ClassLoader                           classLoader;

    public AnnotationDatabaseImpl(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

    @Override
    public List<AnnotationInstance> getAnnotaionOnClass(String className)
    {
        List<AnnotationInstance> list = annotationOnClass.get(className);
        if (list == null)
        {
            list = new ArrayList<AnnotationInstance>();
            for (AnnotationMetadata metadata : BytecodeUtil.findAnnotationsOnClass(className.replace('.', '/'), classLoader))
            {
                if (metadata.isValid() == false || metadata.type().equals(DocumentedName) || metadata.type().equals(RetentionName) || metadata.type().equals(TargetName))
                {
                    continue;
                }
                list.add(new AnnotationInstanceImpl(classLoader, metadata, metadata.type()));
            }
            annotationOnClass.put(className, list);
        }
        return list;
    }

    @Override
    public List<AnnotationInstance> getAnnotationOnMethod(Method method)
    {
        List<AnnotationInstance> list = annotationOnMethod.get(method);
        if (list == null)
        {
            list = new ArrayList<AnnotationInstance>();
            for (AnnotationMetadata metadata : BytecodeUtil.findAnnotationsOnMethod(method, classLoader))
            {
                if (metadata.isValid() == false || metadata.type().equals(DocumentedName) || metadata.type().equals(RetentionName) || metadata.type().equals(TargetName))
                {
                    continue;
                }
                list.add(new AnnotationInstanceImpl(classLoader, metadata, metadata.type()));
            }
            annotationOnMethod.put(method, list);
        }
        return list;
    }

    @Override
    public boolean isAnnotationPresentOnClass(String className, Class<? extends Annotation> ckass)
    {
        String                   replace          = ckass.getName().replace('.', '/');
        List<AnnotationInstance> annotaionOnClass = getAnnotaionOnClass(className);
        for (AnnotationInstance annotationInstance : annotaionOnClass)
        {
            if (annotationInstance.isAnnotationSelfOrPresent(replace))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAnnotationPresentOnMethod(Method method, Class<? extends Annotation> ckass)
    {
        String replace = ckass.getName().replace('.', '/');
        for (AnnotationInstance annotationInstance : getAnnotationOnMethod(method))
        {
            if (annotationInstance.isAnnotationSelfOrPresent(replace))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<AnnotationInstance> getAnnotations(String className, Class<? extends Annotation> ckass)
    {
        String                   annotationResourceName = ckass.getName().replace('.', '/');
        List<AnnotationInstance> list                   = new ArrayList<AnnotationInstance>();
        for (AnnotationInstance annotaionOnClass : getAnnotaionOnClass(className))
        {
            annotaionOnClass.getAnnotations(annotationResourceName, list);
        }
        return list;
    }

    @Override
    public List<AnnotationInstance> getAnnotations(Method method, Class<? extends Annotation> ckass)
    {
        String                   annotationResourceName = ckass.getName().replace('.', '/');
        List<AnnotationInstance> list                   = new ArrayList<AnnotationInstance>();
        for (AnnotationInstance annotationInstance : getAnnotationOnMethod(method))
        {
            annotationInstance.getAnnotations(annotationResourceName, list);
        }
        return list;
    }

    @Override
    public boolean isAnnotation(String className)
    {
        byte[]    bytecode  = BytecodeUtil.loadBytecode(classLoader, className.replace('.', '/'));
        ClassFile classFile = new ClassFileParser(bytecode).parse();
        return classFile.isAnnotation();
    }
}
