package com.jfirer.jfire.exception;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.jfire.core.BeanDefinition;

import java.util.List;

public class BeanDefinitionCanNotFindException extends RuntimeException
{
    /**
     *
     */
    private static final long serialVersionUID = -6930943532766346915L;

    public BeanDefinitionCanNotFindException(String beanName)
    {
        super("无法找到bean：" + beanName);
    }

    public BeanDefinitionCanNotFindException(Class<?> ckass)
    {
        super("无法找到类型为:" + ckass.getName() + "的Bean");
    }

    public BeanDefinitionCanNotFindException(List<BeanDefinition> list, Class<?> type)
    {
        super(StringUtil.format("无法找到合适的Bean,符合类型:{}的bean存在:{}个", type.getName(), list.size()));
    }
}
