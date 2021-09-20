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
package ma.glasnost.orika.test.converter;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

public class LongToStringConverter extends CustomConverter<Long, String> {
    
    public boolean canConvert(Class<Long> sourceClass, Class<? extends String> destinationClass) {
        return Long.class.equals(sourceClass) && String.class.equals(destinationClass);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.Converter#convert(java.lang.Object,
     * ma.glasnost.orika.metadata.Type)
     */
    public String convert(Long source, Type<? extends String> destinationType, MappingContext context) {
        if (source != null) {
            return source.toString();
        }
        return null;
    }
    
}
