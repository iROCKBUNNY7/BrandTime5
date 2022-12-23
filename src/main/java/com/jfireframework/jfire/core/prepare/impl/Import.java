package com.jfireframework.jfire.core.prepare.impl;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.JfirePreparedNotated;
import com.jfireframework.jfire.core.resolver.impl.DefaultBeanInstanceResolver;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

/**
 * 用来引入其他的类配置.
 *
 * @author linbin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import
{
    Class<?>[] value();
    
    @JfirePreparedNotated(order = JfirePreparedConstant.IMPORT_ORDER)
    class ImportProcessor implements JfirePrepare
    {
        private static final Logger logger = LoggerFactory.getLogger(ImportProcessor.class);
        
        @Override
        public void prepare(Environment environment)
        {
            Import[] imports = environment.getAnnotations(Import.class);
            for (Import each : imports)
            {
                processImport(each, environment);
            }
        }
        
        private void processImport(Import annotation, Environment environment)
        {
            String traceId = TRACEID.currentTraceId();
            for (Class<?> each : annotation.value())
            {
                logger.debug("traceId:{} 导入类:{}", traceId, each.getName());
                BeanDefinition beanDefinition = new BeanDefinition(each.getName(), each, false);
                beanDefinition.setBeanInstanceResolver(new DefaultBeanInstanceResolver(each));
                environment.registerBeanDefinition(beanDefinition);
                List<Import> list = Utils.ANNOTATION_UTIL.getAnnotations(Import.class, each);
                for (Import cascadeImport : list)
                {
                    processImport(cascadeImport, environment);
                }
            }
        }
        
    }
}
