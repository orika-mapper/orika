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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategy;
import ma.glasnost.orika.metadata.Type;

/**
 * ConfigurableMapper is a convenience type which provides a 
 * simplification for reuse of a particular Orika mapping configuration in 
 * a given context.<br><br>
 * 
 * It can be especially useful in a Spring context where you'd like initialize
 * Orika with particular configuration(s) at startup and reuse the MapperFacade.<br>
 * Simply wire your own extension of ConfigurableMapper containing your own 
 * configurations and use it as you would the MapperFacade you'd normally 
 * retrieve from MapperFactory.
 * <br><br>
 * 
 * ConfigurableMapper should be extended, overriding the {@link #configure(MapperFactory)}
 * method to provide the necessary initializations and customizations desired.<br><br>
 * 
 * Additionally, if customizations are needed to the DefaultMapperFactory builder (used by
 * ConfigurableMapper), the {@link #configureFactoryBuilder(ma.glasnost.orika.impl.DefaultMapperFactory.Builder)}
 * method may be overridden to apply custom parameters to the builder used to obtain the MapperFactory.<br><br>
 * For example:
 * <pre>
 * public class MyCustomMapper extends ConfigurableMapper {
 * 
 *    protected void configure(MapperFactory factory) {
 *       
 *       factory.registerClassMapping(...);
 *       
 *       factory.getConverterFactory().registerConverter(...);
 *       
 *       factory.registerDefaultMappingHint(...);
 *     
 *    }
 * }
 * 
 * ...
 * 
 * public class SomeOtherClass {
 * 
 *    private MapperFacade mapper = new MyCustomMapper();
 * 
 *    void someMethod() {
 *       
 *       mapper.map(blah, Blah.class);
 *       ...
 *    }
 *    ...
 * }
 * </pre>
 * 
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class ConfigurableMapper implements MapperFacade {

    private final MapperFacade facade;
    private final DefaultMapperFactory factory;
    
    protected ConfigurableMapper() {
        
        DefaultMapperFactory.Builder factoryBuilder = new DefaultMapperFactory.Builder();
        /*
         * Apply optional user customizations to the factory builder
         */
        configureFactoryBuilder(factoryBuilder);
        
        factory = factoryBuilder.build();
        
        /*
         * Apply customizations/configurations
         */
        configure(factory);
        
        facade = factory.getMapperFacade();
    }
   
    /**
     * Implement this method to provide your own configurations to
     * the Orika MapperFactory used by this mapper.
     * 
     * @param factory the MapperFactory instance which may be used to
     * register various configurations, mappings, etc.
     */
    protected void configure(MapperFactory factory) {
        /*
         * No-Op; customize as needed
         */
    }

    
    /**
     * Override this method only if you need to customize any of the parameters
     * passed to the factory builder, in the case that you've provided your
     * own custom implementation of one of the core components of Orika.
     * 
     * @param factoryBuilder the builder which will be used to obtain a MapperFactory instance
     */
    protected void configureFactoryBuilder(DefaultMapperFactory.Builder factoryBuilder) {
        /*
         * No-Op; customize as needed
         */
    }

    /**
     * Delegate methods for MapperFacade;
     */
    
    public <S, D> D map(S sourceObject, Class<D> destinationClass) {
        return facade.map(sourceObject, destinationClass);
    }

    public <S, D> D map(S sourceObject, Class<D> destinationClass, MappingContext context) {
        return facade.map(sourceObject, destinationClass, context);
    }

    public <S, D> void map(S sourceObject, D destinationObject) {
        facade.map(sourceObject, destinationObject);
    }

    public <S, D> void map(S sourceObject, D destinationObject, MappingContext context) {
        facade.map(sourceObject, destinationObject, context);
    }

    public <S, D> void map(S sourceObject, D destinationObject, Type<S> sourceType, Type<D> destinationType) {
        facade.map(sourceObject, destinationObject, sourceType, destinationType);
    }

    public <S, D> void map(S sourceObject, D destinationObject, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        facade.map(sourceObject, destinationObject, sourceType, destinationType, context);
    }

    public <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass) {
        return facade.mapAsSet(source, destinationClass);
    }

    public <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return facade.mapAsSet(source, destinationClass, context);
    }

    public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass) {
        return facade.mapAsSet(source, destinationClass);
    }

    public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass, MappingContext context) {
        return facade.mapAsSet(source, destinationClass, context);
    }

    public <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass) {
        return facade.mapAsList(source, destinationClass);
    }

    public <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return facade.mapAsList(source, destinationClass, context);
    }

    public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass) {
        return facade.mapAsList(source, destinationClass);
    }

    public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass, MappingContext context) {
        return facade.mapAsList(source, destinationClass, context);
    }

    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass) {
        return facade.mapAsArray(destination, source, destinationClass);
    }

    public <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass) {
        return facade.mapAsArray(destination, source, destinationClass);
    }

    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return facade.mapAsArray(destination, source, destinationClass, context);
    }

    public <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass, MappingContext context) {
        return facade.mapAsArray(destination, source, destinationClass, context);
    }

    public <S, D> D map(S sourceObject, Type<S> sourceType, Type<D> destinationType) {
        return facade.map(sourceObject, sourceType, destinationType);
    }

    public <S, D> D map(S sourceObject, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        return facade.map(sourceObject, sourceType, destinationType, context);
    }

    public <S, D> Set<D> mapAsSet(Iterable<S> source, Type<S> sourceType, Type<D> destinationType) {
        return facade.mapAsSet(source, sourceType, destinationType);
    }

    public <S, D> Set<D> mapAsSet(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        return facade.mapAsSet(source, sourceType, destinationType, context);
    }

    public <S, D> Set<D> mapAsSet(S[] source, Type<S> sourceType, Type<D> destinationType) {
        return facade.mapAsSet(source, sourceType, destinationType);
    }

    public <S, D> Set<D> mapAsSet(S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        return facade.mapAsSet(source, sourceType, destinationType, context);
    }

    public <S, D> List<D> mapAsList(Iterable<S> source, Type<S> sourceType, Type<D> destinationType) {
        return facade.mapAsList(source, sourceType, destinationType);
    }

    public <S, D> List<D> mapAsList(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        return facade.mapAsList(source, sourceType, destinationType, context);
    }

    public <S, D> List<D> mapAsList(S[] source, Type<S> sourceType, Type<D> destinationType) {
        return facade.mapAsList(source, sourceType, destinationType);
    }

    public <S, D> List<D> mapAsList(S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        return facade.mapAsList(source, sourceType, destinationType, context);
    }

    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Type<S> sourceType, Type<D> destinationType) {
        return facade.mapAsArray(destination, source, sourceType, destinationType);
    }

    public <S, D> D[] mapAsArray(D[] destination, S[] source, Type<S> sourceType, Type<D> destinationType) {
        return facade.mapAsArray(destination, source, sourceType, destinationType);
    }

    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        return facade.mapAsArray(destination, source, sourceType, destinationType, context);
    }

    public <S, D> D[] mapAsArray(D[] destination, S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        return facade.mapAsArray(destination, source, sourceType, destinationType, context);
    }
    
    public <S, D> void mapAsCollection(Iterable<S> source, Collection<D> destination, Class<D> destinationClass) {
        facade.mapAsCollection(source, destination, destinationClass);
    }

    public <S, D> void mapAsCollection(Iterable<S> source, Collection<D> destination, Class<D> destinationClass, MappingContext context) {
        facade.mapAsCollection(source, destination, destinationClass, context);
    }

    public <S, D> void mapAsCollection(S[] source, Collection<D> destination, Class<D> destinationCollection) {
        facade.mapAsCollection(source, destination, destinationCollection);
    }

    public <S, D> void mapAsCollection(S[] source, Collection<D> destination, Class<D> destinationCollection, MappingContext context) {
        facade.mapAsCollection(source, destination, destinationCollection, context);
    }

    public <S, D> void mapAsCollection(Iterable<S> source, Collection<D> destination, Type<S> sourceType, Type<D> destinationType) {
        facade.mapAsCollection(source, destination, sourceType, destinationType);
    }

    public <S, D> void mapAsCollection(S[] source, Collection<D> destination, Type<S> sourceType, Type<D> destinationType) {
        facade.mapAsCollection(source, destination, sourceType, destinationType);
    }

    public <S, D> void mapAsCollection(Iterable<S> source, Collection<D> destination, Type<S> sourceType, Type<D> destinationType,
            MappingContext context) {
        facade.mapAsCollection(source, destination, sourceType, destinationType, context);
    }

    public <S, D> void mapAsCollection(S[] source, Collection<D> destination, Type<S> sourceType, Type<D> destinationType,
            MappingContext context) {
        facade.mapAsCollection(source, destination, sourceType, destinationType, context);
    }
    
    public <S, D> D convert(S source, Type<S> sourceType, Type<D> destinationType, String converterId) {
        return facade.convert(source, sourceType, destinationType, converterId);
    }


    public <S, D> D convert(S source, Class<D> destinationClass, String converterId) {
        return facade.convert(source, destinationClass, converterId);
    }


    public <S, D> D newObject(S source, Type<? extends D> destinationClass, MappingContext context) {
        return facade.newObject(source, destinationClass, context);
    }
     
    public <Sk, Sv, Dk, Dv> Map<Dk, Dv> mapAsMap(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType,
            Type<? extends Map<Dk, Dv>> destinationType) {
        return facade.mapAsMap(source, sourceType, destinationType);
    }

    public <Sk, Sv, Dk, Dv> Map<Dk, Dv> mapAsMap(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType,
            Type<? extends Map<Dk, Dv>> destinationType, MappingContext context) {
        return facade.mapAsMap(source, sourceType, destinationType, context);
    }

    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(Iterable<S> source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType) {
        return facade.mapAsMap(source, sourceType, destinationType);
    }

    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(Iterable<S> source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType,
            MappingContext context) {
        return facade.mapAsMap(source, sourceType, destinationType, context);
    }

    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(S[] source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType) {
        return facade.mapAsMap(source, sourceType, destinationType);
    }

    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(S[] source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType,
            MappingContext context) {
        return facade.mapAsMap(source, sourceType, destinationType, context);
    }

    public <Sk, Sv, D> List<D> mapAsList(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType) {
        return facade.mapAsList(source, sourceType, destinationType);
    }

    public <Sk, Sv, D> List<D> mapAsList(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType,
            MappingContext context) {
        return facade.mapAsList(source, sourceType, destinationType, context);
    }

    public <Sk, Sv, D> Set<D> mapAsSet(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType) {
        return facade.mapAsSet(source, sourceType, destinationType);
    }

    public <Sk, Sv, D> Set<D> mapAsSet(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType,
            MappingContext context) {
        return facade.mapAsSet(source, sourceType, destinationType, context);
    }

    public <Sk, Sv, D> D[] mapAsArray(D[] destination, Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType) {
        return facade.mapAsArray(destination, source, sourceType, destinationType);
    }

    public <Sk, Sv, D> D[] mapAsArray(D[] destination, Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType,
            MappingContext context) {
        return facade.mapAsArray(destination, source, sourceType, destinationType, context);
    }

    public <S, D> MappingStrategy resolveMappingStrategy(S sourceObject, java.lang.reflect.Type rawAType, java.lang.reflect.Type rawBType,
            boolean mapInPlace, MappingContext context) {
        return facade.resolveMappingStrategy(sourceObject, rawAType, rawBType, mapInPlace, context);
    }

    public <S, D> BoundMapperFacade<S, D> dedicatedMapperFor(Type<S> sourceType, Type<D> destinationType) {
        return factory.getMapperFacade(sourceType, destinationType);
    }

    public <S, D> BoundMapperFacade<S, D> dedicatedMapperFor(Type<S> sourceType, Type<D> destinationType, boolean containsCycles) {
        return factory.getMapperFacade(sourceType, destinationType, containsCycles);
    }

    public <A, B> BoundMapperFacade<A, B> dedicatedMapperFor(Class<A> aType, Class<B> bType) {
        return factory.getMapperFacade(aType, bType);
    }

    public <A, B> BoundMapperFacade<A, B> dedicatedMapperFor(Class<A> aType, Class<B> bType, boolean containsCycles) {
        return factory.getMapperFacade(aType, bType, containsCycles);
    }
}
