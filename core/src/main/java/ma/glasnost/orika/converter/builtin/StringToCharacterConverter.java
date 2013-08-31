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

import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

/**
 * 
 *
 */
public class StringToCharacterConverter extends BidirectionalConverter<String, Character> {

    /* (non-Javadoc)
     * @see ma.glasnost.orika.converter.BidirectionalConverter#convertTo(java.lang.Object, ma.glasnost.orika.metadata.Type)
     */
    @Override
    public Character convertTo(String source, Type<Character> destinationType) {
        return source.charAt(0);
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.converter.BidirectionalConverter#convertFrom(java.lang.Object, ma.glasnost.orika.metadata.Type)
     */
    @Override
    public String convertFrom(Character source, Type<String> destinationType) {
        return "" + source;
    }
    
}
