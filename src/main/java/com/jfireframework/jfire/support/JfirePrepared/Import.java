package com.jfireframework.jfire.support.JfirePrepared;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.kernel.JfirePrepared;
import com.jfireframework.jfire.support.BeanInstanceResolver.ReflectBeanInstanceResolver;

/**
 * 用来引入其他的类配置.
 *
 * @author linbin
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import
{
    Class<?>[] value();
    
    class ProcessImport implements JfirePrepared
    {
        private static final Logger logger = LoggerFactory.getLogger(ProcessImport.class);
        
        @Override
        public void prepared(Environment environment)
        {
            AnnotationUtil annotationUtil = new AnnotationUtil();
            Map<String, BeanDefinition> beanDefinitions = environment.getBeanDefinitions();
            List<Class<?>> tmp = new ArrayList<Class<?>>();
            for (BeanDefinition each : beanDefinitions.values())
            {
                if (each.getType() != null && annotationUtil.isPresent(Import.class, each.getType()))
                {
                    tmp.add(each.getType());
                }
            }
            for (Class<?> each : tmp)
            {
                processImport(each, environment, annotationUtil);
            }
        }
        
        private void processImport(final Class<?> ckass, Environment environment, AnnotationUtil annotationUtil)
        {
            if (annotationUtil.isPresent(Import.class, ckass))
            {
                String traceId = TRACEID.currentTraceId();
                Import[] annotations = annotationUtil.getAnnotations(Import.class, ckass);
                for (Import annotation : annotations)
                {
                    for (Class<?> each : annotation.value())
                    {
                        logger.debug("traceId:{} 导入类:{}", traceId, each.getName());
                        BeanDefinition beanDefinition = new BeanDefinition(each.getName(), each, false, new ReflectBeanInstanceResolver(each.getName(), each, false));
                        environment.registerBeanDefinition(beanDefinition);
                        processImport(each, environment, annotationUtil);
                    }
                }
            }
        }
        
    }
}
