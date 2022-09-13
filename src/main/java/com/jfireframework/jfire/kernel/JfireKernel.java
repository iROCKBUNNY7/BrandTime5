package com.jfireframework.jfire.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.order.AescComparator;
import com.jfireframework.baseutil.time.Timewatch;
import com.jfireframework.jfire.Utils;

public class JfireKernel
{
    protected static final Logger logger = LoggerFactory.getLogger(JfireKernel.class);
    
    public void initJfire(Environment environment, Map<String, BeanDefinition> beanDefinitions, Map<String, String> properties, ClassLoader classLoader, ExtraInfoStore extraInfoStore)
    {
        String traceId = TRACEID.newTraceId();
        Plugin[] plugins = new Plugin[] { //
                new ProcessPreparedPlugin(), //
                new InitializeBeanInstanceResolverPlugin(), //
                new AwareInitializeFinishedPlugin(), //
                new AwareContextInitedPlugin() };
        Timewatch timewatch = new Timewatch();
        for (Plugin plugin : plugins)
        {
            timewatch.start();
            plugin.process(environment, beanDefinitions, properties, classLoader, extraInfoStore);
            timewatch.end();
            logger.debug("traceId:{} 插件:{}耗费时间:{}毫秒", traceId, plugin.name(), timewatch.getTotal());
        }
    }
    
    interface Plugin
    {
        void process(Environment environment, Map<String, BeanDefinition> beanDefinitions, Map<String, String> properties, ClassLoader classLoader, ExtraInfoStore extraInfoStore);
        
        String name();
    }
    
    abstract class NameablePlugin implements Plugin
    {
        @Override
        public String name()
        {
            return this.getClass().getSimpleName();
        }
    }
    
    class ProcessPreparedPlugin extends NameablePlugin
    {
        class OrderEntry
        {
            int                            order;
            Class<? extends JfirePrepared> type;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void process(Environment environment, Map<String, BeanDefinition> beanDefinitions, Map<String, String> properties, ClassLoader classLoader, ExtraInfoStore extraInfoStore)
        {
            AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
            List<OrderEntry> tmp = new LinkedList<OrderEntry>();
            for (BeanDefinition each : beanDefinitions.values())
            {
                Class<?> type = each.getOriginType();
                if (type != null && JfirePrepared.class.isAssignableFrom(type))
                {
                    int order = annotationUtil.isPresent(Order.class, type) ? annotationUtil.getAnnotation(Order.class, type).value() : 0;
                    OrderEntry entry = new OrderEntry();
                    entry.order = order;
                    entry.type = (Class<? extends JfirePrepared>) type;
                    tmp.add(entry);
                }
            }
            Collections.sort(tmp, new Comparator<OrderEntry>() {
                @Override
                public int compare(OrderEntry o1, OrderEntry o2)
                {
                    return o1.order - o2.order;
                }
            });
            String traceId = TRACEID.currentTraceId();
            for (OrderEntry each : tmp)
            {
                try
                {
                    logger.debug("traceId:{} 当前处理的prepared:{}", traceId, each.type.getName());
                    each.type.newInstance().prepared(environment);
                }
                catch (Exception e)
                {
                    throw new JustThrowException(e);
                }
            }
        }
        
    }
    
    class InitializeBeanInstanceResolverPlugin extends NameablePlugin
    {
        
        @Override
        public void process(Environment environment, Map<String, BeanDefinition> beanDefinitions, Map<String, String> properties, ClassLoader classLoader, ExtraInfoStore extraInfoStore)
        {
            for (BeanDefinition each : beanDefinitions.values())
            {
                each.getBeanInstanceResolver().initialize(beanDefinitions);
            }
        }
        
    }
    
    class AwareInitializeFinishedPlugin extends NameablePlugin
    {
        
        @Override
        public void process(Environment environment, Map<String, BeanDefinition> beanDefinitions, Map<String, String> properties, ClassLoader classLoader, ExtraInfoStore extraInfoStore)
        {
            List<JfireAwareInitializeFinished> list = new ArrayList<JfireAwareInitializeFinished>();
            try
            {
                for (BeanDefinition beanDefinition : beanDefinitions.values())
                {
                    if (JfireAwareInitializeFinished.class.isAssignableFrom(beanDefinition.getOriginType()))
                    {
                        list.add((JfireAwareInitializeFinished) beanDefinition.getOriginType().newInstance());
                    }
                }
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
            for (JfireAwareInitializeFinished each : list)
            {
                each.awareInitializeFinished(environment.readOnlyEnvironment());
            }
        }
        
    }
    
    class AwareContextInitedPlugin extends NameablePlugin
    {
        
        @Override
        public void process(Environment environment, Map<String, BeanDefinition> beanDefinitions, Map<String, String> properties, ClassLoader classLoader, ExtraInfoStore extraInfoStore)
        {
            List<JfireAwareContextInited> tmp = new LinkedList<JfireAwareContextInited>();
            Map<String, Object> beanInstanceMap = new HashMap<String, Object>();
            for (BeanDefinition each : beanDefinitions.values())
            {
                if (JfireAwareContextInited.class.isAssignableFrom(each.getOriginType()))
                {
                    beanInstanceMap.clear();
                    tmp.add((JfireAwareContextInited) each.getBeanInstanceResolver().getInstance(beanInstanceMap));
                }
            }
            Collections.sort(tmp, new AescComparator());
            for (JfireAwareContextInited each : tmp)
            {
                logger.trace("准备执行方法{}.afterContextInit", each.getClass().getName());
                try
                {
                    each.awareContextInited();
                }
                catch (Exception e)
                {
                    logger.error("执行方法{}.afterContextInit发生异常", each.getClass().getName(), e);
                    throw new JustThrowException(e);
                }
            }
        }
        
    }
    
}
