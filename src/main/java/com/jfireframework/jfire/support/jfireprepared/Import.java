package com.jfireframework.jfire.support.jfireprepared;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.kernel.JfirePrepared;

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
        @SuppressWarnings("unchecked")
        @Override
        public void prepared(Environment environment)
        {
            AnnotationUtil annotationUtil = new AnnotationUtil();
            Map<String, BeanDefinition> beanDefinitions = environment.getBeanDefinitions();
            List<Class<? extends JfirePrepared>> tmp = new ArrayList<Class<? extends JfirePrepared>>();
            for (BeanDefinition each : beanDefinitions.values())
            {
                if (each.getOriginType() != null && annotationUtil.isPresent(Import.class, each.getOriginType()))
                {
                    tmp.add((Class<? extends JfirePrepared>) each.getOriginType());
                }
            }
            for (Class<? extends JfirePrepared> each : tmp)
            {
                processImport(each, environment, annotationUtil);
            }
        }
        
        private void processImport(final Class<?> ckass, Environment environment, AnnotationUtil annotationUtil)
        {
            if (annotationUtil.isPresent(Import.class, ckass))
            {
                Import[] annotations = annotationUtil.getAnnotations(Import.class, ckass);
                for (Import annotation : annotations)
                {
                    for (Class<?> each : annotation.value())
                    {
                        environment.registerBeanDefinition(each);
                        processImport(each, environment, annotationUtil);
                    }
                }
            }
        }
        
    }
}
