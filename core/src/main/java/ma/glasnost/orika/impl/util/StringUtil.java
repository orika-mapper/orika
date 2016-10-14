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
package ma.glasnost.orika.impl.util;


import static java.lang.Character.isJavaIdentifierPart;
import static java.lang.Character.isJavaIdentifierStart;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class StringUtil {
    
    public static String capitalize(String string) {
        if ("".equals(string)) {
            return "";
        } else if (string.length() == 1) {
            return string.substring(0,1).toUpperCase();
        } else {
            return string.substring(0,1).toUpperCase() + string.substring(1);
        }
    }

    public static String toValidVariableName(String string) {
        StringBuilder output = new StringBuilder();

        if (!isJavaIdentifierStart(string.charAt(0))) {
            output.append("_");
        }

        for (int i=0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (isJavaIdentifierPart(character)) {
                output.append(character);
            }
            else {
                output.append("_");
            }
        }
        return output.toString();
    }
}
