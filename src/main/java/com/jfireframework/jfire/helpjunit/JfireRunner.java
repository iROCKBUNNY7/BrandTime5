package com.jfireframework.jfire.helpjunit;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.support.jfireprepared.Import;

public class JfireRunner extends BlockJUnit4ClassRunner
{
    private Class<?> klass;
    
    public JfireRunner(Class<?> klass) throws InitializationError, URISyntaxException
    {
        super(klass);
        this.klass = klass;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    protected Statement methodBlock(FrameworkMethod method)
    {
        Object test = createTest(method.getMethod());
        Statement statement = methodInvoker(method, test);
        statement = possiblyExpectingExceptions(method, test, statement);
        statement = withPotentialTimeout(method, test, statement);
        statement = withBefores(method, test, statement);
        statement = withAfters(method, test, statement);
        statement = withRules(method, test, statement);
        return statement;
    }
    
    private Statement withRules(FrameworkMethod method, Object target, Statement statement)
    {
        List<TestRule> testRules = getTestRules(target);
        Statement result = statement;
        result = withMethodRules(method, testRules, target, result);
        result = withTestRules(method, testRules, result);
        
        return result;
    }
    
    private Statement withTestRules(FrameworkMethod method, List<TestRule> testRules, Statement statement)
    {
        return testRules.isEmpty() ? statement : new RunRules(statement, testRules, describeChild(method));
    }
    
    @SuppressWarnings("unlikely-arg-type")
    private Statement withMethodRules(FrameworkMethod method, List<TestRule> testRules, Object target, Statement result)
    {
        for (org.junit.rules.MethodRule each : getMethodRules(target))
        {
            if (!testRules.contains(each))
            {
                result = each.apply(result, method, target);
            }
        }
        return result;
    }
    
    private List<org.junit.rules.MethodRule> getMethodRules(Object target)
    {
        return rules(target);
    }
    
    protected Object createTest(Method method)
    {
        JfireConfig jfireConfig = new JfireConfig(klass);
        AnnotationUtil annotationUtil = new AnnotationUtil();
        if (annotationUtil.isPresent(Import.class, method))
        {
            for (Import each : annotationUtil.getAnnotations(Import.class, method))
            {
                jfireConfig.registerBeanDefinition(each.value());
            }
        }
        jfireConfig.getEnvironment().addConfigMethod(method);
        Jfire jfire = new Jfire(jfireConfig);
        return jfire.getBean(klass);
    }
}
