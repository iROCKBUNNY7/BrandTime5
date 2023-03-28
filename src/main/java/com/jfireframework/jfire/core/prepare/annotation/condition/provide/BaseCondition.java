package com.jfireframework.jfire.core.prepare.annotation.condition.provide;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.jfire.core.ApplicationContext;
import com.jfireframework.jfire.core.JfireContext;
import com.jfireframework.jfire.core.prepare.annotation.condition.Condition;
import com.jfireframework.jfire.core.prepare.annotation.condition.ErrorMessage;

import java.lang.annotation.Annotation;
import java.util.List;

public abstract class BaseCondition implements Condition
{
    protected final Class<? extends Annotation> selectAnnoType;

    public BaseCondition(Class<? extends Annotation> selectAnnoType)
    {
        this.selectAnnoType = selectAnnoType;
    }

    @Override
    public boolean match(JfireContext context, AnnotationContext annotationContextOnMember, ErrorMessage errorMessage)
    {
        List<AnnotationMetadata> list = annotationContextOnMember.getAnnotationMetadatas(selectAnnoType);
        for (AnnotationMetadata instance : list)
        {
            if (!handleSelectAnnoType(context, instance, errorMessage))
            {
                return false;
            }
        }
        return true;
    }

    protected abstract boolean handleSelectAnnoType(JfireContext context, AnnotationMetadata annotationMetadata, ErrorMessage errorMessage);
}
