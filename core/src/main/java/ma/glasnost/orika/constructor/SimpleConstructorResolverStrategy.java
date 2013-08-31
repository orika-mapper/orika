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
package ma.glasnost.orika.constructor;

import static ma.glasnost.orika.impl.Specifications.aMappingOfTheRequiredClassProperty;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.MappingDirection;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.ParameterNamesNotFoundException;
import com.thoughtworks.paranamer.Paranamer;

/**
 * SimpleConstructorResolverStrategy attempts to resolve the appropriate constructor
 * to use in a field mapping by the following algorithm:
 * <ol>
 * <li>If an explicit constructor has been defined (based on parameter names), then use it
 * <li>Attempt to find a constructor which has parameter names matching all of the mapped
 * property names of the destination class
 * <li>Return the first constructor in the list
 * </ol>
 *
 */
public class SimpleConstructorResolverStrategy implements ConstructorResolverStrategy {
    
	private Paranamer paranamer = new CachingParanamer(new AdaptiveParanamer(new BytecodeReadingParanamer(), new AnnotationParanamer()));
	
    @SuppressWarnings({ "unchecked" })
    public <T, A, B> ConstructorMapping<T> resolve(ClassMap<A, B> classMap, Type<T> sourceType) {
        boolean aToB = classMap.getBType().equals(sourceType);
        
        Type<?> targetClass = aToB ? classMap.getBType() : classMap.getAType();
        
        String[] declaredParameterNames = aToB ? classMap.getConstructorB() : classMap.getConstructorA();
        
        Map<String, FieldMap> targetParameters = new LinkedHashMap<String, FieldMap>();
        if (declaredParameterNames != null) {
        	/*
        	 * An override to the property names was provided
        	 */
        	Set<FieldMap> fields = new HashSet<FieldMap>(classMap.getFieldsMapping());
        	for (String arg: declaredParameterNames) {
        		Iterator<FieldMap> iter = fields.iterator();
        		while(iter.hasNext()) {
        			FieldMap fieldMap = iter.next();
        			if (!fieldMap.is(aMappingOfTheRequiredClassProperty())) {
	        			if ( !aToB) {
	        				fieldMap = fieldMap.flip();
	        			}
	        			if (fieldMap.getSource().getName().equals(arg)) {
	        				targetParameters.put(arg, fieldMap);
	        				iter.remove();
	        			}
        			}
        		}
        	}
        } else {
        	/*
        	 * Determine the set of constructor argument names
        	 * from the field mapping
        	 */
        	for(FieldMap fieldMap: classMap.getFieldsMapping()) {
        		if (!fieldMap.is(aMappingOfTheRequiredClassProperty())) {
        			if (!aToB) {
        				fieldMap = fieldMap.flip();
        			}
	        		targetParameters.put(fieldMap.getDestination().getName(), fieldMap);
        		}
        	}
        	
        }
        
        Constructor<T>[] constructors = (Constructor<T>[]) targetClass.getRawType().getConstructors();
        TreeMap<Integer, ConstructorMapping<T>> constructorsByMatchedParams = new TreeMap<Integer, ConstructorMapping<T>>();
        for (Constructor<T> constructor: constructors) {
        	ConstructorMapping<T> constructorMapping = new ConstructorMapping<T>();
        	constructorMapping.setDeclaredParameters(declaredParameterNames);
        	boolean byDefault = declaredParameterNames == null;
        	
        	try {
        		/*
        		 * 1) A constructor's parameters are all matched by known parameter names
        		 * 2) ...
        		 */
        		String[] parameterNames = paranamer.lookupParameterNames(constructor);
        		java.lang.reflect.Type[] genericParameterTypes = constructor.getGenericParameterTypes();
        		Type<?>[] parameterTypes = new Type[genericParameterTypes.length];
        		constructorMapping.setParameterNameInfoAvailable(true);
        		if (targetParameters.keySet().containsAll(Arrays.asList(parameterNames))) {
        			constructorMapping.setConstructor(constructor);
        			for (int i=0; i < parameterNames.length; ++i) {
        				String parameterName = parameterNames[i];
        				parameterTypes[i] = TypeFactory.valueOf(genericParameterTypes[i]);
        				FieldMap existingField = targetParameters.get(parameterName);
        				FieldMap argumentMap = mapConstructorArgument(existingField, parameterTypes[i], byDefault);
        				constructorMapping.getMappedFields().add(argumentMap);
        			}
        			constructorMapping.setParameterTypes(parameterTypes);
        			constructorsByMatchedParams.put(parameterNames.length*1000, constructorMapping);
        		}
        	} catch (ParameterNamesNotFoundException e) {
        		/*
        		 * Could not find parameter names of the constructors; attempt to match constructors
        		 * based on the types of the destination properties
        		 */
        	     List<FieldMap> targetTypes = new ArrayList<FieldMap>(targetParameters.values());
    	    	 int matchScore = 0;
    	    	 int exactMatches = 0;
    	    	 java.lang.reflect.Type[] params = constructor.getGenericParameterTypes();
    	    	 Type<?>[] parameterTypes = new Type[params.length];
        	     for (int i=0; i < params.length; ++i) {
        	    	java.lang.reflect.Type param = params[i];
        	    	
        	    	parameterTypes[i] = TypeFactory.valueOf(param);
    	    		for (Iterator<FieldMap> iter = targetTypes.iterator(); iter.hasNext();) {
    	    			FieldMap fieldMap = iter.next();
    	    			Type<?> targetType = fieldMap.getDestination().getType();
    	    			if ((parameterTypes[i].equals(targetType) && ++exactMatches != 0) 
    	    			        || parameterTypes[i].isAssignableFrom(targetType) ) {
    	    				++matchScore;
    	    				
    	    				String parameterName = fieldMap.getDestination().getName();
            				FieldMap existingField = targetParameters.get(parameterName);
            				FieldMap argumentMap = mapConstructorArgument(existingField, parameterTypes[i], byDefault);
            				constructorMapping.getMappedFields().add(argumentMap);
    	    				
    	    				iter.remove();
    	    				break;
    	    			} 
    	    		}
    	    	 }
        		 constructorMapping.setParameterTypes(parameterTypes);
        	     constructorMapping.setConstructor(constructor);
        	     constructorMapping.setDeclaredParameters(declaredParameterNames);
        	     constructorsByMatchedParams.put((matchScore*1000 + exactMatches), constructorMapping); 
        	}
        }
        
        if (constructorsByMatchedParams.size() > 0) {
            return constructorsByMatchedParams.get(constructorsByMatchedParams.lastKey());
        } else if (declaredParameterNames != null) {
        	throw new IllegalArgumentException("No constructors found for " + targetClass + 
        			" matching the specified constructor parameters " + Arrays.toString(declaredParameterNames) +
        			(declaredParameterNames.length == 0 ? " (no-arg constructor)": ""));
        } else {
        
	        /* 
	         * User didn't specify any constructor, and we couldn't find any that seem compatible;
	         * TODO: can we really do anything in this case? maybe we should just throw an error 
	         * describing some alternative options like creating a Converter or declaring their own
	         * custom ObjectFactory...
	         * */
        	
	        ConstructorMapping<T> defaultMapping = new ConstructorMapping<T>();
	        defaultMapping.setConstructor(constructors.length == 0 ? null : constructors[0]);
	        return defaultMapping;
        }
    }
    
	private FieldMap mapConstructorArgument(FieldMap existing, Type<?> argumentType, boolean byDefault) {
		Property destProp = new Property.Builder()
        		.name(existing.getDestination().getName())
        		.getter(existing.getDestination().getName())
    		    .type(argumentType)
    		    .build();
		FieldMap fieldMap = new FieldMap(existing.getSource(), destProp, null,
				null, MappingDirection.A_TO_B, false, existing.getConverterId(), 
				byDefault, null, null);
		return fieldMap;
	}
}
