package com.jfirer.jfire.core.prepare.annotation.condition.provide;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfirer.baseutil.bytecode.annotation.ValuePair;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.condition.Conditional;
import com.jfirer.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfirer.jfire.core.prepare.annotation.condition.provide.ConditionOnProperty.OnProperty;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnProperty.class)
public @interface ConditionOnProperty
{
    /**
     * 需要存在的属性名称
     *
     * @return
     */
    String[] value();

    class OnProperty extends BaseCondition
    {

        public OnProperty()
        {
            super(ConditionOnProperty.class);
        }

        @Override
        protected boolean handleSelectAnnoType(ApplicationContext applicationContext, AnnotationMetadata metadata, ErrorMessage errorMessage)
        {
            for (ValuePair each : metadata.getAttribyte("value").getArray())
            {
                if (StringUtil.isNotBlank(applicationContext.getEnv().getProperty(each.getStringValue())) == false)
                {
                    errorMessage.addErrorMessage("缺少属性:" + each);
                    return false;
                }
            }
            return true;
        }
    }
}
