package com.jfireframework.jfire.core;

import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.smc.compiler.CompileHelper;
import com.jfireframework.jfire.core.prepare.support.annotaion.AnnotationDatabase;
import com.jfireframework.jfire.core.prepare.support.annotaion.AnnotationDatabaseImpl;
import com.jfireframework.jfire.exception.DuplicateBeanNameException;
import com.jfireframework.jfire.util.Utils;

import javax.tools.JavaCompiler;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class Environment
{
    public static final String                      ENVIRONMENT_FIELD_NAME = "environment_jfire_3";
    private final       Map<String, BeanDefinition> beanDefinitions        = new HashMap<String, BeanDefinition>();
    private final       Map<String, String>         properties             = new HashMap<String, String>();
    private final       ReadOnlyEnvironment         readOnlyEnvironment    = new ReadOnlyEnvironment(this);
    private             ClassLoader                 classLoader;
    private             CompileHelper               compileHelper;
    private             JavaCompiler                javaCompiler;
    private             List<Method>                methods                = new ArrayList<Method>();
    private             int                         methodSequence         = 0;
    private             Set<String>                 candidateConfiguration = new HashSet<String>();
    //存储BootStrap类上的所有注解
    private             Annotation[]                annotationStore;
    private             AnnotationDatabase          annotationDatabase;

    public Environment(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
        annotationDatabase = new AnnotationDatabaseImpl(classLoader);
    }

    public Environment()
    {
        classLoader = Environment.class.getClassLoader();
        annotationDatabase = new AnnotationDatabaseImpl(classLoader);
    }

    public int registerMethod(Method method)
    {
        int index = methods.indexOf(method);
        if (index != -1)
        {
            return index;
        }
        methods.add(method);
        methodSequence += 1;
        return methodSequence - 1;
    }

    public Method getMethod(int sequence)
    {
        return methods.get(sequence);
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void registerBeanDefinition(BeanDefinition beanDefinition)
    {
        BeanDefinition pred = beanDefinitions.put(beanDefinition.getBeanName(), beanDefinition);
        if (pred != null)
        {
            throw new DuplicateBeanNameException(pred.getBeanName());
        }
    }

    public ReadOnlyEnvironment readOnlyEnvironment()
    {
        return readOnlyEnvironment;
    }

    public Map<String, BeanDefinition> beanDefinitions()
    {
        return readOnlyEnvironment.beanDefinitions;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annoType)
    {
        AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
        for (Annotation each : annotationStore)
        {
            if (annotationUtil.isPresent(annoType, each))
            {
                return true;
            }
        }
        return false;
    }

    public <T extends Annotation> T getAnnotation(Class<T> type)
    {
        AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
        for (Annotation each : annotationStore)
        {
            if (annotationUtil.isPresent(type, each))
            {
                return annotationUtil.getAnnotation(type, each);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> T[] getAnnotations(Class<T> type)
    {
        AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
        List<T>        list           = new ArrayList<T>();
        for (Annotation each : annotationStore)
        {
            if (annotationUtil.isPresent(type, each))
            {
                for (T anno : annotationUtil.getAnnotations(type, each))
                {
                    list.add(anno);
                }
            }
        }
        return list.toArray((T[]) Array.newInstance(type, list.size()));
    }

    public BeanDefinition getBeanDefinition(String beanName)
    {
        return beanDefinitions.get(beanName);
    }

    public BeanDefinition getBeanDefinition(Class<?> type)
    {
        for (BeanDefinition each : beanDefinitions.values())
        {
            if (type == each.getType())
            {
                return each;
            }
        }
        return null;
    }

    public List<BeanDefinition> getBeanDefinitionByAbstract(Class<?> type)
    {
        if (type.isInterface() == false && Modifier.isAbstract(type.getModifiers()) == false)
        {
            throw new IllegalArgumentException("该方法参数必须为接口或者抽象类");
        }
        List<BeanDefinition> list = new LinkedList<BeanDefinition>();
        for (BeanDefinition each : beanDefinitions.values())
        {
            if (type.isAssignableFrom(each.getType()))
            {
                list.add(each);
            }
        }
        return list;
    }

    public String getProperty(String name)
    {
        return properties.get(name);
    }

    public void putProperty(String name, String value)
    {
        properties.put(name, value);
    }

    public void setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    public CompileHelper getCompileHelper()
    {
        if (compileHelper == null)
        {
            compileHelper = javaCompiler == null ? new CompileHelper(classLoader) : new CompileHelper(classLoader, javaCompiler);
        }
        return compileHelper;
    }

    public void setJavaCompiler(JavaCompiler javaCompiler)
    {
        this.javaCompiler = javaCompiler;
    }

    public static class ReadOnlyEnvironment
    {
        private final   Environment                 host;
        protected final Map<String, BeanDefinition> beanDefinitions;
        protected final Map<String, String>         properties;

        public ReadOnlyEnvironment(Environment host)
        {
            this.host = host;
            beanDefinitions = Collections.unmodifiableMap(host.beanDefinitions);
            properties = Collections.unmodifiableMap(host.properties);
        }

        public Collection<BeanDefinition> beanDefinitions()
        {
            return beanDefinitions.values();
        }

        public boolean isAnnotationPresent(Class<? extends Annotation> annoType)
        {
            return host.isAnnotationPresent(annoType);
        }

        public <T extends Annotation> T getAnnotation(Class<T> type)
        {
            return host.getAnnotation(type);
        }

        public String getProperty(String name)
        {
            return host.getProperty(name);
        }

        public boolean hasProperty(String name)
        {
            return host.getProperty(name) != null;
        }
    }

    public void registerCandidateConfiguration(String className)
    {
        candidateConfiguration.add(className);
    }

    public Annotation[] getAnnotationStore()
    {
        return annotationStore;
    }

    public void setAnnotationStore(Annotation[] annotationStore)
    {
        this.annotationStore = annotationStore;
    }

    public Set<String> getCandidateConfiguration()
    {
        return candidateConfiguration;
    }

    public AnnotationDatabase getAnnotationDatabase()
    {
        return annotationDatabase;
    }
}
