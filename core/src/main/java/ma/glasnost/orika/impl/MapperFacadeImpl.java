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

package ma.glasnost.orika.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingContextFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategy;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategyKey;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategyRecorder;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class MapperFacadeImpl implements MapperFacade {
    
    private final MapperFactory mapperFactory;
    private final MappingContextFactory contextFactory;
    private final UnenhanceStrategy unenhanceStrategy;
    private final Map<MappingStrategyKey, MappingStrategy> strategyCache = new ConcurrentLinkedHashMap.Builder<MappingStrategyKey, MappingStrategy>().maximumWeightedCapacity(
            500).build();
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public MapperFacadeImpl(MapperFactory mapperFactory, MappingContextFactory contextFactory, UnenhanceStrategy unenhanceStrategy) {
        this.mapperFactory = mapperFactory;
        
        this.unenhanceStrategy = unenhanceStrategy;
        this.contextFactory = contextFactory;
    }
    
    /**
     * Normalize the source type based on the registered converters, mappers and
     * accessible super types, as well as available unenhancers
     * 
     * @param sourceObject
     * @param sourceType
     * @param destinationType
     * @return
     */
    @SuppressWarnings("unchecked")
    private <S, D> Type<S> normalizeSourceType(S sourceObject, Type<S> sourceType, Type<D> destinationType) {
        
        Type<?> resolvedType = null;
        
        if (sourceType != null) {
            if (destinationType != null && (canCopyByReference(destinationType, sourceType) || canConvert(sourceType, destinationType))) {
                /*
                 * We shouldn't bother further resolving the source type if we
                 * already have a converter or copy-by-reference for the
                 * originally specified type -- since these operations override
                 * the use of a custom mapper which needs the resolution.
                 */
                resolvedType = sourceType;
            } else {
                
                if (sourceType.isAssignableFrom(sourceObject.getClass())) {
                    sourceType = (Type<S>) TypeFactory.valueOf(sourceObject.getClass());
                }
                if (ClassUtil.isConcrete(sourceType)) {
                    resolvedType = unenhanceStrategy.unenhanceType(sourceObject, sourceType);
                } else {
                    resolvedType = unenhanceStrategy.unenhanceType(sourceObject, TypeFactory.resolveTypeOf(sourceObject, sourceType));
                }
            }
        } else {
            resolvedType = unenhanceStrategy.unenhanceType(sourceObject, TypeFactory.typeOf(sourceObject));
        }
        
        return (Type<S>) resolvedType;
    }
    
    public <S, D> D map(S sourceObject, Type<S> sourceType, Type<D> destinationClass) {
        MappingContext context = contextFactory.getContext();
        try {
            return map(sourceObject, sourceType, destinationClass, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    /**
     * Resolves a reusable MappingStrategy for the given set of inputs.
     * 
     * @param sourceObject
     * @param rawAType
     * @param rawBType
     * @param context
     * @return a MappingStrategy suitable to map the source and destination object
     */
    public <S, D> MappingStrategy resolveMappingStrategy(final S sourceObject, final java.lang.reflect.Type initialSourceType,
            final java.lang.reflect.Type initialDestinationType, boolean mapInPlace, final MappingContext context) {
        
        MappingStrategyKey key = new MappingStrategyKey(sourceObject.getClass(), initialSourceType, initialDestinationType, mapInPlace);
        MappingStrategy strategy = strategyCache.get(key);
        
        if (strategy == null) {
            
            @SuppressWarnings("unchecked")
            Type<S> sourceType = (Type<S>) (initialSourceType != null ? TypeFactory.valueOf(initialSourceType)
                    : TypeFactory.typeOf(sourceObject));
            Type<D> destinationType = TypeFactory.valueOf(initialDestinationType);
            
            MappingStrategyRecorder strategyRecorder = new MappingStrategyRecorder(key, unenhanceStrategy);
            
            final Type<S> resolvedSourceType = normalizeSourceType(sourceObject, sourceType, destinationType);
            final S resolvedSourceObject;
            
            if (mapInPlace) {
                resolvedSourceObject = sourceObject;
            } else {
                resolvedSourceObject = unenhanceStrategy.unenhanceObject(sourceObject, sourceType);
            }
            
            strategyRecorder.setResolvedSourceType(resolvedSourceType);
            strategyRecorder.setResolvedDestinationType(destinationType);
            if (sourceObject != resolvedSourceObject) {
                strategyRecorder.setUnenhance(true);
            }
            
            if (!mapInPlace && canCopyByReference(destinationType, resolvedSourceType)) {
                /*
                 * We can copy by reference when source and destination types
                 * are the same and immutable.
                 */
                strategyRecorder.setCopyByReference(true);
            } else if (!mapInPlace && canConvert(resolvedSourceType, destinationType)) {
                strategyRecorder.setResolvedConverter(mapperFactory.getConverterFactory().getConverter(resolvedSourceType, destinationType));
                
            } else {
                Type<? extends D> resolvedDestinationType;
                if (mapInPlace) {
                    resolvedDestinationType = destinationType;
                } else {
                    strategyRecorder.setInstantiate(true);
                    resolvedDestinationType = mapperFactory.lookupConcreteDestinationType(resolvedSourceType, destinationType, context);
                    if (resolvedDestinationType == null) {
                        if (!ClassUtil.isConcrete(destinationType)) {
                            MappingException e = new MappingException("No concrete class mapping defined for source class "
                                    + resolvedSourceType.getName());
                            e.setDestinationType(destinationType);
                            e.setSourceType(resolvedSourceType);
                            throw e;
                        } else {
                            resolvedDestinationType = destinationType;
                        }
                    }
                }
                strategyRecorder.setResolvedDestinationType(resolvedDestinationType);
                strategyRecorder.setResolvedMapper(resolveMapper(resolvedSourceType, resolvedDestinationType));
                if (!mapInPlace) {
                    strategyRecorder.setResolvedObjectFactory(mapperFactory.lookupObjectFactory(resolvedDestinationType));
                }
            }
            strategy = strategyRecorder.playback();
            if (log.isDebugEnabled()) {
                log.debug(strategyRecorder.describeDetails());
            }
            strategyCache.put(key, strategy);
        }
        
        /*
         * Set the resolved types on the current mapping context; this can be
         * used by downstream Mappers to determine the originally resolved types
         */
        context.setResolvedSourceType(strategy.getSoureType());
        context.setResolvedDestinationType(strategy.getDestinationType());
        
        return strategy;
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> D map(final S sourceObject, final Type<S> sourceType, final Type<D> destinationType, final MappingContext context) {
        
        try {
            if (destinationType == null) {
                throw new MappingException("Can not map to a null class.");
            }
            if (sourceObject == null) {
                return null;
            }
            
            D existingResult = (D) context.getMappedObject(sourceObject, destinationType);
            if (existingResult == null) {
                MappingStrategy strategy = resolveMappingStrategy(sourceObject, sourceType, destinationType, false, context);
                existingResult = (D) strategy.map(sourceObject, null, context);
            }
            return existingResult;
            
        } catch (MappingException e) {
            /* don't wrap our own exceptions */
            throw e;
        } catch (RuntimeException e) {
            if (!ExceptionUtility.originatedByOrika(e)) {
                throw e;
            }
            throw new MappingException("Error encountered while mapping for the following inputs: " + "\nrawSource=" + sourceObject
                    + "\nsourceClass=" + (sourceObject != null ? sourceObject.getClass() : null) + "\nsourceType=" + sourceType
                    + "\ndestinationType=" + destinationType, e);
        }
    }
    
    /**
     * Resolves whether the given mapping operation can use copy-by-reference
     * semantics; should be true if one of the following is true:
     * <ol>
     * <li>resolvedSourceType and destinationType are the same, and one of the
     * immutable types
     * <li>resolvedSourceType is the primitive wrapper for destinationType
     * <li>resolvedSourceType is primitive and destinationType is it's primitive
     * wrapper
     * </ol>
     * 
     * @param destinationType
     * @param resolvedSourceType
     * @return
     */
    private <D, S> boolean canCopyByReference(Type<D> destinationType, final Type<S> resolvedSourceType) {
        if (ClassUtil.isImmutable(resolvedSourceType) && (resolvedSourceType.equals(destinationType))) {
            return true;
        } else if (resolvedSourceType.isPrimitiveWrapper()
                && resolvedSourceType.getRawType().equals(ClassUtil.getWrapperType(destinationType.getRawType()))) {
            return true;
        } else if (resolvedSourceType.isPrimitive()
                && destinationType.getRawType().equals(ClassUtil.getWrapperType(resolvedSourceType.getRawType()))) {
            return true;
        } else {
            return false;
        }
    }
    
    public <S, D> void map(S sourceObject, D destinationObject, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        
        try {
            if (destinationObject == null) {
                throw new MappingException("[destinationObject] can not be null.");
            }
            
            if (destinationType == null) {
                throw new MappingException("[destinationType] can not be null.");
            }
            
            if (sourceObject == null) {
                throw new MappingException("[sourceObject] can not be null.");
            }
            
            if (context.getMappedObject(sourceObject, destinationType) == null) {
                MappingStrategy strategy = resolveMappingStrategy(sourceObject, sourceType, destinationType, true, context);
                strategy.map(sourceObject, destinationObject, context);
            }
            
        } catch (MappingException e) {
            /* don't wrap our own exceptions */
            throw e;
        } catch (RuntimeException e) {
            if (!ExceptionUtility.originatedByOrika(e)) {
                throw e;
            }
            throw new MappingException("Error encountered while mapping for the following inputs: " + "\nrawSource=" + sourceObject
                    + "\nsourceClass=" + (sourceObject != null ? sourceObject.getClass() : null) + "\nsourceType=" + sourceType
                    + "\nrawDestination=" + destinationObject + "\ndestinationClass="
                    + (destinationObject != null ? destinationObject.getClass() : null) + "\ndestinationType=" + destinationType, e);
        }
        
    }
    
    public <S, D> void map(S sourceObject, D destinationObject, Type<S> sourceType, Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            map(sourceObject, destinationObject, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> void map(S sourceObject, D destinationObject, MappingContext context) {
        
        try {
            if (destinationObject == null) {
                throw new MappingException("[destinationObject] can not be null.");
            }
            
            if (sourceObject == null) {
                throw new MappingException("[sourceObject] can not be null.");
            }
            
            if (context.getMappedObject(sourceObject, destinationObject.getClass()) == null) {
                MappingStrategy strategy = resolveMappingStrategy(sourceObject, null, destinationObject.getClass(), true, context);
                strategy.map(sourceObject, destinationObject, context);
            }
            
        } catch (MappingException e) {
            /* don't wrap our own exceptions */
            throw e;
        } catch (RuntimeException e) {
            if (!ExceptionUtility.originatedByOrika(e)) {
                throw e;
            }
            throw new MappingException("Error encountered while mapping for the following inputs: " + "\nrawSource=" + sourceObject
                    + "\nsourceClass=" + (sourceObject != null ? sourceObject.getClass() : null) + "\nrawDestination=" + destinationObject
                    + "\ndestinationClass=" + (destinationObject != null ? destinationObject.getClass() : null), e);
        }
    }
    
    public <S, D> void map(S sourceObject, D destinationObject) {
        MappingContext context = contextFactory.getContext();
        try {
            map(sourceObject, destinationObject, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public final <S, D> Set<D> mapAsSet(Iterable<S> source, Type<S> sourceType, Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsSet(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public final <S, D> Set<D> mapAsSet(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        return (Set<D>) mapAsCollection(source, sourceType, destinationType, new HashSet<D>(), context);
    }
    
    public final <S, D> List<D> mapAsList(Iterable<S> source, Type<S> sourceType, Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return (List<D>) mapAsCollection(source, sourceType, destinationType, new ArrayList<D>(), context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public final <S, D> List<D> mapAsList(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        return (List<D>) mapAsCollection(source, sourceType, destinationType, new ArrayList<D>(), context);
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Type<S> sourceType, Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsArray(destination, source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Type<S> sourceType, Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsArray(destination, source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        int i = 0;
        for (final S s : source) {
            destination[i++] = map(s, sourceType, destinationType);
        }
        return destination;
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        int i = 0;
        for (final S s : source) {
            destination[i++] = map(s, sourceType, destinationType, context);
        }
        return destination;
    }
    
    public <S, D> List<D> mapAsList(S[] source, Type<S> sourceType, Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsList(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> List<D> mapAsList(S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        final List<D> destination = new ArrayList<D>(source.length);
        for (final S s : source) {
            destination.add(map(s, sourceType, destinationType, context));
        }
        return destination;
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Type<S> sourceType, Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsSet(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        final Set<D> destination = new HashSet<D>(source.length);
        for (final S s : source) {
            destination.add(map(s, sourceType, destinationType, context));
        }
        return destination;
    }
    
    /**
     * Map an iterable onto an existing collection
     * 
     * @param source
     *            the source iterable
     * @param destination
     *            the destination into which the results will be mapped
     * @param sourceType
     *            the type of
     * @param destinationType
     * @param context
     */
    public <S, D> void mapAsCollection(Iterable<S> source, Collection<D> destination, Type<S> sourceType, Type<D> destinationType,
            MappingContext context) {
        if (source == null) {
            return;
        }
        if (destination != null) {
            destination.clear();
            for (S item : source) {
                destination.add(map(item, sourceType, destinationType, context));
            }
        }
    }
    
    /**
     * Map an array onto an existing collection
     * 
     * @param source
     * @param destination
     * @param sourceType
     * @param destinationType
     * @param context
     */
    public <S, D> void mapAsCollection(S[] source, Collection<D> destination, Type<S> sourceType, Type<D> destinationType,
            MappingContext context) {
        if (source == null) {
            return;
        }
        if (destination != null) {
            destination.clear();
            for (S item : source) {
                destination.add(map(item, sourceType, destinationType, context));
            }
        }
    }
    
    Mapper<Object, Object> resolveMapper(Type<?> sourceType, Type<?> destinationType) {
        final MapperKey mapperKey = new MapperKey(sourceType, destinationType);
        Mapper<Object, Object> mapper = mapperFactory.lookupMapper(mapperKey);
        
        if (mapper == null) {
            throw new IllegalStateException(String.format("Cannot create a mapper for classes : %s, %s", destinationType, sourceType));
        }
        
        if ((!mapper.getAType().equals(sourceType) && mapper.getAType().equals(destinationType))
                || (!mapper.getAType().isAssignableFrom(sourceType) && mapper.getAType().isAssignableFrom(destinationType))) {
            mapper = ReversedMapper.reverse(mapper);
        }
        return mapper;
    }
    
    /**
     * Maps the declared properties of the source object into the destination
     * object, using the specified mapper.
     * 
     * @param sourceObject
     *            the source object from which to map property values
     * @param destinationObject
     *            the destination object onto which the source object's property
     *            values will be mapped
     * @param sourceType
     *            the type of the source object
     * @param destinationType
     *            the type of the destination object
     * @param context
     *            the current mapping context
     * @param mapper
     *            the mapper to use for mapping the source property values onto
     *            the destination
     * @param strategyBuilder
     *            the strategy builder used to record the mapping strategy taken
     */
    void mapDeclaredProperties(Object sourceObject, Object destinationObject, Type<?> sourceType, Type<?> destinationType,
            MappingContext context, Mapper<Object, Object> mapper, MappingStrategyRecorder strategyBuilder) {
        
        if (mapper.getAType().equals(sourceType)) {
            mapper.mapAtoB(sourceObject, destinationObject, context);
        } else if (mapper.getAType().equals(destinationType)) {
            mapper.mapBtoA(sourceObject, destinationObject, context);
            if (strategyBuilder != null)
                strategyBuilder.setMapReverse(true);
        } else if (mapper.getAType().isAssignableFrom(sourceType)) {
            mapper.mapAtoB(sourceObject, destinationObject, context);
        } else if (mapper.getAType().isAssignableFrom(destinationType)) {
            mapper.mapBtoA(sourceObject, destinationObject, context);
            if (strategyBuilder != null)
                strategyBuilder.setMapReverse(true);
        } else {
            throw new IllegalStateException(String.format("Source object type's must be one of '%s' or '%s'.", mapper.getAType(),
                    mapper.getBType()));
            
        }
    }
    
    private <S, D> D newObject(S sourceObject, Type<? extends D> destinationType, MappingContext context,
            MappingStrategyRecorder strategyBuilder) {
        
        final ObjectFactory<? extends D> objectFactory = mapperFactory.lookupObjectFactory(destinationType);
        
        if (strategyBuilder != null) {
            strategyBuilder.setResolvedObjectFactory(objectFactory);
        }
        return objectFactory.create(sourceObject, context);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.MapperFacade#newObject(java.lang.Object,
     * ma.glasnost.orika.metadata.Type, ma.glasnost.orika.MappingContext)
     */
    public <S, D> D newObject(S sourceObject, Type<? extends D> destinationType, MappingContext context) {
        return newObject(sourceObject, destinationType, context, null);
    }
    
    /**
     * Map the iterable into the provided destination collection and return it
     * 
     * @param source
     * @param sourceType
     * @param destinationType
     * @param destination
     * @param context
     * @return
     */
    @SuppressWarnings("unchecked")
    <S, D> Collection<D> mapAsCollection(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, Collection<D> destination,
            MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        MappingStrategy strategy = null;
        Class<?> sourceClass = null;
        for (final S item : source) {
            if (item == null) {
                continue;
            } else if (strategy == null || (!item.getClass().equals(sourceClass))) {
                /*
                 * Resolve the strategy and reuse; assuming this is a
                 * homogeneous collection, this will save us (n-1) lookups; if
                 * not, we would have done those lookups anyway
                 */
                strategy = resolveMappingStrategy(item, sourceType, destinationType, false, context);
                sourceClass = item.getClass();
            }
            D mappedItem = (D) context.getMappedObject(item, destinationType);
            if (mappedItem == null) {
                mappedItem = (D) strategy.map(item, null, context);
            }
            destination.add(mappedItem);
        }
        return destination;
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> D convert(S source, Type<S> sourceType, Type<D> destinationType, String converterId) {
        
        Converter<S, D> converter;
        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        if (converterId == null) {
            final Type<? extends Object> sourceClass = normalizeSourceType(source, sourceType, destinationType);
            converter = (Converter<S, D>) converterFactory.getConverter(sourceClass, destinationType);
        } else {
            converter = (Converter<S, D>) converterFactory.getConverter(converterId);
        }
        
        return converter.convert(source, destinationType);
    }
    
    private <S, D> boolean canConvert(Type<S> sourceType, Type<D> destinationType) {
        return mapperFactory.getConverterFactory().canConvert(sourceType, destinationType);
    }
    
    public <S, D> D map(S sourceObject, Class<D> destinationClass) {
        MappingContext context = contextFactory.getContext();
        try {
            return map(sourceObject, destinationClass, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> D map(S sourceObject, Class<D> destinationClass, MappingContext context) {
        
        try {
            if (destinationClass == null) {
                throw new MappingException("Can not map to a null class.");
            }
            if (sourceObject == null) {
                return null;
            }
            
            D result = (D) context.getMappedObject(sourceObject, destinationClass);
            if (result == null) {
                MappingStrategy strategy = resolveMappingStrategy(sourceObject, null, destinationClass, false, context);
                result = (D) strategy.map(sourceObject, null, context);
            }
            return result;
            
        } catch (MappingException e) {
            /* don't wrap our own exceptions */
            throw e;
        } catch (RuntimeException e) {
            if (!ExceptionUtility.originatedByOrika(e)) {
                throw e;
            }
            throw new MappingException(
                    "Error encountered while mapping for the following inputs: " + "\nrawSource=" + sourceObject + "\nsourceClass="
                            + (sourceObject != null ? sourceObject.getClass() : null) + "\ndestinationClass=" + destinationClass, e);
        }
    }
    
    public <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass) {
        return mapAsSet(source, TypeFactory.elementTypeOf(source), TypeFactory.<D> valueOf(destinationClass));
    }
    
    public <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return mapAsSet(source, TypeFactory.elementTypeOf(source), TypeFactory.<D> valueOf(destinationClass), context);
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass) {
        return mapAsSet(source, TypeFactory.componentTypeOf(source), TypeFactory.<D> valueOf(destinationClass));
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass, MappingContext context) {
        return mapAsSet(source, TypeFactory.componentTypeOf(source), TypeFactory.<D> valueOf(destinationClass), context);
    }
    
    public <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass) {
        return mapAsList(source, TypeFactory.elementTypeOf(source), TypeFactory.<D> valueOf(destinationClass));
    }
    
    public <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return mapAsList(source, TypeFactory.elementTypeOf(source), TypeFactory.<D> valueOf(destinationClass), context);
    }
    
    public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass) {
        return mapAsList(source, TypeFactory.componentTypeOf(source), TypeFactory.<D> valueOf(destinationClass));
    }
    
    public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass, MappingContext context) {
        return mapAsList(source, TypeFactory.componentTypeOf(source), TypeFactory.<D> valueOf(destinationClass), context);
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass) {
        return mapAsArray(destination, source, TypeFactory.elementTypeOf(source), TypeFactory.<D> valueOf(destinationClass));
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass) {
        return mapAsArray(destination, source, TypeFactory.componentTypeOf(source), TypeFactory.<D> valueOf(destinationClass));
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return mapAsArray(destination, source, TypeFactory.elementTypeOf(source), TypeFactory.<D> valueOf(destinationClass), context);
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass, MappingContext context) {
        return mapAsArray(destination, source, TypeFactory.componentTypeOf(source), TypeFactory.<D> valueOf(destinationClass), context);
    }
    
    public <S, D> D convert(S source, Class<D> destinationClass, String converterId) {
        return convert(source, TypeFactory.typeOf(source), TypeFactory.<D> valueOf(destinationClass), converterId);
    }
    
    /*
     * New mapping type: Map to Map
     */
    public <Sk, Sv, Dk, Dv> Map<Dk, Dv> mapAsMap(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType,
            Type<? extends Map<Dk, Dv>> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsMap(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <Sk, Sv, Dk, Dv> Map<Dk, Dv> mapAsMap(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType,
            Type<? extends Map<Dk, Dv>> destinationType, MappingContext context) {
        Map<Dk, Dv> destination = new HashMap<Dk, Dv>(source.size());
        
        /*
         * Resolve the strategy used for the key and value; only re-resolve a
         * strategy if we encounter a different source class. This should allow
         * us to process a homogeneous key/value typed map as quickly as
         * possible
         */
        MappingStrategy keyStrategy = null;
        MappingStrategy valueStrategy = null;
        Class<?> keyClass = null;
        Class<?> valueClass = null;
        
        for (Entry<Sk, Sv> entry : source.entrySet()) {
            Dk key;
            if (entry.getKey() == null) {
                key = null;
            } else {
                if (keyStrategy == null || !entry.getKey().getClass().equals(keyClass)) {
                    keyStrategy = resolveMappingStrategy(entry.getKey(), sourceType.<Sk> getNestedType(0),
                            destinationType.<Dk> getNestedType(0), false, context);
                    keyClass = entry.getKey().getClass();
                }
                Dk mappedKey = (Dk) context.getMappedObject(entry.getKey(), destinationType.<Dk> getNestedType(0));
                if (mappedKey == null) {
                    mappedKey = (Dk) (Dk) keyStrategy.map(entry.getKey(), null, context);
                }
                
                key = mappedKey;
            }
            
            Dv value;
            if (entry.getValue() == null) {
                value = null;
            } else {
                if (valueStrategy == null || !entry.getValue().getClass().equals(valueClass)) {
                    valueStrategy = resolveMappingStrategy(entry.getValue(), sourceType.<Sv> getNestedType(1),
                            destinationType.<Dv> getNestedType(1), false, context);
                    valueClass = entry.getValue().getClass();
                }
                
                Dv mappedValue = (Dv) context.getMappedObject(entry.getValue(), destinationType.<Dv> getNestedType(1));
                if (mappedValue == null) {
                    mappedValue = (Dv) (Dv) valueStrategy.map(entry.getValue(), null, context);
                }
                
                value = mappedValue;
            }
            
            destination.put(key, value);
        }
        return destination;
    }
    
    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(Iterable<S> source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsMap(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(Iterable<S> source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType,
            MappingContext context) {
        
        Map<Dk, Dv> destination = new HashMap<Dk, Dv>();
        MappingStrategy strategy = null;
        Class<?> entryClass = null;
        
        Type<?> entryType = TypeFactory.valueOf(Map.Entry.class, destinationType.getNestedType(0), destinationType.getNestedType(1));
        
        for (S element : source) {
            if (strategy == null || !element.getClass().equals(entryClass)) {
                strategy = resolveMappingStrategy(element, sourceType, entryType, false, context);
                entryClass = element.getClass();
            }
            
            Map.Entry<Dk, Dv> entry = context.getMappedObject(element, entryType);
            if (entry == null) {
                entry = (Map.Entry<Dk, Dv>) strategy.map(element, null, context);
            }
            destination.put(entry.getKey(), entry.getValue());
        }
        
        return destination;
    }
    
    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(S[] source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsMap(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(S[] source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType,
            MappingContext context) {
        
        Map<Dk, Dv> destination = new HashMap<Dk, Dv>();
        Type<MapEntry<Dk, Dv>> entryType = MapEntry.concreteEntryType(destinationType);
        MappingStrategy strategy = null;
        Class<?> entryClass = null;
        
        for (S element : source) {
            if (strategy == null || !element.getClass().equals(entryClass)) {
                strategy = resolveMappingStrategy(element, sourceType, entryType, false, context);
                entryClass = element.getClass();
            }
            
            MapEntry<Dk, Dv> entry = context.getMappedObject(element, entryType);
            if (entry == null) {
                entry = (MapEntry<Dk, Dv>) strategy.map(element, null, context);
            }
            destination.put(entry.getKey(), entry.getValue());
        }
        
        return destination;
    }
    
    /*
     * New mapping type: Map to List, Set or Array
     */
    public <Sk, Sv, D> List<D> mapAsList(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsList(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <Sk, Sv, D> List<D> mapAsList(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType,
            MappingContext context) {
        /*
         * Use map as collection to map the entry set to a list; requires an
         * existing mapping for Map.Entry to to type D.
         */
        List<D> destination = new ArrayList<D>(source.size());
        
        Type<MapEntry<Sk, Sv>> entryType = MapEntry.concreteEntryType(sourceType);
        
        return (List<D>) mapAsCollection(MapEntry.entrySet(source), entryType, destinationType, destination, context);
    }
    
    public <Sk, Sv, D> Set<D> mapAsSet(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsSet(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <Sk, Sv, D> Set<D> mapAsSet(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType,
            MappingContext context) {
        /*
         * Use map as collection to map the entry set to a list; requires an
         * existing mapping for Map.Entry to to type D.
         */
        Set<D> destination = new HashSet<D>(source.size());
        Type<Entry<Sk, Sv>> entryType = TypeFactory.resolveTypeOf(source.entrySet(), sourceType).getNestedType(0);
        return (Set<D>) mapAsCollection(source.entrySet(), entryType, destinationType, destination, context);
    }
    
    public <Sk, Sv, D> D[] mapAsArray(D[] destination, Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsArray(destination, source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <Sk, Sv, D> D[] mapAsArray(D[] destination, Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType,
            MappingContext context) {
        
        Type<MapEntry<Sk, Sv>> entryType = MapEntry.concreteEntryType(sourceType);
        
        return mapAsArray(destination, MapEntry.entrySet(source), entryType, destinationType, context);
    }
    
    public <S, D> void mapAsCollection(Iterable<S> source, Collection<D> destination, Class<D> destinationClass) {
        MappingContext context = contextFactory.getContext();
        try {
            mapAsCollection(source, destination, destinationClass, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> void mapAsCollection(Iterable<S> source, Collection<D> destination, Class<D> destinationClass, MappingContext context) {
        mapAsCollection(source, destination, null, TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> void mapAsCollection(S[] source, Collection<D> destination, Class<D> destinationClass) {
        MappingContext context = contextFactory.getContext();
        try {
            mapAsCollection(source, destination, destinationClass, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> void mapAsCollection(S[] source, Collection<D> destination, Class<D> destinationClass, MappingContext context) {
        mapAsCollection(source, destination, (Type<S>) TypeFactory.valueOf(source.getClass().getComponentType()),
                TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> void mapAsCollection(Iterable<S> source, Collection<D> destination, Type<S> sourceType, Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            mapAsCollection(source, destination, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> void mapAsCollection(S[] source, Collection<D> destination, Type<S> sourceType, Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            mapAsCollection(source, destination, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
}
