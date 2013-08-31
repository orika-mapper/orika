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
package ma.glasnost.orika.impl;

import java.util.Arrays;
import java.util.List;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class ExceptionUtility {
    
    public static List<String> corePackages = Arrays.asList(
            "java.","javax.","sun.", "sunw.", "com.sun.", "com.ibm.", 
            "javassist.", "com.thoughtworks.paranamer.");
    
    /**
     * Tests whether the passed throwable was originated by orika mapper code.
     * 
     * @param t
     * @return
     */
    public static boolean originatedByOrika(Throwable t) {
        for (StackTraceElement ste: t.getStackTrace()) {
            if (isJreClass(ste.getClassName())) {
                continue;
            } else if (isOrikaClass(ste.getClassName())) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    
    private static boolean isOrikaClass(String className) {
        return className.startsWith("ma.glasnost.orika.") && !className.startsWith("ma.glasnost.orika.test.");
    }
    
    private static boolean isJreClass(String className) {
        for (String pkg: corePackages) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }
}
