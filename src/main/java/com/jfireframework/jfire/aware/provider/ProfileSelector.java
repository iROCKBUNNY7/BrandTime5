package com.jfireframework.jfire.aware.provider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.baseutil.IniReader.IniFile;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.jfire.aware.JfireAware;
import com.jfireframework.jfire.aware.provider.ProfileSelector.ProfileImporter;
import com.jfireframework.jfire.aware.provider.PropertyPath.PropertyPathImporter;
import com.jfireframework.jfire.config.annotation.Import;
import com.jfireframework.jfire.config.annotation.Order;
import com.jfireframework.jfire.config.environment.Environment;

@Import(ProfileImporter.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProfileSelector
{
    
    Profile[] value();
    
    public @interface Profile
    {
        String name();
        
        String[] paths();
    }
    
    @Order(100)
    class ProfileImporter implements JfireAware
    {
        
        @Override
        public void awareBeforeInitialization(Environment environment)
        {
            if (environment.isAnnotationPresent(ProfileSelector.class))
            {
                PropertyPathImporter util = new PropertyPathImporter();
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
        
        @Override
        public void awareAfterInitialization(Environment environment)
        {
            // TODO Auto-generated method stub
            
        }
        
    }
}
