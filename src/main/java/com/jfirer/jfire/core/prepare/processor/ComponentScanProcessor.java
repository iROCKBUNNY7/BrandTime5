package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.PackageScan;
import com.jfirer.baseutil.TRACEID;
import com.jfirer.baseutil.bytecode.ClassFile;
import com.jfirer.baseutil.bytecode.ClassFileParser;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.bytecode.util.BytecodeUtil;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jfire.core.JfireContext;
import com.jfirer.jfire.core.prepare.JfirePrepare;
import com.jfirer.jfire.core.prepare.annotation.ComponentScan;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfirer.jfire.util.JfirePreparedConstant;
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
    public JfireContext.NeedRefresh prepare(JfireContext jfireContext)
    {
        AnnotationContextFactory annotationContextFactory = jfireContext.getAnnotationContextFactory();
        ClassLoader              classLoader              = Thread.currentThread().getContextClassLoader();
        List<String>             classNames               = new LinkedList<String>();
        for (Class<?> each : jfireContext.getConfigurationClassSet())
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
                    if (jfireContext.registerClass(ckass)!= JfireContext.RegisterResult.NODATA)
                    {
                        logger.debug("traceId:{} 扫描发现候选配置类:{}", TRACEID.currentTraceId(), each);
                        needRefresh = true;
                    }
                }
                else if (classFile.hasInterface(JfirePrepare.class))
                {
                    Class<?> ckass = classLoader.loadClass(each);
                    if (jfireContext.registerClass(ckass) == JfireContext.RegisterResult.JFIREPREPARE)
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
            return JfireContext.NeedRefresh.YES;
        }
        else
        {
            return JfireContext.NeedRefresh.NO;
        }
    }

    @Override
    public int order()
    {
        return JfirePreparedConstant.DEFAULT_ORDER;
    }
}
