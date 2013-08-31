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

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

/**
 * InstantiateAndUseCustomMapperStrategy uses a custom ObjectFactory and
 * a custom Mapper to map from source to destination type.
 */
public class InstantiateAndUseCustomMapperStrategy extends UseCustomMapperStrategy {
    
	/**
	 * The custom ObjectFactory used by this strategy
	 */
	protected final ObjectFactory<Object> objectFactory;
    
    /**
     * Creates a new instance of InstantiateAndUseCustomMapperStrategy
     * 
     * @param sourceType
     * @param destinationType
     * @param customMapper
     * @param objectFactory
     * @param unenhancer
     */
    public InstantiateAndUseCustomMapperStrategy(Type<Object> sourceType, Type<Object> destinationType, Mapper<Object, Object> customMapper, ObjectFactory<Object> objectFactory, UnenhanceStrategy unenhancer) {
    	super(sourceType, destinationType, customMapper, unenhancer);
        this.objectFactory = objectFactory;
    }

    protected Object getInstance(Object sourceObject, Object destinationObject, MappingContext context) {
    	return objectFactory.create(sourceObject, context);
    }
}
