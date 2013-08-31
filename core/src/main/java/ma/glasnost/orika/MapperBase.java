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

/**
 * Abstract super-class for all generated mappers and user custom mappers.
 * 
 * @see ma.glasnost.orika.metadata.ClassMapBuilder
 * @author S.M. El Aatifi
 * @deprecated use {@link ma.glasnost.orika.CustomMapper} instead
 */
@Deprecated
public abstract class MapperBase<A, B> {
    
    protected MapperFacade mapperFacade;
    
    public void mapAtoB(A a, B b, MappingContext context) {
        /* */
    }
    
    public void mapBtoA(B b, A a, MappingContext context) {
        /* */
    }
    
    public void setMapperFacade(MapperFacade mapper) {
        this.mapperFacade = mapper;
    }
    
    public Class<A> getAType() {
        throw throwShouldNotCalledCustomMapper();
    }
    
    public Class<B> getBType() {
        throw throwShouldNotCalledCustomMapper();
    }
    
    public void setUsedMappers(Mapper<Object, Object>[] mapper) {
        throw throwShouldNotCalledCustomMapper();
    }
    
    private IllegalStateException throwShouldNotCalledCustomMapper() {
        return new IllegalStateException("Should not be called for a user custom mapper.");
    }
    
    /**
     * Provides backward-compatibility for custom mappers that extend
     * the deprecated MapperBase.
     * 
     * @author matt.deboer@gmail.com
     *
     * @param <A>
     * @param <B>
     */
    public static class MapperBaseAdapter<A, B> extends CustomMapper<A, B> {
        private MapperBase<A, B> delegate;
        
        public MapperBaseAdapter(MapperBase<A, B> delegate) {
            this.delegate = delegate;
        }
        
        public void mapAtoB(A a, B b, MappingContext context) {
            delegate.mapAtoB(a, b, context);
        }
        
        public void mapBtoA(B b, A a, MappingContext context) {
            delegate.mapBtoA(b, a, context);
        }
        
        public void setMapperFacade(MapperFacade mapper) {
            delegate.setMapperFacade(mapper);
        }
    }

}
