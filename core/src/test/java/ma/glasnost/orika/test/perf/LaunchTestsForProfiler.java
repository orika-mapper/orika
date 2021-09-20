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

import java.io.File;
import java.io.IOException;

import ma.glasnost.orika.test.DynamicSuite;

import org.junit.runner.JUnitCore;

/**
 * LaunchTestsForProfiler provides a launcher for using the VisualVm (or another)
 * profiler over all of the unit tests.<br>
 * It provides a break at the beginning pausing for input which allows
 * attaching/configuring the profiler, as well as a similar pause at the
 * end to allow for capturing/saving results.<br><br>
 * 
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class LaunchTestsForProfiler {
    
    public static void main(String[] args) throws IOException {
        
        File classFolder = new File(LaunchTestsForProfiler.class.getResource("/").getFile());
        Class<?>[] testClasses = DynamicSuite.findTestCases(classFolder, ".*TestCase").toArray(new Class<?>[0]);
        
        
        System.out.println("Press enter when ready to start...");
        System.in.read();
        
        /*
         * Manually fire the set of test classes; this avoids having this test included when all
         * test cases are run within an IDE, since this is a special case used only for profiling
         */
        JUnitCore.runClasses(testClasses);
        
        System.out.println("Press enter when ready to quit...");
        System.in.read();
        System.in.read();
        
    }
}
