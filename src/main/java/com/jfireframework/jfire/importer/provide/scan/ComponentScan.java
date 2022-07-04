package com.jfireframework.jfire.importer.provide.scan;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.jfireframework.jfire.config.annotation.Configuration;
import com.jfireframework.jfire.config.annotation.Import;

/**
 * 用来填充配置文件中packageNames的值
 * 
 * @author linbin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
@Import(ComponentScanImporter.class)
@Configuration
public @interface ComponentScan
{
    public String[] value();
}
