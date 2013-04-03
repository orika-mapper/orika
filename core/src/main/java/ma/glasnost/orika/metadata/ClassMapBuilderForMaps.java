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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.property.PropertyResolverStrategy;

/**
 * ClassMapBuilderForMaps is a custom ClassMapBuilder instance which is
 * used for mapping standard JavaBeans to Map instances.
 *
 * @param <A>
 * @param <B>
 */
public class ClassMapBuilderForMaps<A, B> extends ClassMapBuilder<A,B> {
    
	
	public static class Factory extends ClassMapBuilderFactory {

        @Override
        protected <A, B> boolean applied(Type<A> aType, Type<B> bType) {
            return (aType.isMap() && !bType.isMap()) || (bType.isMap() && !aType.isMap());
        }

		/* (non-Javadoc)
		 * @see ma.glasnost.orika.metadata.ClassMapBuilderFactory#newClassMapBuilder(ma.glasnost.orika.metadata.Type, ma.glasnost.orika.metadata.Type, ma.glasnost.orika.property.PropertyResolverStrategy, ma.glasnost.orika.DefaultFieldMapper[])
		 */
        @Override
		protected <A, B> ClassMapBuilder<A,B> newClassMapBuilder(
				Type<A> aType, Type<B> bType,
				MapperFactory mapperFactory,
				PropertyResolverStrategy propertyResolver,
				DefaultFieldMapper[] defaults) {
			
			return new ClassMapBuilderForMaps<A,B>(aType, bType, mapperFactory, propertyResolver, defaults);
		}
	}
	
	private final Set<String> nestedTypesUsed = new HashSet<String>();
	
    /**
     * @param aType
     * @param bType
     * @param propertyResolver
     * @param defaults
     */
    protected ClassMapBuilderForMaps(Type<A> aType, Type<B> bType, MapperFactory mapperFactory, PropertyResolverStrategy propertyResolver, DefaultFieldMapper... defaults) {
	    super(aType, bType, mapperFactory, propertyResolver, defaults);
	}
       
    protected ClassMapBuilderForMaps<A, B> self() {
        return this;
    }           
    
    /**
     * @return true if the A type for this Builder is the Java Bean type
     */
    protected boolean isATypeBean() {
        return !getAType().isMap();
    }
    
    /**
     * Test whether the provided type is the special case type for this Builder
     * (as in, not the standard Java Bean type)
     * @param type
     * @return
     */
    protected boolean isSpecialCaseType(Type<?> type) {
        return type.isMap();
    }
    
    /**
     * Configures this class-map builder to employ the default property mapping
     * behavior to any properties that have not already been mapped or excluded; 
     * if any DefaultFieldMapper instances are passed, they will be used (instead of
     * those configured on the builder) to attempt a property name match if a direct 
     * match is not found.
     * 
     * @param withDefaults zero or more DefaultFieldMapper instances to apply during the default mapping;
     * if none are supplied, the configured DefaultFieldMappers for the builder (if any) should be used.
     * @return this ClassMapBuilder instance
     */
    public ClassMapBuilderForMaps<A, B> byDefault(DefaultFieldMapper... withDefaults) {
    	
    	Set<String> remainingProperties;
    	if (isATypeBean()) {
            remainingProperties = new LinkedHashSet<String>(getPropertiesForTypeA());
            remainingProperties.removeAll(getMappedPropertiesForTypeA());
        } else {
    	    remainingProperties = new LinkedHashSet<String>(getPropertiesForTypeB());
    	    remainingProperties.removeAll(getMappedPropertiesForTypeB());
    	}  
    	remainingProperties.remove("class");
    	
        for (final String propertyName : remainingProperties) {  
            /*
             * Try to avoid mapping properties for which we've already
             * mapped a nested property
             */
            if (!nestedTypesUsed.contains(propertyName)) {                
                fieldMap(propertyName, propertyName, true).add();
            }
        }
        
        return self();
    }

    protected String getParentExpression(String epxression) {
        String[] parts = epxression.split("[.]");
        StringBuilder name = new StringBuilder();
        for (int i=0; i < parts.length - 1; ++i) {
            name.append(parts[i] + ".");
        }
        return name.substring(0, name.length()-1);
    }
    
    public FieldMapBuilder<A, B> fieldMap(String fieldNameA, String fieldNameB, boolean byDefault) {
        if (isATypeBean() && isNestedPropertyExpression(fieldNameA)) {
            nestedTypesUsed.add(getParentExpression(fieldNameA));
        } else if (!isATypeBean() && isNestedPropertyExpression(fieldNameB)) {
            nestedTypesUsed.add(getParentExpression(fieldNameB));
        } 
        return super.fieldMap(fieldNameA, fieldNameB, byDefault);
    }
    
    /**
     * Resolves a property for the particular type, based on the provided property expression
     * 
     * @param rawType the type to resolve
     * @param expr the property expression to resolve
     * @return
     */
    protected Property resolveProperty(java.lang.reflect.Type rawType, String expr) {
        
        Type<?> type = TypeFactory.valueOf(rawType);
        if (isSpecialCaseType(type)) {
            Type<?> propertyType = isSpecialCaseType(getBType()) ? getBType() : getAType();
            return resolveCustomProperty(expr, propertyType);
        } else {
            return super.resolveProperty(type, expr);
        }
    }
    
    protected Property resolveCustomProperty(String expr, Type<?> propertyType) {
        return new MapKeyProperty(expr, propertyType.getNestedType(1), null);
    }
    
}
