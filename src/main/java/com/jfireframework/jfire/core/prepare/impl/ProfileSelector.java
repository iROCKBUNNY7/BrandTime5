package com.jfireframework.jfire.core.prepare.impl;

import com.jfireframework.baseutil.IniReader.IniFile;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.JfirePreparedNotated;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ProfileSelector
{
    String protocol() default "file:";

    String prefix() default "application_";

    String activePropertyName = "jfire.profile.active";

    @JfirePreparedNotated(order = JfirePreparedConstant.PROFILE_SELECTOR_ORDER)
    class ProfileSelectorProcessor implements JfirePrepare
    {

        @Override
        public void prepare(Environment environment)
        {
            if ( environment.isAnnotationPresent(ProfileSelector.class) )
            {
                for (ProfileSelector selector : environment.getAnnotations(ProfileSelector.class))
                {
                    String activeAttribute = environment.getProperty(activePropertyName);
                    if ( StringUtil.isNotBlank(activeAttribute) == false )
                    {
                        return;
                    }
                    String profileFileName = selector.protocol() + selector.prefix() + activeAttribute + ".ini";
                    IniFile iniFile = Utils.processPath(profileFileName);
                    for (String key : iniFile.keySet())
                    {
                        environment.putProperty(key, iniFile.getValue(key));
                    }
                }
            }
        }

    }
}
