package com.jfireframework.jfire.core.prepare.annotation.condition.provide;

import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.annotation.condition.Conditional;
import com.jfireframework.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfireframework.jfire.core.prepare.support.annotaion.AnnotationInstance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Conditional(ConditionOnClass.ConditonOnClassProcessor.class)
public @interface ConditionOnClass
{
    Class<?>[] value();

    class ConditonOnClassProcessor extends BaseCondition
    {

        public ConditonOnClassProcessor()
        {
            super(ConditionOnClass.class);
        }

        @Override
        protected boolean handleSelectAnnoType(Environment.ReadOnlyEnvironment readOnlyEnvironment, AnnotationInstance annotation, ErrorMessage errorMessage)
        {
            ClassLoader classLoader = readOnlyEnvironment.getClassLoader();
            String[]    value       = (String[]) annotation.getAttributes().get("value");
            for (String each : value)
            {
                Class<?> aClass;
                try
                {
                    aClass = classLoader.loadClass(each);
                }
                catch (ClassNotFoundException e)
                {
                    errorMessage.addErrorMessage("classpath不存在类:" + each + "不存在");
                    return false;
                }
            }
            return false;
        }
    }
}
