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

package ma.glasnost.orika.inheritance;

import ma.glasnost.orika.metadata.Type;


public final class SuperTypeResolver {
	
	
	@SuppressWarnings("unchecked")
    public static <T> Type<T> getSuperType(final Type<?> enhancedClass, final SuperTypeResolverStrategy strategy) {
    	
		Type<T> mappedType = (Type<T>) enhancedClass;
    	if (strategy.shouldLookupSuperType(mappedType)) {
    		
    		Type<T> mappedSuper = (Type<T>)tryFirstLookupOption(mappedType,strategy);
    		if (mappedSuper!=null) {
    			mappedType = mappedSuper;
    		} else {
    			mappedSuper = (Type<T>) trySecondLookupOption(mappedType,strategy);
    			if (mappedSuper!=null) {
        			mappedType = mappedSuper;
        		}
    		}
    		
    	}
    	return mappedType;
    }
	
	private static Type<?> tryFirstLookupOption(final Type<?> theClass, final SuperTypeResolverStrategy strategy) {
		if (strategy.shouldPreferClassOverInterface()) {
			return lookupMappedSuperType(theClass,strategy);
		} else {
			return lookupMappedInterface(theClass,strategy);
		}
	}
	
	private static Type<?> trySecondLookupOption(final Type<?> theClass, final SuperTypeResolverStrategy strategy) {
		if (strategy.shouldPreferClassOverInterface()) {
			return lookupMappedInterface(theClass,strategy);
		} else {
			return lookupMappedSuperType(theClass,strategy);
		}
	}
	
	private static Type<?> lookupMappedSuperType(final Type<?> type, final SuperTypeResolverStrategy strategy) { 
    	
		Type<?> targetType = type.getSuperType();
		Type<?> mappedType = null;
    	
    	while (mappedType==null && targetType!=null && !targetType.getRawType().equals(Object.class)) {
    		
    		if(strategy.accept(targetType)) {
    			mappedType = targetType;
    			break;
    		} 
    		targetType = targetType.getSuperType();
    	}
    	
    	return mappedType;
    }
    
    private static Type<?> lookupMappedInterface(final Type<?> type, final SuperTypeResolverStrategy strategy) {
    	
    	Type<?> targetType = type;
		Type<?> mappedType = null;
    	
		while (mappedType==null && targetType!=null && !targetType.getRawType().equals(Object.class)) {
	    	
    		for (Type<?> theInterface: targetType.getInterfaces()) {
	    		if(strategy.accept(theInterface)) {
	    			mappedType = theInterface;
	    			break;
	    		} 
    		}
    		targetType = targetType.getSuperType();
		}
    	
    	return mappedType;
    }
    
}
