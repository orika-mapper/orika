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
package ma.glasnost.orika.test.perf;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import ma.glasnost.orika.impl.generator.EclipseJdtCompiler;
import ma.glasnost.orika.test.MavenProjectUtil;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class MultiLayeredClassloaderTestCase {
    
    /**
     * Creates a new temporary directory
     * 
     * @return
     * @throws IOException
     */
    public static File createTempDirectory() throws IOException {
        final File temp = File.createTempFile("temp",
                Long.toString(System.nanoTime()));
        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: "
                    + temp.getAbsolutePath());
        }
        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: "
                    + temp.getAbsolutePath());
        }
        return temp;
    }
    
    @Test
    public void nestedClassLoader() throws Exception {
        File projectRoot = MavenProjectUtil.findProjectRoot();
        
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        
        File tempClasses = createTempDirectory();
        
        EclipseJdtCompiler complier = new EclipseJdtCompiler(tccl);
        complier.compile(new File(projectRoot, "src/main/java-hidden"),tempClasses);
        
        ClassLoader childLoader = new URLClassLoader(new URL[]{tempClasses.toURI().toURL()}, tccl);
        
        Class<?> runnerClass = childLoader.loadClass("dtotypes.Runner");
        Object runner = runnerClass.newInstance();
        try {
            Thread.currentThread().setContextClassLoader(childLoader);
        
            runnerClass.getMethod("test").invoke(runner);
            
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
        
    }
}
