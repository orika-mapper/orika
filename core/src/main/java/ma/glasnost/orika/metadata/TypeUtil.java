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

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class TypeUtil {
    
    @SuppressWarnings("unchecked")
    static final Set<Type<?>> IGNORED_TYPES = 
            new HashSet<Type<?>>(Arrays.asList(
                    TypeFactory.valueOf(Cloneable.class), 
                    TypeFactory.valueOf(Serializable.class),
                    TypeFactory.valueOf(Externalizable.class)));
    
    
    static java.lang.reflect.Type[] resolveActualTypeArguments(ParameterizedType type, Type<?> reference) {
          
        return resolveActualTypeArguments(((Class<?>)type.getRawType()).getTypeParameters(), type.getActualTypeArguments(), reference);
    }
    
    /**
     * Resolves the array of provided actual type arguments using the actual
     * types of the reference type and the type variables list for comparison.
     * 
     * @param vars
     * @param typeArguments
     * @param reference
     * @return
     */
    static java.lang.reflect.Type[] resolveActualTypeArguments(TypeVariable<?>[] vars, java.lang.reflect.Type[] typeArguments, Type<?> reference) {
    	
		java.lang.reflect.Type[] actualTypeArguments = new java.lang.reflect.Type[typeArguments.length];
        for (int i=0, len=actualTypeArguments.length; i < len; ++i) {
            java.lang.reflect.Type typeArg = typeArguments[i];
            TypeVariable<?> var = vars[i];
            // TODO: need to clean up this section:
            // we should loop through the types provided by the reference type, 
            // and if they are more specific than the existing type, use the reference instead
            if (typeArg instanceof TypeVariable) {
                var = (TypeVariable<?>)typeArg;
            }
            Type<?> typeFromReference = (Type<?>) reference.getTypeByVariable(var);
            if (typeFromReference != null && typeArg.equals(var)) {
                actualTypeArguments[i] = typeFromReference;
            } else {
                Type<?> typeFromArgument = (Type<?>) TypeFactory.valueOf(typeArg);
                actualTypeArguments[i] = getMostSpecificType(typeFromReference, typeFromArgument, IGNORED_TYPES);
            }
        }
        return actualTypeArguments;
	
    }
    
    /**
     * Attempts to determine the more specific type out of the two
     * provided types.<br>
     * 
     * @param type0
     * @param type1
     */ 
    static Type<?> getMostSpecificType(Type<?> type0, Type<?> type1) {
        return getMostSpecificType(type0, type1, IGNORED_TYPES);
    }
    
    
    /**
     * Attempts to determine the more specific type out of the two
     * provided types.<br>
     * Allows a provided list of types to ignore (which are basically considered
     * the same as Object's type in terms of their specificity); this is
     * to allow ignoring types that are not a useful part of the hierarchy
     * comparison.
     * 
     * @param type0
     * @param type1
     * @param ignoredTypes
     * @return
     */
    static Type<?> getMostSpecificType(Type<?> type0, Type<?> type1, Set<Type<?>> ignoredTypes) {
        if (type1 == null && type0 == null) {
            return null;
        } else if (type0 == null && type1 != null) {
            return type1;
        } else if (type1 == null && type0 != null) {
            return type0;
        } else if (type1 == null && type0 == null) {
            return null;
        } else if (ignoredTypes.contains(type1) && ignoredTypes.contains(type0)) {
            return TypeFactory.TYPE_OF_OBJECT;
        } else if (ignoredTypes.contains(type1)) {
            return type0;
        } else if (ignoredTypes.contains(type0)) {
            return type1;
        } else if (type0.isAssignableFrom(type1)) {
            return type1;
        } else if (type1.isAssignableFrom(type0)) {
            return type0;
        } else {
            // Types not comparable...
            throw new IllegalArgumentException("types " + type0 + " and " + type1 + " are not comparable");
        }
    }
    
    /**
     * Converts the provided list of actual type arguments to their equivalent
     * Type representation.
     * 
     * @param rawType
     * @param actualTypeArguments
     * @return
     */
    static Type<?>[] convertTypeArguments(final Class<?> rawType, final java.lang.reflect.Type[] actualTypeArguments,
            final Set<java.lang.reflect.Type> recursiveBounds) {
        
        TypeVariable<?>[] typeVariables = rawType.getTypeParameters();
        Type<?>[] resultTypeArguments = new Type<?>[typeVariables.length];
        
        if (recursiveBounds.contains(rawType)) {
            return new Type<?>[0];
        } else if (actualTypeArguments.length == 0 && typeVariables.length > 0) {
            recursiveBounds.add(rawType);
            resultTypeArguments = convertTypeArgumentsFromAncestry(rawType, recursiveBounds);
            recursiveBounds.remove(rawType);
        } else if (actualTypeArguments.length < typeVariables.length) {
            throw new IllegalArgumentException("Must provide all type-arguments or none");
        } else {
            
            for (int i=0, len=actualTypeArguments.length; i < len; ++i) {
                java.lang.reflect.Type t = actualTypeArguments[i];
                recursiveBounds.add(rawType);
                resultTypeArguments[i] = TypeFactory.limitedValueOf(t, recursiveBounds);
                recursiveBounds.remove(rawType);
            }
        }
        return resultTypeArguments;
    }
    
    /**
     * Use the ancestry of a given raw type in order to determine the most
     * specific type parameters possible from the known bounds.
     * 
     * @param rawType
     * @return
     */
    static Type<?>[] convertTypeArgumentsFromAncestry(final Class<?> rawType, final Set<java.lang.reflect.Type> bounds) {
        
        Map<TypeVariable<?>, Type<?>> typesByVariable = new LinkedHashMap<TypeVariable<?>, Type<?>>();
        for (TypeVariable<?> var : rawType.getTypeParameters()) {
            typesByVariable.put(var, TypeFactory.limitedValueOf(var, bounds));
        }
        
        Set<java.lang.reflect.Type> genericAncestors = new LinkedHashSet<java.lang.reflect.Type>();
        
        genericAncestors.add(rawType.getGenericSuperclass());
        genericAncestors.add(rawType.getSuperclass());
        genericAncestors.addAll(Arrays.asList(rawType.getGenericInterfaces()));
        genericAncestors.addAll(Arrays.asList(rawType.getInterfaces()));
        
        Iterator<java.lang.reflect.Type> iter = genericAncestors.iterator();
        while (iter.hasNext()) {
            
            java.lang.reflect.Type ancestor = iter.next();
            iter.remove();
            
            if (ancestor instanceof ParameterizedType) {
                
                ParameterizedType superType = (ParameterizedType) ancestor;
                TypeVariable<?>[] variables = ((Class<?>) superType.getRawType()).getTypeParameters();
                java.lang.reflect.Type[] actuals = superType.getActualTypeArguments();
                for (int i = 0; i < variables.length; ++i) {
                    Type<?> resolvedActual = TypeFactory.limitedValueOf(actuals[i], bounds);
                    TypeVariable<?> var = (TypeVariable<?>) ((actuals[i] instanceof TypeVariable) ? actuals[i] : variables[i]);
                    Type<?> currentActual = typesByVariable.get(var);
                    if (currentActual != null) {
                        typesByVariable.put(var, getMostSpecificType(currentActual, resolvedActual));
                    }
                }
            } else if (ancestor instanceof Class) {
                
                Class<?> superType = (Class<?>) ancestor;
                
                TypeVariable<?>[] variables = superType.getTypeParameters();
                for (int i = 0; i < variables.length; ++i) {
                    Type<?> resolvedActual = TypeFactory.limitedValueOf(variables[i], bounds);
                    Type<?> currentActual = typesByVariable.get(variables[i]);
                    if (currentActual != null) {
                        typesByVariable.put(variables[i], getMostSpecificType(currentActual, resolvedActual));
                    }
                }
            }
        }
        
        return typesByVariable.values().toArray(new Type<?>[0]);
    }
    
    static class InvalidTypeDescriptorException extends Exception {
        private static final long serialVersionUID = 1L;        
    }
    
    /**
     * Parses a type descriptor to produce the Type instance with a matching representation
     * 
     * @param typeDescriptor
     * @return
     * @throws InvalidTypeDescriptorException
     */
    static Type<?> parseTypeDescriptor(String typeDescriptor) throws InvalidTypeDescriptorException {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        StringBuilder descriptor = new StringBuilder(typeDescriptor);
        int split = descriptor.indexOf("<");
        if (split >= 0) {
            String className = descriptor.substring(0, split);
            if (!descriptor.subSequence(descriptor.length()-1, descriptor.length()).equals(">")) {
                throw new InvalidTypeDescriptorException();
            } else {
                descriptor.setLength(descriptor.length()-1);
                descriptor.replace(0, split+1, "");
            }
            String[] subDescriptors = splitTypeArguments(descriptor.toString());
            Type<?>[] typeArguments = new Type<?>[subDescriptors.length];
            int index = -1;
            for (String subDescriptor: subDescriptors) {
                typeArguments[++index] = parseTypeDescriptor(subDescriptor);
            }
            
            return TypeFactory.valueOf(loadClass(className, cl), typeArguments);
           
        } else {
            return TypeFactory.valueOf(loadClass(typeDescriptor, cl));  
        }
    }
    
    /**
     * Splits the provided string of nested types into separate top-level instances.
     * 
     * @param nestedTypes
     * @return
     */
    private static String[] splitTypeArguments(String nestedTypes) {
        StringBuilder string = new StringBuilder(nestedTypes.replaceAll("\\s*", ""));
        List<String> arguments = new ArrayList<String>();
        while(string.length() > 0) {
            int nextComma = string.indexOf(",");
            int nextOpen = string.indexOf("<");
            if (nextComma == -1 ) {
                arguments.add(string.toString());
                string.setLength(0);
            } else if (nextOpen == -1 || nextComma < nextOpen) {
                arguments.add(string.substring(0, nextComma));
                string.replace(0,nextComma+1,"");
            } else { // nextOpen < nextComma
                int depth = 1;
                int index = nextOpen;
                while (depth > 0 && index < string.length() -1 ) {
                    char nextChar = string.charAt(++index);
                    if ('<' == nextChar) {
                        ++depth;
                    } else if ('>' == nextChar) {
                        --depth;
                    }
                }
                arguments.add(string.substring(0,index+1));
                string.replace(0,index+1,"");
            }
        }
        return arguments.toArray(new String[arguments.size()]);
    }
    
    /**
     * Attempt to load the class specified by the given name, using
     * the provided class loader.<br>
     * Additional attempts are made for classes not found which have no package 
     * declaration, under the assumption that the class may be in the 
     * 'java.lang' package, or 'java.util' package (in that order).
     * 
     * @param name
     * @param cl
     * @return
     */
    private static Class<?> loadClass(String name, ClassLoader cl) {
        try {
            return Class.forName(name, false, cl);
        } catch (ClassNotFoundException e) {
            if (!name.contains(".")) {
                try {
                    return Class.forName("java.lang." + name, false, cl);
                } catch (ClassNotFoundException e1) {
                    try {
                        return Class.forName("java.util." + name, false, cl);
                    } catch (ClassNotFoundException e2) {
                        /*
                         * Report the originally specified type
                         */
                    }
                }
            } else {
                int lastDot = name.lastIndexOf('.');
                String modifiedName = name;
                while (lastDot > 0) {
                    modifiedName = modifiedName.substring(0, lastDot) + "$" + modifiedName.substring(lastDot+1);
                    try {
                        return Class.forName(modifiedName, false, cl);
                    } catch (ClassNotFoundException e2) {
                        lastDot = modifiedName.lastIndexOf('.');
                    }
                }
                
            }
            
            throw new IllegalArgumentException("'" + name + "' is non-existent or inaccessible");
        }
    }
    
}
