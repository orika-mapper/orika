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
package ma.glasnost.orika.converter.builtin;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * CloneableConverter allows configuration of a number of specific types which
 * should be cloned directly instead of creating a mapped copy.<br>
 * <br>
 * 
 * This allows you to declare your own set of types which should be cloned
 * instead of mapped.
 * 
 * @author matt.deboer@gmail.com
 *
 * @deprecated This class does not work starting from Java 17
 *
 */
@Deprecated
public class CloneableConverter extends CustomConverter<Object, Object> {
    
    private final Set<Type<Cloneable>> clonedTypes = new HashSet<>();
    private final Map<Class<?>, Method> cachedMethods;
    private final Method cloneMethod;
    private final String description;
    
    /**
     * Constructs a new ClonableConverter configured to handle the provided list
     * of types by cloning.
     * 
     * @param types
     *            one or more types that should be treated as immutable
     */
    public CloneableConverter(java.lang.reflect.Type... types) {
        Method clone;
        Map<Class<?>, Method> methodCache;
        try {
            clone = Object.class.getDeclaredMethod("clone");
            clone.setAccessible(true);
            methodCache = null;
        } catch (SecurityException e) {
            clone = null;
            methodCache = new WeakHashMap<>();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
        cloneMethod = clone;
        cachedMethods = methodCache;
        
        StringBuilder desc = new StringBuilder(CloneableConverter.class.getSimpleName() + "(Copy by cloning:");
        String separator = "";
        for (java.lang.reflect.Type type : types) {
            Type<Cloneable> cloneableType = TypeFactory.<Cloneable> valueOf(type);
            clonedTypes.add(cloneableType);
            desc.append(separator).append(cloneableType);
            separator = ", ";
        }
        desc.append(")");
        description = desc.toString();
    }
    
    private boolean shouldClone(Type<?> type) {
        for (Type<?> registeredType : clonedTypes) {
            if (registeredType.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }
    
    public String toString() {
        return description;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.Converter#canConvert(ma.glasnost.orika.metadata.Type,
     * ma.glasnost.orika.metadata.Type)
     */
    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
        return shouldClone(sourceType) && sourceType.equals(destinationType);
    }
    
    public Object convert(Object source, Type<?> destinationType, MappingContext context) {
    	if (source == null) {
    		return null;
    	}
    	
        try {
            Method clone;
            if (cloneMethod != null) {
                clone = cloneMethod;
            } else {
                clone = cachedMethods.get(source.getClass());
                if (clone == null) {
                    /*
                     * Keep a cache of 'clone' methods based on the assumption
                     * that it's faster to lookup a method by source class than
                     * to call Class.getMethod on that class.
                     */
                    synchronized (cachedMethods) {
                        try {
                            clone = destinationType.getRawType().getMethod("clone");
                            cachedMethods.put(source.getClass(), clone);
                        } catch (NoSuchMethodException | SecurityException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                }
            }
            if (System.getSecurityManager() != null) {
                return AccessController.doPrivileged(new CloneAction(clone, source));
            } else {
                return clone.invoke(source);
            }
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((clonedTypes == null) ? 0 : clonedTypes.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CloneableConverter other = (CloneableConverter) obj;
        if (clonedTypes == null) {
            if (other.clonedTypes != null) {
                return false;
            }
        } else if (!clonedTypes.equals(other.clonedTypes)) {
            return false;
        }
        return true;
    }
    
    /**
     * CloneAction provides privileged access to the clone method in presence of
     * a SecurityManager
     */
    private static final class CloneAction implements PrivilegedAction<Object> {
        
        private final Method method;
        private final Object target;
        
        private CloneAction(Method method, Object target) {
            this.method = method;
            this.target = target;
        }
        
        public Object run() {
            try {
                return method.invoke(target);
            } catch (IllegalAccessException e) {
                String accessibleClause = method.isAccessible() ? "(even though " + method + " is accessible)" : "";
                throw new IllegalStateException("Call to clone method not accessible for " + target.getClass().getCanonicalName()
                        + accessibleClause, e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Call to clone method failed for " + target.getClass().getCanonicalName(),
                        e.getTargetException());
            }
        }
        
    }
    
    /**
     * Extends CloneableConverter for use as a built-in Converter
     */
    static class Builtin extends CloneableConverter {
        
        private final String description;
        
        Builtin(java.lang.reflect.Type... types) {
            super(types);
            description = "builtin:" + super.toString();
        }
        
        public String toString() {
            return description;
        }
    }
    
}
