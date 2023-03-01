package com.jfireframework.jfire.core.prepare.support.annotaion;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.bytecode.structure.AnnotationInfo;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.util.BytecodeTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnnotationInstanceImpl implements AnnotationInstance
{
    private ClassLoader              classLoader;
    private Map<String, Object>      attribytes;
    private AnnotationMetadata       annotationMetadata;
    private String                   annotationResourceName;
    private List<AnnotationInstance> presentAnnotations;

    public AnnotationInstanceImpl(ClassLoader classLoader, AnnotationMetadata annotationMetadata, String annotationResourceName)
    {
        this.classLoader = classLoader;
        this.annotationMetadata = annotationMetadata;
        this.annotationResourceName = annotationResourceName;
        if (annotationMetadata.isValid() == false)
        {
            ReflectUtil.throwException(new IllegalArgumentException());
        }
    }

    @Override
    public Map<String, Object> getAttributes()
    {
        if (attribytes == null)
        {
            attribytes = annotationMetadata.getAttributes();
        }
        return attribytes;
    }

    @Override
    public List<AnnotationInstance> getPresentAnnotaions()
    {
        if (presentAnnotations == null)
        {
            List<AnnotationMetadata> annotationsOnClass = BytecodeTool.findAnnotationsOnClass(annotationResourceName, classLoader);
            List<AnnotationInstance> list               = new ArrayList<AnnotationInstance>();
            for (AnnotationMetadata metadata : annotationsOnClass)
            {
                if (metadata.isValid() == false)
                {
                    continue;
                }
                if (metadata.type().equals(BytecodeTool.DocumentedName) || metadata.type().equals(BytecodeTool.RetentionName) || metadata.type().equals(BytecodeTool.TargetName))
                {
                    continue;
                }
                list.add(new AnnotationInstanceImpl(classLoader, metadata, metadata.type()));
            }
        }
        return presentAnnotations;
    }

    @Override
    public boolean annotationPresent(String annotationResourceName)
    {
        if (this.annotationResourceName.equals(annotationResourceName))
        {
            return true;
        }
        for (AnnotationInstance presentAnnotaion : getPresentAnnotaions())
        {
            if (presentAnnotaion.annotationPresent(annotationResourceName))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public AnnotationInstance getAnnotation(String annotationResourceName)
    {
        if (this.annotationResourceName.equals(annotationResourceName))
        {
            return this;
        }
        else
        {
            for (AnnotationInstance presentAnnotaion : getPresentAnnotaions())
            {
                if (presentAnnotaion.getAnnotation(annotationResourceName) != null)
                {
                    return presentAnnotaion.getAnnotation(annotationResourceName);
                }
            }
        }
        return null;
    }

    @Override
    public void getAnnotations(String annotationResourceName, List<AnnotationInstance> list)
    {
        if (this.annotationResourceName.equals(annotationResourceName))
        {
            list.add(this);
        }
        else
        {
            for (AnnotationInstance presentAnnotaion : getPresentAnnotaions())
            {
                presentAnnotaion.getAnnotations(annotationResourceName,list);
            }
        }
    }
}
