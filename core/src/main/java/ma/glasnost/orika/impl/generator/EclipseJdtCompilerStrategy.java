/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.glasnost.orika.impl.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.lang.reflect.Modifier;

/**
 * Uses Eclipse JDT to format and compile the source for the specified
 * GeneratedSourceCode objects.<br>
 * <br>
 * 
 * By default, this compiler strategy writes formatted source files relative to
 * the current class path root.
 * 
 * @author matt.deboer@gmail.com
 */
public class EclipseJdtCompilerStrategy extends CompilerStrategy {
    
    private static final String WRITE_SOURCE_FILES_BY_DEFAULT = "true";
    private static final String WRITE_CLASS_FILES_BY_DEFAULT = "false";
    private static final String COMPILER_CLASS_NAME = "ma.glasnost.orika.impl.generator.EclipseJdtCompiler";
    
    private final Object compiler;
    private final Method formatSource;
    private final Method compile;
    private final Method assertTypeAccessible;
    private final Method load;
    
    public EclipseJdtCompilerStrategy() {
        super(WRITE_SOURCE_FILES_BY_DEFAULT, WRITE_CLASS_FILES_BY_DEFAULT);
        
        try {
            Class<?> compilerClass = Class.forName(COMPILER_CLASS_NAME, true, Thread.currentThread().getContextClassLoader());
            this.compiler = compilerClass.newInstance();
            this.formatSource = compilerClass.getMethod("formatSource", String.class);
            this.compile = compilerClass.getMethod("compile", String.class, String.class, String.class);
            this.assertTypeAccessible = compilerClass.getMethod("assertTypeAccessible", Class.class);
            this.load = compilerClass.getMethod("load", String.class, byte[].class);
            
        } catch (Exception e) {
            throw new IllegalStateException(
                    COMPILER_CLASS_NAME
                            + " or one of it's runtime dependencies was not available; is the 'orika-eclipse-tools' module included in your classpath?");
        }
    }
    
    private String formatSource(String rawSource) {
        try {
            return (String) formatSource.invoke(compiler, rawSource);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            } else {
                throw new RuntimeException(e.getTargetException());
            }
        }
    }
    
    /**
     * Produces the requested source and/or class files for debugging purposes.
     * 
     * @throws IOException
     */
    protected void writeSourceFile(String sourceText, String packageName, String className) throws IOException {
        
        File parentDir = preparePackageOutputPath(this.pathToWriteSourceFiles, packageName);
        
        File outputSourceFile = new File(parentDir, className + ".java");
        if (!outputSourceFile.exists() && !outputSourceFile.createNewFile()) {
            throw new IOException("Could not write source file for " + packageName + "." + className);
        }
        
        FileWriter fw = null;
        try {
            fw = new FileWriter(outputSourceFile);
            fw.append(sourceText);
        } finally {
            if (fw != null)
                fw.close();
        }
        
    }
    
    protected void writeClassFile(String packageName, String simpleClassName, byte[] data) throws IOException {
        
        File parentDir = preparePackageOutputPath(this.pathToWriteClassFiles, packageName);
        
        File outputSourceFile = new File(parentDir, simpleClassName + ".class");
        if (!outputSourceFile.exists() && !outputSourceFile.createNewFile()) {
            throw new IOException("Could not write class file for " + packageName + "." + simpleClassName);
        }
        
        FileOutputStream fout = new FileOutputStream(outputSourceFile);
        fout.write(data);
        fout.close();
    }
    
    public void assureTypeIsAccessible(Class<?> type) throws SourceCodeGenerationException {
        try {
            if (!Modifier.isPublic(type.getModifiers())) {
                throw new SourceCodeGenerationException(type + " is not accessible");
            } else if (type.isMemberClass()) {
                /*
                 * The type needs to be publicly accessible (including it's
                 * enclosing classes if any)
                 */
                Class<?> currentType = type;
                while (currentType != null) {
                    if (!Modifier.isPublic(type.getModifiers())) {
                        throw new SourceCodeGenerationException(type + " is not accessible");
                    }
                    currentType = currentType.getEnclosingClass();
                }
            }
            
            assertTypeAccessible.invoke(compiler, type);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new SourceCodeGenerationException(e.getMessage(), e.getTargetException());
        }
    }
    
    private byte[] compile(String source, String packageName, String classSimpleName) throws SourceCodeGenerationException {
        try {
            return (byte[]) compile.invoke(compiler, source, packageName, classSimpleName);
        } catch (IllegalAccessException e) {
            throw classCompilationException(e, packageName, classSimpleName, source);
        } catch (IllegalArgumentException e) {
            throw classCompilationException(e, packageName, classSimpleName, source);
        } catch (InvocationTargetException e) {
            throw classCompilationException(e.getTargetException(), packageName, classSimpleName, source);
        }
    }
    
    private Class<?> load(String className, byte[] data) throws ClassNotFoundException {
        try {
            return (Class<?>) load.invoke(compiler, className, data);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof ClassNotFoundException) {
                throw (ClassNotFoundException) e.getTargetException();
            } else {
                throw new RuntimeException(e.getTargetException());
            }
        }
    }
    
    private SourceCodeGenerationException classCompilationException(Throwable cause, String packageName, String classSimpleName, String source) {
        return new SourceCodeGenerationException("Error compiling " + packageName + "." + classSimpleName, cause);
    }
    
    /**
     * Compile and return the (generated) class; this will also cause the
     * generated class to be detached from the class-pool, and any (optional)
     * source and/or class files to be written.
     * 
     * @return the (generated) compiled class
     * @throws IOException
     */
    public Class<?> compileClass(SourceCodeContext sourceCode) throws SourceCodeGenerationException {
        
        Class<?> compiledClass = null;
        String sourceText = sourceCode.toSourceFile();
        try {
            sourceText = formatSource(sourceCode.toSourceFile());
        } catch (Exception e) {
            /*
             * If source code couldn't be formatted, we should still proceed
             * with compile, allowing the compilation to fail and tell us what
             * the real error was
             */
        }
        String packageName = sourceCode.getPackageName();
        String classSimpleName = sourceCode.getClassSimpleName();
        String className = sourceCode.getClassName();
        byte[] data = null;
        try {
            
            // Write source file before compilation in case of failure
            if (writeSourceFiles) {
                writeSourceFile(sourceText, packageName, classSimpleName);
            }
            
            data = compile(sourceText, packageName, classSimpleName);
            
            if (writeClassFiles) {
                writeClassFile(packageName, classSimpleName, data);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to write files for " + className, e);
        }
        
        try {
            compiledClass = load(className, data);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        return compiledClass;
    }
    
}