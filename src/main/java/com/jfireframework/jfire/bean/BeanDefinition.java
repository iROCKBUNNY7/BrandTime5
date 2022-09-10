package com.jfireframework.jfire.bean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jfireframework.jfire.bean.field.dependency.DIField;
import com.jfireframework.jfire.bean.field.param.ParamField;

public class BeanDefinition
{
    /**
     * 由注解或者扫描出来的类上面有Resource注解定义的
     */
    public static final int     DEFAULT                      = 1;
    /**
     * 直接定义的外部实例
     */
    public static final int     OUTTER                       = 1 << 1;
    /**
     * 由loadBy方式定义，获取实例时需要从实现了LoadBy接口的Bean进行获取
     */
    public static final int     LOADBY                       = 1 << 2;
    /**
     * 由方法上注解了Bean注解进行定义的
     */
    public static final int     METHOD_BEAN_CONFIG           = 1 << 3;
    public static final int     SHIFT                        = 0xfffffff0;
    public static final int     PROTOTYPE                    = 1 << 10;
    public static final int     IMPORTTRIGGER                = 1 << 12;
    public static final int     CONFIGURATION                = 1 << 13;
    public static final int     LAZY_INIT_UNITL_FIRST_INVOKE = 1 << 14;
    private int                 schema;
    private String              beanName;
    // 该bean的类的名称。值是原始类的名称，不能从type上面提取，因为type可能是被增强后的子类
    private String              className;
    private Class<?>            originType;
    private Class<?>            type;
    private Map<String, String> params                       = new HashMap<String, String>();
    private Map<String, String> dependencies                 = new HashMap<String, String>();
    private String              postConstructMethod;
    private String              closeMethod;
    private String              loadByFactoryName;
    private Object              outterEntity;
    private List<DIField>       diFields                 = new ArrayList<DIField>();
    private List<ParamField>    paramFields                  = new ArrayList<ParamField>();
    private Bean                constructedBean;
    // 该属性只用于在JfireInitializationCfg中使用
    private boolean             prototype;
    private String              hostBeanName;
    private Method              beanAnnotatedMethod;
    
    public BeanDefinition()
    {
        switchDefault();
        enablePrototype(false);
    }
    
    public int mode()
    {
        return schema & (~SHIFT);
    }
    
    public int schema()
    {
        return schema;
    }
    
    public void setSchema(int schema)
    {
        this.schema = schema;
    }
    
    public String getHostBeanName()
    {
        return hostBeanName;
    }
    
    public void setHostBeanName(String hostBeanName)
    {
        this.hostBeanName = hostBeanName;
    }
    
    public Method getBeanAnnotatedMethod()
    {
        return beanAnnotatedMethod;
    }
    
    public void setBeanAnnotatedMethod(Method beanAnnotatedMethod)
    {
        this.beanAnnotatedMethod = beanAnnotatedMethod;
    }
    
    public void setPrototype(boolean prototype)
    {
        this.prototype = prototype;
    }
    
    public boolean getCfgPrototype()
    {
        return prototype;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getInstance()
    {
        return (T) constructedBean.getInstance();
    }
    
    public Bean getConstructedBean()
    {
        return constructedBean;
    }
    
    public void setConstructedBean(Bean constructedBean)
    {
        this.constructedBean = constructedBean;
    }
    
    public Class<?> getOriginType()
    {
        return originType;
    }
    
    public void addDIFieldInfos(List<DIField> diFieldInfos)
    {
        this.diFields.addAll(diFieldInfos);
    }
    
    public DIField[] getDiFieldArray()
    {
        return diFields.toArray(new DIField[diFields.size()]);
    }
    
    public ParamField[] getParamFieldArray()
    {
        return paramFields.toArray(new ParamField[paramFields.size()]);
    }
    
    public List<DIField> getDiFields()
    {
        return diFields;
    }
    
    public List<ParamField> getParamFields()
    {
        return paramFields;
    }
    
    public void addParamFields(List<ParamField> paramFields)
    {
        this.paramFields.addAll(paramFields);
    }
    
    public String getClassName()
    {
        return className;
    }
    
    public void setClassName(String className)
    {
        this.className = className;
    }
    
    public Object getOutterEntity()
    {
        return outterEntity;
    }
    
    public void setOutterEntity(Object outterEntity)
    {
        this.outterEntity = outterEntity;
    }
    
    public String getLoadByFactoryName()
    {
        return loadByFactoryName;
    }
    
    public void setLoadByFactoryName(String loadByFactoryName)
    {
        this.loadByFactoryName = loadByFactoryName;
    }
    
    public String getCloseMethod()
    {
        return closeMethod;
    }
    
    public void setCloseMethod(String closeMethod)
    {
        this.closeMethod = closeMethod;
    }
    
    public String getBeanName()
    {
        return beanName;
    }
    
    public void setBeanName(String beanName)
    {
        this.beanName = beanName;
    }
    
    public Class<?> getType()
    {
        return type;
    }
    
    public void setType(Class<?> type)
    {
        this.type = type;
    }
    
    public Map<String, String> getParams()
    {
        return params;
    }
    
    public void setParams(Map<String, String> params)
    {
        this.params = params;
    }
    
    public Map<String, String> getDependencies()
    {
        return dependencies;
    }
    
    public void setDependencies(Map<String, String> dependencies)
    {
        this.dependencies = dependencies;
    }
    
    public String getPostConstructMethod()
    {
        return postConstructMethod;
    }
    
    public void setPostConstructMethod(String postConstructMethod)
    {
        this.postConstructMethod = postConstructMethod;
    }
    
    public void setOriginType(Class<?> originType)
    {
        this.originType = originType;
    }
    
    public void putParam(String key, String value)
    {
        params.put(key, value);
    }
    
    public boolean isDefault()
    {
        return isBit(DEFAULT);
    }
    
    private boolean isBit(int bit)
    {
        return (schema & bit) == bit;
    }
    
    public boolean isLoadBy()
    {
        return isBit(LOADBY);
    }
    
    public boolean isOutter()
    {
        return isBit(OUTTER);
    }
    
    public boolean isPrototype()
    {
        return isBit(PROTOTYPE);
    }
    
    private void setBit(int bit, boolean enable)
    {
        schema = enable ? schema | bit : schema & (~bit);
    }
    
    public void enablePrototype(boolean enable)
    {
        setBit(PROTOTYPE, enable);
    }
    
    public void switchDefault()
    {
        schema = schema & SHIFT;
        setBit(DEFAULT, true);
    }
    
    public void switchLoadBy()
    {
        schema = schema & SHIFT;
        setBit(LOADBY, true);
    }
    
    public void switchOutter()
    {
        schema = schema & SHIFT;
        setBit(OUTTER, true);
    }
    
    public void switchMethodBeanConfig()
    {
        schema = schema & SHIFT;
        setBit(METHOD_BEAN_CONFIG, true);
    }
    
    public void enableImportTrigger()
    {
        setBit(IMPORTTRIGGER, true);
    }
    
    public boolean isImportTrigger()
    {
        return isBit(IMPORTTRIGGER);
    }
    
    public boolean isConfiguration()
    {
        return isBit(CONFIGURATION);
    }
    
    public boolean isMethodBeanConfig()
    {
        return isBit(METHOD_BEAN_CONFIG);
    }
    
    public void enableConfiguration()
    {
        setBit(CONFIGURATION, true);
    }
    
    public void enableLazyInitUntilFirstInvoke()
    {
        setBit(LAZY_INIT_UNITL_FIRST_INVOKE, true);
    }
    
    public boolean isLazyInitUntilFirstInvoke()
    {
        return isBit(LAZY_INIT_UNITL_FIRST_INVOKE);
    }
    
}
