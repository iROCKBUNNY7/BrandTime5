package com.jfirer.jfire.test.function;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.bytecode.support.DefaultAnnotationContextFactory;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.Assert.assertEquals;

@AnnodatationDatabaseTest.test1(value = RetentionPolicy.RUNTIME, value2 = "22", value3 = AnnodatationDatabaseTest.class, value5 = @AnnodatationDatabaseTest.test2("kk"), value6 = {@AnnodatationDatabaseTest.test2("gg")})
public class AnnodatationDatabaseTest
{
    private AnnotationContextFactory annotationDatabase = new DefaultAnnotationContextFactory();

    @Retention(RetentionPolicy.RUNTIME)
    public @interface test1
    {
        RetentionPolicy[] value();

        String value2();

        Class value3();

        Class value4() default String.class;

        test2 value5();

        test2[] value6();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface test2
    {
        String value();
    }

    @Test
    public void test()
    {
        AnnotationContext annotationContext = annotationDatabase.get(AnnodatationDatabaseTest.class, Thread.currentThread().getContextClassLoader());
        test1             test1             = annotationContext.getAnnotation(test1.class);
        test1             annotation        = test1;
        assertEquals("22", annotation.value2());
        assertEquals(RetentionPolicy.RUNTIME, annotation.value()[0]);
        assertEquals(AnnodatationDatabaseTest.class, annotation.value3());
        assertEquals(String.class, annotation.value4());
        assertEquals("kk", annotation.value5().value());
        assertEquals("gg", annotation.value6()[0].value());
    }
}
