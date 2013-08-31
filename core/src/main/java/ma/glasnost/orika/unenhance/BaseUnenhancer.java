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

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import ma.glasnost.orika.inheritance.SuperTypeResolver;
import ma.glasnost.orika.inheritance.SuperTypeResolverStrategy;
import ma.glasnost.orika.metadata.Type;

/**
 * Provides a delegating unenhance strategy which also post-processes the
 * unenhancement results using the associated super-type strategies.<br>
 * 
 * See also: {@link #SuperTypeResolverStrategy}
 * 
 * @author matt.deboer@gmail.com
 */
public class BaseUnenhancer implements UnenhanceStrategy {
    
    private final ConcurrentHashMap<Type<?>, Type<?>> mappedSuperTypes;
    private final LinkedList<UnenhanceStrategy> unenhanceStrategyChain = new LinkedList<UnenhanceStrategy>();
    private final LinkedList<SuperTypeResolverStrategy> supertypeStrategyChain = new LinkedList<SuperTypeResolverStrategy>();
    
    public BaseUnenhancer() {
        this.mappedSuperTypes = new ConcurrentHashMap<Type<?>, Type<?>>();
    }
    
    public synchronized void addUnenhanceStrategy(final UnenhanceStrategy strategy) {
        unenhanceStrategyChain.add(strategy);
    }
    
    public synchronized void addSuperTypeResolverStrategy(final SuperTypeResolverStrategy strategy) {
        supertypeStrategyChain.add(strategy);
    }
    
    @SuppressWarnings("unchecked")
    public <T> Type<T> unenhanceType(T object, Type<T> type) {
        
        Type<T> unenhancedClass = type;
        for (UnenhanceStrategy strategy : unenhanceStrategyChain) {
            Type<T> delegateUnenhanced = strategy.unenhanceType(object, type);
            // Accept the first delegate strategy result which produces
            // something different than the object's getClass method
            if (delegateUnenhanced != null && !unenhancedClass.equals(delegateUnenhanced)) {
                unenhancedClass = delegateUnenhanced;
                break;
            }
        }
        
        for (SuperTypeResolverStrategy strategy : supertypeStrategyChain) {
            Type<?> superType = SuperTypeResolver.getSuperType(unenhancedClass, strategy);
            if (superType != null && !unenhancedClass.equals(superType)) {
                Type<?> superTypePutResult = mappedSuperTypes.putIfAbsent(unenhancedClass, superType);
                // Accept the first delegate strategy result which produces
                // a super-type different than the object's getClass method
                if (superTypePutResult != null) {
                    superType = superTypePutResult;
                }
                unenhancedClass = (Type<T>) superType;
                break;
            }
        }
        
        return unenhancedClass;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T unenhanceObject(T object, Type<T> type) {
        for (UnenhanceStrategy strategy : unenhanceStrategyChain) {
            Object delegateUnenhanced = strategy.unenhanceObject(object, type);
            // Accept the first delegate strategy result which produces
            // something different than the object's getClass method
            if (delegateUnenhanced != null && delegateUnenhanced != object) {
                return (T) delegateUnenhanced;
            }
        }
        return object;
    }
}
