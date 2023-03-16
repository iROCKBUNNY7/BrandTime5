package com.jfireframework.context.test.function.beanannotest;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.jfire.core.EnvironmentTmp.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.annotation.condition.Condition;
import com.jfireframework.jfire.core.prepare.annotation.condition.ErrorMessage;

import java.util.List;

public class Person2Condition implements Condition
{

    @Override
    public boolean match(ReadOnlyEnvironment readOnlyEnvironment, List<AnnotationMetadata> annotationsOnMember, ErrorMessage errorMessage)
    {
        if ("pass".equals(readOnlyEnvironment.getProperty("person2")))
        {
            return true;
        }
        else
        {
            errorMessage.addErrorMessage("环境中不包含属性person2");
            return false;
        }
    }
}
