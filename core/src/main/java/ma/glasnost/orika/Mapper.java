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
 * Defines a contract between internal parts of Orika such as
 * <code>MapperFacade</code>, <code>MapperGenerator</code>,
 * <code>MapperFactory</code> and generated mappers.
 * 
 * @author S.M. El Aatifi
 * 
 * @see MapperFacade
 */
public interface Mapper<A, B> extends MappedTypePair<A, B> {
    
    /**
     * Maps the properties of an instance of type A to the properties
     * of an instance of type B.
     * 
     * @param a the object from which to read the properties
     * @param b the object onto which the properties should be mapped
     * @param context
     */
    void mapAtoB(A a, B b, MappingContext context);
    
    /**
     * Maps the properties of an instance of type B to the properties
     * of an instance of type A.
     * 
     * @param b the object from which to read the properties
     * @param a the object onto which the properties should be mapped
     * @param context
     */
    void mapBtoA(B b, A a, MappingContext context);
    
    /**
     * Store an instance of the current MapperFacade which may be used 
     * in mapping of nested types.
     * 
     * @param mapper
     */
    void setMapperFacade(MapperFacade mapper);
    
    /**
     * Store the set of custom mappers used by this mapper.
     * @param mapper
     */
    void setUsedMappers(Mapper<Object, Object>[] mapper);
    
}
