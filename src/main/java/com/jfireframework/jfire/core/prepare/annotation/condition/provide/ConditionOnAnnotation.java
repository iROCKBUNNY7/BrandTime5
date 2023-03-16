package com.jfireframework.jfire.core.prepare.annotation.condition.provide;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.bytecode.annotation.ValuePair;
import com.jfireframework.jfire.core.EnvironmentTmp.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.annotation.condition.Conditional;
import com.jfireframework.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfireframework.jfire.core.prepare.annotation.condition.provide.ConditionOnAnnotation.OnAnnotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnAnnotation.class)
public @interface ConditionOnAnnotation
{
    Class<? extends Annotation>[] value();

    class OnAnnotation extends BaseCondition
    {

        public OnAnnotation()
        {
            super(ConditionOnAnnotation.class);
        }

        @Override
        protected boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, AnnotationMetadata annotation, ErrorMessage errorMessage)
        {
            ClassLoader classLoader = readOnlyEnvironment.getClassLoader();
            ValuePair[] value       =  annotation.getAttribyte("value").getArray();
            for (ValuePair each : value)
            {
                Class<?> aClass;
                try
                {
                    aClass = classLoader.loadClass(each.getClassName());
                }
                catch (ClassNotFoundException e)
                {
                    errorMessage.addErrorMessage("注解:" + each + "不存在于类路径");
                    return false;
                }
                if (readOnlyEnvironment.isAnnotationPresent((Class<? extends Annotation>) aClass)==false)
                {
                    errorMessage.addErrorMessage("注解:"+each+"没有标注在启动类上");
                    return false;
                }
            }
            return true;
        }
    }
}
