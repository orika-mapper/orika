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
 * EnumConverter is used to convert from one enum to another, based on
 * exact name match
 */
public class EnumConverter extends CustomConverter<Object, Object> {
    
    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
        return sourceType.isEnum() && destinationType.isEnum();
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
     */
    public Object convert(Object source, Type<? extends Object> destinationType) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Enum<?> result = (Enum<?>) Enum.valueOf((Class<Enum>)destinationType.getRawType(), ((Enum)source).name());
        return result;
    }
}
