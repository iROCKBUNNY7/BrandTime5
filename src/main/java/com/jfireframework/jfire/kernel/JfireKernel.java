package com.jfireframework.jfire.kernel;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.time.Timewatch;
import com.jfireframework.jfire.Utils;

public class JfireKernel
{
    protected static final Logger logger = LoggerFactory.getLogger(JfireKernel.class);
    
    /**
     * 初始化容器流程，即核心流程。通过将之前存在于环境中的BeanDefinition确认并且执行其中实现的接口来完成功能。
     * 
     * @param environment
     */
    public void initialize(Environment environment)
    {
        String traceId = TRACEID.newTraceId();
        Stage[] stages = new Stage[] {
                /**
                 * 检查所有的BeanDefinition，如果其实现了JfirePrepared。实例化之后执行。
                 */
                new ProcessPreparedStage(), //
                /**
                 * 为所有的BeanInstanceResolver执行其initialize方法
                 */
                new InitializeBeanInstanceResolverStage(), //
                /**
                 * 检查所有的BeanDefinition，如果其实现了JfireAwareContextInited接口，则获取Bean实例，并且执行
                 */
                new AwareContextInitedStage() };
        Timewatch timewatch = new Timewatch();
        for (Stage stage : stages)
        {
            logger.debug("traceId:{} 准备执行步骤:{}", traceId, stage.name());
            timewatch.start();
            stage.process(environment);
            timewatch.end();
            logger.debug("traceId:{} 步骤:{}耗费时间:{}毫秒", traceId, stage.name(), timewatch.getTotal());
        }
    }
    
    interface Stage
    {
        void process(Environment environment);
        
        String name();
    }
    
    abstract class NameableStage implements Stage
    {
        @Override
        public String name()
        {
            return this.getClass().getSimpleName();
        }
    }
    
    static class OrderEntry
    {
        int                                 order;
        BeanDefinition                      beanDefinition;
        static final Comparator<OrderEntry> COMPARATOR = new Comparator<JfireKernel.OrderEntry>() {
                                                           
                                                           @Override
                                                           public int compare(OrderEntry o1, OrderEntry o2)
                                                           {
                                                               return o1.order - o2.order;
                                                           }
                                                       };
    }
    
    class ProcessPreparedStage extends NameableStage
    {
        
        @Override
        public void process(Environment environment)
        {
            String traceId = TRACEID.currentTraceId();
            IdentityHashMap<BeanDefinition, Object> flags = new IdentityHashMap<BeanDefinition, Object>();
            BeanDefinition beanDefinition;
            while ((beanDefinition = getTopPreparedBeanDefinition(flags, environment)) != null)
            {
                try
                {
                    flags.put(beanDefinition, "");
                    logger.debug("traceId:{} 当前处理的prepared:{}", traceId, beanDefinition.getType().getName());
                    ((JfirePrepared) beanDefinition.getReflectInstance()).prepared(environment);
                }
                catch (Exception e)
                {
                    throw new JustThrowException(e);
                }
            }
        }
        
        BeanDefinition getTopPreparedBeanDefinition(IdentityHashMap<BeanDefinition, Object> flags, Environment environment)
        {
            List<OrderEntry> tmp = new LinkedList<OrderEntry>();
            AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
            for (BeanDefinition each : environment.getBeanDefinitions().values())
            {
                Class<?> type = each.getType();
                if (type != null && JfirePrepared.class.isAssignableFrom(type) && flags.containsKey(each) == false)
                {
                    int order = annotationUtil.isPresent(Order.class, type) ? annotationUtil.getAnnotation(Order.class, type).value() : 0;
                    OrderEntry entry = new OrderEntry();
                    entry.order = order;
                    entry.beanDefinition = each;
                    tmp.add(entry);
                }
            }
            Collections.sort(tmp, OrderEntry.COMPARATOR);
            return tmp.isEmpty() ? null : tmp.get(0).beanDefinition;
        }
    }
    
    class InitializeBeanInstanceResolverStage extends NameableStage
    {
        
        @Override
        public void process(Environment environment)
        {
            for (BeanDefinition each : environment.getBeanDefinitions().values())
            {
                each.getBeanInstanceResolver().initialize(environment);
            }
        }
        
    }
    
    class AwareContextInitedStage extends NameableStage
    {
        
        @Override
        public void process(Environment environment)
        {
            AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
            List<OrderEntry> tmp = new LinkedList<OrderEntry>();
            Map<String, Object> beanInstanceMap = new HashMap<String, Object>();
            for (BeanDefinition each : environment.getBeanDefinitions().values())
            {
                if (JfireAwareContextInited.class.isAssignableFrom(each.getType()))
                {
                    beanInstanceMap.clear();
                    OrderEntry entry = new OrderEntry();
                    entry.beanDefinition = each;
                    entry.order = annotationUtil.isPresent(Order.class, each.getType()) ? annotationUtil.getAnnotation(Order.class, each.getType()).value() : 0;
                    tmp.add(entry);
                }
            }
            Collections.sort(tmp, OrderEntry.COMPARATOR);
            for (OrderEntry each : tmp)
            {
                logger.trace("准备执行方法{}.awareContextInited", each.beanDefinition.getType().getClass().getName());
                try
                {
                    ((JfireAwareContextInited) each.beanDefinition.getBeanInstanceResolver().getInstance(beanInstanceMap)).awareContextInited(environment.readOnlyEnvironment());
                }
                catch (Exception e)
                {
                    logger.error("执行方法{}.awareContextInited发生异常", each.getClass().getName(), e);
                    throw new JustThrowException(e);
                }
            }
        }
        
    }
    
}
