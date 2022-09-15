package com.jfireframework.jfire.support.JfirePrepared;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import com.jfireframework.baseutil.PackageScan;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.Utils;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.BeanInstanceResolver;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.support.BeanInstanceResolver.LoadByBeanInstanceResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.LoadByBeanInstanceResolver.LoadBy;
import com.jfireframework.jfire.support.BeanInstanceResolver.ReflectBeanInstanceResolver;

/**
 * 用来填充配置文件中packageNames的值
 *
 * @author linbin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
@Import(ComponentScan.ComponentScanImporter.class)
public @interface ComponentScan
{
    String[] value();
    
    class ComponentScanImporter implements SelectImport
    {
        
        @Override
        public void selectImport(Environment environment)
        {
            if (environment.isAnnotationPresent(ComponentScan.class))
            {
                List<String> classNames = new LinkedList<String>();
                ComponentScan[] scans = environment.getAnnotations(ComponentScan.class);
                for (ComponentScan componentScan : scans)
                {
                    for (String each : componentScan.value())
                    {
                        Collections.addAll(classNames, PackageScan.scan(each));
                    }
                }
                ClassLoader classLoader = environment.getClassLoader();
                AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
                for (String each : classNames)
                {
                    Class<?> ckass;
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
                    Resource resource = annotationUtil.getAnnotation(Resource.class, ckass);
                    String beanName = resource.name().equals("") ? ckass.getName() : resource.name();
                    boolean prototype = resource.shareable() == false;
                    BeanInstanceResolver resolver;
                    if (annotationUtil.isPresent(LoadBy.class, ckass))
                    {
                        resolver = new LoadByBeanInstanceResolver(ckass, beanName, prototype);
                    }
                    else
                    {
                        resolver = new ReflectBeanInstanceResolver(beanName, ckass, prototype);
                    }
                    environment.registerBeanDefinition(new BeanDefinition(beanName, ckass, prototype, resolver));
                }
            }
        }
        
    }
    
}
