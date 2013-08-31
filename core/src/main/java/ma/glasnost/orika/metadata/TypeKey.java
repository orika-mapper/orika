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
package ma.glasnost.orika.metadata;

import java.util.Arrays;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TypeKey provides a way to uniquely identify a {Class, Type[]} pair without
 * storing references to either class object within itself.
 * 
 * @author matt.deboer@gmail.com
 * 
 */
class TypeKey {

	private static volatile WeakHashMap<java.lang.reflect.Type, Integer> knownTypes = new WeakHashMap<java.lang.reflect.Type, Integer>();
	private static AtomicInteger currentIndex = new AtomicInteger();

	private static int getTypeIndex(java.lang.reflect.Type type) {
		Integer typeIndex = knownTypes.get(type);
		if (typeIndex == null) {
			synchronized (type) {
				typeIndex = knownTypes.get(type);
				if (typeIndex == null) {
					typeIndex = currentIndex.getAndAdd(1);
					knownTypes.put(type, typeIndex);
				}
			}

		}
		return typeIndex;
	}

	/**
	 * Merge an int value into byte array, starting at the specified starting
	 * index (occupies the next 4 bytes);
	 * 
	 * @param value
	 * @param bytes
	 * @param startIndex
	 */
	static final void intToByteArray(int value, byte[] bytes, int startIndex) {
		int i = startIndex * 4;
		bytes[i] = (byte) (value >>> 24);
		bytes[i + 1] = (byte) (value >>> 16);
		bytes[i + 2] = (byte) (value >>> 8);
		bytes[i + 3] = (byte) (value);
	}

	/**
	 * Calculates an identity for a Class, Type[] pair; avoids maintaining a
	 * reference the actual class.
	 * 
	 * @param rawType
	 * @param typeArguments
	 * @return
	 */
	static final TypeKey valueOf(Class<?> rawType,
			java.lang.reflect.Type[] typeArguments) {

		byte[] identityHashBytes = new byte[(typeArguments.length + 1) * 4];
		intToByteArray(getTypeIndex(rawType), identityHashBytes, 0);
		for (int i = 0, len = typeArguments.length; i < len; ++i) {
			intToByteArray(getTypeIndex(typeArguments[i]), identityHashBytes,
					i + 1);
		}
		return new TypeKey(identityHashBytes);
	}

	private final byte[] bytes;
	private final int hashCode;

	private TypeKey(byte[] bytes) {
		this.bytes = bytes;
		this.hashCode = Arrays.hashCode(this.bytes);
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (other.getClass() != getClass())
			return false;
		TypeKey otherKey = (TypeKey) other;

		return Arrays.equals(this.bytes, otherKey.bytes);
	}

	public int hashCode() {
		return hashCode;
	}
}
