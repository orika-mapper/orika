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
 */
public class StringUtil {

    public static String capitalize(String string) {
        if ("".equals(string)) {
            return "";
        } else if (string.length() == 1) {
            return string.substring(0, 1).toUpperCase();
        } else {
            return string.substring(0, 1).toUpperCase() + string.substring(1);
        }
    }

    /**
     * Remark: Copied from commons-lang3 v3.5
     *
     * <p>Uncapitalizes a String, changing the first character to lower case as per {@link
     * Character#toLowerCase(char)}. No other characters are changed.</p>
     *
     * <p>For a word based algorithm, see {@link //org.apache.commons.lang3.text.WordUtils#uncapitalize(String)}.
     * A {@code null} input String returns {@code null}.</p>
     *
     * <pre>
     * StringUtils.uncapitalize(null)  = null
     * StringUtils.uncapitalize("")    = ""
     * StringUtils.uncapitalize("cat") = "cat"
     * StringUtils.uncapitalize("Cat") = "cat"
     * StringUtils.uncapitalize("CAT") = "cAT"
     * </pre>
     *
     * @param str the String to uncapitalize, may be null
     * @return the uncapitalized String, {@code null} if null String input
     * @see //org.apache.commons.lang3.text.WordUtils#uncapitalize(String)
     * @see #capitalize(String)
     * @since 2.0
     */
    public static String uncapitalize(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        final char firstChar = str.charAt(0);
        final char newChar = Character.toLowerCase(firstChar);
        if (firstChar == newChar) {
            // already uncapitalized
            return str;
        }

        char[] newChars = new char[strLen];
        newChars[0] = newChar;
        str.getChars(1, strLen, newChars, 1);
        return String.valueOf(newChars);
    }

    public static String toValidVariableName(String string) {
        StringBuilder output = new StringBuilder();

        if (!isJavaIdentifierStart(string.charAt(0))) {
            output.append("_");
        }

        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (isJavaIdentifierPart(character)) {
                output.append(character);
            } else {
                output.append("_");
            }
        }
        return output.toString();
    }
}
