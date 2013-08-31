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
package ma.glasnost.orika.converter.builtin;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.Type;


/**
 * ConstructorConverter will converter from one type to another if there
 * exists a constructor for the destinationType with a single argument
 * matching the type of the source.
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class ConstructorConverter extends CustomConverter<Object, Object> {

	
	public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
		try {
			return destinationType.getRawType().getConstructor(sourceType.getRawType()) != null;
		} catch (NoSuchMethodException e) {
			try {
				if (sourceType.isPrimitive()) {
					return destinationType.getRawType().getConstructor(ClassUtil.getWrapperType(sourceType.getRawType())) != null;
				} else if (sourceType.isPrimitiveWrapper()) {
					return destinationType.getRawType().getConstructor(ClassUtil.getPrimitiveType(sourceType.getRawType())) != null;
				} else {
					return false;
				}
			} catch (NoSuchMethodException e1) {
				return false;
			}
		} catch (Exception e) {
			return false;
		} 
	}
	

	public Object convert(Object source, Type<? extends Object> destinationType) {
		try {
			return destinationType.getRawType().getConstructor(source.getClass()).newInstance(source);
		} catch (NoSuchMethodException e) {
			try {
				if (source.getClass().isPrimitive()) {
					return destinationType.getRawType().getConstructor(ClassUtil.getWrapperType(source.getClass())).newInstance(source);
				} else if (ClassUtil.isPrimitiveWrapper(source.getClass())) {
					return destinationType.getRawType().getConstructor(ClassUtil.getPrimitiveType(source.getClass())).newInstance(source);
				} else {
					return false;
				}
			} catch (Exception e1) {
				return false;
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		} 
	}
	
}
