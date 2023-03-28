package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.IniReader;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.ProfileSelector;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

public class ProfileSelectorProcessor implements JfirePrepare
{

    @Override
    public boolean prepare(JfireContext jfireContext)
    {
        String activeAttribute = jfireContext.getEnv().getProperty(ProfileSelector.activePropertyName);
        if (StringUtil.isNotBlank(activeAttribute) == false)
        {
            return true;
        }
        ClassLoader              classLoader              = Thread.currentThread().getContextClassLoader();
        AnnotationContextFactory annotationContextFactory = jfireContext.getAnnotationContextFactory();
        for (Class<?> each : jfireContext.getConfigurationClassSet())
        {
            AnnotationContext annotationContext = annotationContextFactory.get(each, classLoader);
            if (annotationContext.isAnnotationPresent(ProfileSelector.class))
            {
                ProfileSelector   profileSelector = annotationContext.getAnnotation(ProfileSelector.class);
                String            profileFileName = profileSelector.protocol() + ":" + profileSelector.prefix() + activeAttribute + ".ini";
                IniReader.IniFile iniFile         = Utils.processPath(profileFileName);
                for (String key : iniFile.keySet())
                {
                    jfireContext.getEnv().putProperty(key, iniFile.getValue(key));
                }
            }
        }
        return true;
    }

    @Override
    public int order()
    {
        return JfirePreparedConstant.PROFILE_SELECTOR_ORDER;
    }
}
