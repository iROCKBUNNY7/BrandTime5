package com.jfireframework.context.test.function;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.jfire.core.prepare.support.annotaion.AnnotationDatabase;
import com.jfireframework.jfire.core.prepare.support.annotaion.AnnotationDatabaseImpl;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static org.junit.Assert.*;

@AnnodatationDatabaseTest.test1(value = RetentionPolicy.RUNTIME, value2 = "22", value3 = AnnodatationDatabaseTest.class, value5 = @AnnodatationDatabaseTest.test2("kk"),value6 = {@AnnodatationDatabaseTest.test2("gg")})
public class AnnodatationDatabaseTest
{
    private AnnotationDatabase annotationDatabase = new AnnotationDatabaseImpl(Thread.currentThread().getContextClassLoader());

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
        List<AnnotationMetadata> annotaionOnClass   = annotationDatabase.getAnnotaionOnClass(AnnodatationDatabaseTest.class.getName());
        AnnotationMetadata       annotationInstance = annotaionOnClass.get(0);
        test1                    annotation         = (test1) annotationInstance.annotation();
        assertEquals("22", annotation.value2());
        assertEquals(RetentionPolicy.RUNTIME, annotation.value()[0]);
        assertEquals(AnnodatationDatabaseTest.class, annotation.value3());
        assertEquals(String.class, annotation.value4());
        assertEquals("kk", annotation.value5().value());
        assertEquals("gg",annotation.value6()[0].value());
    }
}
