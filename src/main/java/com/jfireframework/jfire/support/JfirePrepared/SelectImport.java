package com.jfireframework.jfire.support.JfirePrepared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.kernel.JfirePrepared;
import com.jfireframework.jfire.kernel.Order;

public interface SelectImport
{
    /**
     * 在容器启动之前触发
     */
    void selectImport(Environment environment);
    
    @Order(10)
    class ProcessSelectImport implements JfirePrepared
    {
        private static final Logger logger = LoggerFactory.getLogger(ProcessSelectImport.class);
        
        @Override
        public void prepared(Environment environment)
        {
            class Entry
            {
                private int            order;
                private BeanDefinition beanDefinition;
                
                Entry(int order, BeanDefinition beanDefinition)
                {
                    this.order = order;
                    this.beanDefinition = beanDefinition;
                }
            }
            ArrayList<Entry> list = new ArrayList<Entry>();
            for (BeanDefinition definition : environment.getBeanDefinitions().values())
            {
                if (definition.getType() != null)
                {
                    if (SelectImport.class.isAssignableFrom(definition.getType()))
                    {
                        Entry entry;
                        if (definition.getType().isAnnotationPresent(Order.class))
                        {
                            Order order = definition.getType().getAnnotation(Order.class);
                            entry = new Entry(order.value(), definition);
                        }
                        else
                        {
                            entry = new Entry(0, definition);
                        }
                        list.add(entry);
                    }
                }
            }
            Collections.sort(list, new Comparator<Entry>() {
                
                @Override
                public int compare(Entry o1, Entry o2)
                {
                    return o1.order - o2.order;
                }
            });
            try
            {
                String traceId = TRACEID.currentTraceId();
                for (Entry each : list)
                {
                    logger.debug("traceId:{} 准备执行:{}", traceId, each.beanDefinition.getType().getName());
                    ((SelectImport) each.beanDefinition.getType().newInstance()).selectImport(environment);
                }
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        
    }
}
