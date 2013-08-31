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

package ma.glasnost.orika.impl.generator.eclipsejdt;

import java.util.StringTokenizer;

import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

public class CompilationUnit implements ICompilationUnit {
    
    private String packageName;
    private String source;
    private String mainTypeName;

    /**
     * @param source the source text from which the class will be compiled
     * @param packageName the name of the package
     * @param simpleClassName this is the name of the class without the package, but including
     * the parent type if the class is a nested class.
     */
	public CompilationUnit(String source, String packageName, String simpleClassName) {
		this.packageName = packageName;
		if (simpleClassName.contains(".")) {
			mainTypeName = simpleClassName.split("[.]")[0];
		} else {
			mainTypeName = simpleClassName;
		}
		this.source = source;
	}

	public char[] getFileName() {
		return (mainTypeName + ".java").toCharArray();
	}

	public char[] getContents() {
		return source.toCharArray();
	}

	public char[] getMainTypeName() {
		return mainTypeName.toCharArray();
	}

	public char[][] getPackageName() {
		StringTokenizer izer = new StringTokenizer(packageName, ".");
		char[][] result = new char[izer.countTokens()][];
		for (int i = 0; i < result.length; i++) {
			String tok = izer.nextToken();
			result[i] = tok.toCharArray();
		}
		return result;
	}
}
