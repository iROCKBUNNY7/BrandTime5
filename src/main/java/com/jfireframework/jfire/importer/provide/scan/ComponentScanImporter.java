package com.jfireframework.jfire.importer.provide.scan;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import com.jfireframework.baseutil.PackageScan;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.config.environment.Environment;
import com.jfireframework.jfire.importer.ImportSelecter;

public class ComponentScanImporter implements ImportSelecter
{
    
    @Override
    public void importSelect(Environment environment)
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
