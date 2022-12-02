package com.jfireframework.jfire.core;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.jfire.Utils;
import com.jfireframework.jfire.exception.NewBeanInstanceException;

public class JfireBootstrap
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JfireBootstrap.class);
    
    public void start(Environment environment)
    {
        prepare(environment);
        // initializeBeanInstanceResolver(environment);
        // awareContextInited(environment);
    }
    
    private void prepare(Environment environment)
    {
        PriorityQueue<BeanDefinition> queue = new PriorityQueue<BeanDefinition>(environment.beanDefinitions().size(), new Comparator<BeanDefinition>() {
            
            @Override
            public int compare(BeanDefinition o1, BeanDefinition o2)
            {
                int order1 = Utils.ANNOTATION_UTIL.getAnnotation(JfirePreparedNotated.class, o1.getType()).order();
                int order2 = Utils.ANNOTATION_UTIL.getAnnotation(JfirePreparedNotated.class, o2.getType()).order();
                return order1 - order2;
            }
        });
        List<String> deleteBeanNames = new LinkedList<String>();
        do
        {
            deleteBeanNames.clear();
            environment.markVersion();
            for (Entry<String, BeanDefinition> entry : environment.beanDefinitions().entrySet())
            {
                if (Utils.ANNOTATION_UTIL.isPresent(JfirePreparedNotated.class, entry.getValue().getType()) && JfirePrepare.class.isAssignableFrom(entry.getValue().getType()))
                {
                    queue.add(entry.getValue());
                }
            }
            for (String each : deleteBeanNames)
            {
                environment.removeBeanDefinition(each);
            }
            BeanDefinition minOrderedJfirePrepare = queue.poll();
            if (minOrderedJfirePrepare != null)
            {
                try
                {
                    JfirePrepare jfirePrepareInstance = (JfirePrepare) minOrderedJfirePrepare.getType().newInstance();
                    jfirePrepareInstance.prepare(environment);
                }
                catch (Throwable e)
                {
                    throw new NewBeanInstanceException(e);
                }
            }
        } while (queue.isEmpty() == false || environment.isChanged());
    }
}
