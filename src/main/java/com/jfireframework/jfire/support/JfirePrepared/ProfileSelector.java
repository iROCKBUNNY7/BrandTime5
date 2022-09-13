package com.jfireframework.jfire.support.JfirePrepared;

import com.jfireframework.baseutil.IniReader.IniFile;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.kernel.Order;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Import(ProfileSelector.ProfileImporter.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProfileSelector
{
    
    Profile[] value();
    
     @interface Profile
    {
        String name();
        
        String[] paths();
    }
    
    @Order(100)
    class ProfileImporter implements SelectImport
    {
        
        @Override
        public void selectImport(Environment environment)
        {
            if (environment.isAnnotationPresent(ProfileSelector.class))
            {
                PropertyPath.PropertyPathImporter util = new PropertyPath.PropertyPathImporter();
                for (ProfileSelector selector : environment.getAnnotations(ProfileSelector.class))
                {
                    String activeAttribute = environment.getProperty("jfire.profile.active");
                    if (StringUtil.isNotBlank(activeAttribute) == false)
                    {
                        return;
                    }
                    String[] actives = activeAttribute.split(",");
                    for (String active : actives)
                    {
                        active = active.trim();
                        for (Profile profile : selector.value())
                        {
                            if (active.equals(profile.name()))
                            {
                                for (String path : profile.paths())
                                {
                                    IniFile iniFile = util.processPath(path);
                                    for (String each : iniFile.keySet())
                                    {
                                        environment.putProperty(each, iniFile.getValue(each));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
    }
}
