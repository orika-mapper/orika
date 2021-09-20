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

package ma.glasnost.orika.test;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * MavenProjectUtil provides lookup of the current project's root folder,
 * assuming the default compile directory somewhere beneath the 'target' folder;
 * uses lookup of the current class' class-file as a resource and walks up 
 * to the target folder
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class MavenProjectUtil {
	public static File findProjectRoot() {
		File classFile;
        try {
	        classFile = new File(URLDecoder.decode(
	        		MavenProjectUtil.class.getClassLoader().getResource(MavenProjectUtil.class.getName().replace(".","/") + ".class").getFile(),"UTF-8"));
        } catch (UnsupportedEncodingException e) {
	        throw new RuntimeException("Failed to get Maven project root",e);
        }
		File classFolder = classFile;
		for (int i=0, len=MavenProjectUtil.class.getName().split("\\.").length; i < len; ++i)
			classFolder = classFolder.getParentFile();
		
		while(classFolder != null && !(classFolder.isDirectory() && "target".equals(classFolder.getName())))
			classFolder = classFolder.getParentFile();
		
		return classFolder != null ? classFolder.getParentFile() : null;
			
	}
}
