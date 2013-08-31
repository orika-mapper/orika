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
 * Converts any object to string
 * 
 * @author matt.deboer@gmail.com
 */
public class ToStringConverter extends CustomConverter<Object, Object> {

    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
        return destinationType.isString();
    }
    
	public Object convert(Object source, Type<? extends Object> destinationType) {
		return "" + source;
	}
}
