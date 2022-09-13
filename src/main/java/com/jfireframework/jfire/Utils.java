package com.jfireframework.jfire;

import com.jfireframework.baseutil.anno.AnnotationUtil;

public class Utils
{
    private static ThreadLocal<AnnotationUtil> local = new ThreadLocal<AnnotationUtil>() {
        @Override
        protected AnnotationUtil initialValue()
        {
            return new AnnotationUtil();
        }
    };
    
    public static AnnotationUtil getAnnotationUtil()
    {
        return local.get();
    }
}
