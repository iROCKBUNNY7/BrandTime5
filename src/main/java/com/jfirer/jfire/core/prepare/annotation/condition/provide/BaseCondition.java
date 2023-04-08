package com.jfirer.jfire.core.prepare.annotation.condition.provide;

import com.jfirer.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.condition.Condition;
import com.jfirer.jfire.core.prepare.annotation.condition.ErrorMessage;

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
    public boolean match(ApplicationContext context, AnnotationContext annotationContextOnMember, ErrorMessage errorMessage)
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

    protected abstract boolean handleSelectAnnoType(ApplicationContext context, AnnotationMetadata annotationMetadata, ErrorMessage errorMessage);
}
