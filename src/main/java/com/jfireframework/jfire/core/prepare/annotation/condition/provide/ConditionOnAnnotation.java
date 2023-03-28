package com.jfireframework.jfire.core.prepare.annotation.condition.provide;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.bytecode.annotation.ValuePair;
import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.jfire.core.ApplicationContext;
import com.jfireframework.jfire.core.JfireContext;
import com.jfireframework.jfire.core.prepare.annotation.condition.Conditional;
import com.jfireframework.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfireframework.jfire.core.prepare.annotation.condition.provide.ConditionOnAnnotation.OnAnnotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;

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
        protected boolean handleSelectAnnoType(JfireContext context, AnnotationMetadata metadata, ErrorMessage errorMessage)
        {
            ClassLoader              classLoader              = Thread.currentThread().getContextClassLoader();
            ValuePair[]              value                    = metadata.getAttribyte("value").getArray();
            Collection<Class<?>>     configurationClassSet    = context.getConfigurationClassSet();
            AnnotationContextFactory annotationContextFactory = context.getAnnotationContextFactory();
            for (ValuePair each : value)
            {
                Class<? extends Annotation> aClass;
                try
                {
                    aClass = (Class<? extends Annotation>) classLoader.loadClass(each.getClassName());
                }
                catch (ClassNotFoundException e)
                {
                    errorMessage.addErrorMessage("注解:" + each + "不存在于类路径");
                    return false;
                }
                boolean has = false;
                for (Class<?> configurationClass : configurationClassSet)
                {
                    AnnotationContext annotationContext = annotationContextFactory.get(configurationClass, classLoader);
                    if (annotationContext.isAnnotationPresent(aClass))
                    {
                        has = true;
                        break;
                    }
                }
                if (has == false)
                {
                    errorMessage.addErrorMessage("注解:" + each + "没有标注在配置类上");
                    return false;
                }
            }
            return true;
        }

    }
}
