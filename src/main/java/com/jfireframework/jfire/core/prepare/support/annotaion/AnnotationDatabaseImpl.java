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
    private Map<String, List<AnnotationMetadata>> annotationOnClass  = new HashMap<String, List<AnnotationMetadata>>();
    private Map<Method, List<AnnotationMetadata>> annotationOnMethod = new HashMap<Method, List<AnnotationMetadata>>();
    private ClassLoader                           classLoader;

    public AnnotationDatabaseImpl(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

    @Override
    public List<AnnotationMetadata> getAnnotaionOnClass(String className)
    {
        List<AnnotationMetadata> list = annotationOnClass.get(className);
        if (list == null)
        {
            list = BytecodeUtil.findAnnotationsOnClass(className, classLoader);
            annotationOnClass.put(className, list);
        }
        return list;
    }

    @Override
    public List<AnnotationMetadata> getAnnotationOnMethod(Method method)
    {
        List<AnnotationMetadata> list = annotationOnMethod.get(method);
        if (list == null)
        {
            list = BytecodeUtil.findAnnotationsOnMethod(method, classLoader);
            annotationOnMethod.put(method, list);
        }
        return list;
    }

    @Override
    public boolean isAnnotationPresentOnClass(String className, Class<? extends Annotation> ckass)
    {
        String                   replace          = ckass.getName().replace('.', '/');
        List<AnnotationMetadata> annotaionOnClass = getAnnotaionOnClass(className);
        for (AnnotationMetadata each : annotaionOnClass)
        {
            if (isAnnotationSelfOrPresentOn(each, replace))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isAnnotationSelfOrPresentOn(AnnotationMetadata annotationMetadata, String typeName)
    {
        if (annotationMetadata.isAnnotation(typeName))
        {
            return true;
        }
        for (AnnotationMetadata each : annotationMetadata.getPresentAnnotations())
        {
            if (isAnnotationSelfOrPresentOn(each, typeName))
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
        for (AnnotationMetadata each : getAnnotationOnMethod(method))
        {
            if (isAnnotationSelfOrPresentOn(each, replace))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<AnnotationMetadata> getAnnotations(String className, Class<? extends Annotation> ckass)
    {
        String                   annotationResourceName = ckass.getName().replace('.', '/');
        List<AnnotationMetadata> list                   = new ArrayList<AnnotationMetadata>();
        for (AnnotationMetadata each : getAnnotaionOnClass(className))
        {
            fill(each, annotationResourceName, list);
        }
        return list;
    }

    private void fill(AnnotationMetadata annotationMetadata, String typeName, List<AnnotationMetadata> list)
    {
        if (annotationMetadata.isAnnotation(typeName))
        {
            list.add(annotationMetadata);
        }
        for (AnnotationMetadata each : annotationMetadata.getPresentAnnotations())
        {
            fill(each, typeName, list);
        }
    }

    @Override
    public List<AnnotationMetadata> getAnnotations(Method method, Class<? extends Annotation> ckass)
    {
        String                   annotationResourceName = ckass.getName().replace('.', '/');
        List<AnnotationMetadata> list                   = new ArrayList<AnnotationMetadata>();
        for (AnnotationMetadata each : getAnnotationOnMethod(method))
        {
            fill(each,annotationResourceName,list);
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
