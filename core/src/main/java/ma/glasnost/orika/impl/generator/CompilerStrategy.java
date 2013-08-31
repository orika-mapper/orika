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
import java.io.IOException;

import ma.glasnost.orika.OrikaSystemProperties;

/**
 * Defines a standard compiler profile for use in generating mapping objects.
 * 
 * @author matt.deboer@gmail.com
 *
 */
public abstract class CompilerStrategy {

    /**
     * Compile and return the (generated) class; this will also cause the
     * generated class to be detached from the class-pool, and any (optional)
     * source and/or class files to be written.
     * 
     * @return the (generated) compiled class
     * @throws SourceCodeGenerationException
     */
    public abstract Class<?> compileClass(SourceCodeContext sourceCode) throws SourceCodeGenerationException;

    /**
     * Verify that the Class provided is accessible to the compiler/generator.
     * 
     * @param type
     * @throws SourceCodeGenerationException
     *             if the type is not accessible
     */
    public abstract void assureTypeIsAccessible(Class<?> type) throws SourceCodeGenerationException;

    protected final boolean writeSourceFiles;
    protected final boolean writeClassFiles;
    protected final String pathToWriteSourceFiles;
    protected final String pathToWriteClassFiles;
    
    protected static final String WRITE_RELATIVE_TO_CLASSPATH = "classpath:";
    
    protected CompilerStrategy(String writeSourceByDefault, String writeClassByDefault) {
	
    	this.writeSourceFiles = Boolean.valueOf(System.getProperty(
    		OrikaSystemProperties.WRITE_SOURCE_FILES,
    		System.getProperty("ma.glasnost.orika.GeneratedSourceCode.writeSourceFiles", 
    			writeSourceByDefault)));
    	
    	this.writeClassFiles = Boolean.valueOf(System.getProperty(
    		OrikaSystemProperties.WRITE_CLASS_FILES,
    		System.getProperty("ma.glasnost.orika.GeneratedSourceCode.writeClassFiles", 
    			writeClassByDefault)));
    	
    	this.pathToWriteSourceFiles = 
    			(String)System.getProperty(OrikaSystemProperties.WRITE_SOURCE_FILES_TO_PATH, 
    					WRITE_RELATIVE_TO_CLASSPATH + "/");
    	
    	this.pathToWriteClassFiles = 
    			(String)System.getProperty(OrikaSystemProperties.WRITE_CLASS_FILES_TO_PATH, 
    					WRITE_RELATIVE_TO_CLASSPATH + "/");
    }
    
    /**
     * Prepares the output path for a given package based on the provided base path string.
     * If the base path string begins with "classpath:", then the path is resolved relative
     * to this class' classpath root; otherwise, it is treated as an absolute file name.
     * 
     * @param basePath
     * @param packageName
     * @return
     * @throws IOException
     */
    protected File preparePackageOutputPath(String basePath, String packageName) throws IOException {
    	
    	String packagePath = packageName.replaceAll("\\.", "/") ;
    	String path = null;
		if (basePath.startsWith(WRITE_RELATIVE_TO_CLASSPATH)) {
			path = getClass().getResource(basePath.substring(WRITE_RELATIVE_TO_CLASSPATH.length()))
					.getFile().toString();
		} else {
			path = basePath;
			if (!path.endsWith("/")) {
				path+= "/";
			} 
		}

		File parentDir = new File(path + packagePath);
		if (!parentDir.exists() && !parentDir.mkdirs()) {
			throw new IOException("Could not create package directory for " + packageName );
		}
		
		return parentDir;
    	
    }
    
	public static class SourceCodeGenerationException extends Exception {

		private static final long serialVersionUID = 1L;

		public SourceCodeGenerationException(String message, Throwable cause) {
			super(message, cause);
		}

		public SourceCodeGenerationException(Throwable cause) {
			super(cause);
		}

		public SourceCodeGenerationException(String message) {
			super(message);
		}

	}
}
