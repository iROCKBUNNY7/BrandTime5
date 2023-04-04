package com.jfireframework.jfire.core.prepare.annotation;

import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfireframework.jfire.core.prepare.processor.ComponentScanProcessor;

import java.lang.annotation.*;

/**
 * 用来填充配置文件中packageNames的值
 *
 * @author linbin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
@Import(ComponentScanProcessor.class)
public @interface ComponentScan
{
    String[] value();
}
