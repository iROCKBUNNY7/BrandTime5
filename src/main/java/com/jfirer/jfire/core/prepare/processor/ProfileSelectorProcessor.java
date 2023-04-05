package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.IniReader;
import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.jfire.core.JfireContext;
import com.jfirer.jfire.core.prepare.JfirePrepare;
import com.jfirer.jfire.core.prepare.annotation.ProfileSelector;
import com.jfirer.jfire.util.JfirePreparedConstant;
import com.jfirer.jfire.util.Utils;

public class ProfileSelectorProcessor implements JfirePrepare
{

    @Override
    public JfireContext.NeedRefresh prepare(JfireContext jfireContext)
    {
        String activeAttribute = jfireContext.getEnv().getProperty(ProfileSelector.activePropertyName);
        if (StringUtil.isNotBlank(activeAttribute) == false)
        {
            return JfireContext.NeedRefresh.NO;
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
        return JfireContext.NeedRefresh.NO;
    }

    @Override
    public int order()
    {
        return JfirePreparedConstant.PROFILE_SELECTOR_ORDER;
    }
}
