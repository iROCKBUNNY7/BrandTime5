package com.jfireframework.jfire.core.prepare.impl;

import com.jfireframework.baseutil.PackageScan;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.resolver.BeanInstanceResolver;
import com.jfireframework.jfire.core.resolver.impl.DefaultBeanInstanceResolver;
import com.jfireframework.jfire.core.resolver.impl.LoadByBeanInstanceResolver;
import com.jfireframework.jfire.core.resolver.impl.LoadByBeanInstanceResolver.LoadBy;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.annotation.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 用来填充配置文件中packageNames的值
 *
 * @author linbin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
public @interface ComponentScan
{
    String[] value();

    class ComponentScanProcessor implements JfirePrepare
    {
        private static final Logger logger = LoggerFactory.getLogger(ComponentScanProcessor.class);

        @Override
        public void prepare(Environment environment)
        {
            if ( environment.isAnnotationPresent(ComponentScan.class) )
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
                AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
                for (String each : classNames)
                {
                    Class<?> ckass;
                    try
                    {
                        ckass = classLoader.loadClass(each);
                    } catch (ClassNotFoundException e)
                    {
                        throw new RuntimeException("对应的类不存在", e);
                    }
                    // 如果本身是一个注解或者没有使用resource注解，则忽略
                    if ( ckass.isAnnotation() || annotationUtil.isPresent(Resource.class, ckass) == false )
                    {
                        logger.trace("traceId:{} 扫描发现类:{},但不符合要求", TRACEID.currentTraceId(), ckass.getName());
                        continue;
                    }
                    logger.debug("traceId:{} 扫描发现类:{}", TRACEID.currentTraceId(), ckass.getName());
                    Resource resource = annotationUtil.getAnnotation(Resource.class, ckass);
                    String beanName = resource.name().equals("") ? ckass.getName() : resource.name();
                    boolean prototype = resource.shareable() == false;
                    BeanInstanceResolver resolver;
                    if ( annotationUtil.isPresent(LoadBy.class, ckass) )
                    {
                        resolver = new LoadByBeanInstanceResolver(ckass);
                    }
                    else
                    {
                        resolver = new DefaultBeanInstanceResolver(ckass);
                    }
                    BeanDefinition beanDefinition = new BeanDefinition(beanName, ckass, prototype);
                    beanDefinition.setBeanInstanceResolver(resolver);
                    environment.registerBeanDefinition(beanDefinition);
                }
            }

        }

        @Override
        public int order()
        {
            return JfirePreparedConstant.DEFAULT_ORDER;
        }

    }

}
