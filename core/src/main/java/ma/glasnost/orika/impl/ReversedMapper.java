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

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

/**
 * ReversedMapper is used to wrap an existing mapper and reverse it's direction
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class ReversedMapper<A, B> implements Mapper<A, B> {
        
    private Mapper<B, A> reversedMapper;

    /**
     * Reterns a Mapper which is a reversal of the supplied Mapper
     * 
     * @param mapperToReverse
     * @return
     */
    public static <A,B> Mapper<A, B> reverse(Mapper<B, A> mapperToReverse) {
        /*
         * Avoid nesting reversed mappers by unwrapping an existing reversed mapper
         */
        if (mapperToReverse instanceof ReversedMapper) {
            return ((ReversedMapper<B,A>)mapperToReverse).reversedMapper;
        } else {
            return new ReversedMapper<A,B>(mapperToReverse);
        }
    }
    
    /**
     * Constructs a new ReversedMapper which reverses the directions mapped by the specified mapper
     * 
     * @param mapperToReverse
     */
    private ReversedMapper(Mapper<B,A> mapperToReverse) {
        this.reversedMapper = mapperToReverse;
    }
    
    /* (non-Javadoc)
     * @see ma.glasnost.orika.Mapper#mapAtoB(java.lang.Object, java.lang.Object, ma.glasnost.orika.MappingContext)
     */
    public void mapAtoB(A a, B b, MappingContext context) {
        reversedMapper.mapBtoA(a, b, context);
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.Mapper#mapBtoA(java.lang.Object, java.lang.Object, ma.glasnost.orika.MappingContext)
     */
    public void mapBtoA(B b, A a, MappingContext context) {
        reversedMapper.mapAtoB(b, a, context);
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.Mapper#setMapperFacade(ma.glasnost.orika.MapperFacade)
     */
    public void setMapperFacade(MapperFacade mapper) {
        reversedMapper.setMapperFacade(mapper);
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.Mapper#setUsedMappers(ma.glasnost.orika.Mapper<java.lang.Object,java.lang.Object>[])
     */
    public void setUsedMappers(final Mapper<Object, Object>[] mappers) {
        /*
         * Flip the used mappers; assume that they have been sent in according
         * to the proper direction
         */
        Mapper<Object, Object>[] usedMappers = mappers.clone();
        for(int i=0; i < usedMappers.length; ++i) {
            usedMappers[i] = reverse(usedMappers[i]);
        }
        reversedMapper.setUsedMappers(usedMappers);
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.Mapper#getAType()
     */
    public Type<A> getAType() {
        return reversedMapper.getBType();
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.Mapper#getBType()
     */
    public Type<B> getBType() {
        return reversedMapper.getAType();
    } 
}
