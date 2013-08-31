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

package ma.glasnost.orika;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import ma.glasnost.orika.cern.colt.map.OpenIntObjectHashMap;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;

/**
 * MappingContext provides storage for information shared among the various
 * mapping objects for a given mapping request.
 *
 */
public class MappingContext {
    
    private final Map<Type<?>, Type<?>> mapping;
    private final OpenIntObjectHashMap typeCache;
    private List<Map<MapperKey, ClassMap<?, ?>>> mappersSeen;
    private Map<Object, Object> properties;
    private Map<Object, Object> globalProperties;
    private boolean isNew = true;
    private boolean containsCycle = true;
    private int depth;
    private Type<?> resolvedSourceType;
    private Type<?> resolvedDestinationType;
    
    /**
     * Factory constructs instances of the base MappingContext
     */
    public static class Factory implements MappingContextFactory {
        
        LinkedBlockingQueue<MappingContext> contextQueue = new LinkedBlockingQueue<MappingContext>();
        ConcurrentHashMap<Object, Object> globalProperties = new ConcurrentHashMap<Object, Object>();
        
        public MappingContext getContext() {
            MappingContext context = contextQueue.poll();
            if (context == null) {
                context = new MappingContext(globalProperties);
            }
            context.containsCycle = true;
            return context;
        }
        
        public void release(MappingContext context) {
            context.reset();
            contextQueue.offer(context);
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see ma.glasnost.orika.MappingContextFactory#getGlobalProperties()
         */
        public Map<Object, Object> getGlobalProperties() {
            return globalProperties;
        }
    }
    
    /**
     * @param globalProperties
     */
    protected MappingContext(Map<Object, Object> globalProperties) {
        this.mapping = new HashMap<Type<?>, Type<?>>();
        this.typeCache = new OpenIntObjectHashMap();
        this.globalProperties = globalProperties;
    }
    
    /**
     * Sets whether this MappingContext needs to guard against cycles when mapping
     * the current object graph; specifying <code>false</code> when applicable can
     * lend improved performance.
     * 
     * @param containsCycle
     */
    public void containsCycle(boolean containsCycle) {
        this.containsCycle = containsCycle;
    }
    
    /**
     * @return true if this mapping context is watching for cycles in the object graph
     */
    public boolean containsCycle() {
        return containsCycle;
    }
    
    /**
     * @return the current mapping depth
     */
    public int getDepth() {
        return depth;
    }
    
    /**
     * Searches for a concrete class that has been registered for the given abstract
     * class or interface within this mapping session.
     * 
     * @param sourceType
     * @param destinationType
     * @return a concrete class that has been registered for the given abstract
     * class or interface within this mapping session, if any
     */
    @SuppressWarnings("unchecked")
    public <S, D> Type<? extends D> getConcreteClass(Type<S> sourceType, Type<D> destinationType) {
        if (isNew) {
            return null;
        }
        final Type<?> type = mapping.get(sourceType);
        if (type != null && destinationType.isAssignableFrom(type)) {
            return (Type<? extends D>) type;
        }
        return null;
    }
    
    /**
     * Registers a concrete class to be used for the given abstract class or interface
     * within this mapping session only.
     * 
     * @param subjectClass
     * @param concreteClass
     */
    public void registerConcreteClass(Type<?> subjectClass, Type<?> concreteClass) {
        mapping.put(subjectClass, concreteClass);
        isNew = false;
    }
    
    /**
     * Caches an object instance which has been mapped for a particular source
     * instance and destination type in this mapping context; this will later
     * be referenced in avoiding infinite recursion mapping the same object.
     * 
     * @param source
     * @param destinationType
     * @param destination
     */
    @SuppressWarnings("unchecked")
    public <S, D> void cacheMappedObject(S source, Type<Object> destinationType, D destination) {
        if (containsCycle) {
            Map<Object, Object> localCache = (Map<Object, Object>) typeCache.get(destinationType.getUniqueIndex());
            if (localCache == null) {
                localCache = new IdentityHashMap<Object, Object>(2);
                typeCache.put(destinationType.getUniqueIndex(), localCache);
                
            }
            localCache.put(source, destination);
            
            // Quick fix for Issue 68
            for (Type<Object> t : (Type<Object>[])destinationType.getInterfaces()) {
                cacheMappedObject(source, t, destination);
            }
            
            isNew = false;
        }
    } 

    
    /**
     * Looks for an object which has already been mapped for the source and
     * destination type in this context.
     * 
     * @param source
     * @param destinationType
     * @return the mapped object, or null if none exists for the source instance
     *         and destination type
     */
    @SuppressWarnings("unchecked")
    public <D> D getMappedObject(Object source, Type<?> destinationType) {
        
        if (isNew || !containsCycle) {
            return null;
        }
        Map<Object, Object> localCache = (Map<Object, Object>) typeCache.get(destinationType.getUniqueIndex());
        return (D) (localCache == null ? null : localCache.get(source));
    }
    
    /**
     * Registers a ClassMap marking it as mapped within the current context;
     * 
     * @param classMap
     */
    public void registerMapperGeneration(ClassMap<?, ?> classMap) {
        if (mappersSeen == null) {
            mappersSeen = new ArrayList<Map<MapperKey, ClassMap<?, ?>>>();
        }
        Map<MapperKey, ClassMap<?, ?>> list = mappersSeen.isEmpty() ? null : this.mappersSeen.get(depth - 1);
        if (list == null) {
            list = new HashMap<MapperKey, ClassMap<?, ?>>();
        }
        list.put(classMap.getMapperKey(), classMap);
    }
    
    /**
     * Looks up a ClassMap among the mappers generated with this mapping context
     * 
     * @param mapperKey
     * @return the ClassMap for which a Mapper was generated in this context, if any
     */
    public ClassMap<?, ?> getMapperGeneration(MapperKey mapperKey) {
        ClassMap<?, ?> result = null;
        Map<MapperKey, ClassMap<?, ?>> map = (mappersSeen == null || mappersSeen.isEmpty()) ? null : this.mappersSeen.get(depth - 1);
        if (map != null) {
            result = map.get(mapperKey);
        }
        return result;
    }
    
    /**
     * Mark the beginning of a particular mapping
     */
    public void beginMapping() {
        ++depth;
    }
    
    /**
     * Mark the end of a particular mapping
     */
    public void endMapping() {
        --depth;
    }
    
    /**
     * Resets this context instance, in preparation for use by another mapping request
     */
    public void reset() {
        mapping.clear();
        typeCache.clear();
        if (properties != null) {
            properties.clear();
        }
        if (mappersSeen != null) {
            mappersSeen.clear();
        }
        isNew = true;
        depth = 0;
    }
    
    /**
     * Sets an instance property on this MappingContext
     * 
     * @param key
     * @param value
     */
    public void setProperty(Object key, Object value) {
        if (this.properties == null) {
            this.properties = new HashMap<Object, Object>();
        }
        this.properties.put(key, value);
    }
    
    /**
     * Get a property set on the current mapping context; individual properties
     * set on this context instance are checked first, followed by global properties.
     * 
     * @param key
     * @return the object stored under the specified key as a instance or global property.
     */
    public Object getProperty(Object key) {
        Object result = this.properties != null ? this.properties.get(key) : null;
        if (result == null && this.globalProperties != null) {
            result = this.globalProperties.get(key);
        }
        return result;
    }
    
    /**
     * @return the resolvedSourceType in the current context
     */
    public Type<?> getResolvedSourceType() {
        return resolvedSourceType;
    }
    
    /**
     * @param resolvedSourceType
     *            the resolvedSourceType to set
     */
    public void setResolvedSourceType(Type<?> resolvedSourceType) {
        this.resolvedSourceType = resolvedSourceType;
    }
    
    /**
     * @return the resolvedDestinationType in the current context
     */
    public Type<?> getResolvedDestinationType() {
        return resolvedDestinationType;
    }
    
    /**
     * @param resolvedDestinationType
     *            the resolvedDestinationType to set
     */
    public void setResolvedDestinationType(Type<?> resolvedDestinationType) {
        this.resolvedDestinationType = resolvedDestinationType;
    }
}