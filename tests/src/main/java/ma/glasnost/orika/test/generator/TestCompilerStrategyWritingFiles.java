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

package ma.glasnost.orika.test.generator;

import java.io.File;

import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.test.DynamicSuite;
import ma.glasnost.orika.test.DynamicSuite.Scenario;
import ma.glasnost.orika.test.DynamicSuite.TestCasePattern;

import org.h2.util.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;

/**
 * This provides a test case that verifies that Orika generates source and 
 * class files to the locations expected.
 * 
 * It is marked to be ignored, because writing to an absolute file location
 * is generally not repeatable in a unit test.
 * The test is provided anyway for verification of the functionality.
 * 
 * @author matt.deboer@gmail.com
 *
 */ 
@RunWith(DynamicSuite.class)
@TestCasePattern(".*TestCase.class")
@Scenario(name="writeToUserHome")
@Ignore 
public class TestCompilerStrategyWritingFiles {
    
    @BeforeClass
    public static void eclipseJdt() {
    	System.setProperty(OrikaSystemProperties.COMPILER_STRATEGY, EclipseJdtCompilerStrategy.class.getName());
    	System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES_TO_PATH, 
    			System.getProperty("user.home")+"/.orikaGenerated/src/");
    	System.setProperty(OrikaSystemProperties.WRITE_CLASS_FILES_TO_PATH, 
    			System.getProperty("user.home")+"/.orikaGenerated/bin/");
    	System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES, "true");
    	System.setProperty(OrikaSystemProperties.WRITE_CLASS_FILES, "true");
    	
    }
   
    @AfterClass
    public static void tearDown() {
    	System.clearProperty(OrikaSystemProperties.WRITE_SOURCE_FILES_TO_PATH);
    	System.clearProperty(OrikaSystemProperties.WRITE_CLASS_FILES_TO_PATH);
    	System.clearProperty(OrikaSystemProperties.WRITE_SOURCE_FILES);
    	System.clearProperty(OrikaSystemProperties.WRITE_CLASS_FILES);
    	System.clearProperty(OrikaSystemProperties.COMPILER_STRATEGY);
    	
    	File generatedSrc = new File(System.getProperty("user.home")+"/.orikaGenerated/src/");
    	File generatedBin = new File(System.getProperty("user.home")+"/.orikaGenerated/bin/");
    	File generatedSrcFiles = null;
    	File generatedBinFiles = null;
    	try {
    		Assert.assertTrue(generatedSrc.exists());
    		generatedSrcFiles = new File(generatedSrc,"ma/glasnost/orika/generated/");
        	Assert.assertTrue(generatedSrcFiles.isDirectory());
        	Assert.assertTrue(generatedSrcFiles.listFiles().length > 0);
        	
        	Assert.assertTrue(generatedBin.exists());
        	generatedBinFiles = new File(generatedBin,"ma/glasnost/orika/generated/");
        	Assert.assertTrue(generatedBinFiles.isDirectory());
        	Assert.assertTrue(generatedBinFiles.listFiles().length > 0);
    	
    	} finally {
    		// Note: this is coming from hibernate jar which is included only for test
    		IOUtils.deleteRecursive(generatedSrc.getParentFile().getAbsolutePath(), true);
    	}
    	
    }

}
