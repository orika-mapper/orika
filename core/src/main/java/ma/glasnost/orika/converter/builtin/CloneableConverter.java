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
package ma.glasnost.orika.converter.builtin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * CloneableConverter allows configuration of a number of specific types which
 * should be cloned directly instead of creating a mapped copy.<br><br>
 * 
 * This allows you to declare your own set of types which should be cloned instead
 * of mapped.
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class CloneableConverter extends CustomConverter<Object, Object> {

	private final Set<Type<Cloneable>> clonedTypes = new HashSet<Type<Cloneable>>();
	private final Map<Class<?>, Method> cachedMethods;
	private final Method cloneMethod;
	/**
	 * Constructs a new ClonableConverter configured to handle the provided
	 * list of types by cloning.
	 * 
	 * @param types one or more types that should be treated as immutable
	 */
	public CloneableConverter(java.lang.reflect.Type...types) {
	    Method clone;
	    Map<Class<?>, Method> methodCache;
	    try {
            clone = Object.class.getDeclaredMethod("clone");
            clone.setAccessible(true);
            methodCache = null;
        } catch (SecurityException e) {
            clone = null;
            methodCache = new WeakHashMap<Class<?>, Method>();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
	    cloneMethod = clone;
	    cachedMethods = methodCache;
	    
	    for (java.lang.reflect.Type type: types) {
			clonedTypes.add(TypeFactory.<Cloneable>valueOf(type));
		}
	}
	
	private boolean shouldClone(Type<?> type) {
		for (Type<?> registeredType: clonedTypes) {
			if (registeredType.isAssignableFrom(type)) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see ma.glasnost.orika.Converter#canConvert(ma.glasnost.orika.metadata.Type, ma.glasnost.orika.metadata.Type)
	 */
	public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
	    return shouldClone(sourceType) && sourceType.equals(destinationType);
    }

	public Object convert(Object source, Type<? extends Object> destinationType) {
	    try {
	        Method clone;
	        if (cloneMethod != null) {
	            clone = cloneMethod;
	        } else {
	                clone = cachedMethods.get(source.getClass());
	                if (clone == null) {
	                    /*
	                     * Keep a cache of 'clone' methods based on the assumption that it's 
	                     * faster to lookup a method by source class than to call Class.getMethod
	                     * on that class.
	                     */
	                    synchronized(cachedMethods) {
	                        try {
	                            clone = destinationType.getRawType().getMethod("clone");
	                            cachedMethods.put(source.getClass(), clone);
	                        } catch (NoSuchMethodException e) {
	                            throw new IllegalStateException(e);
	                        } catch (SecurityException e) {
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
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
	
	/**
	 * CloneAction provides privileged access to the clone method in
	 * presence of a SecurityManager
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
				throw new IllegalStateException("Call to clone method not accessible for " + 
						target.getClass().getCanonicalName() + accessibleClause, e);
			} catch (InvocationTargetException e) {
				throw new IllegalStateException("Call to clone method failed for " + 
						target.getClass().getCanonicalName(), e.getTargetException());
			}
		}
		
	}
	
}
