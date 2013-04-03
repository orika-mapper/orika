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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingContextFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.Properties;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.converter.builtin.BuiltinConverters;
import ma.glasnost.orika.impl.generator.CodeGenerationStrategy;
import ma.glasnost.orika.impl.generator.CompilerStrategy;
import ma.glasnost.orika.impl.generator.CompilerStrategy.SourceCodeGenerationException;
import ma.glasnost.orika.impl.generator.MapperGenerator;
import ma.glasnost.orika.impl.generator.ObjectFactoryGenerator;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.inheritance.DefaultSuperTypeResolverStrategy;
import ma.glasnost.orika.inheritance.SuperTypeResolverStrategy;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.ClassMapBuilderFactory;
import ma.glasnost.orika.metadata.ClassMapBuilderForArrays;
import ma.glasnost.orika.metadata.ClassMapBuilderForLists;
import ma.glasnost.orika.metadata.ClassMapBuilderForMaps;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.property.PropertyResolverStrategy;
import ma.glasnost.orika.unenhance.BaseUnenhancer;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;
import ma.glasnost.orika.util.SortedCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The mapper factory is the heart of Orika, a small container where metadata
 * are stored, it's used by other components, to look up for generated mappers,
 * converters, object factories ... etc.
 * 
 * @author S.M. El Aatifi
 * 
 */
public class DefaultMapperFactory implements MapperFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMapperFactory.class);
    
    private final MapperFacade mapperFacade;
    private final MapperGenerator mapperGenerator;
    private final ObjectFactoryGenerator objectFactoryGenerator;
    
    private final Map<MapperKey, ClassMap<Object, Object>> classMapRegistry;
    private final SortedCollection<Mapper<Object, Object>> mappersRegistry;
    private final MappingContextFactory contextFactory;
    private final ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>> objectFactoryRegistry;
    private final Map<Type<?>, Set<Type<?>>> explicitAToBRegistry;
    private final Map<Type<?>, Set<Type<?>>> dynamicAToBRegistry;
    private final List<DefaultFieldMapper> defaultFieldMappers;
    private final UnenhanceStrategy unenhanceStrategy;
    private final ConverterFactory converterFactory;
    private final CompilerStrategy compilerStrategy;
    private final PropertyResolverStrategy propertyResolverStrategy;
    private final Map<java.lang.reflect.Type, Type<?>> concreteTypeRegistry;
    private final ClassMapBuilderFactory classMapBuilderFactory;
    private final ClassMapBuilderForMaps.Factory classMapBuilderForMapsFactory;
    private final ClassMapBuilderForLists.Factory classMapBuilderForListsFactory;
    private final ClassMapBuilderForArrays.Factory classMapBuilderForArraysFactory;
    private final Map<MapperKey, Set<ClassMap<Object, Object>>> usedMapperMetadataRegistry;
    
    private final boolean useAutoMapping;
    private final boolean useBuiltinConverters;
    private volatile boolean isBuilt = false;
    private volatile boolean isBuilding = false;
    
    
    /**
     * Constructs a new instance of DefaultMapperFactory
     * 
     * @param builder
     */
    protected DefaultMapperFactory(MapperFactoryBuilder<?, ?> builder) {
        
        this.converterFactory = builder.converterFactory;
        this.compilerStrategy = builder.compilerStrategy;
        this.classMapRegistry = new ConcurrentHashMap<MapperKey, ClassMap<Object, Object>>();
        this.mappersRegistry = new SortedCollection<Mapper<Object, Object>>(Comparators.MAPPER);
        this.explicitAToBRegistry = new ConcurrentHashMap<Type<?>, Set<Type<?>>>();
        this.dynamicAToBRegistry = new ConcurrentHashMap<Type<?>, Set<Type<?>>>();
        this.usedMapperMetadataRegistry = new ConcurrentHashMap<MapperKey, Set<ClassMap<Object, Object>>>();
        this.objectFactoryRegistry = new ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>>();
        this.defaultFieldMappers = new CopyOnWriteArrayList<DefaultFieldMapper>();
        this.unenhanceStrategy = buildUnenhanceStrategy(builder.unenhanceStrategy, builder.superTypeStrategy);
        this.contextFactory = new MappingContext.Factory();
        this.mapperFacade = buildMapperFacade(contextFactory, unenhanceStrategy);
        this.concreteTypeRegistry = new ConcurrentHashMap<java.lang.reflect.Type, Type<?>>();
        
        if (builder.classMaps != null) {
            for (final ClassMap<?, ?> classMap : builder.classMaps) {
                registerClassMap(classMap);
            }
        }
        
        this.propertyResolverStrategy = builder.propertyResolverStrategy;
        this.classMapBuilderFactory = builder.classMapBuilderFactory;
        this.classMapBuilderFactory.setPropertyResolver(this.propertyResolverStrategy);
        this.classMapBuilderFactory.setMapperFactory(this);
        this.classMapBuilderForMapsFactory = new ClassMapBuilderForMaps.Factory();
        this.classMapBuilderForMapsFactory.setPropertyResolver(this.propertyResolverStrategy);
        this.classMapBuilderForMapsFactory.setMapperFactory(this);
        this.classMapBuilderForListsFactory = new ClassMapBuilderForLists.Factory();
        this.classMapBuilderForListsFactory.setPropertyResolver(this.propertyResolverStrategy);
        this.classMapBuilderForListsFactory.setMapperFactory(this);
        this.classMapBuilderForArraysFactory = new ClassMapBuilderForArrays.Factory();
        this.classMapBuilderForArraysFactory.setPropertyResolver(this.propertyResolverStrategy);
        this.classMapBuilderForArraysFactory.setMapperFactory(this);
        
        this.mapperGenerator = new MapperGenerator(this, builder.compilerStrategy);
        this.objectFactoryGenerator = new ObjectFactoryGenerator(this, builder.constructorResolverStrategy, builder.compilerStrategy);
        this.useAutoMapping = builder.useAutoMapping;
        this.useBuiltinConverters = builder.useBuiltinConverters;
        
        builder.codeGenerationStrategy.setMapperFactory(this);
        
        Map<Object, Object> props = this.contextFactory.getGlobalProperties();
        props.put(Properties.SHOULD_MAP_NULLS, builder.mapNulls);
        props.put(Properties.CODE_GENERATION_STRATEGY, builder.codeGenerationStrategy);
        props.put(Properties.COMPILER_STRATEGY, builder.compilerStrategy);
        props.put(Properties.PROPERTY_RESOLVER_STRATEGY, builder.propertyResolverStrategy);
        props.put(Properties.MAPPER_FACTORY, this);
        
        /*
         * Register default concrete types for common collection types; these
         * can be overridden as needed by user code.
         */
        this.registerConcreteType(Collection.class, ArrayList.class);
        this.registerConcreteType(List.class, ArrayList.class);
        this.registerConcreteType(Set.class, LinkedHashSet.class);
        this.registerConcreteType(Map.class, LinkedHashMap.class);
        this.registerConcreteType(Map.Entry.class, MapEntry.class);
    }

    /**
     * MapperFactoryBuilder provides an extensible Builder definition usable for
     * providing your own Builder class for subclasses of DefaultMapperFactory.<br>
     * <br>
     * 
     * See the defined {@link Builder} below for example of how to subclass.
     * 
     * @author matt.deboer@gmail.com
     * 
     * @param <F>
     * @param <B>
     */
    public static abstract class MapperFactoryBuilder<F extends DefaultMapperFactory, B extends MapperFactoryBuilder<F, B>> {
        
        /**
         * The UnenhanceStrategy configured for the MapperFactory
         */
        protected UnenhanceStrategy unenhanceStrategy;
        /**
         * The SuperTypeResolverStrategy configured for the MapperFactory
         */
        protected SuperTypeResolverStrategy superTypeStrategy;
        /**
         * The ConstructorResolverStrategy configured for the MapperFactory
         */
        protected ConstructorResolverStrategy constructorResolverStrategy;
        /**
         * The CompilerStrategy configured for the MapperFactory
         */
        protected CompilerStrategy compilerStrategy;
        /**
         * The class maps configured to initialize the MapperFactory
         */
        protected Set<ClassMap<?, ?>> classMaps;
        /**
         * The ConverterFactory configured for the MapperFactory
         */
        protected ConverterFactory converterFactory;
        /**
         * The PropertyResolverStrategy configured for the MapperFactory
         */
        protected PropertyResolverStrategy propertyResolverStrategy;
        /**
         * The ClassMapBuilderFactory configured for the MapperFactory
         */
        protected ClassMapBuilderFactory classMapBuilderFactory;
        /**
         * The CodeGenerationStrategy configured for the MapperFactory
         */
        protected CodeGenerationStrategy codeGenerationStrategy = new DefaultCodeGenerationStrategy();
        /**
         * The configured value of whether or not to use built-in converters for
         * the MapperFactory
         */
        protected boolean useBuiltinConverters = true;
        /**
         * The configured value of whether or not to use auto-mapping for the
         * MapperFactory
         */
        protected boolean useAutoMapping = true;
        /**
         * The configured value of whether or not to map null values; if false,
         * they will be ignored, and any existing value is unchanged in case of null.
         */
        protected boolean mapNulls = true;
        
        /**
         * Instantiates a new MapperFactoryBuilder
         */
        public MapperFactoryBuilder() {
            converterFactory = UtilityResolver.getDefaultConverterFactory();
            constructorResolverStrategy = UtilityResolver.getDefaultConstructorResolverStrategy();
            compilerStrategy = UtilityResolver.getDefaultCompilerStrategy();
            propertyResolverStrategy = UtilityResolver.getDefaultPropertyResolverStrategy();
            classMapBuilderFactory = UtilityResolver.getDefaultClassMapBuilderFactory();
        }
        
        /**
         * @return an appropriately type-cast reference to <code>this</code>
         *         MapperFactoryBuilder
         */
        protected abstract B self();
        
        /**
         * Set the class maps to be used in initializing this mapper factory
         * 
         * @param classMaps
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B classMaps(Set<ClassMap<?, ?>> classMaps) {
            this.classMaps = classMaps;
            return self();
        }
        
        /**
         * Configure the UnenhanceStrategy to use with the generated
         * MapperFactory
         * 
         * @param unenhanceStrategy
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B unenhanceStrategy(UnenhanceStrategy unenhanceStrategy) {
            this.unenhanceStrategy = unenhanceStrategy;
            return self();
        }
        
        /**
         * Configure the SuperTypeResolverStrategy to use with the generated
         * MapperFactory
         * 
         * @param superTypeStrategy
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B superTypeResolverStrategy(SuperTypeResolverStrategy superTypeStrategy) {
            this.superTypeStrategy = superTypeStrategy;
            return self();
        }
        
        /**
         * Configure the ConstructorResolverStrategy to use with the generated
         * MapperFactory
         * 
         * @param constructorResolverStrategy
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B constructorResolverStrategy(ConstructorResolverStrategy constructorResolverStrategy) {
            this.constructorResolverStrategy = constructorResolverStrategy;
            return self();
        }
        
        /**
         * Configure the ConverterFactory to use with the generated
         * MapperFactory
         * 
         * @param converterFactory
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B converterFactory(ConverterFactory converterFactory) {
            this.converterFactory = converterFactory;
            return self();
        }
        
        /**
         * Configure the CompilerStrategy to use with the generated
         * MapperFactory
         * 
         * @param compilerStrategy
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B compilerStrategy(CompilerStrategy compilerStrategy) {
            this.compilerStrategy = compilerStrategy;
            return self();
        }
        
        /**
         * Configure the PropertyResolverStrategy to use with the generated
         * MapperFactory
         * 
         * @param propertyResolverStrategy
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B propertyResolverStrategy(PropertyResolverStrategy propertyResolverStrategy) {
            this.propertyResolverStrategy = propertyResolverStrategy;
            return self();
        }
        
        /**
         * Configure the ClassMapBuilderFactory to use with the generated
         * MapperFactory
         * 
         * @param classMapBuilderFactory
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B classMapBuilderFactory(ClassMapBuilderFactory classMapBuilderFactory) {
            this.classMapBuilderFactory = classMapBuilderFactory;
            return self();
        }
        
        /**
         * Configure whether to use auto-mapping with the generated
         * MapperFactory
         * 
         * @param useAutoMapping
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B useAutoMapping(boolean useAutoMapping) {
            this.useAutoMapping = useAutoMapping;
            return self();
        }
        
        /**
         * Configure whether to use built-in converters with the generated
         * MapperFactory<br>
         * 
         * @param useBuiltinConverters
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B useBuiltinConverters(boolean useBuiltinConverters) {
            this.useBuiltinConverters = useBuiltinConverters;
            return self();
        }
        
        /**
         * Mis-spelled method signature
         * 
         * @param useBuiltinConverters
         * @return true if the built-in converters should be used
         * @deprecated use {@link #useBuiltinConverters(boolean)} instead
         */
        @Deprecated
        public B usedBuiltinConverters(boolean useBuiltinConverters) {
            this.useBuiltinConverters = useBuiltinConverters;
            return self();
        }
        
        /**
         * Configure whether to map nulls in generated mapper code
         * 
         * @param mapNulls
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B mapNulls(boolean mapNulls) {
            this.mapNulls = mapNulls;
            return self();
        }
        
        /**
         * Get a reference to the CodeGenerationStrategy associated with this MapperFactory,
         * which may be used to configure/customize the individual mapping Specifications
         * that are used to generate code for the various mapping scenarios.
         * 
         * @return the CodeGenerationStrategy to be associated with this MapperFactory
         */
        public CodeGenerationStrategy getCodeGenerationStrategy() {
            return codeGenerationStrategy;
        }
        
        /**
         * @return a new instance of the Factory for which this builder is
         *         defined. The construction should be performed via the
         *         single-argument constructor which takes in a builder; no
         *         initialization code should be performed here, as it will not
         *         be inherited by subclasses; instead, place such
         *         initialization (defaults, etc.) in the Builder's constructor.
         */
        public abstract F build();
        
    }
    
    /**
     * Use this builder to generate instances of DefaultMapperFactory with the
     * desired customizations.<br>
     * <br>
     * 
     * For example, an instance with no customizations could be generated with
     * the following code:
     * 
     * <pre>
     * MapperFactory factory = new DefaultMapperFactory.Builder().build();
     * </pre>
     * 
     * @author matt.deboer@gmail.com
     */
    public static class Builder extends MapperFactoryBuilder<DefaultMapperFactory, Builder> {
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * ma.glasnost.orika.impl.DefaultMapperFactory.MapperFactoryBuilder#
         * build()
         */
        @Override
        public DefaultMapperFactory build() {
            return new DefaultMapperFactory(this);
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * ma.glasnost.orika.impl.DefaultMapperFactory.MapperFactoryBuilder#
         * self()
         */
        @Override
        protected Builder self() {
            return this;
        }
        
    }
    
    /**
     * Generates the UnenhanceStrategy to be used for this MapperFactory,
     * applying the passed delegateStrategy if not null.<br>
     * This allows the MapperFactory a chance to fill in the unenhance strategy
     * with references to other parts of the factory (registered mappers,
     * converters, object factories) which may be important in the "unenhancing"
     * process.
     * 
     * @param unenhanceStrategy
     * @param overrideDefaultUnenhanceBehavior
     *            true if the passed UnenhanceStrategy should take full
     *            responsibility for un-enhancement; false if the default
     *            behavior should be applied as a fail-safe after consulting the
     *            passed strategy.
     * 
     * @return the resulting UnenhanceStrategy
     */
    protected UnenhanceStrategy buildUnenhanceStrategy(UnenhanceStrategy unenhanceStrategy, SuperTypeResolverStrategy superTypeStrategy) {
        
        BaseUnenhancer unenhancer = new BaseUnenhancer();
        
        if (unenhanceStrategy != null) {
            unenhancer.addUnenhanceStrategy(unenhanceStrategy);
        }
        
        if (superTypeStrategy != null) {
            unenhancer.addSuperTypeResolverStrategy(superTypeStrategy);
        } 
        
        /*
         * This strategy produces super-types whenever the proposed class type
         * is not accessible to the compilerStrategy and/or the current thread
         * context class-loader; it is added last as a fail-safe in case a
         * suggested type cannot be used. It is automatically included, as
         * there's no case when skipping it would be desired....
         */
        final SuperTypeResolverStrategy inaccessibleTypeStrategy = new DefaultSuperTypeResolverStrategy() {
            
            /**
             * Tests whether the specified type is accessible to both the
             * current thread's class-loader, and also to the compilerStrategy.
             * 
             * @param type
             * @return true if the type is accessible
             */
            public boolean isTypeAccessible(Type<?> type) {
                
                try {
                    compilerStrategy.assureTypeIsAccessible(type.getRawType());
                    return true;
                } catch (SourceCodeGenerationException e) {
                    return false;
                }
            }
            
            @Override
            public boolean isAcceptable(Type<?> type) {
                return isTypeAccessible(type) && !java.lang.reflect.Proxy.class.equals(type.getRawType());
            }
            
        };
        
        unenhancer.addSuperTypeResolverStrategy(inaccessibleTypeStrategy);
        
        return unenhancer;
        
    }
    
    /**
	 * Builds the MapperFacade for this factory. Subclasses can override this
	 * method to build a custom MapperFacade. Please note that this method is
	 * called from the constructor and as such properties of the factory may not
	 * yet be initialized.
	 * 
	 * @param contextFactory
	 * @param unenhanceStrategy
	 * @return the MapperFacade to use
	 */
	protected MapperFacade buildMapperFacade(MappingContextFactory contextFactory, UnenhanceStrategy unenhanceStrategy) {
		return new MapperFacadeImpl(this, contextFactory, unenhanceStrategy);
	}
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.MapperFactory#lookupMapper(ma.glasnost.orika.metadata
     * .MapperKey)
     */
    @SuppressWarnings("unchecked")
    public <A, B> Mapper<A, B> lookupMapper(MapperKey mapperKey) {
        MappingContext context = contextFactory.getContext();
        try {
            return (Mapper<A, B>) lookupMapper(mapperKey, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    @SuppressWarnings("unchecked")
    public Mapper<Object, Object> lookupMapper(MapperKey mapperKey, MappingContext context) {
        
        Mapper<?, ?> mapper = getRegisteredMapper(mapperKey.getAType(), mapperKey.getBType(), false);
        if (mapper == null && useAutoMapping) {
            synchronized (this) {
                try {
                    /*
                     * We shouldn't create a mapper for an immutable type;
                     * although it will succeed in generating an empty mapper,
                     * it won't actually result in a valid mapping, so it's
                     * better to throw an exception to indicate more clearly
                     * that something went wrong. However, there is a
                     * possibility that a custom ObjectFactory was registered
                     * for the immutable type, which would be valid.
                     */
                    if (ClassUtil.isImmutable(mapperKey.getBType()) && !objectFactoryRegistry.containsKey(mapperKey.getBType())) {
                        throw new MappingException("No converter registered for conversion from " + mapperKey.getAType() + " to "
                                + mapperKey.getBType() + ", nor any ObjectFactory which can generate " + mapperKey.getBType() + " from "
                                + mapperKey.getAType());
                    }
                    
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("No mapper registered for " + mapperKey + ": attempting to generate");
                    }
                    final ClassMap<?, ?> classMap = classMap(mapperKey.getAType(), mapperKey.getBType()).byDefault().toClassMap();
                    buildObjectFactories(classMap, context);
                    mapper = buildMapper(classMap, true, context);
                    initializeUsedMappers(classMap);
                } catch (MappingException e) {
                    e.setSourceType(mapperKey.getAType());
                    e.setDestinationType(mapperKey.getBType());
                    throw e;
                }
            }
            
        }
        return (Mapper<Object, Object>) mapper;
    }
    
    public boolean existsRegisteredMapper(Type<?> sourceType, Type<?> destinationType, boolean includeAutoGeneratedMappers) {
        return getRegisteredMapper(sourceType, destinationType, includeAutoGeneratedMappers) != null;
    }
    
    /**
     * @param mapperKey
     * @return a registered Mapper which is able to map the specified types
     */
    @SuppressWarnings("unchecked")
    protected <A, B> Mapper<A, B> getRegisteredMapper(MapperKey mapperKey) {
        return getRegisteredMapper((Type<A>) mapperKey.getAType(), (Type<B>) mapperKey.getBType(), false);
    }
    
    /**
     * @param typeA
     * @param typeB
     * @param includeAutoGeneratedMappers
     *            whether auto-generated mappers should be included in the
     *            lookup
     * 
     * @return a registered Mapper which is able to map the specified types
     */
    @SuppressWarnings("unchecked")
    protected <A, B> Mapper<A, B> getRegisteredMapper(Type<A> typeA, Type<B> typeB, boolean includeAutoGeneratedMappers) {
        
        for (Mapper<?, ?> mapper : mappersRegistry) {
            
            if ((mapper.getAType().equals(typeA) && mapper.getBType().equals(typeB))
                    || (mapper.getAType().equals(typeB) && mapper.getBType().equals(typeA))) {
                
                return (Mapper<A, B>) mapper;
                
            } else if ((mapper.getAType().isAssignableFrom(typeA) && mapper.getBType().isAssignableFrom(typeB))
                    || (mapper.getAType().isAssignableFrom(typeB) && mapper.getBType().isAssignableFrom(typeA))) {
                
                if (includeAutoGeneratedMappers || !(mapper instanceof GeneratedMapperBase)) {
                    return (Mapper<A, B>) mapper;
                } else if (!((GeneratedMapperBase) mapper).isFromAutoMapping()) {
                    return (Mapper<A, B>) mapper;
                }
            }
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.MapperFactory#getMapperFacade()
     * 
     * Since getMapperFacade() triggers the build() process, it is important
     * that none of the methods called during the build() invoke
     * getMapperFacade() again.
     */
    public MapperFacade getMapperFacade() {
        if (!isBuilt) {
            synchronized (mapperFacade) {
                if (!isBuilt) {
                    build();
                }
            }
        }
        return mapperFacade;
    }
    
    public <D> void registerObjectFactory(ObjectFactory<D> objectFactory, Type<D> destinationType) {
        objectFactoryRegistry.put(destinationType, objectFactory);
    }
    
    @Deprecated
    public void registerMappingHint(ma.glasnost.orika.MappingHint... hints) {
        
        DefaultFieldMapper[] mappers = new DefaultFieldMapper[hints.length];
        for (int i = 0, len = hints.length; i < len; ++i) {
            mappers[i] = new ma.glasnost.orika.MappingHint.DefaultFieldMappingConverter(hints[i]);
        }
        registerDefaultFieldMapper(mappers);
    }
    
    public void registerDefaultFieldMapper(DefaultFieldMapper... mappers) {
        this.defaultFieldMappers.addAll(Arrays.asList(mappers));
    }
    
    public void registerConcreteType(Type<?> abstractType, Type<?> concreteType) {
        this.concreteTypeRegistry.put(abstractType, concreteType);
    }
    
    public void registerConcreteType(Class<?> abstractType, Class<?> concreteType) {
        this.concreteTypeRegistry.put(abstractType, TypeFactory.valueOf(concreteType));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.MapperFactory#lookupObjectFactory(ma.glasnost.orika
     * .metadata.Type)
     */
    public <T> ObjectFactory<T> lookupObjectFactory(Type<T> targetType) {
        MappingContext context = contextFactory.getContext();
        try {
            return lookupObjectFactory(targetType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    /**
     * 
     * @param type
     * @param context
     * @return an ObjectFactory instance which is able to instantiate the specified type
     */
    @SuppressWarnings("unchecked")
    public <T> ObjectFactory<T> lookupObjectFactory(final Type<T> type, final MappingContext context) {
        if (type == null) {
            return null;
        }
        Type<T> targetType = type;
        ObjectFactory<T> result = (ObjectFactory<T>) objectFactoryRegistry.get(targetType);
        if (result == null) {
            // Check if we can use default constructor...
            synchronized (this) {
                if (!ClassUtil.isConcrete(targetType)) {
                    targetType = (Type<T>) resolveConcreteType(targetType, targetType);
                }
                
                Constructor<?>[] constructors = targetType.getRawType().getConstructors();
                if (useAutoMapping || !isBuilt) {
                    if (constructors.length == 1 && constructors[0].getParameterTypes().length == 0) {
                        /*
                         * Use the default constructor in the case where it is
                         * the only option
                         */
                        result = new DefaultConstructorObjectFactory<T>(targetType.getRawType());
                    } else {
                        try {
                            result = (ObjectFactory<T>) objectFactoryGenerator.build(targetType, context);
                        } catch (MappingException e) {
                            for (Constructor<?> c : constructors) {
                                if (c.getParameterTypes().length == 0) {
                                    result = new DefaultConstructorObjectFactory<T>(targetType.getRawType());
                                    break;
                                }
                            }
                            if (result == null) {
                                throw e;
                            }
                        }
                    }
                    ObjectFactory<T> existing = (ObjectFactory<T>) objectFactoryRegistry.putIfAbsent(targetType, result);
                    if (existing != null) {
                        result = existing;
                    }
                    
                } else {
                    for (Constructor<?> constructor : constructors) {
                        if (constructor.getParameterTypes().length == 0) {
                            result = new DefaultConstructorObjectFactory<T>(targetType.getRawType());
                            break;
                        }
                    }
                }
                
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> Type<? extends D> lookupConcreteDestinationType(Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        
        /*
         * Check for a pre-resolved type
         */
        Type<? extends D> concreteType = context == null ? null : context.getConcreteClass(sourceType, destinationType);
        
        if (concreteType != null) {
            return concreteType;
        } 
        
        /*
         * Look for a match in the explicitly registered types
         */
        Set<Type<?>> destinationSet = explicitAToBRegistry.get(sourceType);
        if (destinationSet != null && !destinationSet.isEmpty()) {
            for (final Type<?> type : destinationSet) {
                if (destinationType.isAssignableFrom(type) && ClassUtil.isConcrete(type)) {
                    if (type.equals(destinationType) || existsRegisteredMapper(sourceType, type, false)
                            || !ClassUtil.isConcrete(destinationType)) {
                        return (Type<? extends D>) type;
                    }
                }
            }
        }
        
        /*
         * Return the original destinationType if it's concrete
         */
        if (ClassUtil.isConcrete(destinationType)) {
            return destinationType;
        }
        
        /*
         * Look for a match in the dynamically registered types
         */
        destinationSet = dynamicAToBRegistry.get(sourceType);
        if (destinationSet != null && !destinationSet.isEmpty()) {
            for (final Type<?> type : destinationSet) {
                if (destinationType.isAssignableFrom(type) && ClassUtil.isConcrete(type)) {
                    if (type.equals(destinationType) || existsRegisteredMapper(sourceType, type, false)
                            || !ClassUtil.isConcrete(destinationType)) {
                        return (Type<? extends D>) type;
                    }
                }
            }
        } else {
            /*
             * Try the registered mappers for a possible type match
             */
            Mapper<S, D> registeredMapper = getRegisteredMapper(sourceType, destinationType, true);
            if (registeredMapper != null) {
                concreteType = (Type<? extends D>) (registeredMapper.getAType().isAssignableFrom(sourceType) ? registeredMapper.getBType()
                        : registeredMapper.getAType());
                if (!ClassUtil.isConcrete(concreteType)) {
                    concreteType = (Type<? extends D>) resolveConcreteType(concreteType, destinationType);
                } else {
                    return null;
                }
            } else {
                concreteType = (Type<? extends D>) resolveConcreteType(destinationType, destinationType);
            }
        } 
        
        if (concreteType == null) {
            concreteType = (Type<? extends D>) resolveConcreteType(destinationType, destinationType);
        }
        
        return concreteType;
    }
    
    /**
     * @param type
     * @param originalType
     * @return a concrete type (if any) which has been registered for the
     *         specified abstract type
     */
    protected Type<?> resolveConcreteType(Type<?> type, Type<?> originalType) {
        
        Type<?> concreteType = (Type<?>) this.concreteTypeRegistry.get(type);
        if (concreteType == null) {
            concreteType = (Type<?>) this.concreteTypeRegistry.get(type.getRawType());
            if (concreteType != null) {
                concreteType = TypeFactory.resolveValueOf(concreteType.getRawType(), type);
            }
        }
        
        if (concreteType != null && !concreteType.isAssignableFrom(originalType)) {
            if (ClassUtil.isConcrete(originalType)) {
                concreteType = originalType;
            } else {
                concreteType = (Type<?>) this.concreteTypeRegistry.get(originalType);
                if (concreteType == null) {
                    concreteType = (Type<?>) this.concreteTypeRegistry.get(originalType.getRawType());
                    if (concreteType != null) {
                        concreteType = TypeFactory.resolveValueOf(concreteType, originalType);
                    }
                }
            }
        }
        return concreteType;
    }
    
    @SuppressWarnings("unchecked")
    public synchronized <A, B>  void registerClassMap(ClassMap<A, B> classMap) {
        classMapRegistry.put(new MapperKey(classMap.getAType(), classMap.getBType()), (ClassMap<Object, Object>) classMap);
        if (isBuilding || isBuilt) {
            MappingContext context = contextFactory.getContext();
            try {
                buildMapper(classMap, /** isAutoGenerated == **/isBuilding, context);
                
                buildObjectFactories(classMap, context);
                initializeUsedMappers(classMap);
            } finally {
                contextFactory.release(context);
            }
        }
    }
    
    public <A, B> void registerClassMap(ClassMapBuilder<A, B> builder) {
        registerClassMap(builder.toClassMap());
    }
    
    public synchronized void build() {
        
        if (!isBuilding && !isBuilt) {
            isBuilding = true;
            
            MappingContext context = contextFactory.getContext();
            try {
                converterFactory.setMapperFacade(mapperFacade);
                
                if (useBuiltinConverters) {
                    BuiltinConverters.register(converterFactory);
                }
                
                buildClassMapRegistry();
                
                for (final ClassMap<?, ?> classMap : classMapRegistry.values()) {
                    buildMapper(classMap, false, context);
                }
                
                for (final ClassMap<?, ?> classMap : classMapRegistry.values()) {
                    buildObjectFactories(classMap, context);
                    initializeUsedMappers(classMap);
                }
            } finally {
                contextFactory.release(context);
            }

            isBuilt = true;
            isBuilding = false;
        }
        
    }
    
    public Set<ClassMap<Object, Object>> lookupUsedClassMap(MapperKey mapperKey) {
        Set<ClassMap<Object, Object>> usedClassMapSet = usedMapperMetadataRegistry.get(mapperKey);
        if (usedClassMapSet == null) {
            usedClassMapSet = Collections.emptySet();
        }
        return usedClassMapSet;
    }
    
    private void buildClassMapRegistry() {
        // prepare a map for classmap (stored as set)
        Map<MapperKey, ClassMap<Object, Object>> classMapsDictionary = new HashMap<MapperKey, ClassMap<Object, Object>>();
        
        Set<ClassMap<Object, Object>> classMaps = new HashSet<ClassMap<Object, Object>>(classMapRegistry.values());
        
        for (final ClassMap<Object, Object> classMap : classMaps) {
            classMapsDictionary.put(new MapperKey(classMap.getAType(), classMap.getBType()), classMap);
        }
        
        for (final ClassMap<?, ?> classMap : classMaps) {
            MapperKey key = new MapperKey(classMap.getAType(), classMap.getBType());
            
            Set<ClassMap<Object, Object>> usedClassMapSet = new HashSet<ClassMap<Object, Object>>();
            
            for (final MapperKey parentMapperKey : classMap.getUsedMappers()) {
                ClassMap<Object, Object> usedClassMap = classMapsDictionary.get(parentMapperKey);
                if (usedClassMap == null) {
                    throw new MappingException("Cannot find class mapping using mapper : " + classMap.getMapperClassName());
                }
                usedClassMapSet.add(usedClassMap);
            }
            usedMapperMetadataRegistry.put(key, usedClassMapSet);
        }
        
    }
    
    @SuppressWarnings({ "unchecked" })
    private <S, D> void buildObjectFactories(ClassMap<S, D> classMap, MappingContext context) {
        Type<?> aType = classMap.getAType();
        Type<?> bType = classMap.getBType();
        
        if (classMap.getConstructorA() != null && lookupObjectFactory(aType) == null) {
            GeneratedObjectFactory objectFactory = objectFactoryGenerator.build(aType, context);
            registerObjectFactory(objectFactory, (Type<Object>) aType);
        }
        
        if (classMap.getConstructorB() != null && lookupObjectFactory(bType) == null) {
            GeneratedObjectFactory objectFactory = objectFactoryGenerator.build(bType, context);
            registerObjectFactory(objectFactory, (Type<Object>) bType);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void initializeUsedMappers(ClassMap<?, ?> classMap) {
        
        Mapper<Object, Object> mapper = lookupMapper(new MapperKey(classMap.getAType(), classMap.getBType()));
        
        List<Mapper<Object, Object>> parentMappers = new ArrayList<Mapper<Object, Object>>();
        
        if (!classMap.getUsedMappers().isEmpty()) {
            for (MapperKey parentMapperKey : classMap.getUsedMappers()) {
                collectUsedMappers(classMap, parentMappers, parentMapperKey);
            }
        } else {
            /*
             * Attempt to auto-determine used mappers for this classmap;
             * however, we should only add the most-specific of the available
             * mappers to avoid calling the same mapper multiple times during a
             * single map request;
             */
            SortedCollection<MapperKey> usedMappers = new SortedCollection<MapperKey>(Comparators.MAPPER_KEY);
            for (MapperKey key : this.classMapRegistry.keySet()) {
                if (!key.getAType().equals(classMap.getAType()) || !key.getBType().equals(classMap.getBType())) {
                    if (key.getAType().isAssignableFrom(classMap.getAType()) && key.getBType().isAssignableFrom(classMap.getBType())) {
                        usedMappers.add(key);
                    }
                }
            }
            if (!usedMappers.isEmpty()) {
                MapperKey parentKey = usedMappers.first();
                collectUsedMappers(classMap, parentMappers, parentKey);
            }
        }
        
        /*
         * Flip any used mappers which are specified in the wrong direction
         */
        Mapper<Object, Object>[] usedMappers = parentMappers.toArray(new Mapper[parentMappers.size()]);
        for (int i = 0; i < usedMappers.length; ++i) {
            Mapper<Object, Object> usedMapper = usedMappers[i];
            if (usedMapper.getAType().isAssignableFrom(classMap.getBType()) && usedMapper.getBType().isAssignableFrom(classMap.getAType())) {
                usedMappers[i] = ReversedMapper.reverse(usedMapper);
            }
        }
        
        mapper.setUsedMappers(usedMappers);
    }
    
    private void collectUsedMappers(ClassMap<?, ?> classMap, List<Mapper<Object, Object>> parentMappers, MapperKey parentMapperKey) {
        Mapper<Object, Object> parentMapper = lookupMapper(parentMapperKey);
        if (parentMapper == null) {
            throw new MappingException("Cannot find used mappers for : " + classMap.getMapperClassName());
        }
        parentMappers.add(parentMapper);
        
        Set<ClassMap<Object, Object>> usedClassMapSet = usedMapperMetadataRegistry.get(parentMapperKey);
        if (usedClassMapSet != null) {
            for (ClassMap<Object, Object> cm : usedClassMapSet) {
                collectUsedMappers(cm, parentMappers, new MapperKey(cm.getAType(), cm.getBType()));
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private GeneratedMapperBase buildMapper(ClassMap<?, ?> classMap, boolean isAutoGenerated, MappingContext context) {
        
        register(classMap.getAType(), classMap.getBType(), isAutoGenerated);
        register(classMap.getBType(), classMap.getAType(), isAutoGenerated);
        
        final MapperKey mapperKey = new MapperKey(classMap.getAType(), classMap.getBType());
        final GeneratedMapperBase mapper = this.mapperGenerator.build(classMap, context);
        mapper.setMapperFacade(mapperFacade);
        mapper.setFromAutoMapping(isAutoGenerated);
        if (classMap.getCustomizedMapper() != null) {
            final Mapper<Object, Object> customizedMapper = (Mapper<Object, Object>) classMap.getCustomizedMapper();
            mapper.setCustomMapper(customizedMapper);
        }
        mappersRegistry.add(mapper);
        classMapRegistry.put(mapperKey, (ClassMap<Object, Object>) classMap);
        
        return mapper;
    }
    
    /**
     * Registers that a mapping exists from the specified source type to the
     * specified destination type
     * 
     * @param sourceType
     * @param destinationType
     */
    protected <S, D> void register(Type<S> sourceType, Type<D> destinationType, boolean isAutoGenerated) {
        
        Map<Type<?>, Set<Type<?>>> registry = isAutoGenerated ? dynamicAToBRegistry : explicitAToBRegistry;
        
        Set<Type<?>> destinationSet = registry.get(sourceType);
        if (destinationSet == null) {
            destinationSet = new TreeSet<Type<?>>();
            registry.put(sourceType, destinationSet);
        }
        destinationSet.add(destinationType);
    }
    
    @SuppressWarnings("unchecked")
    public <A, B> ClassMap<A, B> getClassMap(MapperKey mapperKey) {
        return (ClassMap<A, B>) classMapRegistry.get(mapperKey);
    }
    
    public Set<Type<? extends Object>> lookupMappedClasses(Type<?> type) {
        /*
         * Combine the dynamically registered classes with the explicitly registered
         */
        TreeSet<Type<?>> mappedClasses = new TreeSet<Type<?>>();
        Set<Type<? extends Object>> types = explicitAToBRegistry.get(type);
        if (types != null) {
            mappedClasses.addAll(types);
        }
        types = dynamicAToBRegistry.get(type);
        if (types != null) {
            mappedClasses.addAll(types);
        }
        
        return mappedClasses;
    }
    
    public ConverterFactory getConverterFactory() {
        return converterFactory;
    }
    
    public <T> void registerObjectFactory(ObjectFactory<T> objectFactory, Class<T> targetClass) {
        registerObjectFactory(objectFactory, TypeFactory.<T> valueOf(targetClass));
    }
    
    /**
     * @return the (initialized) ClassMapBuilderFactory configured for this
     *         mapper factory
     */
    protected ClassMapBuilderFactory getClassMapBuilderFactory() {
        if (!classMapBuilderFactory.isInitialized()) {
            classMapBuilderFactory.setDefaultFieldMappers(defaultFieldMappers.toArray(new DefaultFieldMapper[defaultFieldMappers.size()]));
        }
        return classMapBuilderFactory;
    }
    
    public <A, B> ClassMapBuilder<A, B> classMap(Type<A> aType, Type<B> bType) {
        if ((aType.isMap() && !bType.isMap()) || (bType.isMap() && !aType.isMap())) {
            return this.classMapBuilderForMapsFactory.map(aType, bType);
        } else if ((aType.isList() && !bType.isList()) || (bType.isList() && !aType.isList())) {
            return this.classMapBuilderForListsFactory.map(aType, bType);
        } else if ((aType.isArray() && !bType.isArray()) || (bType.isArray() && !aType.isArray())) {
            return this.classMapBuilderForArraysFactory.map(aType, bType);
        } else {
            return getClassMapBuilderFactory().map(aType, bType);
        }
    }
    
    public <A, B> ClassMapBuilder<A, B> classMap(Class<A> aType, Type<B> bType) {
        return classMap(TypeFactory.<A> valueOf(aType), bType);
    }
    
    public <A, B> ClassMapBuilder<A, B> classMap(Type<A> aType, Class<B> bType) {
        return classMap(aType, TypeFactory.<B> valueOf(bType));
    }
    
    public <A, B> ClassMapBuilder<A, B> classMap(Class<A> aType, Class<B> bType) {
        return classMap(TypeFactory.<A> valueOf(aType), TypeFactory.<B> valueOf(bType));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.MapperFactory#registerMapper(ma.glasnost.orika.Mapper)
     */
    @SuppressWarnings("unchecked")
    public synchronized <A, B> void registerMapper(Mapper<A, B> mapper) {
        this.mappersRegistry.add((Mapper<Object, Object>) mapper);
        mapper.setMapperFacade(this.mapperFacade);
        register(mapper.getAType(), mapper.getBType(), false);
        register(mapper.getBType(), mapper.getAType(), false);
    }
    
    public <S, D> BoundMapperFacade<S, D> getMapperFacade(Type<S> sourceType, Type<D> destinationType) {
        return getMapperFacade(sourceType, destinationType, true);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.MapperFacade#dedicatedMapperFor(ma.glasnost.orika.metadata
     * .Type, ma.glasnost.orika.metadata.Type, boolean)
     */
    public <S, D> BoundMapperFacade<S, D> getMapperFacade(Type<S> sourceType, Type<D> destinationType, boolean containsCycles) {
        if (!isBuilt && !isBuilding) {
            build();
        }
        
        if (!containsCycles) {
            return new NonCyclicBoundMapperFacade<S, D>(this, contextFactory, sourceType, destinationType);
        } else {
            return new DefaultBoundMapperFacade<S, D>(this, contextFactory, sourceType, destinationType);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.MapperFacade#dedicatedMapperFor(java.lang.Class,
     * java.lang.Class)
     */
    public <A, B> BoundMapperFacade<A, B> getMapperFacade(Class<A> aType, Class<B> bType) {
        return getMapperFacade(aType, bType, true);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.MapperFacade#dedicatedMapperFor(java.lang.Class,
     * java.lang.Class, boolean)
     */
    public <A, B> BoundMapperFacade<A, B> getMapperFacade(Class<A> aType, Class<B> bType, boolean containsCycles) {
        return getMapperFacade(TypeFactory.valueOf(aType), TypeFactory.valueOf(bType), containsCycles);
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.MapperFactory#getCodeGenerationStrategy()
     */
    
    
}
