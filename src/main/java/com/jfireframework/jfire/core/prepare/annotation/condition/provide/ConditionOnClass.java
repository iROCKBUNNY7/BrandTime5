package com.jfireframework.jfire.core.prepare.annotation.condition.provide;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.bytecode.annotation.ValuePair;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.annotation.condition.Conditional;
import com.jfireframework.jfire.core.prepare.annotation.condition.ErrorMessage;

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
        protected boolean handleSelectAnnoType(Environment.ReadOnlyEnvironment readOnlyEnvironment, AnnotationMetadata annotation, ErrorMessage errorMessage)
        {
            ClassLoader classLoader = readOnlyEnvironment.getClassLoader();
            ValuePair[] value       = annotation.getAttribyte("value").getArray();
            for (ValuePair each : value)
            {
                Class<?> aClass;
                try
                {
                    aClass = classLoader.loadClass(each.getClassName());
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
