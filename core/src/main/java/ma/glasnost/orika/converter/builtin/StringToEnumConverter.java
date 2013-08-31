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

import ma.glasnost.orika.converter.Converter;
import ma.glasnost.orika.converter.CustomConverterBase;


/**
 * 
 * @deprecated use {@link FromStringConverter} instead
 */
@SuppressWarnings({ "rawtypes", "unused" })
@Deprecated
public class StringToEnumConverter extends CustomConverterBase<String, Enum> {
    
    @SuppressWarnings("unchecked")
    public Enum convert(String source, Class<? extends Enum> destinationClass) {
        return Enum.valueOf(destinationClass, source);
    }
    
    public boolean canConvert(Class<String> sourceClass, Class<? extends Enum> destinationClass) {
        return String.class.equals(sourceClass) && destinationClass != null && destinationClass.isEnum();
    }
    
}
