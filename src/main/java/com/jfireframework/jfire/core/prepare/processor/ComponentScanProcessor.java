package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.PackageScan;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.JfireContext;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.ComponentScan;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfireframework.jfire.core.resolver.BeanInstanceResolver;
import com.jfireframework.jfire.core.resolver.impl.DefaultBeanInstanceResolver;
import com.jfireframework.jfire.core.resolver.impl.LoadByBeanInstanceResolver;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 负责在路径之下扫描具备@Resource和@Configuration注解的类
 */
public class ComponentScanProcessor implements JfirePrepare
{
    private static final Logger logger = LoggerFactory.getLogger(ComponentScanProcessor.class);

    @Override
    public void prepare(JfireContext jfireContext)
    {
        AnnotationContext bootStarpClassAnnotationContext = jfireContext.getEnv().getBootStarpClassAnnotationContext();
        if (bootStarpClassAnnotationContext.isAnnotationPresent(ComponentScan.class))
        {
            List<String>        classNames = new LinkedList<String>();
            List<ComponentScan> scans      = bootStarpClassAnnotationContext.getAnnotations(ComponentScan.class);
            for (ComponentScan componentScan : scans)
            {
                for (String each : componentScan.value())
                {
                    Collections.addAll(classNames, PackageScan.scan(each));
                }
            }
            for (String each : classNames)
            {
                try
                {
                    if (annotationDatabase.isAnnotation(each))
                    {
                        continue;
                    }
                    if (annotationDatabase.isAnnotationPresentOnClass(each, Resource.class))
                    {
                        Class<?> ckass = classLoader.loadClass(each);
                        logger.debug("traceId:{} 扫描发现类:{}", TRACEID.currentTraceId(), ckass.getName());
                        Resource             resource  = annotationUtil.getAnnotation(Resource.class, ckass);
                        String               beanName  = resource.name().equals("") ? ckass.getName() : resource.name();
                        boolean              prototype = resource.shareable() == false;
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
                    }
                    else if (annotationDatabase.isAnnotationPresentOnClass(each, Configuration.class))
                    {
                        logger.debug("traceId:{} 扫描发现候选配置类:{}", TRACEID.currentTraceId(), each);
                        environment.registerCandidateConfiguration(each);
                    }
                }
                catch (ClassNotFoundException e)
                {
                    ReflectUtil.throwException(e);
                }
            }
        }
    }

    @Override
    public int order()
    {
        return JfirePreparedConstant.DEFAULT_ORDER;
    }
}
