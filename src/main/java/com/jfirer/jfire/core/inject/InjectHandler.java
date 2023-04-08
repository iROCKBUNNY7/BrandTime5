package com.jfirer.jfire.core.inject;

import com.jfirer.jfire.core.ApplicationContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * 注入处理器。可能注入的是参数，也可能是依赖
 *
 * @author linbin
 */
public interface InjectHandler
{
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomInjectHanlder
    {
        Class<InjectHandler> value();
    }

    void init(Field field, ApplicationContext context);

    void inject(Object instance);
}
