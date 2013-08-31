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

package ma.glasnost.orika.impl;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

public abstract class GeneratedMapperBase extends GeneratedObjectBase implements Mapper<Object, Object> {
    
    protected Mapper<Object, Object> customMapper;
    private Mapper<Object, Object>[] usedMappers;
    private Type<Object> aType;
    private Type<Object> bType;
    
    public Type<Object> getAType() {
        return aType;
    }
    
    public Type<Object> getBType() {
        return bType;
    }
    
    @SuppressWarnings("unchecked")
    public void setAType(Type<?> aType) {
        this.aType = (Type<Object>) aType;
    }
    
    @SuppressWarnings("unchecked")
    public void setBType(Type<?> bType) {
        this.bType = (Type<Object>) bType;
    }
    
    public void setCustomMapper(Mapper<Object, Object> customMapper) {
        this.customMapper = customMapper;
        this.customMapper.setMapperFacade(mapperFacade);
    }
    
    protected Mapper<Object, Object>[] getUsedMappers() {
        return usedMappers;
    }
    
    public void setUsedMappers(Mapper<Object, Object>[] usedMappers) {
        this.usedMappers = usedMappers;
    }
    
    public void setUsedTypes(Type<Object>[] types) {
        this.usedTypes = types;
    }
    
    public void setUsedConverters(Converter<Object, Object>[] usedConverters) {
        this.usedConverters = usedConverters;
    }
    
    public void setUsedMapperFacades(BoundMapperFacade<Object, Object>[] usedMapperFacades) {
        this.usedMapperFacades = usedMapperFacades;
    }
    
    public void mapAtoB(Object a, Object b, MappingContext context) {
        if (usedMappers == null) {
            return;
        }
        for (Mapper<Object, Object> mapper : usedMappers) {
            mapper.mapAtoB(a, b, context);
        }
    }
    
    public void mapBtoA(Object b, Object a, MappingContext context) {
        if (usedMappers == null) {
            return;
        }
        for (Mapper<Object, Object> mapper : usedMappers) {
            mapper.mapBtoA(b, a, context);
        }
    }
    
    public String toString() {
        return "GeneratedMapper(" + aType + ", " + bType + ")";
    }
}