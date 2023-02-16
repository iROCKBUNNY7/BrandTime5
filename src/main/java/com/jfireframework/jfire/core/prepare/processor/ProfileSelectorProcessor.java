package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.IniReader;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.ProfileSelector;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

public class ProfileSelectorProcessor implements JfirePrepare
{

    @Override
    public void prepare(Environment environment)
    {
        if ( environment.isAnnotationPresent(ProfileSelector.class) )
        {
            for (ProfileSelector selector : environment.getAnnotations(ProfileSelector.class))
            {
                String activeAttribute = environment.getProperty(ProfileSelector.activePropertyName);
                if ( StringUtil.isNotBlank(activeAttribute) == false )
                {
                    return;
                }
                String profileFileName = selector.protocol() + selector.prefix() + activeAttribute + ".ini";
                IniReader.IniFile iniFile = Utils.processPath(profileFileName);
                for (String key : iniFile.keySet())
                {
                    environment.putProperty(key, iniFile.getValue(key));
                }
            }
        }
    }

    @Override
    public int order()
    {
        return JfirePreparedConstant.PROFILE_SELECTOR_ORDER;
    }

}
