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

package ma.glasnost.orika;

import ma.glasnost.orika.cern.colt.map.OpenIntObjectHashMap;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * MappingContext provides storage for information shared among the various
 * mapping objects for a given mapping request.
 * 
 */
public class MappingContext {
    
    protected final Map<Type<?>, Type<?>> mapping;
    protected final OpenIntObjectHashMap typeCache;
    protected List<Map<MapperKey, ClassMap<?, ?>>> mappersSeen;
    protected Map<Object, Object> properties;
    protected Map<Object, Object> globalProperties;
    protected boolean isNewConcreteClass = true;
    protected boolean containsCycle = true;
    protected int depth;
    protected Type<?> resolvedSourceType;
    protected Type<?> resolvedDestinationType;
    protected MappingStrategy resolvedStrategy;
    protected List<Object[]> fieldMappingStack;
    protected boolean capturesFieldContext;
    
    public static enum StackElement {
        SOURCE_NAME, SOURCE_TYPE, SOURCE, DEST_NAME, DEST_TYPE, DEST
    }
    
    /**
     * Factory constructs instances of the base MappingContext
     */
    public static class Factory implements MappingContextFactory {
        
        LinkedBlockingQueue<MappingContext> contextQueue = new LinkedBlockingQueue<>();
        ConcurrentHashMap<Object, Object> globalProperties = new ConcurrentHashMap<>();
        
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
     * Constructs a new MappingContext with the specified (immutable) global
     * properties;
     * 
     * @param globalProperties
     */
    public MappingContext(Map<Object, Object> globalProperties) {
        this.mapping = new HashMap<>();
        this.typeCache = new OpenIntObjectHashMap();
        this.globalProperties = globalProperties;
        Boolean capture = globalProperties != null ? (Boolean)globalProperties.get(Properties.CAPTURE_FIELD_CONTEXT) : null;
        this.capturesFieldContext = capture == null || capture;
    }
    
    /**
     * Sets whether this MappingContext needs to guard against cycles when
     * mapping the current object graph; specifying <code>false</code> when
     * applicable can lend improved performance.
     * 
     * @param containsCycle
     */
    public void containsCycle(boolean containsCycle) {
        this.containsCycle = containsCycle;
    }
    
    /**
     * @return true if this mapping context is watching for cycles in the object
     *         graph
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
     * Searches for a concrete class that has been registered for the given
     * abstract class or interface within this mapping session.
     * 
     * @param sourceType
     * @param destinationType
     * @return a concrete class that has been registered for the given abstract
     *         class or interface within this mapping session, if any
     */
    @SuppressWarnings("unchecked")
    public <S, D> Type<? extends D> getConcreteClass(Type<S> sourceType, Type<D> destinationType) {
        if (isNewConcreteClass) {
            return null;
        }
        final Type<?> type = mapping.get(sourceType);
        if (type != null && destinationType.isAssignableFrom(type)) {
            return (Type<? extends D>) type;
        }
        return null;
    }
    
    /**
     * Registers a concrete class to be used for the given abstract class or
     * interface within this mapping session only.
     * 
     * @param subjectClass
     * @param concreteClass
     */
    public void registerConcreteClass(Type<?> subjectClass, Type<?> concreteClass) {
        mapping.put(subjectClass, concreteClass);
        isNewConcreteClass = false;
    }
    
    /**
     * Caches an object instance which has been mapped for a particular source
     * instance and destination type in this mapping context; this will later be
     * referenced in avoiding infinite recursion mapping the same object.
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
                localCache = new IdentityHashMap<>(2);
                typeCache.put(destinationType.getUniqueIndex(), localCache);
                
            }
            localCache.put(source, destination);
            
            isNewConcreteClass = false;
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
        
        if (isNewConcreteClass || !containsCycle) {
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
            mappersSeen = new ArrayList<>();
        }
        Map<MapperKey, ClassMap<?, ?>> list = mappersSeen.isEmpty() ? null : this.mappersSeen.get(depth - 1);
        if (list == null) {
            list = new HashMap<>();
        }
        list.put(classMap.getMapperKey(), classMap);
    }
    
    /**
     * Looks up a ClassMap among the mappers generated with this mapping context
     * 
     * @param mapperKey
     * @return the ClassMap for which a Mapper was generated in this context, if
     *         any
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
     * 
     * @deprecated This variant exists for backwards compatibility only; if
     *             overriding, override
     *             {@link #beginMapping(Type, Object, Type, Object)} instead.
     */
    @Deprecated
    public void beginMapping() {
        ++depth;
    }
    
    /**
     * Mark the beginning of a particular mapping
     * 
     * @param sourceType
     *            the type of the source object being mapped
     * @param source
     *            the source object being mapped
     * @param destType
     *            the type of the destination object being mapped into
     * @param dest
     *            the destination object being mapped into
     * @deprecated This variant exists for backwards compatibility only; if
     *             overriding, override
     *             {@link #
     *             //beginMapping(Type, Object, Type, String, Object)}
     *             instead.
     */
    @Deprecated
    public void beginMapping(Type<?> sourceType, Object source, Type<?> destType, Object dest) {
        beginMapping();
    }
    
    /**
     * Mark the start of mapping a particular field
     * 
     * @param sourceType
     *            the type of the source field
     * @param sourceName
     *            the name of the source field
     * @param source
     *            the source object being mapped
     * @param destType
     *            the type of the destination object being mapped into
     * @param destName
     *            the name of the destination field
     * @param dest
     *            the destination object being mapped into
     */
    public void beginMappingField(String sourceName, Type<?> sourceType, Object source, String destName, Type<?> destType, Object dest) {
        if (fieldMappingStack == null) {
            fieldMappingStack = new ArrayList<>();
        }
        Object[] stackElement = new Object[StackElement.values().length];
        stackElement[StackElement.SOURCE_NAME.ordinal()] = sourceName;
        stackElement[StackElement.SOURCE_TYPE.ordinal()] = sourceType;
        stackElement[StackElement.SOURCE.ordinal()] = source;
        stackElement[StackElement.DEST_NAME.ordinal()] = destName;
        stackElement[StackElement.DEST_TYPE.ordinal()] = destType;
        stackElement[StackElement.DEST.ordinal()] = dest;
        fieldMappingStack.add(stackElement);
    }
    
    public void endMappingField() {
        fieldMappingStack.remove(fieldMappingStack.size() - 1);
    }
    
    /**
     * @return the qualified property expression describing the source field
     *         currently being mapped
     */
    public String getFullyQualifiedSourcePath() {
        if (!capturesFieldContext || fieldMappingStack == null) {
            return null;
        }
        StringBuilder path = new StringBuilder("source");
        for (Object[] element : fieldMappingStack) {
            path.append(".");
            path.append(element[StackElement.SOURCE_NAME.ordinal()]);
        }
        return path.toString();
    }
    
    /**
     * @return an array of expressions describing the path to the source field
     *         being currently mapped; each array element contains the
     *         expression value of the field as referenced within the individual
     *         class-map.
     */
    public String[] getSourceExpressionPaths() {
        if (!capturesFieldContext || fieldMappingStack == null) {
            return null;
        }
        String[] path = new String[fieldMappingStack.size()];
        int idx = 0;
        for (Object[] element : fieldMappingStack) {
            path[idx++] = (String) element[StackElement.SOURCE_NAME.ordinal()];
        }
        return path;
    }
    
    /**
     * @return an array of the source object values on the current stack,
     *         where the last element of the array is the value of the 
     *         source field being currently mapped
     */
    public Object[] getSourceObjects() {
        if (!capturesFieldContext || fieldMappingStack == null) {
            return null;
        }
        Object[] path = new Object[fieldMappingStack.size()];
        int idx = 0;
        for (Object[] element : fieldMappingStack) {
            path[idx++] = element[StackElement.SOURCE.ordinal()];
        }
        return path;
    }
    
    /**
     * @return an array of types representing the path to the type of the source
     *         field being currently mapped; this represents each type in the
     *         chain of mappers called to map the current field.
     * 
     */
    public java.lang.reflect.Type[] getSourceTypePaths() {
        if (!capturesFieldContext || fieldMappingStack == null) {
            return null;
        }
        java.lang.reflect.Type[] path = new java.lang.reflect.Type[fieldMappingStack.size()];
        int idx = 0;
        for (Object[] element : fieldMappingStack) {
            path[idx++] = (java.lang.reflect.Type) element[StackElement.SOURCE_TYPE.ordinal()];
        }
        return path;
    }
    
    /**
     * @return the qualified property expression describing the destination
     *         field currently being mapped
     */
    public String getFullyQualifiedDestinationPath() {
        if (!capturesFieldContext || fieldMappingStack == null) {
            return null;
        }
        StringBuilder path = new StringBuilder("destination");
        for (Object[] element : fieldMappingStack) {
            path.append(".");
            path.append(element[StackElement.DEST_NAME.ordinal()]);
        }
        return path.toString();
    }
    
    /**
     * @return an array of expressions describing the path to the source field
     *         being currently mapped; each array element contains the
     *         expression value of the field as referenced within the individual
     *         class-map.
     */
    public String[] getDestinationExpressionPaths() {
        if (!capturesFieldContext || fieldMappingStack == null) {
            return null;
        }
        String[] path = new String[fieldMappingStack.size()];
        int idx = 0;
        for (Object[] element : fieldMappingStack) {
            path[idx++] = (String) element[StackElement.DEST_NAME.ordinal()];
        }
        return path;
    }
    
    /**
     * @return an array of the destination object values on the current stack,
     *         where the last element of the array is the value of the 
     *         destination field being currently mapped
     */
    public Object[] getDestinationObjects() {
        if (!capturesFieldContext || fieldMappingStack == null) {
            return null;
        }
        Object[] path = new Object[fieldMappingStack.size()];
        int idx = 0;
        for (Object[] element : fieldMappingStack) {
            path[idx++] = element[StackElement.DEST.ordinal()];
        }
        return path;
    }
    
    /**
     * @return an array of types representing the path to the type of the
     *         destination field being currently mapped; this represents each
     *         type in the chain of mappers called to map the current field.
     */
    public java.lang.reflect.Type[] getDestinationTypePaths() {
        if (!capturesFieldContext || fieldMappingStack == null) {
            return null;
        }
        java.lang.reflect.Type[] path = new java.lang.reflect.Type[fieldMappingStack.size()];
        int idx = 0;
        for (Object[] element : fieldMappingStack) {
            path[idx++] = (java.lang.reflect.Type) element[StackElement.DEST_TYPE.ordinal()];
        }
        return path;
    }
    
    /**
     * Mark the end of a particular mapping
     */
    public void endMapping() {
        --depth;
    }
    
    /**
     * Resets this context instance, in preparation for use by another mapping
     * request
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
        if (fieldMappingStack != null) {
            fieldMappingStack.clear();
        }
        resolvedSourceType = null;
        resolvedDestinationType = null;
        resolvedStrategy = null;
        isNewConcreteClass = true;
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
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
    }
    
    /**
     * Get a property set on the current mapping context; individual properties
     * set on this context instance are checked first, followed by global
     * properties.
     * 
     * @param key
     * @return the object stored under the specified key as a instance or global
     *         property.
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
    
    /**
     * @return the resolved strategy in the current context
     */
    public MappingStrategy getResolvedStrategy() {
        return resolvedStrategy;
    }
    
    /**
     * @param resolvedStrategy
     *            the mapping strategy to set
     */
    public void setResolvedStrategy(MappingStrategy resolvedStrategy) {
        this.resolvedStrategy = resolvedStrategy;
    }
}
