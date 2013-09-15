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

import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;

/**
 * Filter defines the contract for manipulating the runtime behavior of
 * generated mappers.
 * 
 * @author mattdeboer
 * 
 * @param <A>
 * @param <B>
 */
public interface Filter<A, B> extends MappedTypePair<A, B> {
    
    /**
     * This method will be called at build time to determine if this filter
     * should be applied to the mapping of the specified properties
     * 
     * @param source
     *            the source property
     * @param destination
     *            the destination property
     * @return true if this Filter applies
     */
    public boolean appliesTo(Property source, Property destination);
    
    /**
     * Called at code generation time to determine whether this filter modifies
     * the source.
     * <p>
     * Implementations should not implement dynamic behavior here, as this
     * method will most likely be called only once (at generation time) for a
     * given Filter instance.
     * 
     * @return true if this Filter should be called to filter the source value
     *         the mapping results
     */
    public boolean filtersSource();
    
    /**
     * Called at code generation time to determine whether this filter modifies
     * the destination.
     * <p>
     * Implementations should not implement dynamic behavior here, as this
     * method will most likely be called only once (at generation time) for a
     * given Filter instance.
     * 
     * @return true if this Filter should be called to filter the destination of
     *         the mapping results
     */
    public boolean filtersDestination();
    
    /**
     * This method is called at runtime to determine whether the mapping implied
     * by the field names and types should be performed; if <code>false</code>
     * is returned, the mapping is skipped.
     * 
     * @param sourceType
     *            the type of the source field
     * @param sourceName
     *            the name of the source field
     * @param source
     *            the value of the source field
     * @param destType
     *            the type of the destination field
     * @param destName
     *            the name of the destination field
     * @param mappingContext
     *            the current mapping context
     * @return true if the fields represented by these types and names
     */
    public boolean shouldMap(Type<?> sourceType, String sourceName, A source, Type<?> destType, String destName,
            MappingContext mappingContext);
    
    /**
     * This method is called to provide the Filter an opportunity to modify the destination
     * field's value in some way before it is mapped onto the destination type. 
     * <p>
     * Note that the
     * return value should still be an instance of the provided destination type, else ClassCastException 
     * will likely occur. 
     * 
     * @param destinationValue
     *            the destination value
     * @param sourceType
     *            the type of the source field
     * @param sourceName
     *            the name of the source field
     * @param destType
     *            the type of the destination field
     * @param destName
     *            the name of the destination field
     * @param mappingContext
     *            the current mapping context
     * @return the filtered output value
     */
    public <D> D filterDestination(D destinationValue, Type<?> sourceType, String sourceName, Type<D> destType, String destName,
            MappingContext mappingContext);
    
    /**
     * This method is called to provide the Filter an opportunity to replace the source
     * field value before it is passed into the mapping code which transforms it to the 
     * destination type. 
     * <p>
     * It's recommended that the filter should return a new instance if it's necessary to 
     * modify the source, as a mapping request is not generally expected to have side 
     * effects on the source.
     * 
     * @param sourceValue
     *            the source value
     * @param sourceType
     *            the type of the source field
     * @param sourceName
     *            the name of the source field
     * @param destType
     *            the type of the destination field
     * @param destName
     *            the name of the destination field
     * @param mappingContext
     *            the current mapping context
     * @return the filtered output value
     */
    public <S> S filterSource(S sourceValue, Type<S> sourceType, String sourceName, Type<?> destType, String destName,
            MappingContext mappingContext);
    
}
