package com.jfireframework.jfire.support.JfirePrepared;

import com.jfireframework.baseutil.IniReader.IniFile;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.jfire.Utils;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.kernel.JfirePrepared;
import com.jfireframework.jfire.kernel.Order;
import com.jfireframework.jfire.support.SupportConstant;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Import(ProfileSelector.ProfileImporter.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProfileSelector
{
	String protocol() default "file:";
	
	String prefix() default "application_";
	
	public static final String activePropertyName = "jfire.profile.active";
	
	@Order(SupportConstant.PROFILE_SELECTOR_ORDER)
	class ProfileImporter implements JfirePrepared
	{
		
		@Override
		public void prepared(Environment environment)
		{
			if (environment.isAnnotationPresent(ProfileSelector.class))
			{
				for (ProfileSelector selector : environment.getAnnotations(ProfileSelector.class))
				{
					String activeAttribute = environment.getProperty(activePropertyName);
					if (StringUtil.isNotBlank(activeAttribute) == false)
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
