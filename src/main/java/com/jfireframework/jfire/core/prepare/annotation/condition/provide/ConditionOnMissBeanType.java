package com.jfireframework.jfire.core.prepare.annotation.condition.provide;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.bytecode.annotation.ValuePair;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.annotation.condition.Conditional;
import com.jfireframework.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfireframework.jfire.core.prepare.annotation.condition.provide.ConditionOnMissBeanType.OnMissBeanType;

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
        protected boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, AnnotationMetadata annotation, ErrorMessage errorMessage)
        {
            ClassLoader classLoader = readOnlyEnvironment.getClassLoader();
            ValuePair[] value       =  annotation.getAttributes().get("value").getArray();
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
                boolean match = false;
                for (BeanDefinition beanDefinition : readOnlyEnvironment.beanDefinitions())
                {
                    if (aClass.isAssignableFrom(beanDefinition.getType()))
                    {
                        match = true;
                        break;
                    }
                }
                if (match)
                {
                    errorMessage.addErrorMessage("已经存在类型:"+each+"的Bean");
                    return false;
                }
            }
            return true;
        }
    }
}
