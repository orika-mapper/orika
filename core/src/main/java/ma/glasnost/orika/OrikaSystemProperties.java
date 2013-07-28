package ma.glasnost.orika;

/**
 * A hook for the various system properties which may be used to configure
 * Orika's runtime behavior.
 * 
 * @author matt.deboer@gmail.com
 *
 */
public final class OrikaSystemProperties {

    private OrikaSystemProperties() {
	// prevent instantiation
    }
    
    /**
     * Specifies whether .java source files should be written for generated objects; 
     * valid choices are "true" or "false". <br>
     * Any non-null value will be evaluated to "false"; if this property is not specified,
     * default behavior is determined by the compiler strategy.<br><br>
     * Note that Orika makes no effort to delete such generated files.
     */
    public static final String WRITE_SOURCE_FILES = "ma.glasnost.orika.writeSourceFiles";
    
    /**
     * Specifies the output location where source files should be written (if writing source files is enabled).
     * This defaults to the value "classpath:/ma/glasnost/orika/generated/", which signals to write
     * the files to the location "/ma/glasnost/orika/generated/" relative to the root of the class-path.<br>
     * An absolute file location may also be specified, such as: "/Users/me/orikaGeneratedFiles/src/".
     */
    public static final String WRITE_SOURCE_FILES_TO_PATH = "ma.glasnost.orika.writeSourceFilesToPath";
    
    /**
     * Specifies whether class files should be written for generated objects; 
     * valid choices are "true" or "false". <br>
     * Any non-null value will be evaluated to "false"; if this property is not specified,
     * default behavior is determined by the compiler strategy.<br><br>
     * Note that Orika makes no effort to delete such generated files.
     */
    public static final String WRITE_CLASS_FILES = "ma.glasnost.orika.writeClassFiles";
    
    /**
     * Specifies the output location where class files should be written (if writing class files is enabled).
     * This defaults to the value "classpath:/ma/glasnost/orika/generated/", which signals to write
     * the files to the location "/ma/glasnost/orika/generated/" relative to the root of the class-path.<br>
     * An absolute file location may also be specified, such as: "/Users/me/orikaGeneratedFiles/bin/".
     */
    public static final String WRITE_CLASS_FILES_TO_PATH = "ma.glasnost.orika.writeClassFilesToPath";
    
    /**
     * Specifies the fully-qualified class name of the compiler strategy to use when creating generated objects;
     * default value is determined by the MapperFactory implementation.
     */
    public static final String COMPILER_STRATEGY = "ma.glasnost.orika.compilerStrategy";
    
    /**
     * Specifies the fully-qualified class name of the un-enhancement strategy to use when performing type lookup 
     * in order to map objects;  <br>
     * default value is determined by the MapperFactory implementation.
     */
    public static final String UNENHANCE_STRATEGY = "ma.glasnost.orika.unenhanceStrategy";
    
    /**
     * Specifies the fully-qualified class name of the constructor-resolver strategy to use when resolving 
     * constructors for instantiation of target types;  <br><br>
     * default value is determined by the MapperFactory implementation.
     */
    public static final String CONSTRUCTOR_RESOLVER_STRATEGY = "ma.glasnost.orika.constructorResolverStrategy";
    
    /**
     * Specifies the fully-qualified class name of the property-resolver strategy to use when resolving 
     * mappable properties of target types; <br><br>
     * default value is {@link ma.glasnost.orika.property.IntrospectorPropertyResolver}
     */
    public static final String PROPERTY_RESOLVER_STRATEGY = "ma.glasnost.orika.propertyResolverStrategy";
    
    /**
     * Specifies the fully-qualified class name of the classmap builder factory be used by
     * the default mapper factory to generate new ClassMapBuilder instances. <br><br>
     * default value is {@link ma.glasnost.orika.metadata.ClassMapBuilderFactory}
     */
    public static final String CLASSMAP_BUILDER_FACTORY = "ma.glasnost.orika.classMapBuilderFactory";
    
    /**
     * Specifies the fully-qualified class name of the MappingContextFactory to be used by
     * the default mapper factory to generate new MappingContext instances. <br><br>
     * default value is {@link ma.glasnost.orika.MappingContext.Factory}
     */
    public static final String MAPPING_CONTEXT_FACTORY = "ma.glasnost.orika.mappingContextFactory";
    
    /**
     * Specifies the fully-qualified class name of the converter factory to use when generating converters
     * for target types;<br><br>
     * default value is determined by the MapperFactory implementation.
     */
    public static final String CONVERTER_FACTORY = "ma.glasnost.orika.converterFactory";
    
    /**
     * Specifies that the new mapping strategy cache should be used to cache mapping strategies;<br><br>
     * default value is <code>false</code>
     */
    public static final String USE_STRATEGY_CACHE = "ma.glasnost.orika.useStrategyCache";
    
    /**
     * Specifies whether null values should be mapped.
     * default value is <code>true</code>
     */
    public static final String MAP_NULLS = "ma.glasnost.orika.mapNulls";
    
    /**
     * Specifies whether to use built-in converters
     * default value is <code>true</code>
     */
    public static final String USE_BUILTIN_CONVERTERS = "ma.glasnost.orika.useBuiltinConverters";
    
    /**
     * Specifies whether Mapper and ObjectFactory instances for registered ClassMap instances
     * default value is <code>false</code>
     */
    public static final String USE_AUTO_MAPPING = "ma.glasnost.orika.useAutoMapping";
}
