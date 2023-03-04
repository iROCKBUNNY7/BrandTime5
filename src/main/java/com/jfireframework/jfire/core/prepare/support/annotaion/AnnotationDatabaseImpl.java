package com.jfireframework.jfire.core.prepare.support.annotaion;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.jfire.util.BytecodeTool;

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
    public List<AnnotationInstance> getAnnotaionOnClass(String name)
    {
        List<AnnotationInstance> list = annotationOnClass.get(name);
        if (list == null)
        {
            list = new ArrayList<AnnotationInstance>();
            for (AnnotationMetadata metadata : BytecodeTool.findAnnotationsOnClass(name.replace('.', '/'), classLoader))
            {
                if (metadata.isValid() == false || metadata.type().equals(BytecodeTool.DocumentedName) || metadata.type().equals(BytecodeTool.RetentionName) || metadata.type().equals(BytecodeTool.TargetName))
                {
                    continue;
                }
                list.add(new AnnotationInstanceImpl(classLoader, metadata, metadata.type()));
            }
            annotationOnClass.put(name, list);
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
            for (AnnotationMetadata metadata : BytecodeTool.findAnnotationsOnMethod(method, classLoader))
            {
                if (metadata.isValid() == false || metadata.type().equals(BytecodeTool.DocumentedName) || metadata.type().equals(BytecodeTool.RetentionName) || metadata.type().equals(BytecodeTool.TargetName))
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
            if (annotationInstance.isAnnotationPresent(replace))
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
            if (annotationInstance.isAnnotationPresent(replace))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<AnnotationInstance> getAnnotations(String className, Class<? extends Annotation> ckass)
    {
        String                   replace = ckass.getName().replace('.', '/');
        List<AnnotationInstance> list    = new ArrayList<AnnotationInstance>();
        for (AnnotationInstance annotaionOnClass : getAnnotaionOnClass(className))
        {
            annotaionOnClass.getAnnotations(replace, list);
        }
        return list;
    }

    @Override
    public List<AnnotationInstance> getAnnotations(Method method, Class<? extends Annotation> ckass)
    {
        String                   replace = ckass.getName().replace('.', '/');
        List<AnnotationInstance> list    = new ArrayList<AnnotationInstance>();
        for (AnnotationInstance annotationInstance : getAnnotationOnMethod(method))
        {
            annotationInstance.getAnnotations(replace, list);
        }
        return list;
    }
}
