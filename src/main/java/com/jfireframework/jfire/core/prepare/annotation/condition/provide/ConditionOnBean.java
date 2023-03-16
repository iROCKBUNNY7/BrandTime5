package com.jfireframework.jfire.core.prepare.annotation.condition.provide;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.bytecode.annotation.ValuePair;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.EnvironmentTmp.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.annotation.condition.Conditional;
import com.jfireframework.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfireframework.jfire.core.prepare.annotation.condition.provide.ConditionOnBean.OnBean;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Conditional(OnBean.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionOnBean
{
    Class<?>[] value();

    class OnBean extends BaseCondition
    {

        public OnBean()
        {
            super(ConditionOnBean.class);
        }

        @Override
        protected boolean handleSelectAnnoType(ReadOnlyEnvironment readOnlyEnvironment, AnnotationMetadata annotation, ErrorMessage errorMessage)
        {
            ClassLoader classLoader = readOnlyEnvironment.getClassLoader();
            ValuePair[] beanTypes   =  annotation.getAttribyte("value").getArray();
            for (ValuePair beanType : beanTypes)
            {
                Class<?> aClass;
                try
                {
                    aClass = classLoader.loadClass(beanType.getClassName());
                }
                catch (ClassNotFoundException e)
                {
                    errorMessage.addErrorMessage("类:" + beanType + "不存在classpath，对应的Bean也不会存在");
                    return false;
                }
                boolean miss = true;
                for (BeanDefinition beanDefinition : readOnlyEnvironment.beanDefinitions())
                {
                    if (aClass.isAssignableFrom(beanDefinition.getType()))
                    {
                        miss = false;
                        break;
                    }
                }
                if (miss)
                {
                    errorMessage.addErrorMessage("没有Bean是" + beanType + "类型或者其子类");
                    return false;
                }
            }
            return true;
        }
    }
}
