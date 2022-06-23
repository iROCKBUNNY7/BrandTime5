package com.jfireframework.jfire.bean.field.dependency.impl;

import java.lang.reflect.Field;
import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.jfire.bean.BeanDefinition;

/**
 * Map注入，map的key是bean的名称，也就是value的bean的名称
 * 
 * @author eric(eric@jfire.cn)
 * 
 */
public class BeanNameMapField extends AbstractDependencyField
{
    private BeanDefinition[] dependencyBeans;
    private String[]         beanNames;
    private String           msg;
    
    public BeanNameMapField(Field field, BeanDefinition[] beans, String[] beanNames)
    {
        super(field);
        this.dependencyBeans = beans;
        this.beanNames = beanNames;
        msg = StringUtil.format("属性{}.{}不能为空", field.getDeclaringClass(), field.getName());
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void inject(Object src, Map<String, Object> beanInstanceMap)
    {
        Map map = (Map) unsafe.getObject(src, offset);
        Verify.notNull(map, msg);
        for (int i = 0; i < dependencyBeans.length; i++)
        {
            map.put(beanNames[i], dependencyBeans[i].getConstructedBean().getInstance());
        }
    }
}
