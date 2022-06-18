package com.jfireframework.jfire.helpjunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.codejson.JsonObject;
import com.jfireframework.codejson.JsonTool;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;

public class EachMethodEachJfireRunner extends BlockJUnit4ClassRunner
{
    private Class<?> klass;
    private String   path;
    
    public EachMethodEachJfireRunner(Class<?> klass) throws InitializationError, URISyntaxException
    {
        super(klass);
        this.klass = klass;
        ConfigPath path = klass.getAnnotation(ConfigPath.class);
        this.path = path.value();
        
    }
    
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
        JfireConfig jfireConfig = new JfireConfig();
        if (path.startsWith("classpath:"))
        {
            jfireConfig.readConfig((JsonObject) JsonTool.fromString(StringUtil.readFromClasspath(path.substring(10), Charset.forName("utf8"))));
        }
        else if (path.startsWith("file:"))
        {
            InputStream inputStream = null;
            try
            {
                inputStream = new FileInputStream(new File(path.substring(5)));
                byte[] src = new byte[inputStream.available()];
                inputStream.read(src);
                jfireConfig.readConfig((JsonObject) JsonTool.fromString(new String(src, Charset.forName("utf8"))));
            }
            catch (IOException e)
            {
                throw new JustThrowException(e);
            }
            finally
            {
                if (inputStream != null)
                {
                    try
                    {
                        inputStream.close();
                    }
                    catch (IOException e)
                    {
                        throw new JustThrowException(e);
                    }
                }
            }
        }
        jfireConfig.addBean(klass.getName(), false, klass);
        if (method.isAnnotationPresent(PropertyAdd.class))
        {
            Properties properties = new Properties();
            PropertyAdd add = method.getAnnotation(PropertyAdd.class);
            for (String each : add.value().split(","))
            {
                String[] tmp = each.split("=");
                properties.put(tmp[0], tmp[1]);
            }
            jfireConfig.addProperties(properties);
        }
        Jfire jfire = new Jfire(jfireConfig);
        return jfire.getBean(klass);
    }
}
