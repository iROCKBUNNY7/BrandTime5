package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.IniReader;
import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.ProfileSelector;
import com.jfirer.jfire.util.PrepareConstant;
import com.jfirer.jfire.util.Utils;

public class ProfileSelectorProcessor implements ContextPrepare
{

    @Override
    public ApplicationContext.NeedRefresh prepare(ApplicationContext jfireContext)
    {
        String activeAttribute = jfireContext.getEnv().getProperty(ProfileSelector.activePropertyName);
        if (StringUtil.isNotBlank(activeAttribute) == false)
        {
            return ApplicationContext.NeedRefresh.NO;
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
        return ApplicationContext.NeedRefresh.NO;
    }

    @Override
    public int order()
    {
        return PrepareConstant.PROFILE_SELECTOR_ORDER;
    }
}
