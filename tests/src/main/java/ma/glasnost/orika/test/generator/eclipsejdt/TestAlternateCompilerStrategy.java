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

package ma.glasnost.orika.test.generator.eclipsejdt;

import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.test.DynamicSuite;
import ma.glasnost.orika.test.DynamicSuite.Scenario;
import ma.glasnost.orika.test.DynamicSuite.TestCasePattern;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * This provides the equivalent of a test suite which will run
 * all the defined test cases (matching ".*TestCase.class") using 
 * EclipseJdtCompilerStrategy as the compiler strategy instead of 
 * JavassistCompilerStrategy which is the default.
 * 
 * @author matt.deboer@gmail.com
 *
 */ 
@RunWith(DynamicSuite.class)
@TestCasePattern(".*TestCase")
@Scenario(name="eclipseJdt")
public class TestAlternateCompilerStrategy {
    
    @BeforeClass
    public static void eclipseJdt() {
    	System.setProperty(OrikaSystemProperties.COMPILER_STRATEGY, 
    			EclipseJdtCompilerStrategy.class.getCanonicalName());
    }
   
    @AfterClass
    public static void tearDown() {
    	System.clearProperty(OrikaSystemProperties.COMPILER_STRATEGY);
    }

}
