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

package ma.glasnost.orika.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ma.glasnost.orika.property.PropertyResolver;

/**
 * NestedProperty represents a property which is accessed by
 * chaining calls to multiple properties.
 */
public class NestedProperty extends Property {
    
    private final Property[] path;
    private final Property tail;
    
    /**
     * Creates a new NestedProperty
     * 
     * @param expression
     * @param property
     * @param path
     */
    public NestedProperty(String expression, Property property, Property[] path) {
        super(expression,property.getName(),property.getGetter(),property.getSetter(),property.getType(),property.getElementType(), property.getContainer());
        this.path = collapse(path);
        this.tail = property;
    }
    
    private static Property[] collapse(Property[] path) {
        List<Property> collapsed = new ArrayList<Property>();
        collapsed.addAll(Arrays.asList(path));
        int i = 0;
        while (i < collapsed.size()) {
            Property p = collapsed.get(i);
            if (p instanceof NestedProperty) {
                collapsed.remove(i);
                for (Property element: p.getPath()) {
                    collapsed.add(i++, element);
                }
                Property tail = ((NestedProperty)p).tail;
                collapsed.add(i, tail);
            }
            ++i;
        }
        return collapsed.toArray(path);
    }
    
    @Override
    public NestedProperty copy() {
    	
    	Property[] copyPath = new Property[path.length];
    	for (int i=0, count = path.length; i < count; ++i) {
    		copyPath[i] = path[i].copy();
    	}
    	NestedProperty copy = new NestedProperty(this.getExpression(), super.copy(), copyPath);
        return copy;
    }
    
    @Override
    public Property[] getPath() {
        return path;
    }
    
    @Override
    public boolean hasPath() {
        return true;
    }
    
    public boolean equals(Object other) {
    	return super.equals(other);
    }
    
    public int hashCode() {
    	return super.hashCode();
    }
    
    public boolean isListElement() {
        return tail.isListElement();
    }
    
    public boolean isArrayElement() {
        return tail.isArrayElement();
    }
    
    public boolean isMapKey() {
        return tail.isMapKey();
    }
    
    /**
     * NestedProperty.Builder is a builder for nested properties
     */
    static class Builder extends Property.Builder {

        private Property.Builder parent;
        
        /**
         * @param owningType
         * @param name
         */
        Builder(Property.Builder parent, String name) {
            super(null, name);
            this.parent = parent;
        }
        
        public Property build(PropertyResolver propertyResolver) {
            Property parentProperty = parent.build(propertyResolver);
            
            Property[] path;
            if (parentProperty instanceof NestedProperty) {
                path = ((NestedProperty)parentProperty).getPath();
                System.arraycopy(path, 0, path, 0, path.length + 1);
                path[path.length-1] = parentProperty;
            } else {
                path = new Property[]{parentProperty};
            }
            owningType(parentProperty.getType());
            
            Property p = super.build(propertyResolver);
            return new NestedProperty("", p, path);
        }
        
    }
    
}
