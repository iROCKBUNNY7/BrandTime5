package com.jfirer.jfire.helpjunit;

import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.ApplicationContext;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;

public class ApplicaiontContextRunner extends BlockJUnit4ClassRunner
{
    private Class<?> klass;

    public ApplicaiontContextRunner(Class<?> klass) throws InitializationError
    {
        super(klass);
        this.klass = klass;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Statement methodBlock(FrameworkMethod method)
    {
        Object    test      = createTest();
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
        Statement      result    = statement;
        result = withMethodRules(method, testRules, target, result);
        result = withTestRules(method, testRules, result);
        return result;
    }

    private Statement withTestRules(FrameworkMethod method, List<TestRule> testRules, Statement statement)
    {
        return testRules.isEmpty() ? statement : new RunRules(statement, testRules, describeChild(method));
    }

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

    protected Object createTest()
    {
        ApplicationContext context = new DefaultApplicationContext(klass);
        return context.getBean(klass);
    }
}
