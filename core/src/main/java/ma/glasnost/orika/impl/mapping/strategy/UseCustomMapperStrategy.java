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
 * UseCustomMapperStrategy uses a custom mapper to map the source to the destination
 */
public abstract class UseCustomMapperStrategy extends AbstractMappingStrategy {
    
    /**
     * The custom mapper resolved for this strategy
     */
    protected final Mapper<Object, Object> customMapper;
    /**
     * The Unenhancer to be used for this strategy
     */
    protected final UnenhanceStrategy unenhancer;
    
    /**
     * Creates a new instance of UseCustomMapperStrategy
     * 
     * @param sourceType
     * @param destinationType
     * @param customMapper
     * @param unenhancer
     */
    public UseCustomMapperStrategy(Type<Object> sourceType, Type<Object> destinationType, Mapper<Object, Object> customMapper,
            UnenhanceStrategy unenhancer) {
        super(sourceType, destinationType);
        this.customMapper = customMapper;
        this.unenhancer = unenhancer;
    }
    
    public Object map(final Object sourceObject, final Object destinationObject, final MappingContext context) {
        
        context.beginMapping();
        
        Object resolvedSourceObject = unenhancer.unenhanceObject(sourceObject, sourceType);
        
        Object newInstance = getInstance(resolvedSourceObject, destinationObject, context);
        
        context.cacheMappedObject(sourceObject, destinationType, newInstance);
        
        customMapper.mapAtoB(resolvedSourceObject, newInstance, context);
        
        context.endMapping();
        
        return newInstance;
    }
    
    /**
     * Gets an instance of the destination object to be mapped; may return the
     * provided destinationObject for map-in-place scenarios
     * 
     * @param sourceObject
     * @param destinationObject
     * @param context
     * @return an instance of the destination type to be mapped
     */
    protected abstract Object getInstance(Object sourceObject, Object destinationObject, MappingContext context);
}
