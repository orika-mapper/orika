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

import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * CustomFilter provides the base implementation of Filter.
 * 
 * @author mattdeboer
 * 
 * @param <A>
 * @param <B>
 */
public abstract class CustomFilter<A, B> implements Filter<A, B> {
    
    private final Type<A> sourceType;
    private final Type<B> destinationType;
    
    /**
     * Constructs a new CustomFilter, inferring A-type and B-type from
     * the generic arguments.
     */
    public CustomFilter() {
        java.lang.reflect.Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass != null && genericSuperclass instanceof ParameterizedType) {
            ParameterizedType superType = (ParameterizedType) genericSuperclass;
            sourceType = TypeFactory.valueOf(superType.getActualTypeArguments()[0]);
            destinationType = TypeFactory.valueOf(superType.getActualTypeArguments()[1]);
        } else {
            throw new IllegalStateException("When you subclass the ConverterBase S and D type-parameters are required.");
        }
    }
    
    /* (non-Javadoc)
     * @see ma.glasnost.orika.MappedTypePair#getAType()
     */
    public Type<A> getAType() {
        return sourceType;
    }
    
    /* (non-Javadoc)
     * @see ma.glasnost.orika.MappedTypePair#getBType()
     */
    public Type<B> getBType() {
        return destinationType;
    }
    
    /* (non-Javadoc)
     * @see ma.glasnost.orika.Filter#appliesTo(ma.glasnost.orika.metadata.Property, ma.glasnost.orika.metadata.Property)
     */
    public boolean appliesTo(Property source, Property destination) {
        return sourceType.isAssignableFrom(source.getType()) && destinationType.isAssignableFrom(destination.getType());
    }
    
}
