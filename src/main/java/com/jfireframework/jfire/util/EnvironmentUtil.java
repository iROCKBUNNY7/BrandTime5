package com.jfireframework.jfire.util;

import com.jfireframework.baseutil.aliasanno.AnnotationUtil;

public class EnvironmentUtil
{
    private static ThreadLocal<AnnotationUtil> annoUtilLocal = new ThreadLocal<AnnotationUtil>() {
        protected AnnotationUtil initialValue()
        {
            return new AnnotationUtil();
        }
    };
    
    public static AnnotationUtil getAnnoUtil()
    {
        return annoUtilLocal.get();
    }
    
}
