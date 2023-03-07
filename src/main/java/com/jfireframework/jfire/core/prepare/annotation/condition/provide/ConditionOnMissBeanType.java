package com.jfireframework.jfire.core.prepare.annotation.condition.provide;

import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.annotation.condition.Conditional;
import com.jfireframework.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfireframework.jfire.core.prepare.annotation.condition.provide.ConditionOnMissBeanType.OnMissBeanType;
import com.jfireframework.jfire.core.prepare.support.annotaion.AnnotationInstance;

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
        protected boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, AnnotationInstance annotation, ErrorMessage errorMessage)
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
