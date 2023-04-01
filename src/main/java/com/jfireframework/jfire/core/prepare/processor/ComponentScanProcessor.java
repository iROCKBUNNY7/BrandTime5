package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.PackageScan;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.bytecode.ClassFile;
import com.jfireframework.baseutil.bytecode.ClassFileParser;
import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.baseutil.bytecode.util.BytecodeUtil;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.core.JfireContext;
import com.jfireframework.jfire.core.aop.EnhanceManager;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.ComponentScan;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Collection;
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
    public boolean prepare(JfireContext jfireContext)
    {
        AnnotationContextFactory annotationContextFactory = jfireContext.getAnnotationContextFactory();
        Collection<Class<?>>     configurationClassSet    = jfireContext.getConfigurationClassSet();
        ClassLoader              classLoader              = Thread.currentThread().getContextClassLoader();
        List<String>             classNames               = new LinkedList<String>();
        for (Class<?> each : configurationClassSet)
        {
            AnnotationContext annotationContext = annotationContextFactory.get(each);
            if (annotationContext.isAnnotationPresent(ComponentScan.class))
            {
                ComponentScan componentScan = annotationContext.getAnnotation(ComponentScan.class);
                for (String scanPath : componentScan.value())
                {
                    Collections.addAll(classNames, PackageScan.scan(scanPath));
                }
            }
        }
        boolean needRefresh = false;
        for (String each : classNames)
        {
            String resourceName = each.replace('.', '/');
            try
            {
                byte[]    bytecode  = BytecodeUtil.loadBytecode(classLoader, resourceName);
                ClassFile classFile = new ClassFileParser(bytecode).parse();
                if (classFile.isAnnotation())
                {
                    continue;
                }
                AnnotationContext annotationContext = annotationContextFactory.get(resourceName);
                if (annotationContext.isAnnotationPresent(Resource.class))
                {
                    Class<?> ckass = classLoader.loadClass(each);
                    if (jfireContext.registerBean(ckass))
                    {
                        logger.debug("traceId:{} 扫描发现类:{}", TRACEID.currentTraceId(), ckass.getName());
                    }
                }
                else if (annotationContext.isAnnotationPresent(Configuration.class))
                {
                    Class<?> ckass = classLoader.loadClass(each);
                    if (jfireContext.registerConfiguration(ckass))
                    {
                        logger.debug("traceId:{} 扫描发现候选配置类:{}", TRACEID.currentTraceId(), each);
                        needRefresh = true;
                    }
                }
                else if (classFile.hasInterface(JfirePrepare.class) || classFile.hasInterface(EnhanceManager.class))
                {
                    Class<?> ckass = classLoader.loadClass(each);
                    if (jfireContext.registerClass(ckass) != -1)
                    {
                        needRefresh = true;
                    }
                }
            }
            catch (Throwable e)
            {
                ReflectUtil.throwException(e);
            }
        }
        if (needRefresh)
        {
            jfireContext.refresh();
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public int order()
    {
        return JfirePreparedConstant.DEFAULT_ORDER;
    }
}
