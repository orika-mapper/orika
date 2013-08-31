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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple class-loader that can load classes from bytes that have been
 * pre-cached for the given class name.
 * 
 * @author matt.deboer@gmail.com
 * 
 */
public class ByteCodeClassLoader extends ClassLoader {

	private Map<String, byte[]> classData;

	public ByteCodeClassLoader(ClassLoader parent) {
		super(parent);
		classData = new ConcurrentHashMap<String, byte[]>();
	}

	/**
	 * Cache the bytes for a given class by name; will be used upon a subsequent
	 * load request.
	 * 
	 * @param name
	 * @param data
	 */
	void putClassData(String name, byte[] data) {
		classData.put(name, data);
	}

	byte[] getBytes(String name) {
		byte[] data = classData.get(name);
		return data != null ? data.clone() : null;
	}
	
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] b = classData.get(name);
		if (b == null) {
			throw new ClassNotFoundException(name);
		}
		return defineClass(name, b, 0, b.length);
	}
}
