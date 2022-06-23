package com.jfireframework.jfire.bean.field.dependency.impl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.jfire.bean.BeanDefinition;

public class ListField extends AbstractDependencyField
{
    private BeanDefinition[] dependencyBeans;
    private String           msg;
    
    public ListField(Field field, BeanDefinition[] beans)
    {
        super(field);
        dependencyBeans = beans;
        msg = StringUtil.format("属性{}.{}为空,无法进行list注入", field.getDeclaringClass().getName(), field.getName());
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void inject(Object src, Map<String, Object> beanInstanceMap)
    {
        List list = (List) unsafe.getObject(src, offset);
        Verify.exist(list, msg);
        for (BeanDefinition each : dependencyBeans)
        {
            list.add(each.getConstructedBean().getInstance(beanInstanceMap));
        }
    }
    
}
