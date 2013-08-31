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

package ma.glasnost.orika.property;

import java.util.Map;

import ma.glasnost.orika.metadata.NestedProperty;
import ma.glasnost.orika.metadata.Property;

/**
 * PropertyResolverStrategy specifies a contract for resolution of
 * mappable properties for a java type.
 * 
 * @author matt.deboer@gmail.com
 *
 */
public interface PropertyResolverStrategy {
    
    /**
     * Collects and returns the (mappable) properties for the given type.
     * Such properties are not required to have both getter and setter, 
     * as in some cases, they will participate in one-way mappings.
     * 
     * @param type the type for which to resolve properties 
     * @return a Map keyed by property names, with corresponding Property instances as values
     */
    Map<String, Property> getProperties(java.lang.reflect.Type type);
    
    /**
     * Resolves a nested property for the provided type, based on the specified
     * property expression (a sequence property names qualified by '.').
     * 
     * @param type
     * @param propertyExpression
     * @return the NestedProperty instance defined by the provided expression
     * @deprecated use {@link #getProperty(java.lang.reflect.Type, String)} instead
     */
    @Deprecated
    NestedProperty getNestedProperty(java.lang.reflect.Type type, String propertyExpression);
    
    /**
     * Resolves a property for the specified type; nested and dynamically defined properties
     * should be handled automatically.
     * 
     * @param type
     * @param dynamicPropertyExpression
     * @return the property (of any format)
     */
    Property getProperty(java.lang.reflect.Type type, String dynamicPropertyExpression);
    
    /**
     * Resolves a property for the specified type; nested and dynamically defined properties
     * should be handled automatically.
     * 
     * @param owner
     * @param dynamicPropertyExpression
     * @return the property (of any format)
     */
    Property getProperty(Property owner, String dynamicPropertyExpression);
}
