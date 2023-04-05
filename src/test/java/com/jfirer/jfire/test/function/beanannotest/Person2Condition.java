package com.jfirer.jfire.test.function.beanannotest;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.jfire.core.JfireContext;
import com.jfirer.jfire.core.prepare.annotation.condition.Condition;
import com.jfirer.jfire.core.prepare.annotation.condition.ErrorMessage;

public class Person2Condition implements Condition
{

    @Override
    public boolean match(JfireContext context, AnnotationContext annotationContext, ErrorMessage errorMessage)
    {
        if ("pass".equals(context.getEnv().getProperty("person2")))
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
