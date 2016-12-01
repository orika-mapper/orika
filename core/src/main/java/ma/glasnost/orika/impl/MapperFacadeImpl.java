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

package ma.glasnost.orika.impl;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import ma.glasnost.orika.*;
import ma.glasnost.orika.MappingStrategy.Key;
import ma.glasnost.orika.StateReporter.Reportable;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategyRecorder;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

import static ma.glasnost.orika.StateReporter.DIVIDER;
import static ma.glasnost.orika.StateReporter.humanReadableSizeInMemory;
import static ma.glasnost.orika.util.HashMapUtility.getConcurrentLinkedHashMap;

/**
 * MapperFacadeImpl is the base implementation of MapperFacade
 */
public class MapperFacadeImpl implements MapperFacade, Reportable {
    
    protected final MapperFactory mapperFactory;
    private final MappingContextFactory contextFactory;
    protected final UnenhanceStrategy unenhanceStrategy;
    private final UnenhanceStrategy userUnenhanceStrategy;
    private final ConcurrentLinkedHashMap<Key, MappingStrategy> strategyCache = getConcurrentLinkedHashMap(500);
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ExceptionUtility exceptionUtil;
    
    /**
     * Constructs a new MapperFacadeImpl
     * 
     * @param mapperFactory
     * @param contextFactory
     * @param unenhanceStrategy
     */
    public MapperFacadeImpl(final MapperFactory mapperFactory, final MappingContextFactory contextFactory,
            final UnenhanceStrategy unenhanceStrategy, final ExceptionUtility exceptionUtil) {
        this.mapperFactory = mapperFactory;
        this.exceptionUtil = exceptionUtil;
        this.unenhanceStrategy = unenhanceStrategy;
        this.userUnenhanceStrategy = mapperFactory.getUserUnenhanceStrategy();
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
    private <S, D> Type<S> normalizeSourceType(final S sourceObject, Type<S> sourceType, final Type<D> destinationType) {
        
        Type<?> resolvedType;
        
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
                if (sourceType.isConcrete()) {
                    resolvedType = unenhanceStrategy.unenhanceType(sourceObject, sourceType);
                } else {
                    resolvedType = unenhanceStrategy.unenhanceType(sourceObject, resolveTypeOf(sourceObject, sourceType));
                }
            }
        } else {
            resolvedType = unenhanceStrategy.unenhanceType(sourceObject, typeOf(sourceObject));
        }
        
        return (Type<S>) resolvedType;
    }
    
    public <S, D> D map(final S sourceObject, final Type<S> sourceType, final Type<D> destinationClass) {
        MappingContext context = contextFactory.getContext();
        try {
            return map(sourceObject, sourceType, destinationClass, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    /**
     * Get the class for the specified object, accounting for unwrapping
     * 
     * @param object
     * @return the unwrapped class for the specified target object
     */
    protected Class<?> getClass(final Object object) {
        if (this.userUnenhanceStrategy == null) {
            return object.getClass();
        } else {
            return userUnenhanceStrategy.unenhanceObject(object, TypeFactory.TYPE_OF_OBJECT).getClass();
        }
    }
    
    /**
     * Resolves a reusable MappingStrategy for the given set of inputs.
     * 
     * @param sourceObject
     * @param context
     * @return a MappingStrategy suitable to map the source and destination
     *         object
     */
    public <S, D> MappingStrategy resolveMappingStrategy(final S sourceObject, final java.lang.reflect.Type initialSourceType,
            final java.lang.reflect.Type initialDestinationType, final boolean mapInPlace, final MappingContext context) {
        
        Key key = new Key(getClass(sourceObject), initialSourceType, initialDestinationType, mapInPlace);
        MappingStrategy strategy = strategyCache.get(key);
        
        if (strategy == null) {
            
            @SuppressWarnings("unchecked")
            Type<S> sourceType = (Type<S>) (initialSourceType != null ? TypeFactory.valueOf(initialSourceType)
                    : typeOf(sourceObject));
            Type<D> destinationType = TypeFactory.valueOf(initialDestinationType);
            
            MappingStrategyRecorder strategyRecorder = new MappingStrategyRecorder(key, unenhanceStrategy);
            
            final Type<S> resolvedSourceType = normalizeSourceType(sourceObject, sourceType, destinationType);
            
            strategyRecorder.setResolvedSourceType(resolvedSourceType);
            strategyRecorder.setResolvedDestinationType(destinationType);
            
            if (!mapInPlace && canCopyByReference(destinationType, resolvedSourceType)) {
                /*
                 * We can copy by reference when destination is assignable from
                 * source and the source is immutable
                 */
                strategyRecorder.setCopyByReference(true);
            } else if (!mapInPlace && canConvert(resolvedSourceType, destinationType)) {
                strategyRecorder.setResolvedConverter(mapperFactory.getConverterFactory().getConverter(resolvedSourceType, destinationType));
                
            } else {
                strategyRecorder.setInstantiate(true);
                Type<? extends D> resolvedDestinationType = resolveDestinationType(context, sourceType, destinationType,
                        resolvedSourceType);
                
                strategyRecorder.setResolvedDestinationType(resolvedDestinationType);
                strategyRecorder.setResolvedMapper(resolveMapper(resolvedSourceType, resolvedDestinationType));
                if (!mapInPlace) {
                    strategyRecorder.setResolvedObjectFactory(mapperFactory.lookupObjectFactory(resolvedDestinationType, resolvedSourceType));
                }
            }
            strategy = strategyRecorder.playback();
            if (log.isDebugEnabled()) {
                log.debug(strategyRecorder.describeDetails());
            }
            MappingStrategy existing = strategyCache.putIfAbsent(key, strategy);
            if (existing != null) {
                strategy = existing;
            }
        }
        
        /*
         * Set the resolved types on the current mapping context; this can be
         * used by downstream Mappers to determine the originally resolved types
         */
        context.setResolvedSourceType(strategy.getAType());
        context.setResolvedDestinationType(strategy.getBType());
        context.setResolvedStrategy(strategy);
        
        return strategy;
    }

    private <S, D> Type<? extends D> resolveDestinationType(MappingContext context, Type<S> sourceType, Type<D> destinationType, Type<S> resolvedSourceType) {
        Type<? extends D> resolvedDestinationType = mapperFactory.lookupConcreteDestinationType(resolvedSourceType, destinationType, context);
        if (resolvedDestinationType == null) {
            if (destinationType.isAssignableFrom(sourceType)) {
                resolvedDestinationType = (Type<? extends D>) resolvedSourceType;
            } else {
                if (!destinationType.isConcrete()) {
                    MappingException e = new MappingException("No concrete class mapping defined for source class " + resolvedSourceType.getName());
                    e.setDestinationType(destinationType);
                    e.setSourceType(resolvedSourceType);
                    throw exceptionUtil.decorate(e);
                } else {
                    resolvedDestinationType = destinationType;
                }
            }

        }
        return resolvedDestinationType;
    }

    public <S, D> D map(final S sourceObject, final Type<S> sourceType, final Type<D> destinationType, final MappingContext context) {
        return map(sourceObject, sourceType, destinationType, context, null);
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> D map(final S sourceObject, final Type<S> sourceType, final Type<D> destinationType, final MappingContext context,
            final MappingStrategy suggestedStrategy) {
        
        MappingStrategy strategy = suggestedStrategy;
        try {
            if (destinationType == null) {
                throw new MappingException("Can not map to a null class.");
            }
            if (sourceObject == null) {
                return null;
            }
            
            D existingResult = context.getMappedObject(sourceObject, destinationType);
            if (existingResult == null) {
                if (strategy == null) {
                    strategy = resolveMappingStrategy(sourceObject, sourceType, destinationType, false, context);
                }
                if (strategy.getBType() != null && !strategy.getBType().equals(destinationType)) {
                    existingResult = context.getMappedObject(sourceObject, strategy.getBType());
                    if (existingResult == null) {
                        existingResult = (D) strategy.map(sourceObject, null, context);
                    }
                } else {
                    existingResult = (D) strategy.map(sourceObject, null, context);
                }
            }
            return existingResult;
            
        } catch (MappingException e) {
            throw exceptionUtil.decorate(e);
        } catch (RuntimeException e) {
            if (!ExceptionUtility.originatedByOrika(e)) {
                throw e;
            }
            MappingException me = exceptionUtil.newMappingException(e);
            me.setSourceClass(sourceObject.getClass());
            me.setSourceType(sourceType);
            me.setDestinationType(destinationType);
            me.setMappingStrategy(strategy);
            throw me;
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
    private <D, S> boolean canCopyByReference(final Type<D> destinationType, final Type<S> resolvedSourceType) {
        if (resolvedSourceType.isImmutable() && (destinationType.isAssignableFrom(resolvedSourceType))) {
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
    
    public <S, D> void map(final S sourceObject, final D destinationObject, final Type<S> sourceType, final Type<D> destinationType,
            final MappingContext context) {
        map(sourceObject, destinationObject, sourceType, destinationType, context, null);
    }
    
    private <S, D> void map(final S sourceObject, final D destinationObject, final Type<S> sourceType, final Type<D> destinationType,
            final MappingContext context, final MappingStrategy suggestedStrategy) {
        MappingStrategy strategy = suggestedStrategy;
        try {
            
            if (context.getMappedObject(sourceObject, destinationType) == null) {
                if (strategy == null) {
                    strategy = resolveMappingStrategy(sourceObject, sourceType, destinationType, true, context);
                }
                strategy.map(sourceObject, destinationObject, context);
            }
            
        } catch (MappingException e) {
            throw exceptionUtil.decorate(e);
        } catch (RuntimeException e) {
            
            if (destinationObject == null) {
                throw new MappingException("[destinationObject] can not be null.");
            }
            
            if (destinationType == null) {
                throw new MappingException("[destinationType] can not be null.");
            }
            
            if (sourceObject == null) {
                throw new MappingException("[sourceObject] can not be null.");
            }
            
            if (!ExceptionUtility.originatedByOrika(e)) {
                throw e;
            }
            MappingException me = exceptionUtil.newMappingException(e);
            me.setSourceClass(sourceObject.getClass());
            me.setSourceType(sourceType);
            me.setDestinationType(destinationType);
            me.setMappingStrategy(strategy);
            throw me;
        }
        
    }
    
    public <S, D> void map(final S sourceObject, final D destinationObject, final Type<S> sourceType, final Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            map(sourceObject, destinationObject, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> void map(final S sourceObject, final D destinationObject, final MappingContext context) {
        map(sourceObject, destinationObject, context, null);
    }
    
    private <S, D> void map(final S sourceObject, final D destinationObject, final MappingContext context,
            final MappingStrategy suggestedStrategy) {
        
        MappingStrategy strategy = suggestedStrategy;
        try {
            if (strategy == null) {
                strategy = resolveMappingStrategy(sourceObject, null, destinationObject.getClass(), true, context);
            }
            if (null == context.getMappedObject(sourceObject, strategy.getBType())) {
                strategy.map(sourceObject, destinationObject, context);
            }
            
        } catch (MappingException e) {
            /* don't wrap our own exceptions */
            throw e;
        } catch (RuntimeException e) {
            
            if (destinationObject == null) {
                throw new MappingException("[destinationObject] can not be null.");
            }
            
            if (sourceObject == null) {
                throw new MappingException("[sourceObject] can not be null.");
            }
            
            if (!ExceptionUtility.originatedByOrika(e)) {
                throw e;
            }
            MappingException me = exceptionUtil.newMappingException(e);
            me.setSourceClass(sourceObject.getClass());
            me.setDestinationType(TypeFactory.valueOf(destinationObject.getClass()));
            me.setMappingStrategy(strategy);
            throw me;
        }
    }
    
    public <S, D> void map(final S sourceObject, final D destinationObject) {
        MappingContext context = contextFactory.getContext();
        try {
            map(sourceObject, destinationObject, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public final <S, D> Set<D> mapAsSet(final Iterable<S> source, final Type<S> sourceType, final Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsSet(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public final <S, D> Set<D> mapAsSet(final Iterable<S> source, final Type<S> sourceType, final Type<D> destinationType,
            final MappingContext context) {
        return (Set<D>) mapAsCollection(source, sourceType, destinationType, new HashSet<D>(), context);
    }
    
    public final <S, D> List<D> mapAsList(final Iterable<S> source, final Type<S> sourceType, final Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return (List<D>) mapAsCollection(source, sourceType, destinationType, new ArrayList<D>(), context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public final <S, D> List<D> mapAsList(final Iterable<S> source, final Type<S> sourceType, final Type<D> destinationType,
            final MappingContext context) {
        return (List<D>) mapAsCollection(source, sourceType, destinationType, new ArrayList<D>(), context);
    }
    
    public <S, D> D[] mapAsArray(final D[] destination, final Iterable<S> source, final Type<S> sourceType, final Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsArray(destination, source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> D[] mapAsArray(final D[] destination, final S[] source, final Type<S> sourceType, final Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsArray(destination, source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> D[] mapAsArray(final D[] destination, final Iterable<S> source, final Type<S> sourceType, final Type<D> destinationType,
            final MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        int i = 0;
        ElementStrategyContext<S, D> elementContext = new ElementStrategyContext<S, D>(context, sourceType, destinationType);
        for (final S item : source) {
            if (item != null) {
                destination[i++] = mapElement(item, elementContext);
            }
        }
        
        return destination;
    }
    
    public <S, D> D[] mapAsArray(final D[] destination, final S[] source, final Type<S> sourceType, final Type<D> destinationType,
            final MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        int i = 0;
        ElementStrategyContext<S, D> elementContext = new ElementStrategyContext<S, D>(context, sourceType, destinationType);
        for (final S item : source) {
            if (item != null) {
                destination[i++] = mapElement(item, elementContext);
            }
        }
        
        return destination;
    }
    
    public <S, D> List<D> mapAsList(final S[] source, final Type<S> sourceType, final Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsList(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> List<D> mapAsList(final S[] source, final Type<S> sourceType, final Type<D> destinationType, final MappingContext context) {
        final List<D> destination = new ArrayList<D>(source.length);
        for (final S s : source) {
            destination.add(map(s, sourceType, destinationType, context));
        }
        return destination;
    }
    
    public <S, D> Set<D> mapAsSet(final S[] source, final Type<S> sourceType, final Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsSet(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> Set<D> mapAsSet(final S[] source, final Type<S> sourceType, final Type<D> destinationType, final MappingContext context) {
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
    public <S, D> void mapAsCollection(final Iterable<S> source, final Collection<D> destination, final Type<S> sourceType,
            final Type<D> destinationType, final MappingContext context) {
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
    public <S, D> void mapAsCollection(final S[] source, final Collection<D> destination, final Type<S> sourceType,
            final Type<D> destinationType, final MappingContext context) {
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
    
    private Mapper<Object, Object> resolveMapper(final Type<?> sourceType, final Type<?> destinationType) {
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

    private <S, D> D newObject(final S sourceObject, final Type<? extends D> destinationType, final MappingContext context,
            final MappingStrategyRecorder strategyBuilder) {
        
        final ObjectFactory<? extends D> objectFactory = mapperFactory.lookupObjectFactory(destinationType,
                TypeFactory.valueOf(sourceObject.getClass()));
        
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
    public <S, D> D newObject(final S sourceObject, final Type<? extends D> destinationType, final MappingContext context) {
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
    private <S, D> Collection<D> mapAsCollection(final Iterable<S> source, final Type<S> sourceType, final Type<D> destinationType,
                                                 final Collection<D> destination, final MappingContext context) {
        
        if (source == null) {
            return null;
        }
        ElementStrategyContext<S, D> elementContext = new ElementStrategyContext<S, D>(context, sourceType, destinationType);
        for (final S item : source) {
            if (item != null) {
                destination.add(mapElement(item, elementContext));
            }
        }
        return destination;
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> D convert(final S source, final Type<S> sourceType, final Type<D> destinationType, final String converterId,
            MappingContext context) {
        
        Converter<S, D> converter;
        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        if (converterId == null) {
            final Type<?> sourceClass = normalizeSourceType(source, sourceType, destinationType);
            converter = (Converter<S, D>) converterFactory.getConverter(sourceClass, destinationType);
        } else {
            converter = (Converter<S, D>) converterFactory.getConverter(converterId);
        }
        
        return converter.convert(source, destinationType, context);
    }
    
    private <S, D> boolean canConvert(final Type<S> sourceType, final Type<D> destinationType) {
        return mapperFactory.getConverterFactory().canConvert(sourceType, destinationType);
    }
    
    public <S, D> D map(final S sourceObject, final Class<D> destinationClass) {
        MappingContext context = contextFactory.getContext();
        try {
            return map(sourceObject, destinationClass, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> D map(final S sourceObject, final Class<D> destinationClass, final MappingContext context) {
        
        MappingStrategy strategy = null;
        try {
            if (destinationClass == null) {
                throw new MappingException("'destinationClass' is required");
            }
            if (sourceObject == null) {
                return null;
            }
            
            D result = context.getMappedObject(sourceObject, TypeFactory.valueOf(destinationClass));
            if (result == null) {
                strategy = resolveMappingStrategy(sourceObject, null, destinationClass, false, context);
                result = (D) strategy.map(sourceObject, null, context);
            }
            return result;
            
        } catch (MappingException e) {
            throw exceptionUtil.decorate(e);
        } catch (RuntimeException e) {
            if (!ExceptionUtility.originatedByOrika(e)) {
                throw e;
            }
            MappingException me = exceptionUtil.newMappingException(e);
            me.setSourceClass(sourceObject.getClass());
            me.setDestinationType(TypeFactory.valueOf(destinationClass));
            me.setMappingStrategy(strategy);
            throw me;
        }
    }
    
    public <S, D> Set<D> mapAsSet(final Iterable<S> source, final Class<D> destinationClass) {
        return mapAsSet(source, elementTypeOf(source), TypeFactory.valueOf(destinationClass));
    }
    
    public <S, D> Set<D> mapAsSet(final Iterable<S> source, final Class<D> destinationClass, final MappingContext context) {
        return mapAsSet(source, elementTypeOf(source), TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> Set<D> mapAsSet(final S[] source, final Class<D> destinationClass) {
        return mapAsSet(source, componentTypeOf(source), TypeFactory.valueOf(destinationClass));
    }
    
    public <S, D> Set<D> mapAsSet(final S[] source, final Class<D> destinationClass, final MappingContext context) {
        return mapAsSet(source, componentTypeOf(source), TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> List<D> mapAsList(final Iterable<S> source, final Class<D> destinationClass) {
        return mapAsList(source, elementTypeOf(source), TypeFactory.valueOf(destinationClass));
    }
    
    public <S, D> List<D> mapAsList(final Iterable<S> source, final Class<D> destinationClass, final MappingContext context) {
        return mapAsList(source, elementTypeOf(source), TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> List<D> mapAsList(final S[] source, final Class<D> destinationClass) {
        return mapAsList(source, componentTypeOf(source), TypeFactory.valueOf(destinationClass));
    }
    
    public <S, D> List<D> mapAsList(final S[] source, final Class<D> destinationClass, final MappingContext context) {
        return mapAsList(source, componentTypeOf(source), TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> D[] mapAsArray(final D[] destination, final Iterable<S> source, final Class<D> destinationClass) {
        return mapAsArray(destination, source, elementTypeOf(source), TypeFactory.valueOf(destinationClass));
    }
    
    public <S, D> D[] mapAsArray(final D[] destination, final S[] source, final Class<D> destinationClass) {
        return mapAsArray(destination, source, componentTypeOf(source), TypeFactory.valueOf(destinationClass));
    }
    
    public <S, D> D[] mapAsArray(final D[] destination, final Iterable<S> source, final Class<D> destinationClass,
            final MappingContext context) {
        return mapAsArray(destination, source, elementTypeOf(source), TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> D[] mapAsArray(final D[] destination, final S[] source, final Class<D> destinationClass, final MappingContext context) {
        return mapAsArray(destination, source, componentTypeOf(source), TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> D convert(final S source, final Class<D> destinationClass, final String converterId, MappingContext context) {
        return convert(source, typeOf(source), TypeFactory.valueOf(destinationClass), converterId, context);
    }
    
    public <Sk, Sv, Dk, Dv> Map<Dk, Dv> mapAsMap(final Map<Sk, Sv> source, final Type<? extends Map<Sk, Sv>> sourceType,
            final Type<? extends Map<Dk, Dv>> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsMap(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    /**
     * A context object used to track the strategy specific to mapping the
     * elements of a multi-occurrence object.
     * 
     * @param <S>
     * @param <D>
     */
    private static class ElementStrategyContext<S, D> {
        
        public ElementStrategyContext(MappingContext mappingContext, Type<S> sourceType, Type<D> destinationType) {
            super();
            this.mappingContext = mappingContext;
            this.sourceType = sourceType;
            this.destinationType = destinationType;
        }
        
        private MappingContext mappingContext;
        private MappingStrategy strategy;
        private Type<S> sourceType;
        private Type<D> destinationType;
        private Class<?> sourceClass;
    }
    
    /**
     * Provides mapping specific to elements of a multi-occurrence object, such
     * as an array, collection, or map. Hadles caching of the resolved strategy
     * based on the class type of the source object using the
     * ElementStrategyContext.
     * <p>
     * Additionally, ensures that the MappingContext instance associated has
     * source, destination, and strategy consistently set.
     * 
     * @param source
     * @param context
     * @return
     */
    private <S, D> D mapElement(S source, ElementStrategyContext<S, D> context) {
        if (context.strategy == null || !source.getClass().equals(context.sourceClass)) {
            context.strategy = resolveMappingStrategy(source, context.sourceType, context.destinationType, false, context.mappingContext);
            context.sourceClass = source.getClass();
        } else {
            context.mappingContext.setResolvedSourceType(context.sourceType);
            context.mappingContext.setResolvedDestinationType(context.destinationType);
            context.mappingContext.setResolvedStrategy(context.strategy);
        }
        return map(source, context.sourceType, context.destinationType, context.mappingContext, context.strategy);
    }
    
    public <Sk, Sv, Dk, Dv> Map<Dk, Dv> mapAsMap(final Map<Sk, Sv> source, final Type<? extends Map<Sk, Sv>> sourceType,
            final Type<? extends Map<Dk, Dv>> destinationType, final MappingContext context) {
        
        // TODO: should use the registered concrete type for Map here...
        // Type<? extends Map<Dk, Dv>> destType =
        // mapperFactory.lookupConcreteDestinationType(sourceType,
        // destinationType, context);
        
        Map<Dk, Dv> destination = new LinkedHashMap<Dk, Dv>(source.size());
        
        /*
         * Resolve the strategy used for the key and value; only re-resolve a
         * strategy if we encounter a different source class. This should allow
         * us to process a homogeneous key/value typed map as quickly as
         * possible
         */
        ElementStrategyContext<Sk, Dk> keyContext = new ElementStrategyContext<Sk, Dk>(context, sourceType.<Sk> getNestedType(0),
                destinationType.<Dk> getNestedType(0));
        ElementStrategyContext<Sv, Dv> valContext = new ElementStrategyContext<Sv, Dv>(context, sourceType.<Sv> getNestedType(1),
                destinationType.<Dv> getNestedType(1));
        
        for (Entry<Sk, Sv> entry : source.entrySet()) {
            Dk key;
            if (entry.getKey() == null) {
                key = null;
            } else {
                key = mapElement(entry.getKey(), keyContext);
            }
            
            Dv value;
            if (entry.getValue() == null) {
                value = null;
            } else {
                value = mapElement(entry.getValue(), valContext);
            }
            
            destination.put(key, value);
        }
        return destination;
    }
    
    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(final Iterable<S> source, final Type<S> sourceType,
            final Type<? extends Map<Dk, Dv>> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsMap(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(final Iterable<S> source, final Type<S> sourceType,
            final Type<? extends Map<Dk, Dv>> destinationType, final MappingContext context) {
        
        Map<Dk, Dv> destination = new HashMap<Dk, Dv>();
        
        Type<?> entryType = TypeFactory.valueOf(Entry.class, destinationType.getNestedType(0), destinationType.getNestedType(1));
        ElementStrategyContext<S, Entry<Dk, Dv>> elementContext = new ElementStrategyContext<S, Entry<Dk, Dv>>(context, sourceType,
                (Type<Entry<Dk, Dv>>) entryType);
        
        for (S element : source)
            if (element != null) {
                Entry<Dk, Dv> entry = mapElement(element, elementContext);
                destination.put(entry.getKey(), entry.getValue());
            }

        return destination;
    }
    
    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(final S[] source, final Type<S> sourceType, final Type<? extends Map<Dk, Dv>> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsMap(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(final S[] source, final Type<S> sourceType, final Type<? extends Map<Dk, Dv>> destinationType,
            final MappingContext context) {
        
        Map<Dk, Dv> destination = new HashMap<Dk, Dv>();
        Type<MapEntry<Dk, Dv>> entryType = MapEntry.concreteEntryType(destinationType);
        ElementStrategyContext<S, MapEntry<Dk, Dv>> elementContext = new ElementStrategyContext<S, MapEntry<Dk, Dv>>(context, sourceType,
                entryType);
        
        for (S element : source) {
            if (element != null) {
                Entry<Dk, Dv> entry = mapElement(element, elementContext);
                destination.put(entry.getKey(), entry.getValue());
            }
        }
        
        return destination;
    }
    
    public <Sk, Sv, D> List<D> mapAsList(final Map<Sk, Sv> source, final Type<? extends Map<Sk, Sv>> sourceType,
            final Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsList(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <Sk, Sv, D> List<D> mapAsList(final Map<Sk, Sv> source, final Type<? extends Map<Sk, Sv>> sourceType,
            final Type<D> destinationType, final MappingContext context) {
        /*
         * Use map as collection to map the entry set to a list; requires an
         * existing mapping for Map.Entry to to type D.
         */
        List<D> destination = new ArrayList<D>(source.size());
        
        Type<MapEntry<Sk, Sv>> entryType = MapEntry.concreteEntryType(sourceType);
        
        return (List<D>) mapAsCollection(MapEntry.entrySet(source), entryType, destinationType, destination, context);
    }
    
    public <Sk, Sv, D> Set<D> mapAsSet(final Map<Sk, Sv> source, final Type<? extends Map<Sk, Sv>> sourceType, final Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsSet(source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <Sk, Sv, D> Set<D> mapAsSet(final Map<Sk, Sv> source, final Type<? extends Map<Sk, Sv>> sourceType,
            final Type<D> destinationType, final MappingContext context) {
        /*
         * Use map as collection to map the entry set to a list; requires an
         * existing mapping for Map.Entry to to type D.
         */
        Set<D> destination = new HashSet<D>(source.size());
        Type<Entry<Sk, Sv>> entryType = resolveTypeOf(source.entrySet(), sourceType).getNestedType(0);
        return (Set<D>) mapAsCollection(source.entrySet(), entryType, destinationType, destination, context);
    }
    
    public <Sk, Sv, D> D[] mapAsArray(final D[] destination, final Map<Sk, Sv> source, final Type<? extends Map<Sk, Sv>> sourceType,
            final Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAsArray(destination, source, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <Sk, Sv, D> D[] mapAsArray(final D[] destination, final Map<Sk, Sv> source, final Type<? extends Map<Sk, Sv>> sourceType,
            final Type<D> destinationType, final MappingContext context) {
        
        Type<MapEntry<Sk, Sv>> entryType = MapEntry.concreteEntryType(sourceType);
        
        return mapAsArray(destination, MapEntry.entrySet(source), entryType, destinationType, context);
    }
    
    public <S, D> void mapAsCollection(final Iterable<S> source, final Collection<D> destination, final Class<D> destinationClass) {
        MappingContext context = contextFactory.getContext();
        try {
            mapAsCollection(source, destination, destinationClass, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> void mapAsCollection(final Iterable<S> source, final Collection<D> destination, final Class<D> destinationClass,
            final MappingContext context) {
        mapAsCollection(source, destination, null, TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> void mapAsCollection(final S[] source, final Collection<D> destination, final Class<D> destinationClass) {
        MappingContext context = contextFactory.getContext();
        try {
            mapAsCollection(source, destination, destinationClass, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> void mapAsCollection(final S[] source, final Collection<D> destination, final Class<D> destinationClass,
            final MappingContext context) {
        mapAsCollection(source, destination, (Type<S>) TypeFactory.valueOf(source.getClass().getComponentType()),
                TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> void mapAsCollection(final Iterable<S> source, final Collection<D> destination, final Type<S> sourceType,
            final Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            mapAsCollection(source, destination, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public <S, D> void mapAsCollection(final S[] source, final Collection<D> destination, final Type<S> sourceType,
            final Type<D> destinationType) {
        MappingContext context = contextFactory.getContext();
        try {
            mapAsCollection(source, destination, sourceType, destinationType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public void factoryModified(MapperFactory factory) {
        strategyCache.clear();
    }
    
    /**
     * Prints the current state of this MapperFacade to the supplied
     * StringBuilder instance.
     * 
     * @param out
     */
    public void reportCurrentState(StringBuilder out) {
        out.append(DIVIDER);
        out.append("\nResolved strategies: ")
                .append(strategyCache.size())
                .append(" (approximate size: ")
                .append(humanReadableSizeInMemory(strategyCache))
                .append(")");
        for (Entry<Key, MappingStrategy> entry : strategyCache.entrySet()) {
            out.append("\n").append(entry.getKey()).append(": ").append(entry.getValue());
        }
        out.append(DIVIDER);
        out.append("\nUnenhance strategy: ").append(unenhanceStrategy);
    }

    /**
     * Return the Type for the given object.
     *
     * @param object
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    private <T> Type<T> typeOf(final T object) {
        return object == null ? null : TypeFactory.valueOf((Class<T>) object.getClass());
    }

    /**
     * Resolve the (element) component type for the given array.
     *
     * @param object
     * @return the resolved Type instance
     */
    @SuppressWarnings("unchecked")
    private <T> Type<T> componentTypeOf(final T[] object) {
        return object == null ? null : TypeFactory.valueOf((Class<T>) object.getClass().getComponentType());
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
    private <T> Type<T> resolveTypeOf(final T object, Type<?> referenceType) {
        return object == null ? null : TypeFactory.resolveValueOf((Class<T>) object.getClass(), referenceType);
    }

    /**
     * Resolve the nested element type for the given Iterable.
     *
     * @param object
     * @return the resolved Type instance
     */
    private <T> Type<T> elementTypeOf(final Iterable<T> object) {
        try {
            Method iterator = object.getClass().getMethod("iterator");
            Type<Iterable<T>> type = TypeFactory.valueOf(iterator.getGenericReturnType());
            return type.getNestedType(0);
        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
    
}
