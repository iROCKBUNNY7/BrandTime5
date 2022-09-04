package com.jfireframework.jfire.aware.provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import com.jfireframework.baseutil.PackageScan;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.aware.JfireAwareBeforeInitialization;
import com.jfireframework.jfire.aware.provider.ComponentScan.ComponentScanImporter;
import com.jfireframework.jfire.config.annotation.Import;
import com.jfireframework.jfire.config.environment.Environment;

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
public @interface ComponentScan
{
    public String[] value();
    
    class ComponentScanImporter implements JfireAwareBeforeInitialization
    {
        
        @Override
        public void awareBeforeInitialization(Environment environment)
        {
            if (environment.isAnnotationPresent(ComponentScan.class))
            {
                List<String> classNames = new LinkedList<String>();
                ComponentScan[] scans = environment.getAnnotations(ComponentScan.class);
                for (ComponentScan componentScan : scans)
                {
                    for (String each : componentScan.value())
                    {
                        for (String var : PackageScan.scan(each))
                        {
                            classNames.add(var);
                        }
                    }
                }
                ClassLoader classLoader = environment.getClassLoader();
                AnnotationUtil annotationUtil = environment.getAnnotationUtil();
                for (String each : classNames)
                {
                    Class<?> ckass = null;
                    try
                    {
                        ckass = classLoader.loadClass(each);
                    }
                    catch (ClassNotFoundException e)
                    {
                        throw new RuntimeException("对应的类不存在", e);
                    }
                    // 如果本身是一个注解或者没有使用resource注解，则忽略
                    if (ckass.isAnnotation() || annotationUtil.isPresent(Resource.class, ckass) == false)
                    {
                        continue;
                    }
                    environment.registerBeanDefinition(ckass);
                }
            }
        }
        
    }
    
}
