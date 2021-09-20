/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
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

import ma.glasnost.orika.test.MavenProjectUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author matt.deboer@gmail.com
 *
 * Note: this test is named '_IDEOnly' to allow it to be skipped 
 * during maven tests, as it has achieved inconsistent results
 * using that method.
 * Until a better solution can be found, we relegate it to running only
 * within the IDE.
 */
public class MultiLayeredClassloader_IDEOnly {
    
	/**
	 * @return a copy of the current thread context class-loader
	 */
	public static ClassLoader copyThreadContextClassLoader() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl instanceof URLClassLoader) {
			URLClassLoader ucl = (URLClassLoader)cl;
			return new URLClassLoader(ucl.getURLs());
		} else {
			throw new IllegalStateException("ThreadContextClassLoader is not a URLClassLoader");
		}
	}

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Test that Orika can be run from a nested class-loader
     */
    @Test
    public void nestedClassLoader() throws Exception {
        File projectRoot = MavenProjectUtil.findProjectRoot();
        
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        File tempClasses = temporaryFolder.getRoot();
        
        ClassLoader childLoader = new URLClassLoader(new URL[]{tempClasses.toURI().toURL()},
        		copyThreadContextClassLoader());
        
        Class<?> runnerClass = childLoader.loadClass("dtotypes.Runner");
        Object runner = runnerClass.newInstance();
        try {
            Thread.currentThread().setContextClassLoader(childLoader);
        
            childLoader.loadClass("dtotypes.BookHiddenDto");
            childLoader.loadClass("types.BookHidden");
            
            runnerClass.getMethod("test").invoke(runner);
            
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
        
    }
}
