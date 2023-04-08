package com.jfirer.jfire.core.prepare.annotation.condition.provide;

import com.jfirer.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfirer.baseutil.bytecode.annotation.ValuePair;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.BeanDefinition;
import com.jfirer.jfire.core.prepare.annotation.condition.Conditional;
import com.jfirer.jfire.core.prepare.annotation.condition.ErrorMessage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Conditional(ConditionOnBean.OnBean.class)
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
        protected boolean handleSelectAnnoType(ApplicationContext applicationContext, AnnotationMetadata metadata, ErrorMessage errorMessage)
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ValuePair[] beanTypes   = metadata.getAttribyte("value").getArray();
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
                boolean        miss           = true;
                BeanDefinition beanDefinition = applicationContext.getBeanDefinition(aClass);
                if (beanDefinition == null)
                {
                    errorMessage.addErrorMessage("没有Bean是" + beanType + "类型或者其子类");
                    return false;
                }
            }
            return true;
        }
    }
}
