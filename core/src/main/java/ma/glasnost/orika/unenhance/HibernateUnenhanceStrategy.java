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

package ma.glasnost.orika.unenhance;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HibernateUnenhanceStrategy is used to unwrapped objects from
 * their Hibernate-generated proxy, which may have been created to
 * match methods of a super-type (missing some of the details important
 * in a child class mapping).
 * 
 */
public class HibernateUnenhanceStrategy implements UnenhanceStrategy {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateUnenhanceStrategy.class);
    
    private static final String HIBERNATE_CLASS = "org.hibernate.Hibernate";
    private static final String HIBERNATE__GET_CLASS = "getClass";
    private static final String HIBERNATE_PROXY_CLASS = "org.hibernate.proxy.HibernateProxy";
    private static final String HIBERNATE_PROXY__GET_LAZY_INITIALIZER = "getHibernateLazyInitializer"; 
    private static final String LAZY_INITIALIZER_CLASS = "org.hibernate.proxy.LazyInitializer";
    private static final String LAZY_INITIALIZER__GET_IMPLEMENTATION = "getImplementation";
    
    
    private Method getHibernateClass;
    private Class<?> hibernateProxy;
    private Method getHibernateLazyInitializer;
    private Method getImplementation;
    
    public HibernateUnenhanceStrategy() {
        try {
            
            Class<?> hibernate = Class.forName(HIBERNATE_CLASS, false, Thread.currentThread().getContextClassLoader());
            getHibernateClass = hibernate.getMethod(HIBERNATE__GET_CLASS, Object.class);
            
            hibernateProxy = Class.forName(HIBERNATE_PROXY_CLASS, false, Thread.currentThread().getContextClassLoader());
            getHibernateLazyInitializer = hibernateProxy.getMethod(HIBERNATE_PROXY__GET_LAZY_INITIALIZER);
            
            Class<?> hibernateLazyInitializer = Class.forName(LAZY_INITIALIZER_CLASS, false, Thread.currentThread().getContextClassLoader());
            getImplementation = hibernateLazyInitializer.getMethod(LAZY_INITIALIZER__GET_IMPLEMENTATION);
            
        } catch (ClassNotFoundException e) {
            hibernateInaccessible(e);
        } catch (NoSuchMethodException e) {
            hibernateInaccessible(e);
        } catch (SecurityException e) {
            hibernateInaccessible(e);
        }
    }
    
    private static void hibernateInaccessible(Exception e) {
        throw new ExceptionInInitializerError("One of " + HIBERNATE_CLASS + "#" + HIBERNATE__GET_CLASS+"(), " + 
                HIBERNATE_PROXY_CLASS + "#" + HIBERNATE_PROXY__GET_LAZY_INITIALIZER + "() , or " + 
                LAZY_INITIALIZER_CLASS + "#" + LAZY_INITIALIZER__GET_IMPLEMENTATION + "() required by " + 
                HibernateUnenhanceStrategy.class.getCanonicalName() + " is not accessible" + e);
    }
    
    private static void hibernateGetClassUnavailable(Exception e) {
        LOGGER.warn(HIBERNATE_CLASS + "#" + HIBERNATE__GET_CLASS+"() is not available", e);
    }
    
    private static void hibernateGetLazyInitUnavailable(Exception e) {
        LOGGER.warn(LAZY_INITIALIZER_CLASS + "#" + LAZY_INITIALIZER__GET_IMPLEMENTATION + " is not available; ", e); 
    }
    
    @SuppressWarnings("unchecked")
    public <T> Type<T> unenhanceType(T object, Type<T> type) {
        
        try {
            return TypeFactory.resolveValueOf((Class<T>) getHibernateClass.invoke(null, object), type);
        } catch (IllegalAccessException e) {
            hibernateGetClassUnavailable(e);
        } catch (IllegalArgumentException e) {
            hibernateGetClassUnavailable(e);
        } catch (InvocationTargetException e) {
            hibernateGetClassUnavailable(e);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T unenhanceObject(T object, Type<T> type) {
        if (hibernateProxy.isAssignableFrom(object.getClass())) {
            try {
                return (T) getImplementation.invoke(getHibernateLazyInitializer.invoke(object));
            } catch (IllegalAccessException e) {
                hibernateGetLazyInitUnavailable(e);
            } catch (IllegalArgumentException e) {
                hibernateGetLazyInitUnavailable(e);
            } catch (InvocationTargetException e) {
                hibernateGetLazyInitUnavailable(e);
            }
        }
        return object;
    }
}
