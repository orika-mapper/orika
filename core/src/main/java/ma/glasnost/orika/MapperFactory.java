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

import java.util.Set;

import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

/**
 * MapperFactory is used to both configure, register, and generate the
 * underlying Mapper and Converter instances which will be used to perform the
 * the mapping functions.
 * 
 * @author S.M. El Aatifi
 * 
 */
public interface MapperFactory {
    
    /**
     * Get the Mapper (if any) which has been associated with the given
     * MapperKey. If one does not exist, and the Mapper supports automatic
     * generation, an appropriate mapper will be generated.
     * 
     * @param mapperKey
     *            the MapperKey for which to look up an associated Mapper
     * @return the Mapper associated with <code>mapperKey</code>;
     */
    <A, B> Mapper<A, B> lookupMapper(MapperKey mapperKey);
    
    /**
     * Tests for the existence of a registered mapper which corresponds to the
     * supplied mapperKey
     * 
     * @param sourceType
     *            the source type
     * @param destinationType
     *            the destination type
     * @param includeAutoGeneratedMappers
     *            specifies whether an auto-generated mapper for the given
     *            source and destination type should be considered
     * @return true if such a mapper is registered with this factory
     */
    boolean existsRegisteredMapper(Type<?> sourceType, Type<?> destinationType, boolean includeAutoGeneratedMappers);
    
    /**
     * Registers the specified custom mapper with the factory; it will be used
     * for mapping between it's configured types.
     * 
     * @param mapper
     *            the mapper to register
     */
    <A, B> void registerMapper(Mapper<A, B> mapper);
    
    /**
     * Registers the given ClassMap instance with the factory; it will be used
     * to configure an appropriate mapping.
     * 
     * @param classMap
     *            the ClassMap instance to register
     */
    <A, B> void registerClassMap(ClassMap<A, B> classMap);
    
    /**
     * Registers the ClassMap configured by the specified ClassMapBuilder; it
     * will be used to configure an appropriate mapping.
     * 
     * @param builder
     *            the ClassMapBuilder to register
     */
    <A, B> void registerClassMap(ClassMapBuilder<A, B> builder);
    
    /**
     * Register the given ObjectFactory with the MapperFactory; it will be used
     * when constructing new instances of the specified targetType.
     * 
     * @param objectFactory
     *            the object factory to register
     * @param targetType
     *            the type which is generated by the given object factory
     * @deprecated use {@link #registerObjectFactory(ObjectFactory, Type)}
     *             instead.
     */
    @Deprecated
    <T> void registerObjectFactory(ObjectFactory<T> objectFactory, Class<T> targetClass);
    
    /**
     * Register the given ObjectFactory with the MapperFactory; it will be used
     * when constructing new instances of the specified targetType.
     * 
     * @param objectFactory
     *            the object factory to register
     * @param targetType
     *            the type which is generated by the given object factory
     */
    <T> void registerObjectFactory(ObjectFactory<T> objectFactory, Type<T> targetType);
    
    /**
     * Return the object factory (if any) which has been registered for the
     * given type.
     * 
     * @param targetType
     *            the type for which to lookup a registered ObjectFactory.
     * @return the ObjectFactory registered for the given targetType;
     *         <code>null</code> if no ObjectFactory has been registered for the
     *         given type.
     */
    <T> ObjectFactory<T> lookupObjectFactory(Type<T> targetType);
    
    /**
     * @param sourceType
     * @param destinationType
     * @param allowAutoGeneratedMappers
     * @param context
     * @return
     */
    <S, D> Type<? extends D> lookupConcreteDestinationType(Type<S> sourceType, Type<D> destinationType,
            MappingContext context);
    
    @Deprecated
    void registerMappingHint(MappingHint... hints);
    
    /**
     * Register one or more DefaultFieldMapper instances to be used by this
     * MapperFactory; these instances will be used whenever automatically
     * generating a Mapper for a given pair of types.
     * 
     * @param fieldDefaults
     *            one or more DefaultFieldMapper instances to register
     */
    void registerDefaultFieldMapper(DefaultFieldMapper... fieldDefaults);
    
    /**
     * Register a concrete type to use when a need arises to generate a new
     * instance of a given abstract type (abstract class or interface)
     * 
     * @param abstractType
     *            the type of the abstract class or interface
     * @param concreteType
     *            the concrete type to instantiate
     */
    void registerConcreteType(Type<?> abstractType, Type<?> concreteType);
    
    /**
     * Register a concrete type to use when a need arises to generate a new
     * instance of a given abstract type (abstract class or interface)
     * 
     * @param abstractType
     *            the abstract class or interface
     * @param concreteType
     *            the concrete class to instantiate
     */
    void registerConcreteType(Class<?> abstractType, Class<?> concreteType);
    
    /**
     * Lookup the class map hierarchy used for the mapping defined by the
     * specified MapperKey, if any exists
     * 
     * @param mapperKey
     *            the mapper key defining the mapping to resolve
     * @return the set of class mappings in the hierarchy of mappings used for
     *         the specified mapper key
     */
    Set<ClassMap<Object, Object>> lookupUsedClassMap(MapperKey mapperKey);
    
    /**
     * Gets the ClassMap instance (if any) which has been associated with the
     * types represented by the given MapperKey instance.
     * 
     * @param mapperKey
     *            the MapperKey which should be used to look up an associated
     *            ClassMap
     * @return the ClassMap which has been associated with the given MapperKey;
     *         <code>null</code> if no instance has been associated with this
     *         MapperKey instance.
     */
    <A, B> ClassMap<A, B> getClassMap(MapperKey mapperKey);
    
    /**
     * Get the set of classes which have been mapped for the specified type.
     * 
     * @param type
     *            the type for which to look up mapped types
     * @return the set of types which have been mapped for the specified type.
     */
    Set<Type<? extends Object>> lookupMappedClasses(Type<?> type);
    
    /**
     * @return a thread-safe MapperFacade instance as configured by this
     *         MapperFactory
     */
    MapperFacade getMapperFacade();
    
    
    /**
     * Returns a BoundMapperFacade for the specified source and destination
     * types; this mapper is bidirectional by default, and provides signatures for
     * mapping in place, or with instantiation, as needed.
     * 
     * @param aType
     *            the 'A' type for the returned DedicatedMapperFacade
     * @param bType
     *            the 'B' type for the returned DedicatedMapperFacade
     * 
     */
    <A, B> BoundMapperFacade<A, B> getMapperFacade(Type<A> aType, Type<B> bType);
    
    /**
     * Returns a BoundMapperFacade for the specified source and destination
     * types; this mapper is bidirectional by default, and provides signatures for
     * mapping in place, or with instantiation, as needed.
     * 
     * @param aType
     *            the 'A' type for the returned DedicatedMapperFacade
     * @param bType
     *            the 'B' type for the returned DedicatedMapperFacade
     * 
     */
    <A, B> BoundMapperFacade<A, B> getMapperFacade(Class<A> aType, Class<B> bType);
    
    /**
     * Returns a BoundMapperFacade for the specified source and destination
     * types; this mapper is bidirectional by default, and provides signatures for
     * mapping in place, or with instantiation, as needed.<br>
     * In the case that <code>false</code> is provided for the 'containsCycles' parameter,
     * an optimized mapper instance will be provided.
     * 
     * 
     * @param aType
     *            the 'A' type for the returned DedicatedMapperFacade
     * @param bType
     *            the 'B' type for the returned DedicatedMapperFacade
     * @param containsCycles
     *            true if the object graph for the source and destination types
     *            may contain cycles in it's graph; if 'false', an optimized
     *            DedicatedMapperFacade instance can be provided.
     * 
     * @return
     */
    <A, B> BoundMapperFacade<A, B> getMapperFacade(Type<A> aType, Type<B> bType, boolean containsCycles);
    
    /**
     * Returns a BoundMapperFacade for the specified source and destination
     * types; this mapper is bidirectional by default, and provides signatures for
     * mapping in place, or with instantiation, as needed.<br>
     * In the case that <code>false</code> is provided for the 'containsCycles' parameter,
     * an optimized mapper instance will be provided.
     * 
     * 
     * @param aType
     *            the 'A' type for the returned DedicatedMapperFacade
     * @param bType
     *            the 'B' type for the returned DedicatedMapperFacade
     * @param containsCycles
     *            true if the object graph for the source and destination types
     *            may contain cycles in it's graph; if 'false', an optimized
     *            DedicatedMapperFacade instance can be provided.
     * 
     * @return
     */
    <A, B> BoundMapperFacade<A, B> getMapperFacade(Class<A> aType, Class<B> bType, boolean containsCycles);
    
    
    /**
     * Get an instance of the ConverterFactory associated with this
     * MapperFactory; it may be used to register Converter instances to be used
     * during mapping.
     * 
     * @return the ConverterFactory instance associated with this MapperFactory;
     */
    ConverterFactory getConverterFactory();
    
    /**
     * Builds this MapperFactory.
     * 
     * @deprecated this method no longer needs to be called by clients; the
     *             MapperFactory instances should automatically be built upon
     *             the first call to {@link #getMapperFacade()}.
     */
    void build();
    
    /**
     * Constructs a new ClassMapBuilder instance initialized with the provided
     * types which can be used to configure/customize the mapping between the
     * two types.<br>
     * <br>
     * The returned ClassMapBuilder instance, after being fully configured,
     * should finally be registered with the factory using the
     * <code>registerClassMap</code> method.
     * 
     * 
     * @param aType
     *            the Type instance representing the "A" side of the mapping
     * @param bType
     *            the Type instance representing the "B" side of the mapping
     * @return
     */
    public <A, B> ClassMapBuilder<A, B> classMap(Type<A> aType, Type<B> bType);
    
    /**
     * Constructs a new ClassMapBuilder instance initialized with the provided
     * types which can be used to configure/customize the mapping between the
     * two types.<br>
     * <br>
     * The returned ClassMapBuilder instance, after being fully configured,
     * should finally be registered with the factory using the
     * <code>registerClassMap</code> method.
     * 
     * @param aType
     *            the Class instance representing the "A" side of the mapping
     * @param bType
     *            the Type instance representing the "B" side of the mapping
     * @return
     */
    public <A, B> ClassMapBuilder<A, B> classMap(Class<A> aType, Type<B> bType);
    
    /**
     * Constructs a new ClassMapBuilder instance initialized with the provided
     * types which can be used to configure/customize the mapping between the
     * two types.<br>
     * <br>
     * The returned ClassMapBuilder instance, after being fully configured,
     * should finally be registered with the factory using the
     * <code>registerClassMap</code> method.
     * 
     * @param aType
     *            the Type instance representing the "A" side of the mapping
     * @param bType
     *            the Class instance representing the "B" side of the mapping
     * @return
     */
    public <A, B> ClassMapBuilder<A, B> classMap(Type<A> aType, Class<B> bType);
    
    /**
     * Constructs a new ClassMapBuilder instance initialized with the provided
     * types which can be used to configure/customize the mapping between the
     * two types.<br>
     * <br>
     * The returned ClassMapBuilder instance, after being fully configured,
     * should finally be registered with the factory using the
     * <code>registerClassMap</code> method.
     * 
     * @param aType
     *            the Class instance representing the "A" side of the mapping
     * @param bType
     *            the Class instance representing the "B" side of the mapping
     * @return
     */
    public <A, B> ClassMapBuilder<A, B> classMap(Class<A> aType, Class<B> bType);
    
    /**
	 * Returns the {@link UnenhanceStrategy} associated with this MapperFactory,
	 * provided when this MapperFactory was constructed.
	 * 
	 * @return the {@link UnenhanceStrategy} associated with this MapperFactory.
	 */
    public UnenhanceStrategy getUnenhanceStrategy();
}
