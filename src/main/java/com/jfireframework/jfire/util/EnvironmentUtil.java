package com.jfireframework.jfire.util;

import java.util.HashMap;
import java.util.Map;
import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.config.Condition;

public class EnvironmentUtil
{
    private static Map<Class<? extends Condition>, Condition> conditionImplStore = new HashMap<Class<? extends Condition>, Condition>();
    private static ThreadLocal<AnnotationUtil>                annoUtilLocal      = new ThreadLocal<AnnotationUtil>() {
                                                                                     protected AnnotationUtil initialValue()
                                                                                     {
                                                                                         return new AnnotationUtil();
                                                                                     }
                                                                                 };
    
    public static AnnotationUtil getAnnoUtil()
    {
        return annoUtilLocal.get();
    }
    
    public static Condition getCondition(Class<? extends Condition> ckass)
    {
        Condition instance = conditionImplStore.get(ckass);
        if (instance == null)
        {
            try
            {
                instance = ckass.newInstance();
                conditionImplStore.put(ckass, instance);
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        return instance;
    }
    
    public static void clear()
    {
        conditionImplStore.clear();
        annoUtilLocal.get().clear();
    }
    
}
