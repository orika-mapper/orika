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

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.Filter;
import ma.glasnost.orika.MappedTypePair;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingContextFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.Properties;
import ma.glasnost.orika.StateReporter.Reportable;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.converter.builtin.BuiltinConverters;
import ma.glasnost.orika.impl.generator.CodeGenerationStrategy;
import ma.glasnost.orika.impl.generator.CompilerStrategy;
import ma.glasnost.orika.impl.generator.CompilerStrategy.SourceCodeGenerationException;
import ma.glasnost.orika.impl.generator.MapperGenerator;
import ma.glasnost.orika.impl.generator.ObjectFactoryGenerator;
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
import ma.glasnost.orika.util.Ordering;
import ma.glasnost.orika.util.SortedCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Boolean.valueOf;
import static java.lang.System.getProperty;
import static ma.glasnost.orika.OrikaSystemProperties.CAPTURE_FIELD_CONTEXT;
import static ma.glasnost.orika.OrikaSystemProperties.DUMP_STATE_ON_EXCEPTION;
import static ma.glasnost.orika.OrikaSystemProperties.FAVOR_EXTENSION;
import static ma.glasnost.orika.OrikaSystemProperties.GET_DESTINATION_ON_MAPPING;
import static ma.glasnost.orika.OrikaSystemProperties.MAP_NULLS;
import static ma.glasnost.orika.OrikaSystemProperties.USE_AUTO_MAPPING;
import static ma.glasnost.orika.OrikaSystemProperties.USE_BUILTIN_CONVERTERS;
import static ma.glasnost.orika.StateReporter.DIVIDER;
import static ma.glasnost.orika.StateReporter.humanReadableSizeInMemory;

/**
 * The mapper factory is the heart of Orika, a small container where metadata
 * are stored, it's used by other components, to look up for generated mappers,
 * converters, object factories ... etc.
 * 
 * @author S.M. El Aatifi
 * 
 */
public class DefaultMapperFactory implements MapperFactory, Reportable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMapperFactory.class);

    protected final MapperFacade mapperFacade;
    protected final MapperGenerator mapperGenerator;
    protected final ObjectFactoryGenerator objectFactoryGenerator;

    protected final ConcurrentHashMap<MapperKey, ClassMap<Object, Object>> classMapRegistry;
    protected final SortedCollection<Mapper<Object, Object>> mappersRegistry;
    protected final SortedCollection<Filter<Object, Object>> filtersRegistry;
    protected final MappingContextFactory contextFactory;
    protected final MappingContextFactory nonCyclicContextFactory;
    protected final ConcurrentHashMap<Type<?>, ConcurrentHashMap<Type<?>, ObjectFactory<?>>> objectFactoryRegistry;
    protected final ConcurrentHashMap<Type<?>, Set<Type<?>>> explicitAToBRegistry;
    protected final ConcurrentHashMap<Type<?>, Set<Type<?>>> dynamicAToBRegistry;
    protected final List<DefaultFieldMapper> defaultFieldMappers;
    protected final UnenhanceStrategy unenhanceStrategy;
    protected final UnenhanceStrategy userUnenahanceStrategy;

    /**
     * 转换器工厂, 转换器的注册及获取.
     */
    protected final ConverterFactory converterFactory;

    protected final CompilerStrategy compilerStrategy;
    protected final PropertyResolverStrategy propertyResolverStrategy;
    protected final Map<java.lang.reflect.Type, Type<?>> concreteTypeRegistry;
    /** @see MapperFactoryBuilder#alwaysCreateMultipleMapperWrapper */
    protected boolean alwaysCreateMultipleMapperWrapper;
    protected final ClassMapBuilderFactory classMapBuilderFactory;
    protected ClassMapBuilderFactory chainClassMapBuilderFactory;
    protected final Map<MapperKey, Set<ClassMap<Object, Object>>> usedMapperMetadataRegistry;

    protected final boolean useAutoMapping;
    protected final boolean useBuiltinConverters;
    protected final boolean favorExtension;
    protected volatile boolean isBuilt = false;
    protected volatile boolean isBuilding = false;

    protected final ExceptionUtility exceptionUtil;
    
    /**
     * Constructs a new instance of DefaultMapperFactory
     * 
     * @param builder
     */
    protected DefaultMapperFactory(MapperFactoryBuilder<?, ?> builder) {
        // 对转换器工厂赋值, 使用了个代理类
        this.converterFactory = new ConverterFactoryFacade(builder.converterFactory);

        // todo 编译策略, 有点不太理解为什么需要编译
        this.compilerStrategy = builder.compilerStrategy;

        // 映射类注册容器
        this.classMapRegistry = new ConcurrentHashMap<>();

        //
        this.mappersRegistry = new SortedCollection<>(Ordering.MAPPER);
        this.filtersRegistry = new SortedCollection<>(Ordering.FILTER);
        this.explicitAToBRegistry = new ConcurrentHashMap<>();
        this.dynamicAToBRegistry = new ConcurrentHashMap<>();
        this.usedMapperMetadataRegistry = new ConcurrentHashMap<>();
        this.objectFactoryRegistry = new ConcurrentHashMap<>();
        this.defaultFieldMappers = new CopyOnWriteArrayList<>();
        this.userUnenahanceStrategy = builder.unenhanceStrategy;
        this.unenhanceStrategy = buildUnenhanceStrategy(builder.unenhanceStrategy, builder.superTypeStrategy);
        this.contextFactory = builder.mappingContextFactory;
        this.nonCyclicContextFactory = new NonCyclicMappingContext.Factory(this.contextFactory.getGlobalProperties());
        this.exceptionUtil = new ExceptionUtility(this, builder.dumpStateOnException);
        this.mapperFacade = buildMapperFacade(contextFactory, unenhanceStrategy);
        this.concreteTypeRegistry = new ConcurrentHashMap<>();
        this.alwaysCreateMultipleMapperWrapper = builder.alwaysCreateMultipleMapperWrapper;
        
        if (builder.classMaps != null) {
            for (final ClassMap<?, ?> classMap : builder.classMaps) {
                registerClassMap(classMap);
            }
        }
        
        this.propertyResolverStrategy = builder.propertyResolverStrategy;
        this.classMapBuilderFactory = builder.classMapBuilderFactory;
        this.classMapBuilderFactory.setPropertyResolver(this.propertyResolverStrategy);
        this.classMapBuilderFactory.setMapperFactory(this);
        addClassMapBuilderFactory(new ClassMapBuilderForArrays.Factory());
        addClassMapBuilderFactory(new ClassMapBuilderForLists.Factory());
        addClassMapBuilderFactory(new ClassMapBuilderForMaps.Factory());
        
        this.mapperGenerator = new MapperGenerator(this, builder.compilerStrategy);
        this.objectFactoryGenerator = new ObjectFactoryGenerator(this, builder.constructorResolverStrategy, builder.compilerStrategy);
        this.useAutoMapping = builder.useAutoMapping;
        this.favorExtension = builder.favorExtension;
        this.useBuiltinConverters = builder.useBuiltinConverters;
        
        builder.codeGenerationStrategy.setMapperFactory(this);
        
        Map<Object, Object> props = this.contextFactory.getGlobalProperties();
        props.put(Properties.SHOULD_MAP_NULLS, builder.mapNulls);
        props.put(Properties.CODE_GENERATION_STRATEGY, builder.codeGenerationStrategy);
        props.put(Properties.COMPILER_STRATEGY, builder.compilerStrategy);
        props.put(Properties.PROPERTY_RESOLVER_STRATEGY, builder.propertyResolverStrategy);
        props.put(Properties.UNENHANCE_STRATEGY, unenhanceStrategy);
        props.put(Properties.MAPPER_FACTORY, this);
        props.put(Properties.FILTERS, this.filtersRegistry);
        props.put(Properties.CAPTURE_FIELD_CONTEXT, builder.captureFieldContext);
        props.put(Properties.SHOULD_GET_DESTINATION_ON_MAPPING, builder.getDestinationOnMapping);


        /*
         * Register default concrete types for common collection types; these
         * can be overridden as needed by user code.
         */
        for (Entry<Class, Class> concreteTypeMap : DefaultConcreteTypeMap.getAll()) {
            this.registerConcreteType(concreteTypeMap.getKey(), concreteTypeMap.getValue());
        }

    }
    
    /**
     * Add factory to the factories chain
     * 
     * @param factory
     */
    protected void addClassMapBuilderFactory(ClassMapBuilderFactory factory) {
        factory.setChainClassMapBuilderFactory(chainClassMapBuilderFactory);
        chainClassMapBuilderFactory = factory;
        factory.setPropertyResolver(this.propertyResolverStrategy);
        factory.setMapperFactory(this);
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
         * The MappingContextFactory configured for the MapperFactory
         */
        protected MappingContextFactory mappingContextFactory;
        /**
         * The CodeGenerationStrategy configured for the MapperFactory
         */
        protected CodeGenerationStrategy codeGenerationStrategy;
        /**
         * The configured value of whether or not to use built-in converters for
         * the MapperFactory
         */
        protected Boolean useBuiltinConverters;
        /**
         * The configured value of whether or not to use auto-mapping for the
         * MapperFactory
         */
        protected Boolean useAutoMapping;
        /**
         * The configured value of whether or not to map null values; if false,
         * they will be ignored, and any existing value is unchanged in case of
         * null.
         */
        protected Boolean mapNulls;
        /**
         * The configured value of whether the full state of the core Orika
         * mapping objects should be printed on exception
         */
        protected Boolean dumpStateOnException;
        /**
         * The configured default value for the 'favorExtension' option on
         * registered class-maps (when one has not been explicitly specified).
         */
        protected Boolean favorExtension;
        /**
         * The configured value for whether full field context should be captured
         * upon mapping of every field.
         */
        protected Boolean captureFieldContext;
        /**
         * Internal Property to always use {@link MultipleMapperWrapper} even if there are no custom ObjectFactory and only one Mapper
         * found.
         * <p>
         * This makes no sense for production: Because of the (small) performance penalty the {@link MultipleMapperWrapper} should only be
         * use when necessary.
         * <p>
         * But for Testing the MultipleMapperWrapper should be use in one TestSuite for all TestCase to test as much scenarios as possible.
         */
        protected boolean alwaysCreateMultipleMapperWrapper;
        /**
         * The configured value of whether or not to retrieve destination property value on mapping;
         * if false, it will be assumed that the value of the destination property is always null;
         * if true, the real value of the destination property will be used.
         * <p>
         * Sometimes it may be useful to set this value to false. For example, in case when you don't need to know
         * current destination value and you want to map the value from source to distination regardless of the current
         * destination value. Also, it may be useful if your logic to retrieve a destination value is too expensive or
         * produces exceptions if the destination value isn't specified yet.
         * <p>
         */
        protected Boolean getDestinationOnMapping;

        /**
         * Instantiates a new MapperFactoryBuilder
         */
        public MapperFactoryBuilder() {
            converterFactory = UtilityResolver.getDefaultConverterFactory();
            constructorResolverStrategy = UtilityResolver.getDefaultConstructorResolverStrategy();
            compilerStrategy = UtilityResolver.getDefaultCompilerStrategy();
            propertyResolverStrategy = UtilityResolver.getDefaultPropertyResolverStrategy();
            classMapBuilderFactory = UtilityResolver.getDefaultClassMapBuilderFactory();
            mappingContextFactory = UtilityResolver.getDefaultMappingContextFactory();
            
            useBuiltinConverters = valueOf(getProperty(USE_BUILTIN_CONVERTERS, "true"));
            useAutoMapping = valueOf(getProperty(USE_AUTO_MAPPING, "true"));
            mapNulls = valueOf(getProperty(MAP_NULLS, "true"));
            dumpStateOnException = valueOf(getProperty(DUMP_STATE_ON_EXCEPTION, "false"));
            favorExtension = valueOf(getProperty(FAVOR_EXTENSION, "false"));
            captureFieldContext = valueOf(getProperty(CAPTURE_FIELD_CONTEXT, "false"));
            alwaysCreateMultipleMapperWrapper = Boolean.parseBoolean(
                    getProperty("ma.glasnost.orika.alwaysCreateMultipleMapperWrapper", "false"));
            codeGenerationStrategy = new DefaultCodeGenerationStrategy();
            getDestinationOnMapping = valueOf(getProperty(GET_DESTINATION_ON_MAPPING, "true"));
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
         * Configure the MappingContextFactory to use with the generated
         * MapperFactory
         * 
         * @param mappingContextFactory
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B mappingContextFactory(MappingContextFactory mappingContextFactory) {
            this.mappingContextFactory = mappingContextFactory;
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
         * Configure whether to dump the current state of the mapping
         * infrastructure objects upon occurrence of an exception while mapping.
         * 
         * @param dumpStateOnException
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B dumpStateOnException(boolean dumpStateOnException) {
            this.dumpStateOnException = dumpStateOnException;
            return self();
        }
        
        /**
         * Configure whether to favorExtension by default in registered
         * class-maps (when a value has not been explicitly specified on the
         * class-map builder).
         * 
         * @param favorExtension
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B favorExtension(boolean favorExtension) {
            this.favorExtension = favorExtension;
            return self();
        }
        
        /**
         * Specifies whether full field context should be captured by generated mappers.<p>
         * If <code>true</code>, the result is that the following calls will return a meaningful value, relative to
         * the currently mapped source and destination fields:
         * <ul>
         *  <li>{@link MappingContext#getFullyQualifiedSourcePath}
         *  <li>{@link MappingContext#getFullyQualifiedDestinationPath}
         *  <li>{@link MappingContext#getSourceExpressionPaths}
         *  <li>{@link MappingContext#getDestinationExpressionPaths}
         *  <li>{@link MappingContext#getSourceTypePaths}
         *  <li>{@link MappingContext#getDestinationTypePaths}
         *  </ul>
         * <p><p>
         * If <code>false</code>, these methods will return <code>null</code>.<p>
         * Default value is <code>false</code>
         * 
         * @param captureFieldContext
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B captureFieldContext(boolean captureFieldContext) {
            this.captureFieldContext = captureFieldContext;
            return self();
        }
        
        /**
         * Get a reference to the CodeGenerationStrategy associated with this
         * MapperFactory, which may be used to configure/customize the
         * individual mapping Specifications that are used to generate code for
         * the various mapping scenarios.
         * 
         * @return the CodeGenerationStrategy to be associated with this
         *         MapperFactory
         */
        public CodeGenerationStrategy getCodeGenerationStrategy() {
            return codeGenerationStrategy;
        }
        
        /**
         * Configure  a CodeGenerationStrategy with this
         * MapperFactory, which may be used to configure/customize the
         * individual mapping Specifications that are used to generate code for
         * the various mapping scenarios.
         *
         * @param codeGenerationStrategy
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B codeGenerationStrategy(CodeGenerationStrategy codeGenerationStrategy) {
            this.codeGenerationStrategy  = codeGenerationStrategy ;
            return self();
        }

        /**
         * Configure whether to retrieve distination property value during mapping in generated mapper code
         *
         * @param getDestinationOnMapping
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B getDestinationOnMapping(boolean getDestinationOnMapping) {
            this.getDestinationOnMapping = getDestinationOnMapping;
            return self();
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
     * @param superTypeStrategy
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
        return new MapperFacadeImpl(this, contextFactory, unenhanceStrategy, exceptionUtil);
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
    
    /**
     * Searches for a Mapper which is capable of mapping the classes identified
     * by the provided MapperKey instance
     * 
     * @param mapperKey
     * @param context
     * @return the Mapper instance which is able to support the set of classes
     *         identified by the passed MapperKey
     */
    @SuppressWarnings("unchecked")
    public Mapper<Object, Object> lookupMapper(MapperKey mapperKey, MappingContext context) {
        
        Mapper<?, ?> mapper = getRegisteredMapper(mapperKey.getAType(), mapperKey.getBType(), false);
        if (internalMapperMustBeGenerated(mapper, mapperKey)) {
            mapper = null;
        }
        if (mapper == null && useAutoMapping) {
            synchronized (this) {
                mapper = getRegisteredMapper(mapperKey.getAType(), mapperKey.getBType(), false);
                boolean internalMapperMustBeGenerated = internalMapperMustBeGenerated(mapper, mapperKey);
                if (internalMapperMustBeGenerated) {
                    mapper = null;
                }
                if (mapper == null) {
                    try {
                        /*
                         * We shouldn't create a mapper for an immutable type;
                         * although it will succeed in generating an empty
                         * mapper, it won't actually result in a valid mapping,
                         * so it's better to throw an exception to indicate more
                         * clearly that something went wrong. However, there is
                         * a possibility that a custom ObjectFactory was
                         * registered for the immutable type, which would be
                         * valid.
                         */
                        if (mapperKey.getBType().isImmutable() && !objectFactoryRegistry.containsKey(mapperKey.getBType())) {
                            throw new MappingException("No converter registered for conversion from " + mapperKey.getAType() + " to "
                                    + mapperKey.getBType() + ", nor any ObjectFactory which can generate " + mapperKey.getBType()
                                    + " from " + mapperKey.getAType());
                        }
                        
                        LOGGER.debug("No mapper registered for {}: attempting to generate", mapperKey);
                        
                        ClassMapBuilder<?, ?> builder = classMap(mapperKey.getAType(), mapperKey.getBType()).byDefault();
                        for (MapperKey key : discoverUsedMappers(builder)) {
                            builder.use(key.getAType(), key.getBType());
                        }
                        final ClassMap<?, ?> classMap = builder.toClassMap();
                        
                        buildObjectFactories(classMap, context);
                        mapper = buildMapper(classMap, true, context);
                        initializeUsedMappers(mapper, classMap, context);
                        if (internalMapperMustBeGenerated || alwaysCreateMultipleMapperWrapper) {
                            // regenerate MultipleMapperWrapper.
                            mapper = getRegisteredMapper(mapperKey.getAType(), mapperKey.getBType(), false);
                        }
                    } catch (MappingException e) {
                        e.setSourceType(mapperKey.getAType());
                        e.setDestinationType(mapperKey.getBType());
                        throw exceptionUtil.decorate(e);
                    }
                }
            }
            
        }
        return (Mapper<Object, Object>) mapper;
    }

    private boolean internalMapperMustBeGenerated(Mapper<?, ?> mapper, MapperKey mapperKey) {
        boolean internalMapperMustBeGenerated = false;
        if (mapperKey.getBType().isConcrete() && mapper instanceof MultipleMapperWrapper) {
            MultipleMapperWrapper mapperWrapper = (MultipleMapperWrapper) mapper;
            Mapper<Object, Object> internalMapper = mapperWrapper.findMapperFor(mapperKey);
            if (internalMapper == null) {
                internalMapperMustBeGenerated = true;
            }
        }
        return internalMapperMustBeGenerated;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.MapperFactory#existsRegisteredMapper(ma.glasnost.orika
     * .metadata.Type, ma.glasnost.orika.metadata.Type, boolean)
     */
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
     *            whether auto-generated mappers should be included in the lookup
     * 
     * @return a registered Mapper which is able to map the specified types
     */
    @SuppressWarnings("unchecked")
    private <A, B> Mapper<A, B> getRegisteredMapper(Type<A> typeA, Type<B> typeB, boolean includeAutoGeneratedMappers) {
        List<Mapper<A, B>> foundMappers = new ArrayList<>();
        
        boolean objFactoryBExists = customObjectFactoryForDestinationExists(typeA, typeB);
        boolean objFactoryAExists = customObjectFactoryForDestinationExists(typeB, typeA);
        
        for (Mapper<?, ?> mapper : mappersRegistry) {
            if ((mapper.getAType().equals(typeA) && mapper.getBType().equals(typeB))
                    || (mapper.getAType().equals(typeB) && mapper.getBType().equals(typeA))) {
                foundMappers.add((Mapper<A, B>) mapper);
            } else if ((mapper.getAType().isAssignableFrom(typeA) && mapper.getBType().isAssignableFrom(typeB))
                    || (mapper.getBType().isAssignableFrom(typeA) && mapper.getAType().isAssignableFrom(typeB))
                    || (mapper.getAType().isAssignableFrom(typeA) && typeB.isAssignableFrom(mapper.getBType()) && objFactoryBExists)
                    || (mapper.getBType().isAssignableFrom(typeA) && typeB.isAssignableFrom(mapper.getAType()) && objFactoryAExists)) {
                if (!favorsExtension(mapper) || !canBeExtended(typeA, typeB, mapper)) {
                    if (includeAutoGeneratedMappers || !(mapper instanceof GeneratedMapperBase)) {
                        foundMappers.add((Mapper<A, B>) mapper);
                    } else if (!((GeneratedMapperBase) mapper).isFromAutoMapping()) {
                        foundMappers.add((Mapper<A, B>) mapper);
                    }
                }
            }
        }
        if ((objFactoryBExists || objFactoryAExists) && foundMappers.size() > 1) {
            if (LOGGER.isDebugEnabled()) {
                StringBuilder msg = new StringBuilder();
                msg.append("Found Multiple Mappers:\n");
                for (Mapper<A, B> mapper : foundMappers) {
                    msg.append("\t");
                    msg.append(mapper.getAType());
                    msg.append(" <-> ");
                    msg.append(mapper.getBType());
                    msg.append("\n");
                }
                LOGGER.debug(msg.toString());
            }
            return (Mapper<A, B>) new MultipleMapperWrapper((Type<Object>) typeA, (Type<Object>) typeB, (List) foundMappers);
        } else if (foundMappers.size() > 0) {
            if (alwaysCreateMultipleMapperWrapper) {
                return (Mapper<A, B>) new MultipleMapperWrapper((Type<Object>) typeA, (Type<Object>) typeB, (List) foundMappers);
            }
            return foundMappers.get(0);
        }
        
        return null;
    }
    
    private boolean favorsExtension(Mapper<?, ?> mapper) {
        return mapper.favorsExtension() == null ? favorExtension : mapper.favorsExtension();
    }
    
    /**
     * Returns true if the types passed are subclasses of the types of th mapper
     * 
     * @param typeA
     * @param typeB
     * @param mapper
     * @return
     */
    private boolean canBeExtended(Type<?> typeA, Type<?> typeB, Mapper<?, ?> mapper) {
        boolean extensible;
        try {
            compilerStrategy.assureTypeIsAccessible(typeA.getRawType());
            compilerStrategy.assureTypeIsAccessible(typeB.getRawType());
            extensible = true;
        } catch (SourceCodeGenerationException e) {
            extensible = false;
        }
        return extensible;
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
        registerObjectFactory(objectFactory, destinationType, TypeFactory.TYPE_OF_OBJECT);
    }
    
    public <D, S> void registerObjectFactory(ObjectFactory<D> objectFactory, Type<D> destinationType, Type<S> sourceType) {
        ConcurrentHashMap<Type<?>, ObjectFactory<?>> localCache = objectFactoryRegistry.get(destinationType);
        if (localCache == null) {
            localCache = new ConcurrentHashMap<>();
            ConcurrentHashMap<Type<?>, ObjectFactory<?>> existing = objectFactoryRegistry.putIfAbsent(
                    destinationType, localCache);
            if (existing != null) {
                localCache = existing;
            }
        }
        localCache.put(sourceType, objectFactory);
        if (isBuilding || isBuilt) {
            mapperFacade.factoryModified(this);
        }
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
        return lookupObjectFactory(targetType, TypeFactory.TYPE_OF_OBJECT);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.MapperFactory#lookupObjectFactory(ma.glasnost.orika
     * .metadata.Type)
     */
    public <T, S> ObjectFactory<T> lookupObjectFactory(Type<T> targetType, Type<S> sourceType) {
        MappingContext context = contextFactory.getContext();
        try {
            return lookupObjectFactory(targetType, sourceType, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    /**
     * @param destinationType
     * @param sourceType
     * @param context
     * @return an object factory capable of generating the provided destination
     *         type, if any exists.
     */
    protected <T, S> ObjectFactory<T> lookupExistingObjectFactory(final Type<T> destinationType, final Type<S> sourceType,
            final MappingContext context) {
        
        if (destinationType == null || sourceType == null) {
            return null;
        }
        
        ConcurrentHashMap<Type<?>, ObjectFactory<?>> objFactoryCacheForDestType = objectFactoryRegistry
                .get(destinationType);
        if (objFactoryCacheForDestType != null) {
            ObjectFactory<T> result = findObjectFactory(objFactoryCacheForDestType, sourceType, false);
            if (result != null) {
                return result;
            }
        }
        
        Set<Type<?>> objFactoryDestTypes = getKeys(objectFactoryRegistry);
        for (Type<?> objFactoryDestType : objFactoryDestTypes) {
            if (destinationType.isAssignableFrom(objFactoryDestType)) {
                ObjectFactory<T> result = findObjectFactory(objectFactoryRegistry.get(objFactoryDestType), sourceType, true);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * {@link ConcurrentHashMap#keySet()} returns with java 8 a new Class KeySetView which doesn't exist in older JDKs which can produce
     * compile issues.
     * 
     * @param concurrentHashMap
     *            {@link ConcurrentHashMap}
     * @return a {@link Set} of K
     */
    private <K, V> Set<K> getKeys(ConcurrentHashMap<K, V> concurrentHashMap) {
        Map<K, V> map = concurrentHashMap;
        return map.keySet();
    }
    
    @SuppressWarnings("unchecked")
    private <T, S> ObjectFactory<T> findObjectFactory(
            ConcurrentHashMap<Type<?>, ObjectFactory<?>> objFactoryCachePerSrcType,
            final Type<S> sourceType, boolean onlyCustomObjectFactories) {
        Type<?> checkSourceType = sourceType;
        ObjectFactory<T> result;
        do {
            result = (ObjectFactory<T>) objFactoryCachePerSrcType.get(checkSourceType);
            if (result != null && onlyCustomObjectFactories && !isCustomObjectFactory(result)) {
                result = null;
            }
            checkSourceType = checkSourceType.getSuperType();
        } while (result == null && !TypeFactory.TYPE_OF_OBJECT.equals(checkSourceType));
        if (result == null) {
            result = (ObjectFactory<T>) objFactoryCachePerSrcType.get(TypeFactory.TYPE_OF_OBJECT);
        }
        return result;
    }
    
    /**
     * 
     * @param destinationType
     * @param sourceType
     * @param context
     * @return an ObjectFactory instance which is able to instantiate the
     *         specified type
     */
    @SuppressWarnings("unchecked")
    public <T, S> ObjectFactory<T> lookupObjectFactory(final Type<T> destinationType, final Type<S> sourceType, final MappingContext context) {
        
        if (destinationType == null || sourceType == null) {
            return null;
        }
        
        Type<T> targetType = destinationType;
        ObjectFactory<T> result = lookupExistingObjectFactory(targetType, sourceType, context);
        
        if (result == null) {
            // Check if we can use default constructor...
            synchronized (this) {
                if (!targetType.isConcrete()) {
                    targetType = (Type<T>) resolveConcreteType(targetType, targetType);
                }
                if (targetType == null) {
                    throw new IllegalStateException(String.format(
                            "Cannot create ObjectFactory for \n\t destinationType = %s\n\t sourceType = %s",
                            destinationType,
                            sourceType));
                }
                
                Constructor<?>[] constructors = targetType.getRawType().getDeclaredConstructors();
                if (useAutoMapping || !isBuilt) {
                    if (constructors.length == 1 && constructors[0].getParameterTypes().length == 0) {
                        /*
                         * Use the default constructor in the case where it is
                         * the only option
                         */
                        result = new DefaultConstructorObjectFactory<>(targetType.getRawType());
                    } else {
                        try {
                            result = (ObjectFactory<T>) objectFactoryGenerator.build(targetType, sourceType, context);
                        } catch (MappingException e) {
                            for (Constructor<?> c : constructors) {
                                if (c.getParameterTypes().length == 0) {
                                    result = new DefaultConstructorObjectFactory<>(targetType.getRawType());
                                    break;
                                }
                            }
                            if (result == null) {
                                throw exceptionUtil.decorate(e);
                            }
                        }
                    }
                    
                    ConcurrentHashMap<Type<?>, ObjectFactory<?>> localCache = objectFactoryRegistry.get(targetType);
                    if (localCache == null) {
                        localCache = new ConcurrentHashMap<>();
                        ConcurrentHashMap<Type<?>, ObjectFactory<?>> existing = objectFactoryRegistry.putIfAbsent(
                                targetType, localCache);
                        if (existing != null) {
                            localCache = existing;
                        }
                    }
                    
                    ObjectFactory<T> existing = (ObjectFactory<T>) localCache.putIfAbsent(sourceType, result);
                    if (existing != null) {
                        result = existing;
                    }
                    
                } else {
                    for (Constructor<?> constructor : constructors) {
                        if (constructor.getParameterTypes().length == 0) {
                            result = new DefaultConstructorObjectFactory<>(targetType.getRawType());
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
         * Look for some (custom) ObjectFactories.
         * If there is an Object Factory, then the ObjectFactory will generate dynamically the concrete type.
         * So return the destinationType even if it is not "concrete", because we cannot exactly say what the concrete type will be.
         */
        if (customObjectFactoryForDestinationExists(sourceType, destinationType)) {
            return destinationType;
        }
        
        /*
         * Look for a match in the explicitly registered types
         */
        Set<Type<?>> destinationSet = explicitAToBRegistry.get(sourceType);
        if (destinationSet != null && !destinationSet.isEmpty()) {
            for (final Type<?> type : destinationSet) {
                if (destinationType.isAssignableFrom(type) && type.isConcrete()) {
                    if (type.equals(destinationType) || existsRegisteredMapper(sourceType, type, false)
                            || !destinationType.isConcrete()) {
                        return (Type<? extends D>) type;
                    }
                }
            }
        }
        
        /*
         * Return the original destinationType if it's concrete
         */
        if (destinationType.isConcrete()) {
            return destinationType;
        }
        
        /*
         * Look for a match in the dynamically registered types
         */
        destinationSet = dynamicAToBRegistry.get(sourceType);
        if (destinationSet != null && !destinationSet.isEmpty()) {
            for (final Type<?> type : destinationSet) {
                if (destinationType.isAssignableFrom(type) && type.isConcrete()) {
                    if (type.equals(destinationType) || existsRegisteredMapper(sourceType, type, false)
                            || !destinationType.isConcrete()) {
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
                if (!concreteType.isConcrete()) {
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

    private <S, D> boolean customObjectFactoryForDestinationExists(Type<S> sourceType, Type<D> destinationType) {
        Set<Type<?>> objFactoryDestTypes = getKeys(objectFactoryRegistry);
        for (Type<?> objFactoryDestType : objFactoryDestTypes) {
            if (destinationType.isAssignableFrom(objFactoryDestType)
                    && objectFactoryRegistry.get(objFactoryDestType).containsKey(sourceType)) {
                ObjectFactory<?> objectFactory = objectFactoryRegistry.get(objFactoryDestType).get(sourceType);
                if (isCustomObjectFactory(objectFactory)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isCustomObjectFactory(ObjectFactory<?> objectFactory) {
        if (objectFactory instanceof GeneratedObjectFactory) {
            return false;
        }
        if (objectFactory.getClass().equals(DefaultConstructorObjectFactory.class)) {
            return false;
        }
        return true;
    }
    
    /**
     * @param type
     * @param originalType
     * @return a concrete type (if any) which has been registered for the
     *         specified abstract type
     */
    protected Type<?> resolveConcreteType(Type<?> type, Type<?> originalType) {
        
        Type<?> concreteType = concreteTypeOf(type);
        
        if (concreteType != null && !concreteType.isAssignableFrom(originalType)) {
            if (originalType.isConcrete()) {
                concreteType = originalType;
            } else {
                concreteType = concreteTypeOf(originalType);
            }
        }

        return concreteType;
    }

    private Type<?> concreteTypeOf(Type<?> type) {
        Type<?> concreteType = this.concreteTypeRegistry.get(type);
        if (concreteType == null) {
            concreteType = this.concreteTypeRegistry.get(type.getRawType());
            if (concreteType != null) {
                concreteType = TypeFactory.resolveValueOf(concreteType.getRawType(), type);
            }
        }
        return concreteType;
    }

    @SuppressWarnings("unchecked")
    public synchronized <A, B> void registerClassMap(ClassMap<A, B> classMap) {
        classMapRegistry.put(new MapperKey(classMap.getAType(), classMap.getBType()), (ClassMap<Object, Object>) classMap);
        if (isBuilding || isBuilt) {
            MappingContext context = contextFactory.getContext();
            try {
                if (classMap.getUsedMappers().isEmpty()) {
                    classMap = classMap.copyWithUsedMappers(discoverUsedMappers(classMap));
                }
                GeneratedMapperBase generatedMapper = buildMapper(classMap, /** isAutoGenerated == **/
                isBuilding, context);
                
                buildObjectFactories(classMap, context);
                initializeUsedMappers(generatedMapper, classMap, context);
                mapperFacade.factoryModified(this);
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
                if (useBuiltinConverters) {
                    BuiltinConverters.register(converterFactory);
                }
                converterFactory.setMapperFacade(mapperFacade);
                
                for (Map.Entry<MapperKey, ClassMap<Object, Object>> classMapEntry : classMapRegistry.entrySet()) {
                    ClassMap<Object, Object> classMap = classMapEntry.getValue();
                    if (classMap.getUsedMappers().isEmpty()) {
                        classMapEntry.setValue(classMap.copyWithUsedMappers(discoverUsedMappers(classMap)));
                    }
                }

                buildClassMapRegistry();

                Map<ClassMap<?, ?>, GeneratedMapperBase> generatedMappers = new HashMap<>();
                for (ClassMap<?, ?> classMap : classMapRegistry.values()) {
                    generatedMappers.put(classMap, buildMapper(classMap, false, context));
                }
                
                Set<Entry<ClassMap<?, ?>, GeneratedMapperBase>> generatedMapperEntries = generatedMappers.entrySet();
                for (Entry<ClassMap<?, ?>, GeneratedMapperBase> generatedMapperEntry : generatedMapperEntries) {
                    buildObjectFactories(generatedMapperEntry.getKey(), context);
                    initializeUsedMappers(generatedMapperEntry.getValue(), generatedMapperEntry.getKey(), context);
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
    
    /**
     * Builds up metadata regarding which classmaps are used by others.
     */
    private void buildClassMapRegistry() {
        
        // prepare a map for classmap (stored as set)
        Map<MapperKey, ClassMap<Object, Object>> classMapsDictionary = new HashMap<>();
        
        for (final ClassMap<Object, Object> classMap : classMapRegistry.values()) {
            classMapsDictionary.put(new MapperKey(classMap.getAType(), classMap.getBType()), classMap);
        }
        
        for (final ClassMap<?, ?> classMap : classMapRegistry.values()) {
            MapperKey key = new MapperKey(classMap.getAType(), classMap.getBType());
            
            Set<ClassMap<Object, Object>> usedClassMapSet = new LinkedHashSet<>();
            
            for (final MapperKey parentMapperKey : classMap.getUsedMappers()) {
                ClassMap<Object, Object> usedClassMap = classMapsDictionary.get(parentMapperKey);
                if (usedClassMap == null) {
                    throw exceptionUtil.newMappingException("Cannot find class mapping using mapper : " + classMap.getMapperClassName());
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
        
        if (classMap.getConstructorA() != null && lookupExistingObjectFactory(aType, TypeFactory.TYPE_OF_OBJECT, context) == null) {
            GeneratedObjectFactory objectFactory = objectFactoryGenerator.build(aType, bType, context);
            registerObjectFactory(objectFactory, (Type<Object>) aType);
        }
        
        if (classMap.getConstructorB() != null && lookupExistingObjectFactory(bType, TypeFactory.TYPE_OF_OBJECT, context) == null) {
            GeneratedObjectFactory objectFactory = objectFactoryGenerator.build(bType, aType, context);
            registerObjectFactory(objectFactory, (Type<Object>) bType);
        }
    }
    
    private Set<MapperKey> discoverUsedMappers(MappedTypePair<?, ?> classMapBuilder) {
        Set<MapperKey> mappers = new LinkedHashSet<>();
        /*
         * Attempt to auto-determine used mappers for this classmap; however, we
         * should only add the most-specific of the available mappers to avoid
         * calling the same mapper multiple times during a single map request;
         */
        for (ClassMap<?, ?> map : classMapRegistry.values()) {
            if (map.getAType().isAssignableFrom(classMapBuilder.getAType()) && map.getBType().isAssignableFrom(classMapBuilder.getBType())) {
                if (!map.getAType().equals(classMapBuilder.getAType()) || !map.getBType().equals(classMapBuilder.getBType())) {
                    MapperKey key = new MapperKey(map.getAType(), map.getBType());
                    mappers.add(key);
                }
            } else if (map.getAType().isAssignableFrom(classMapBuilder.getBType())
                    && map.getBType().isAssignableFrom(classMapBuilder.getAType())) {
                if (!map.getAType().equals(classMapBuilder.getBType()) || !map.getBType().equals(classMapBuilder.getAType())) {
                    MapperKey key = new MapperKey(map.getBType(), map.getAType());
                    mappers.add(key);
                }
            }
        }
        return mappers;
    }
    
    private void initializeUsedMappers(Mapper<?, ?> mapper, ClassMap<?, ?> classMap, MappingContext context) {

        Set<Mapper<Object, Object>> parentMappers = new LinkedHashSet<>();

        if (!classMap.getUsedMappers().isEmpty()) {
            for (MapperKey parentMapperKey : classMap.getUsedMappers()) {
                collectUsedMappers(classMap, parentMappers, parentMapperKey, context);
            }
        }
        
        parentMappers.remove(mapper);
        
        for (Mapper<Object, Object> curParrentMapper : parentMappers) {
            if (!GeneratedMapperBase.isUsedMappersInitialized(curParrentMapper)) {
                initializeUsedMappers(curParrentMapper, getClassMap(new MapperKey(
                        curParrentMapper.getAType(),
                        curParrentMapper.getBType())), context);
            }
        }

        /*
         * De-duplicate used mappers within the hierarchy
         * TODO: need to find a consistent way to avoid creating
         * duplication while building the hierarchy, and remove this code
         */
        Mapper<Object, Object>[] usedMappers = parentMappers.toArray(new Mapper[parentMappers.size()]);
        parentMappers.clear();
        for (int i=0, len=usedMappers.length; i < len; ++i) {
            boolean exists = false;
            for (int j=0; j < len; ++j) {
                if( i != j && GeneratedMapperBase.isUsedMapperOf(usedMappers[i], usedMappers[j])) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                parentMappers.add(usedMappers[i]);
            }
        }
        if (parentMappers.size() < usedMappers.length) {
            usedMappers = parentMappers.toArray(new Mapper[parentMappers.size()]);
        }
        
        /*
         * Flip any used mappers which are specified in the wrong direction
         */
        for (int i = 0; i < usedMappers.length; ++i) {
            Mapper<Object, Object> usedMapper = usedMappers[i];
            if (usedMapper.getAType().isAssignableFrom(classMap.getBType()) && usedMapper.getBType().isAssignableFrom(classMap.getAType())) {
                usedMappers[i] = ReversedMapper.reverse(usedMapper);
            }
        }
        mapper.setUsedMappers(usedMappers);
    }
    
    private void collectUsedMappers(ClassMap<?, ?> classMap, Set<Mapper<Object, Object>> parentMappers, MapperKey parentMapperKey, MappingContext context) {
        Mapper<Object, Object> parentMapper = lookupMapper(parentMapperKey, context);
        if (parentMapper == null) {
            throw exceptionUtil.newMappingException("Cannot find used mappers for : " + classMap.getMapperClassName());
        }
        if (parentMapper instanceof MultipleMapperWrapper) {
            MultipleMapperWrapper multiMapperWrapper = (MultipleMapperWrapper) parentMapper;
            Collection<Mapper<Object, Object>> fromMultipleMappers = multiMapperWrapper.getMappersRegistry();
            for (Mapper<Object, Object> fromMultipleMapper : fromMultipleMappers) {
                if (fromMultipleMapper.getAType().isAssignableFrom(classMap.getAType())
                        && fromMultipleMapper.getBType().isAssignableFrom(classMap.getBType())) {
                    parentMappers.add(fromMultipleMapper);
                } else if (fromMultipleMapper.getAType().isAssignableFrom(classMap.getBType())
                        && fromMultipleMapper.getBType().isAssignableFrom(classMap.getAType())) {
                    parentMappers.add(fromMultipleMapper);
                }
            }
        } else {
            parentMappers.add(parentMapper);
        }

        Set<ClassMap<Object, Object>> usedClassMapSet = usedMapperMetadataRegistry.get(parentMapperKey);
        if (usedClassMapSet != null) {
            for (ClassMap<Object, Object> cm : usedClassMapSet) {
                collectUsedMappers(cm, parentMappers, new MapperKey(cm.getAType(), cm.getBType()), context);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private GeneratedMapperBase buildMapper(ClassMap<?, ?> classMap, boolean isAutoGenerated, MappingContext context) {
        
        register(classMap.getAType(), classMap.getBType(), isAutoGenerated);
        register(classMap.getBType(), classMap.getAType(), isAutoGenerated);
        
        final MapperKey mapperKey = new MapperKey(classMap.getAType(), classMap.getBType());
        final GeneratedMapperBase mapper = mapperGenerator.build(classMap, context);
        mapper.setMapperFacade(mapperFacade);
        mapper.setFromAutoMapping(isAutoGenerated);
        if (classMap.getCustomizedMapper() != null) {
            final Mapper<Object, Object> customizedMapper = (Mapper<Object, Object>) classMap.getCustomizedMapper();
            mapper.setCustomMapper(customizedMapper);
        }
        mappersRegistry.remove(mapper);
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
        
        ConcurrentHashMap<Type<?>, Set<Type<?>>> registry = isAutoGenerated ? dynamicAToBRegistry : explicitAToBRegistry;
        
        Set<Type<?>> destinationSet = registry.get(sourceType);
        if (destinationSet == null) {
            destinationSet = new TreeSet<>();
            Set<Type<?>> existing = registry.putIfAbsent(sourceType, destinationSet);
            if (existing != null) {
                destinationSet = existing;
            }
        }
        destinationSet.add(destinationType);
    }
    
    @SuppressWarnings("unchecked")
    public <A, B> ClassMap<A, B> getClassMap(MapperKey mapperKey) {
        return (ClassMap<A, B>) classMapRegistry.get(mapperKey);
    }
    
    public Set<Type<?>> lookupMappedClasses(Type<?> type) {
        /*
         * Combine the dynamically registered classes with the explicitly
         * registered
         */
        TreeSet<Type<?>> mappedClasses = new TreeSet<>();
        Set<Type<?>> types = explicitAToBRegistry.get(type);
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
        ClassMapBuilderFactory classMapBuilderFactory = chainClassMapBuilderFactory.chooseClassMapBuilderFactory(aType, bType);
        
        if (classMapBuilderFactory != null) {
            return classMapBuilderFactory.map(aType, bType);
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
        if (isBuilding || isBuilt) {
            mapperFacade.factoryModified(this);
        }
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
        getMapperFacade();
        MappingContextFactory ctxFactory = containsCycles ? contextFactory : nonCyclicContextFactory;
        return new DefaultBoundMapperFacade<>(this, ctxFactory, sourceType, destinationType);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.MapperFacade#dedicatedMapperFor(java.lang.Class,
     * java.lang.Class)
     */
    public <A, B> BoundMapperFacade<A, B> getMapperFacade(Class<A> aType, Class<B> bType) {
        return getMapperFacade(TypeFactory.valueOf(aType), TypeFactory.valueOf(bType));
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
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.MapperFactory#getCodeGenerationStrategy()
     */
    
    public UnenhanceStrategy getUserUnenhanceStrategy() {
        return userUnenahanceStrategy;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.MapperFactory#registerFilter(ma.glasnost.orika.Filter)
     */
    @SuppressWarnings("unchecked")
    public void registerFilter(Filter<?, ?> filter) {
        this.filtersRegistry.add((Filter<Object, Object>) filter);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.StateReporter.Reportable#reportCurrentState(java.lang
     * .StringBuilder)
     */
    public void reportCurrentState(StringBuilder out) {
        out.append(DIVIDER);
        out.append("\nRegistered object factories: ")
                .append(objectFactoryRegistry.size())
                .append(" (approximate size: ")
                .append(humanReadableSizeInMemory(objectFactoryRegistry))
                .append(")");
        for (Entry<Type<?>, ConcurrentHashMap<Type<?>, ObjectFactory<?>>> entry : objectFactoryRegistry.entrySet()) {
            out.append("\n  [").append(entry.getKey()).append("] : ").append(entry.getValue());
        }
        out.append(DIVIDER);
        out.append("\nRegistered mappers: ")
                .append(mappersRegistry.size())
                .append(" (approximate size: ")
                .append(humanReadableSizeInMemory(mappersRegistry))
                .append(")");
        int index = 0;
        for (Mapper<Object, Object> mapper : mappersRegistry) {
            out.append("\n  [").append(index++).append("] : ").append(mapper);
        }
        out.append(DIVIDER);
        out.append("\nRegistered concrete types: ")
                .append(concreteTypeRegistry.size())
                .append(" (approximate size: ")
                .append(humanReadableSizeInMemory(concreteTypeRegistry))
                .append(")");
        for (Entry<java.lang.reflect.Type, Type<?>> entry : concreteTypeRegistry.entrySet()) {
            out.append("\n  [").append(entry.getKey()).append("] : ").append(entry.getValue());
        }
    }
    
    /**
     * ConverterFactoryFacade is a nested intercepter class for ConverterFactory
     * that listens for registry of new converters and calls the appropriate
     * change event on MapperFacade if the factory has already started building.
     *
     * ConverterFactoryFacade是ConverterFactory的嵌套拦截器类，
     * 它侦听新转换器的注册并在工厂已经开始构建时调用MapperFacade上的适当更改事件。
     * 
     */
    private class ConverterFactoryFacade implements ConverterFactory {
        private final ConverterFactory delegate;
        
        public ConverterFactoryFacade(ConverterFactory delegate) {
            this.delegate = delegate;
        }
        
        public void setMapperFacade(MapperFacade mapperFacade) {
            delegate.setMapperFacade(mapperFacade);
        }
        
        public Converter<Object, Object> getConverter(Type<?> sourceType, Type<?> destinationType) {
            return delegate.getConverter(sourceType, destinationType);
        }
        
        public Converter<Object, Object> getConverter(String converterId) {
            return delegate.getConverter(converterId);
        }
        
        public <S, D> void registerConverter(Converter<S, D> converter) {
            delegate.registerConverter(converter);
            // todo
            if (isBuilding || isBuilt) {
                mapperFacade.factoryModified(DefaultMapperFactory.this);
            }
        }
        
        public <S, D> void registerConverter(String converterId, Converter<S, D> converter) {
            delegate.registerConverter(converterId, converter);
            // todo
            if (isBuilding || isBuilt) {
                mapperFacade.factoryModified(DefaultMapperFactory.this);
            }
        }
        
        public boolean hasConverter(String converterId) {
            return delegate.hasConverter(converterId);
        }
        
        public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
            return delegate.canConvert(sourceType, destinationType);
        }
    }
    
}
