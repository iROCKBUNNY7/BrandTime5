package com.jfireframework.jfire.core.prepare.annotation.condition.provide;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.jfire.core.EnvironmentTmp.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.annotation.condition.Condition;
import com.jfireframework.jfire.core.prepare.annotation.condition.ErrorMessage;

import java.util.LinkedList;
import java.util.List;

public abstract class BaseCondition implements Condition
{
    protected final Class<?> selectAnnoType;
    protected       String   selectAnnoResourceName;

    public BaseCondition(Class<?> selectAnnoType)
    {
        this.selectAnnoType = selectAnnoType;
        selectAnnoResourceName = selectAnnoType.getName().replace('.', '/');
    }

    @Override
    public boolean match(ReadOnlyEnvironment readOnlyEnvironment, List<AnnotationMetadata> annotationsOnMember, ErrorMessage errorMessage)
    {
        List<AnnotationMetadata> list = new LinkedList<AnnotationMetadata>();
        for (AnnotationMetadata annotationInstance : annotationsOnMember)
        {
            fill(annotationInstance,selectAnnoResourceName,list);
        }
        for (AnnotationMetadata instance : list)
        {
            if (!handleSelectAnnoType(readOnlyEnvironment, instance, errorMessage))
            {
                return false;
            }
        }
        return true;
    }

    private void fill(AnnotationMetadata annotationMetadata, String typeName, List<AnnotationMetadata> list)
    {
        if (annotationMetadata.isAnnotation(typeName))
        {
            list.add(annotationMetadata);
        }
        for (AnnotationMetadata each : annotationMetadata.getPresentAnnotations())
        {
            if (each.isAnnotation(typeName))
            {
                list.add(each);
            }
        }
    }

    protected abstract boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, AnnotationMetadata annotationMetadata, ErrorMessage errorMessage);
}
