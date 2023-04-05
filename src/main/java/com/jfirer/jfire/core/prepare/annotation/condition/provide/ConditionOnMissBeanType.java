package com.jfirer.jfire.core.prepare.annotation.condition.provide;

import com.jfirer.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfirer.baseutil.bytecode.annotation.ValuePair;
import com.jfirer.jfire.core.JfireContext;
import com.jfirer.jfire.core.prepare.annotation.condition.Conditional;
import com.jfirer.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfirer.jfire.core.prepare.annotation.condition.provide.ConditionOnMissBeanType.OnMissBeanType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnMissBeanType.class)
public @interface ConditionOnMissBeanType
{
    Class<?>[] value();

    class OnMissBeanType extends BaseCondition
    {

        public OnMissBeanType()
        {
            super(ConditionOnMissBeanType.class);
        }

        @Override
        protected boolean handleSelectAnnoType(JfireContext applicationContext, AnnotationMetadata metadata, ErrorMessage errorMessage)
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ValuePair[] value       = metadata.getAttribyte("value").getArray();
            for (ValuePair each : value)
            {
                Class<?> aClass;
                try
                {
                    aClass = classLoader.loadClass(each.getClassName());
                }
                catch (ClassNotFoundException e)
                {
                    continue;
                }
                if (applicationContext.getBeanDefinition(aClass) != null)
                {
                    errorMessage.addErrorMessage("已经存在类型:" + each + "的Bean");
                    return false;
                }
            }
            return true;
        }
    }
}
