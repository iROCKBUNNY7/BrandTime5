package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.PackageScan;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.bytecode.ClassFile;
import com.jfireframework.baseutil.bytecode.ClassFileParser;
import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.ComponentScan;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfireframework.jfire.core.resolver.BeanInstanceResolver;
import com.jfireframework.jfire.core.resolver.impl.DefaultBeanInstanceResolver;
import com.jfireframework.jfire.core.resolver.impl.LoadByBeanInstanceResolver;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 负责在路径之下扫描具备@Resource和@Configuration注解的类
 */
public class ComponentScanProcessor implements JfirePrepare
{
    private static final Logger logger = LoggerFactory.getLogger(ComponentScanProcessor.class);
    private static final String ConfigurationName = Configuration.class.getName();
    private static final String ResourceName = Resource.class.getName();

    @Override
    public void prepare(Environment environment)
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
            AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
            for (String each : classNames)
            {
                byte[] bytes = loadBytecode(each, classLoader);
                ClassFile classFile = ((new ClassFileParser(bytes))).parse();
                // 如果本身是一个注解或者没有使用resource注解，则忽略
                if (classFile.isAnnotation())
                {
                    logger.trace("traceId:{} 扫描发现类:{},但不符合要求", TRACEID.currentTraceId(), each);
                    continue;
                }
                List<AnnotationMetadata> annotations = classFile.getAnnotations(classLoader);
                if (isResourceAnnotationed(annotations))
                {
                    try
                    {
                        Class<?> ckass = classLoader.loadClass(each);
                        logger.debug("traceId:{} 扫描发现类:{}", TRACEID.currentTraceId(), ckass.getName());
                        Resource resource = annotationUtil.getAnnotation(Resource.class, ckass);
                        String beanName = resource.name().equals("") ? ckass.getName() : resource.name();
                        boolean prototype = resource.shareable() == false;
                        BeanInstanceResolver resolver;
                        if (annotationUtil.isPresent(LoadByBeanInstanceResolver.LoadBy.class, ckass))
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
                    } catch (ClassNotFoundException e)
                    {
                        ReflectUtil.throwException(e);
                    }
                }
                else if (isConfigurationAnnotationed(annotations))
                {
                }
            }
        }
    }

    private boolean isConfigurationAnnotationed(List<AnnotationMetadata> annotations)
    {
        for (AnnotationMetadata annotation : annotations)
        {
            if (annotation.isAnnotation(ConfigurationName) || isConfigurationAnnotationed(annotation.getPresentAnnotations()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isResourceAnnotationed(List<AnnotationMetadata> annotations)
    {
        for (AnnotationMetadata annotation : annotations)
        {
            if (annotation.isAnnotation(ResourceName) || isResourceAnnotationed(annotation.getPresentAnnotations()))
            {
                return true;
            }
        }
        return false;
    }

    private byte[] loadBytecode(String name, ClassLoader loader)
    {
        try
        {
            String s = name.replace('.', '/') + ".class";
            InputStream resourceAsStream = loader.getResourceAsStream(s);
            byte[] content = new byte[resourceAsStream.available()];
            resourceAsStream.read(content);
            resourceAsStream.close();
            return content;
        } catch (IOException e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    @Override
    public int order()
    {
        return JfirePreparedConstant.DEFAULT_ORDER;
    }
}
