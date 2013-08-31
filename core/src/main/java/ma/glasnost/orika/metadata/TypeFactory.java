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

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ma.glasnost.orika.metadata.TypeUtil.InvalidTypeDescriptorException;

/**
 * TypeFactory contains various methods for obtaining a Type instance to
 * represent various type situations.
 * 
 * @author matt.deboer@gmail.com
 * 
 * 
 */
public abstract class TypeFactory {
    
    /**
     * Should not be extended
     */
    private TypeFactory() {
    }
    
    /**
     * Use a weak-valued concurrent map to avoid keeping static references to
     * Types (classes) which may belong to descendant class-loaders
     */
    private static final ConcurrentHashMap<TypeKey, WeakReference<Type<?>>> typeCache = new ConcurrentHashMap<TypeKey, WeakReference<Type<?>>>();
    
    /**
     * The Type instance which represents the Object class
     */
    public static final Type<Object> TYPE_OF_OBJECT = valueOf(Object.class);
    
    /**
     * Store the combination of rawType and type arguments as a Type within the
     * type cache.<br>
     * Use the existing type if already available; we try to enforce that Type
     * should be immutable.
     * 
     * @param rawType
     *            the raw class of the type
     * @param typeArguments
     *            the type arguments of the type
     * @param recursiveBounds
     *            the limits on recursively nested types
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> Type<T> intern(final Class<T> rawType, final java.lang.reflect.Type[] typeArguments,
            final Set<java.lang.reflect.Type> recursiveBounds) {
        
        Type<?>[] convertedArguments = TypeUtil.convertTypeArguments(rawType, typeArguments, recursiveBounds);
        TypeKey key = TypeKey.valueOf(rawType, convertedArguments);
        
        WeakReference<Type<?>> mapped = typeCache.get(key);
        Type<T> typeResult = null;
        if (mapped != null) {
            typeResult = (Type<T>) mapped.get();
        }
        if (typeResult == null) {
            synchronized (rawType) {
                mapped = typeCache.get(key);
                if (mapped != null) {
                    typeResult = (Type<T>) mapped.get();
                }
                if (typeResult == null) {
                    typeResult = createType(key, rawType, convertedArguments);
                    mapped = new WeakReference<Type<?>>(typeResult);
                    WeakReference<Type<?>> existing = typeCache.putIfAbsent(key, mapped);
                    if (existing != null) {
                        if (existing.get() == null) {
                            /*
                             *  Should not be possible, since the references are
                             *  based on Class objects, which cannot be GC'd until 
                             *  their respective class loader is GC'd, in which case, 
                             *  such a Class could not be passed into this method as
                             *  an argument, or embedded within an argument 
                             */
                            typeCache.put(key, mapped);
                        } else {
                            mapped = existing;
                            typeResult = (Type<T>) mapped.get();
                        }
                    }
                }
            }
        }
        return typeResult;
        
    }
    
    private static <T> Type<T> createType(TypeKey key, Class<T> rawType, Type<?>[] typeArguments) {
        Map<String, Type<?>> typesByVariable = null;
        if (typeArguments.length > 0) {
            typesByVariable = new HashMap<String, Type<?>>(typeArguments.length);
            for (int i = 0, len = typeArguments.length; i < len; ++i) {
                typesByVariable.put(rawType.getTypeParameters()[i].getName(), typeArguments[i]);
            }
        }
        return new Type<T>(key, rawType, typesByVariable, typeArguments);
    }
    
    /**
     * Resolves the Type value of the specified raw Class type
     * 
     * @param rawType
     * @return the resolved Type instance
     */
    public static <E> Type<E> valueOf(final Class<E> rawType) {
        if (rawType == null) {
            return null;
        } else if (rawType.isAnonymousClass() && rawType.getGenericSuperclass() instanceof ParameterizedType) {
            ParameterizedType genericSuper = (ParameterizedType) rawType.getGenericSuperclass();
            return valueOf(genericSuper);
        } else {
            return intern(rawType, new java.lang.reflect.Type[0], new HashSet<java.lang.reflect.Type>());
        }
    }
    
    /**
     * @param rawType
     * @param recursiveBounds
     * @param actualTypeArguments
     * @return the resolved Type instance
     */
    public static <E> Type<E> limitedValueOf(final Class<E> rawType, Set<java.lang.reflect.Type> recursiveBounds,
            final java.lang.reflect.Type... actualTypeArguments) {
        if (rawType == null) {
            return null;
        } else if (rawType.isAnonymousClass() && rawType.getGenericSuperclass() instanceof ParameterizedType) {
            ParameterizedType genericSuper = (ParameterizedType) rawType.getGenericSuperclass();
            return limitedValueOf(genericSuper, recursiveBounds);
        } else {
            return (Type<E>) intern(rawType, actualTypeArguments, recursiveBounds);
        }
    }
    
    /**
     * Resolve the Type value of the given raw Class type, filling the type
     * parameters with the provided actual type arguments
     * 
     * @param rawType
     * @param actualTypeArguments
     * @return the resolved Type instance
     */
    public static <E> Type<E> valueOf(final Class<E> rawType, final java.lang.reflect.Type... actualTypeArguments) {
        if (rawType == null) {
            return null;
        } else {
            return (Type<E>) intern((Class<E>) rawType, actualTypeArguments, new HashSet<java.lang.reflect.Type>());
        }
    }
    
    /**
     * This method declaration helps to shortcut the other methods for
     * ParameterizedType which it extends; we just return it.
     * 
     * @param type
     * @return the resolved Type instance
     */
    public static <T> Type<T> valueOf(final Type<T> type) {
        return type;
    }
    
    /**
     * Return the Type for the given ParameterizedType, resolving actual type
     * arguments where possible.
     * 
     * @param type
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> valueOf(final ParameterizedType type) {
        return valueOf((Class<T>) type.getRawType(), type.getActualTypeArguments());
    }
    
    /**
     * Return the Type for the given ParameterizedType, resolving actual type
     * arguments where possible; uses recursiveBounds to limit the recursion.
     * 
     * @param type
     * @param recursiveBounds
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> limitedValueOf(final ParameterizedType type, final Set<java.lang.reflect.Type> recursiveBounds) {
        return limitedValueOf((Class<T>) type.getRawType(), recursiveBounds, type.getActualTypeArguments());
    }
    
    /**
     * Finds the Type value of the given TypeVariable
     * 
     * @param var
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> valueOf(final TypeVariable<?> var) {
        
        if (var.getBounds().length > 0) {
            Set<Type<?>> bounds = new HashSet<Type<?>>(var.getBounds().length);
            for (int i = 0, len = var.getBounds().length; i < len; ++i) {
                bounds.add(valueOf(var.getBounds()[i]));
            }
            return (Type<T>) refineBounds(bounds);
        } else {
            return (Type<T>) TYPE_OF_OBJECT;
        }
    }
    
    /**
     * Finds the Type value of the given TypeVariable, using recursiveBounds to
     * limit the recursion.
     * 
     * @param var
     * @param recursiveBounds
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> limitedValueOf(final TypeVariable<?> var, final Set<java.lang.reflect.Type> recursiveBounds) {
        
        if (var.getBounds().length > 0) {
            Set<Type<?>> bounds = new HashSet<Type<?>>(var.getBounds().length);
            for (int i = 0, len = var.getBounds().length; i < len; ++i) {
                bounds.add(limitedValueOf(var.getBounds()[i], recursiveBounds));
            }
            return (Type<T>) refineBounds(bounds);
        } else {
            return (Type<T>) TYPE_OF_OBJECT;
        }
    }
    
    /**
     * Finds the Type value of the given wildcard type
     * 
     * @param var
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> valueOf(final WildcardType var) {
        
        Set<Type<?>> bounds = new HashSet<Type<?>>(var.getUpperBounds().length + var.getLowerBounds().length);
        for (int i = 0, len = var.getUpperBounds().length; i < len; ++i) {
            bounds.add(valueOf(var.getUpperBounds()[i]));
        }
        for (int i = 0, len = var.getLowerBounds().length; i < len; ++i) {
            bounds.add(valueOf(var.getLowerBounds()[i]));
        }
        return (Type<T>) refineBounds(bounds);
    }
    
    /**
     * 
     * Finds the Type value of the given wildcard type, using recursiveBounds to
     * limit the recursion.
     * 
     * @param var
     * @param recursiveBounds
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> limitedValueOf(final WildcardType var, final Set<java.lang.reflect.Type> recursiveBounds) {
        
        Set<Type<?>> bounds = new HashSet<Type<?>>(var.getUpperBounds().length + var.getLowerBounds().length);
        for (int i = 0, len = var.getUpperBounds().length; i < len; ++i) {
            bounds.add(limitedValueOf(var.getUpperBounds()[i], recursiveBounds));
        }
        for (int i = 0, len = var.getLowerBounds().length; i < len; ++i) {
            bounds.add(limitedValueOf(var.getLowerBounds()[i], recursiveBounds));
        }
        return (Type<T>) refineBounds(bounds);
    }
    
    /**
     * Returns the most specific type from the set of provided bounds.
     * 
     * @param bounds
     * @return the resolved Type instance
     */
    private static Type<?> refineBounds(Set<Type<?>> bounds) {
        if (bounds.size() > 1) {
            // Consolidate bounds to most significant
            Iterator<Type<?>> currentBoundIter = bounds.iterator();
            while (currentBoundIter.hasNext()) {
                Type<?> currentBound = currentBoundIter.next();
                Iterator<Type<?>> boundIter = bounds.iterator();
                while (boundIter.hasNext()) {
                    Type<?> nextType = boundIter.next();
                    if (nextType.equals(currentBound)) {
                        continue;
                    } else {
                        Type<?> mostSpecific = TypeUtil.getMostSpecificType(currentBound, nextType);
                        if (nextType.equals(mostSpecific)) {
                            boundIter.remove();
                        }
                    }
                    
                }
            }
            if (bounds.size() != 1) {
                throw new IllegalArgumentException(bounds + " is not refinable");
            }
        }
        
        return bounds.iterator().next();
    }
    
    /**
     * Return the Type for the given java.lang.reflect.Type, either for a
     * ParameterizedType or a Class instance
     * 
     * @param type
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> valueOf(final java.lang.reflect.Type type) {
        if (type instanceof Type) {
            return (Type<T>) type;
        } else if (type instanceof ParameterizedType) {
            return valueOf((ParameterizedType) type);
        } else if (type instanceof Class) {
            return valueOf((Class<T>) type);
        } else if (type instanceof TypeVariable) {
            return valueOf((TypeVariable<?>) type);
        } else if (type instanceof WildcardType) {
            return valueOf((WildcardType) type);
        } else {
            throw new IllegalArgumentException(type + " is an unsupported type");
        }
    }
    
    /**
     * Return the Type for the given java.lang.reflect.Type, limiting the
     * recursive depth on any type already contained in recursiveBounds.
     * 
     * @param type
     * @param recursiveBounds
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> limitedValueOf(final java.lang.reflect.Type type, final Set<java.lang.reflect.Type> recursiveBounds) {
        if (type instanceof Type) {
            return (Type<T>) type;
        } else if (type instanceof ParameterizedType) {
            return limitedValueOf((ParameterizedType) type, recursiveBounds);
        } else if (type instanceof Class) {
            return limitedValueOf((Class<T>) type, recursiveBounds, new java.lang.reflect.Type[0]);
        } else if (type instanceof TypeVariable) {
            return limitedValueOf((TypeVariable<?>) type, recursiveBounds);
        } else if (type instanceof WildcardType) {
            return limitedValueOf((WildcardType) type, recursiveBounds);
        } else {
            throw new IllegalArgumentException(type + " is an unsupported type");
        }
    }
    
    /**
     * Resolve the Type for the given ParameterizedType, using the provided
     * referenceType to resolve any unresolved actual type arguments.
     * 
     * @param type
     * @param referenceType
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> resolveValueOf(final ParameterizedType type, final Type<?> referenceType) {
        if (type == null) {
            return null;
        } else {
            java.lang.reflect.Type[] actualTypeArguments = TypeUtil.resolveActualTypeArguments(type, referenceType);
            Type<T> result = intern((Class<T>) type.getRawType(), actualTypeArguments, new HashSet<java.lang.reflect.Type>());
            
            return result;
        }
    }
    
    /**
     * Resolve the Type for the given Class, using the provided referenceType to
     * resolve the actual type arguments.
     * 
     * @param type
     * @param referenceType
     * @return the resolved Type instance
     */
    public static <T> Type<T> resolveValueOf(final Class<T> type, final Type<?> referenceType) {
        if (type == null) {
            return null;
        } else {
            if (type.getTypeParameters() != null && type.getTypeParameters().length > 0) {
                java.lang.reflect.Type[] actualTypeArguments = TypeUtil.resolveActualTypeArguments(type.getTypeParameters(),
                        type.getTypeParameters(), referenceType);
                return valueOf(type, actualTypeArguments);
            } else {
                return valueOf(type);
            }
        }
    }
    
    /**
     * Return the Type for the given object.
     * 
     * @param object
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> typeOf(final T object) {
        return (Type<T>) (object == null ? null : valueOf((Class<T>) object.getClass()));
    }
    
    /**
     * Resolve the Type for the given object, using the provided referenceType
     * to resolve the actual type arguments.
     * 
     * @param object
     * @param referenceType
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> resolveTypeOf(final T object, Type<?> referenceType) {
        return object == null ? null : resolveValueOf((Class<T>) object.getClass(), referenceType);
    }
    
    /**
     * Resolve the (element) component type for the given array.
     * 
     * @param object
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> componentTypeOf(final T[] object) {
        return (Type<T>) (object == null ? null : valueOf((Class<T>) object.getClass().getComponentType()));
    }
    
    /**
     * Resolve the nested element type for the given Iterable.
     * 
     * @param object
     * @return the resolved Type instance
     */
    public static <T> Type<T> elementTypeOf(final Iterable<T> object) {
        
        try {
            Method iterator = object.getClass().getMethod("iterator");
            Type<Iterable<T>> type = valueOf(iterator.getGenericReturnType());
            return type.getNestedType(0);
        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Constructs a nested type from a string description of that type; allows for package names
     * to be omitted for 'java.lang' and 'java.util' classes.
     * 
     * @param typeDescriptor a string representation of the java declaration of a generic type
     * @return
     */
    public static Type<?> valueOf(final String typeDescriptor) {
        try {
            return TypeUtil.parseTypeDescriptor(typeDescriptor);
        } catch (InvalidTypeDescriptorException e) {
            throw new IllegalArgumentException(typeDescriptor + " is an invalid type descriptor");
        }
    }

}
