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
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

/**
 * InstantiateByDefaultAndUseCustomMapperStrategy uses a custom mapper and creates instances
 * using the default constructor for the destination type.
 *
 */
public class InstantiateByDefaultAndUseCustomMapperStrategy extends UseCustomMapperStrategy {
    
    /**
     * Creates a new instance of InstantiateByDefaultAndUseCustomMapperStrategy
     * 
     * @param sourceType
     * @param destinationType
     * @param customMapper
     * @param unenhancer
     */
    public InstantiateByDefaultAndUseCustomMapperStrategy(Type<Object> sourceType, Type<Object> destinationType, Mapper<Object, Object> customMapper, UnenhanceStrategy unenhancer) {
    	super(sourceType, destinationType, customMapper, unenhancer);
    }

    protected Object getInstance(Object sourceObject, Object destinationObject, MappingContext context) {
    	try {
            return destinationType.getRawType().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
