package com.jfireframework.jfire.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.code.CodeLocation;
import com.jfireframework.jfire.bean.field.dependency.DIFieldInfo;
import com.jfireframework.jfire.bean.field.param.ParamField;

public class BeanDefinition
{
    /**
     * 由注解或者扫描出来的类上面有Resource注解定义的
     */
    public static final int     DEFAULT            = 1;
    /**
     * 直接定义的外部实例
     */
    public static final int     OUTTER             = 1 << 1;
    /**
     * 由loadBy方式定义，获取实例时需要从实现了LoadBy接口的Bean进行获取
     */
    public static final int     LOADBY             = 1 << 2;
    /**
     * 由方法上注解了Bean注解进行定义的
     */
    public static final int     METHOD_BEAN_CONFIG = 1 << 3;
    public static final int     SHIFT              = 0xfffffff0;
    public static final int     PROTOTYPE          = 1 << 4;
    public static final int     JFIRE_INIT_FINISH  = 1 << 5;
    public static final int     IMPORTTRIGGER      = 1 << 6;
    public static final int     CONFIGURATION      = 1 << 7;
    private int                 schema;
    private String              beanName;
    // 该bean的类的名称。值是原始类的名称，不能从type上面提取，因为type可能是被增强后的子类
    private String              className;
    private Class<?>            originType;
    private Class<?>            type;
    private Map<String, String> params             = new HashMap<String, String>();
    private Map<String, String> dependencies       = new HashMap<String, String>();
    private String              postConstructMethod;
    private String              closeMethod;
    private String              loadByFactoryName;
    private Object              outterEntity;
    private List<DIFieldInfo>   diFieldInfos       = new ArrayList<DIFieldInfo>();
    private List<ParamField>    paramFields        = new ArrayList<ParamField>();
    private Bean                constructedBean;
    // 该属性只用于在JfireInitializationCfg中使用
    private boolean             prototype;
    private String              hostBeanName;
    private String              beanAnnotatedMethod;
    private String              trace;
    
    public BeanDefinition()
    {
        switchDefault();
        enablePrototype(false);
        trace = CodeLocation.getCodeLocation(3);
    }
    
    public String trace()
    {
        return trace;
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
    
    public String getBeanAnnotatedMethod()
    {
        return beanAnnotatedMethod;
    }
    
    public void setBeanAnnotatedMethod(String beanAnnotatedMethod)
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
    
    public void addDIFieldInfos(List<DIFieldInfo> diFieldInfos, boolean clearBeforeAdd)
    {
        if (clearBeforeAdd)
        {
            this.diFieldInfos.clear();
        }
        this.diFieldInfos.addAll(diFieldInfos);
    }
    
    public List<DIFieldInfo> getDiFieldInfos()
    {
        return diFieldInfos;
    }
    
    public void setDiFieldInfos(List<DIFieldInfo> diFieldInfos)
    {
        this.diFieldInfos = diFieldInfos;
    }
    
    public void setParamFields(List<ParamField> paramFields)
    {
        this.paramFields = paramFields;
    }
    
    public List<ParamField> getParamFields()
    {
        return paramFields;
    }
    
    public void addParamFields(List<ParamField> paramFields, boolean clearBeforeAdd)
    {
        if (clearBeforeAdd)
        {
            this.paramFields.clear();
        }
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
    
    public boolean isJfireInitFinish()
    {
        return isBit(JFIRE_INIT_FINISH);
    }
    
    private void setBit(int bit, boolean enable)
    {
        schema = enable ? schema | bit : schema & (~bit);
    }
    
    public void enableJfireInitFinish()
    {
        setBit(JFIRE_INIT_FINISH, true);
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
        schema = schema & METHOD_BEAN_CONFIG;
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
}
