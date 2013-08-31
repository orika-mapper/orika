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
import ma.glasnost.orika.metadata.Type;


/**
 * FromStringConverter provides conversion from String to one of the following
 * categories of type, depending on the destination:
 * <ul>
 * <li>enum
 * <li>primitive
 * <li>primitive wrapper
 * </ul>
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class FromStringConverter extends CustomConverter<Object, Object> {

	
	public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
		return String.class == sourceType.getRawType() && destinationType.isConvertibleFromString();
	}
	
	public Object convert(Object source, Type<? extends Object> destinationType) {
		if (destinationType.isEnum()) {
			return convertToEnum((String)source, destinationType);
		} else if (destinationType.isPrimitive()) {
			return convertToPrimitive((String)source, destinationType);
		} else {
			return convertToWrapper((String)source, destinationType);
		} 
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object convertToEnum(String source, Type<? extends Object> destinationType) {
		return Enum.valueOf((Class<Enum>)destinationType.getRawType(), source.toString());
	}
	
	private Object convertToPrimitive(String source, Type<? extends Object> destinationType) {
		
		if (Character.TYPE == destinationType.getRawType()) {
			return source.charAt(0);
		} else if (Byte.TYPE == destinationType.getRawType()) {
			return Byte.parseByte(source);
		} else if (Short.TYPE == destinationType.getRawType()) {
			return Short.parseShort(source);	
		} else if (Integer.TYPE == destinationType.getRawType()) {
			return Integer.parseInt(source);
		} else if (Long.TYPE == destinationType.getRawType()) {
			return Long.parseLong(source);
		} else if (Float.TYPE == destinationType.getRawType()) {
			return Float.parseFloat(source);
		} else if (Double.TYPE == destinationType.getRawType()) {
			return Double.parseDouble(source);
		} else if (Boolean.TYPE == destinationType.getRawType()) {
			return Boolean.parseBoolean(source);
		}
		return null;
	}
	
	private Object convertToWrapper(String source, Type<? extends Object> destinationType) {
		
		if (Character.class == destinationType.getRawType()) {
			return Character.valueOf(source.charAt(0));
		} else if (Byte.class == destinationType.getRawType()) {
			return Byte.valueOf(source);
		} else if (Short.class == destinationType.getRawType()) {
			return Short.valueOf(source);	
		} else if (Integer.class == destinationType.getRawType()) {
			return Integer.valueOf(source);
		} else if (Long.class == destinationType.getRawType()) {
			return Long.valueOf(source);
		} else if (Float.class == destinationType.getRawType()) {
			return Float.valueOf(source);
		} else if (Double.class == destinationType.getRawType()) {
			return Double.valueOf(source);
		} else if (Boolean.class == destinationType.getRawType()) {
			return Boolean.valueOf(source);
		}
		return null;
	}
	
}
