package com.jfireframework.jfire.core.prepare.annotation.condition.provide;

import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.annotation.condition.Condition;
import com.jfireframework.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfireframework.jfire.core.prepare.support.annotaion.AnnotationInstance;

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
    public boolean match(ReadOnlyEnvironment readOnlyEnvironment, List<AnnotationInstance> annotationsOnMember, ErrorMessage errorMessage)
    {
        List<AnnotationInstance> list = new LinkedList<AnnotationInstance>();
        for (AnnotationInstance annotationInstance : annotationsOnMember)
        {
            annotationInstance.getAnnotations(selectAnnoResourceName,list);
        }
        for (AnnotationInstance instance : list)
        {
            if (!handleSelectAnnoType(readOnlyEnvironment, instance,errorMessage ))
            {
                return false;
            }
        }
        return true;
    }

    protected abstract boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, AnnotationInstance annotation, ErrorMessage errorMessage);
}
