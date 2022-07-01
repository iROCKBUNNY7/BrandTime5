package com.jfireframework.jfire.smc.compiler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import com.jfireframework.jfire.smc.model.CompilerModel;

/**
 * In-memory compile Java source code as String.
 * 
 * @author michael
 */
public class JavaStringCompiler
{
    
    JavaCompiler            compiler;
    StandardJavaFileManager stdManager;
    
    public JavaStringCompiler()
    {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.stdManager = compiler.getStandardFileManager(null, null, null);
    }
    
    /**
     * Compile a Java source file in memory.
     * 
     * @param fileName Java file name, e.g. "Test.java"
     * @param source The source code as String.
     * @return The compiled results as Map that contains class name as key,
     *         class binary as value.
     * @throws IOException If compile error.
     */
    public Map<String, byte[]> compile(String fileName, String source) throws IOException
    {
        MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager);
        try
        {
            JavaFileObject javaFileObject = manager.makeStringSource(fileName, source);
            CompilationTask task = compiler.getTask(null, manager, null, null, null, Arrays.asList(javaFileObject));
            Boolean result = task.call();
            if (result == null || !result.booleanValue())
            {
                throw new RuntimeException("Compilation failed.");
            }
            return manager.getClassBytes();
        }
        finally
        {
            manager.close();
        }
    }
    
    public Class<?> compile(CompilerModel modle, ClassLoader classLoader) throws IOException, ClassNotFoundException
    {
        MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager);
        try
        {
            JavaFileObject javaFileObject = manager.makeStringSource(modle.fileName(), modle.output());
            CompilationTask task = compiler.getTask(null, manager, null, null, null, Arrays.asList(javaFileObject));
            Boolean result = task.call();
            if (result == null || !result.booleanValue())
            {
                throw new RuntimeException("Compilation failed.");
            }
            return loadClass("com.jfireframe.smc.output." + modle.className(), manager.getClassBytes(), classLoader);
        }
        finally
        {
            manager.close();
        }
    }
    
    /**
     * Load class from compiled classes.
     * 
     * @param name Full class name.
     * @param classBytes Compiled results as a Map.
     * @return The Class instance.
     * @throws ClassNotFoundException If class not found.
     * @throws IOException If load error.
     */
    private Class<?> loadClass(String name, Map<String, byte[]> classBytes, ClassLoader classLoader) throws ClassNotFoundException, IOException
    {
        MemoryClassLoader memoryClassLoader = null;
        try
        {
            memoryClassLoader = new MemoryClassLoader(classBytes, classLoader);
            return memoryClassLoader.loadClass(name);
        }
        finally
        {
            if (memoryClassLoader != null)
            {
                memoryClassLoader.close();
            }
        }
    }
}
