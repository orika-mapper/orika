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

package ma.glasnost.orika.impl.mapping.strategy;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

/**
 *
 */
public class UseConverterStrategy extends AbstractMappingStrategy {
    
    private final Converter<Object,Object> converter;
    private final UnenhanceStrategy unenhancer;
    
    /**
     * Creates a new instance of UseConverterStrategy
     * 
     * @param sourceType
     * @param destinationType
     * @param converter
     * @param unenhancer
     */
    public UseConverterStrategy(Type<Object> sourceType, Type<Object> destinationType, Converter<Object,Object> converter, UnenhanceStrategy unenhancer) {
        super(sourceType, destinationType);
        this.converter = converter;
        this.unenhancer = unenhancer;
    }

    public Object map(Object sourceObject, Object destinationObject, MappingContext context) {
        // TODO: mappingContext is not passed to converters, which could 
        //       be a problem with converters now (recently) having access
        //       to the MapperFacade; 
        return converter.convert(unenhancer.unenhanceObject(sourceObject, sourceType), destinationType);
    }
}
