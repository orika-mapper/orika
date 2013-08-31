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

package ma.glasnost.orika;

import java.lang.reflect.ParameterizedType;

import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * Abstract super-class for all generated mappers and user custom mappers.
 * 
 * @see ma.glasnost.orika.metadata.ClassMapBuilder
 * @author S.M. El Aatifi
 * 
 */
public abstract class CustomMapper<A, B> implements Mapper<A, B> {
    
    protected Type<A> aType;
    protected Type<B> bType;
    protected MapperFacade mapperFacade;
    
    public CustomMapper() {
        java.lang.reflect.Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass != null && genericSuperclass instanceof ParameterizedType) {
            ParameterizedType superType = (ParameterizedType)getClass().getGenericSuperclass();
            try {
                aType = TypeFactory.valueOf(superType.getActualTypeArguments()[0]);
                bType = TypeFactory.valueOf(superType.getActualTypeArguments()[1]);
            } catch (IllegalArgumentException e) {
                /* do nothing; this was not extended by a user 
                 * getXXType methods must be manually overridden 
                 */
            }
        }
    }
    
    public void mapAtoB(A a, B b, MappingContext context) {
        /* */
    }
    
    public void mapBtoA(B b, A a, MappingContext context) {
        /* */
    }
    
    public Type<A> getAType() {
        if (aType==null) {
            throw new IllegalStateException("getAType() must be overridden when Type parameters are not supplied");
        }
        return aType;
    }
    
    public Type<B> getBType() {
        if (bType==null) {
            throw new IllegalStateException("getBType() must be overridden when Type parameters are not supplied");
        }
        return bType;
    }
    
    public void setMapperFacade(MapperFacade mapperFacade) {
        this.mapperFacade = mapperFacade;
    }
    
    public void setUsedMappers(Mapper<Object, Object>[] mapper) {
        throw throwShouldNotCalledCustomMapper();
    }
    
    private IllegalStateException throwShouldNotCalledCustomMapper() {
        return new IllegalStateException("Should not be called for a user custom mapper.");
    }
}
