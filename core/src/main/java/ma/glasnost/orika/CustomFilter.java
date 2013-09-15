/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
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
     * Constructs a new CustomFilter, invoking {@link #inferTypes} to get the A-type and B-type.
     */
    public CustomFilter() {
        MappedTypePair<A, B> types = inferTypes();
        sourceType = types.getAType();
        destinationType = types.getBType();
    }

    /**
     * Infer A-type and B-type from the generic arguments. Subclasses may override this to
     * do their own inference.
     *
     * @return the A-type and B-type
     * @throws IllegalStateException if the types cannot be inferred
     */
    protected MappedTypePair<A, B> inferTypes() {
        java.lang.reflect.Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass != null && genericSuperclass instanceof ParameterizedType) {
            final ParameterizedType superType = (ParameterizedType) genericSuperclass;
            return new MappedTypePairHolder<A, B>(
                    (Type<A>) TypeFactory.valueOf(superType.getActualTypeArguments()[0]),
                    (Type<B>) TypeFactory.valueOf(superType.getActualTypeArguments()[1]));
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
        return sourceType.isAssignableFrom(getObjectType(source.getType())) &&
               destinationType.isAssignableFrom(getObjectType(destination.getType()));
    }

    private static Type<?> getObjectType(Type<?> type) {
        return (type.isPrimitive() ? type.getWrapperType() : type);
    }
    
    /**
     * Simple implementation of MappedTypePair that holds the given pair of types.
     */
    protected static class MappedTypePairHolder<A, B> implements MappedTypePair<A, B> {
        private final Type<A> aType;
        private final Type<B> bType;

        /**
         * Create a MappedTypePairHolder with the given types.
         *
         * @param aType the A-type
         * @param bType the B-type
         */
        public MappedTypePairHolder(Type<A> aType, Type<B> bType) {
            this.aType = aType;
            this.bType = bType;
        }

        /* (non-Javadoc)
         * @see ma.glasnost.orika.MappedTypePair#getAType()
         */
        public Type<A> getAType() {
            return aType;
        }

        /* (non-Javadoc)
         * @see ma.glasnost.orika.MappedTypePair#getBType()
         */
        public Type<B> getBType() {
            return bType;
        }
    }
}
