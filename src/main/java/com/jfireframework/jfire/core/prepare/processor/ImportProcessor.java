package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.Import;
import com.jfireframework.jfire.core.resolver.impl.DefaultBeanInstanceResolver;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ImportProcessor implements JfirePrepare
{
    private static final Logger logger = LoggerFactory.getLogger(ImportProcessor.class);

    @Override
    public void prepare(Environment environment)
    {
        Set<Class<?>> set = new HashSet<Class<?>>();
        Import[] imports = environment.getAnnotations(Import.class);
        for (Import each : imports)
        {
            for (Class<?> ckass : each.value())
            {
                set.add(ckass);
            }
        }
        String traceId = TRACEID.currentTraceId();
        for (Class<?> each : set)
        {
            logger.debug("traceId:{} 导入类:{}", traceId, each.getName());
            BeanDefinition beanDefinition = new BeanDefinition(each.getName(), each, false);
            beanDefinition.setBeanInstanceResolver(new DefaultBeanInstanceResolver(each));
            environment.registerBeanDefinition(beanDefinition);
        }
    }

    @Override
    public int order()
    {
        return JfirePreparedConstant.IMPORT_ORDER;
    }

}
