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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class FilePathUtility {

	static String readFileAsString(File filePath) throws IOException {
		StringBuffer fileData = new StringBuffer(2048);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[2048];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			fileData.append(buf, 0, numRead);
		}
		reader.close();
		return fileData.toString();
	}

	static String getJavaPackage(File sourceFile, File classPathRoot) {
		return sourceFile.getParentFile().getAbsolutePath()
				.replace(classPathRoot.getAbsolutePath(), "")
				.replaceAll("(\\\\|/)", ".")
				.replaceAll("(^\\.|\\.$)", "");
	}

	static File createTempDirectory() throws IOException {
		final File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
	   if(!(temp.delete())) {
	       throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
	   }
	   if(!(temp.mkdir())) {
	       throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
	   }
	   return temp;
	}
	
	static String getJavaClassName(File javaSource) {
		return javaSource.getName().replace(".java", "");
	}

	static File getClassFilePath(String key, File binDir) {
		return new File(binDir, key.replace(".", "/") + ".class");
	}
	
	static void writeClassFile(String className, byte[] classFileBytes, File binDir) throws IOException {
		
		File destination = getClassFilePath(className, binDir);
		if (!destination.getParentFile().exists() && !destination.getParentFile().mkdirs()) {
			throw new IOException("Could not create class at output path " + destination);
		}
		destination.createNewFile();
		FileOutputStream out = new FileOutputStream(destination);
		out.write(classFileBytes);
		out.close();
	}
	
	static Collection<File> getJavaSourceFiles(File sourceDir) {
		
		Set<File> javaSources = new HashSet<File>();
		LinkedList<File> stack = new LinkedList<File>();
		stack.add(sourceDir);
		while (!stack.isEmpty()) {
			File current = stack.removeFirst();
			if (current.exists()) {
				if (current.isDirectory()) {
					stack.addAll(Arrays.asList(current.listFiles()));
				} else if (current.getName().endsWith(".java")) {
					javaSources.add(current);
				}
			}
		}
		return javaSources;
	}
}
