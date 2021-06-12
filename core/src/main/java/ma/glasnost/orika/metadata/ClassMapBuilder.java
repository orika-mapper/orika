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

package ma.glasnost.orika.metadata;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MappedTypePair;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.UtilityResolver;
import ma.glasnost.orika.property.PropertyResolver;
import ma.glasnost.orika.property.PropertyResolverStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static ma.glasnost.orika.impl.Specifications.aManyToOneElementMap;
import static ma.glasnost.orika.impl.Specifications.aMultiOccurrenceElementMap;
import static ma.glasnost.orika.impl.Specifications.aOneToManyElementMap;

/**
 * ClassMapBuilder provides a fluent API which can be used to define a mapping
 * from one class to another.
 *
 * @param <A>
 * @param <B>
 */
public class ClassMapBuilder<A, B> implements MappedTypePair<A, B> {
    
    public static class Factory extends ClassMapBuilderFactory {
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * ma.glasnost.orika.metadata.ClassMapBuilderFactory#newClassMapBuilder
         * (ma.glasnost.orika.metadata.Type, ma.glasnost.orika.metadata.Type,
         * ma.glasnost.orika.property.PropertyResolverStrategy,
         * ma.glasnost.orika.DefaultFieldMapper[])
         */
        @Override
        protected <A, B> ClassMapBuilder<A, B> newClassMapBuilder(Type<A> aType, Type<B> bType, MapperFactory mapperFactory,
                PropertyResolverStrategy propertyResolver, DefaultFieldMapper[] defaults) {
            
            return new ClassMapBuilder<>(aType, bType, mapperFactory, propertyResolver, defaults);
        }
    }
    
    private final Map<String, Property> aProperties;
    private final Map<String, Property> bProperties;
    private final Set<String> propertiesCacheA;
    private final Set<String> propertiesCacheB;
    final private Type<A> aType;
    final private Type<B> bType;
    final private Set<FieldMap> fieldsMapping;
    
    final private Set<MapperKey> usedMappers;
    private Mapper<A, B> customizedMapper;
    private String[] constructorA;
    private String[] constructorB;
    private final PropertyResolverStrategy propertyResolver;
    private final MapperFactory mapperFactory;
    private final DefaultFieldMapper[] defaults;
    private Boolean sourcesMappedOnNull;
    private Boolean destinationsMappedOnNull;
    private Boolean favorsExtension;
    private Boolean destinationValueRetrievedOnMapping;

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassMapBuilder.class);
    
    /**
     * Note: this static member variable exists to support the deprecated static
     * map methods; it can be removed as soon as they are removed
     */
    private static volatile WeakReference<PropertyResolverStrategy> defaultPropertyResolver;
    
    /**
     * @param aType
     * @param bType
     * @param propertyResolver
     * @param defaults
     */
    protected ClassMapBuilder(Type<A> aType, Type<B> bType, MapperFactory mapperFactory, PropertyResolverStrategy propertyResolver,
            DefaultFieldMapper... defaults) {
        
        if (aType == null) {
            throw new MappingException("[aType] is required");
        }
        
        if (bType == null) {
            throw new MappingException("[bType] is required");
        }
        
        this.mapperFactory = mapperFactory;
        this.propertyResolver = propertyResolver;
        this.defaults = defaults;
        
        aProperties = propertyResolver.getProperties(aType);
        bProperties = propertyResolver.getProperties(bType);
        propertiesCacheA = new LinkedHashSet<>();
        propertiesCacheB = new LinkedHashSet<>();
        
        this.aType = aType;
        this.bType = bType;
        this.fieldsMapping = new LinkedHashSet<>();
        this.usedMappers = new LinkedHashSet<>();
        
    }
    
    /**
     * Gets all of the property expressions for a given type, including all
     * nested properties. If the type of a property is not immutable and has any
     * nested properties, it will not be included. (Note that the 'class'
     * property is explicitly excluded.)
     * 
     * @param type
     *            the type for which to gather properties
     * @return the map of nested properties keyed by expression name
     */
    protected Map<String, Property> getPropertyExpressions(Type<?> type) {
        
        PropertyResolverStrategy propertyResolver = getPropertyResolver();
        
        Map<String, Property> properties = new HashMap<>();
        LinkedHashMap<String, Property> toProcess = new LinkedHashMap<>(propertyResolver.getProperties(type));
        
        if (type.isMap() || type.isList() || type.isArray()) {
            Property selfReferenceProperty = new Property.Builder().name("")
                    .getter("")
                    .setter(" = %s")
                    .type(TypeFactory.valueOf(type))
                    .build((PropertyResolver) propertyResolver);
            toProcess.put("", selfReferenceProperty);
        }
        
        while (!toProcess.isEmpty()) {
            
            Entry<String, Property> entry = toProcess.entrySet().iterator().next();
            if (!entry.getKey().equals("class")) {
                Property owningProperty = entry.getValue();
                Type<?> propertyType = owningProperty.getType();
                if (!propertyType.isImmutable()) {
                    Map<String, Property> props = propertyResolver.getProperties(propertyType);
                    if (propertyType.isMap()) {
                        Map<String, Property> valueProperties = getPropertyExpressions(propertyType.getNestedType(1));
                        for (Entry<String, Property> prop : valueProperties.entrySet()) {
                            Property elementProp = new NestedElementProperty(entry.getValue(), prop.getValue(), propertyResolver);
                            String key = entry.getKey() + PropertyResolver.ELEMENT_PROPERT_PREFIX + prop.getKey()
                                    + PropertyResolver.ELEMENT_PROPERT_SUFFIX;
                            toProcess.put(key, elementProp);
                        }
                    } else if (propertyType.isList()) {
                        Map<String, Property> valueProperties = getPropertyExpressions(propertyType.getNestedType(0));
                        for (Entry<String, Property> prop : valueProperties.entrySet()) {
                            Property elementProp = new NestedElementProperty(owningProperty, prop.getValue(), propertyResolver);
                            String key = entry.getKey() + PropertyResolver.ELEMENT_PROPERT_PREFIX + prop.getValue().getExpression()
                                    + PropertyResolver.ELEMENT_PROPERT_SUFFIX;
                            toProcess.put(key, elementProp);
                        }
                    } else if (propertyType.isArray()) {
                        Map<String, Property> valueProperties = getPropertyExpressions(propertyType.getComponentType());
                        for (Entry<String, Property> prop : valueProperties.entrySet()) {
                            Property elementProp = new NestedElementProperty(entry.getValue(), prop.getValue(), propertyResolver);
                            String key = entry.getKey() + PropertyResolver.ELEMENT_PROPERT_PREFIX + prop.getKey()
                                    + PropertyResolver.ELEMENT_PROPERT_SUFFIX;
                            toProcess.put(key, elementProp);
                        }
                    } else if (!props.isEmpty()) {
                        for (Entry<String, Property> property : props.entrySet()) {
                            if (!property.getKey().equals("class")) {
                                String expression = entry.getKey() + "." + property.getKey();
                                toProcess.put(expression, resolveProperty(type, expression));
                            }
                        }
                    } else {
                        properties.put(entry.getKey(), resolveProperty(type, entry.getKey()));
                    }
                } else {
                    properties.put(entry.getKey(), resolveProperty(type, entry.getKey()));
                }
            }
            toProcess.remove(entry.getKey());
        }
        return properties;
    }
    
    /**
     * @param aType
     * @param bType
     * @deprecated Use of this method instantiates a new
     *             PropertyResolverStrategy instance each time; instead,
     *             {@link ma.glasnost.orika.MapperFactory#classMap(Type, Type)}
     *             should be used which will leverage the
     *             PropertyResolverStrategy instance associated with the
     *             factory.
     */
    @Deprecated
    private ClassMapBuilder(Type<A> aType, Type<B> bType) {
        
        this(aType, bType, null, getDefaultPropertyResolver());
    }
    
    private static PropertyResolverStrategy getDefaultPropertyResolver() {
        if (defaultPropertyResolver == null || defaultPropertyResolver.get() == null) {
            synchronized (ClassMapBuilder.class) {
                if (defaultPropertyResolver == null || defaultPropertyResolver.get() == null) {
                    defaultPropertyResolver = new WeakReference<>(
                            UtilityResolver.getDefaultPropertyResolverStrategy());
                }
            }
        }
        return defaultPropertyResolver.get();
    }
    
    /**
     * Map a field in both directions
     * 
     * @param fieldNameA
     *            property name in type A
     * @param fieldNameB
     *            property name in type B
     * @return
     */
    public ClassMapBuilder<A, B> field(String fieldNameA, String fieldNameB) {
        return fieldMap(fieldNameA, fieldNameB).add();
    }
    
    /**
     * Map a field in one direction only (from fieldNameA to fieldNameB)
     * 
     * @param fieldNameA
     *            the (source) fieldName from type A
     * @param fieldNameB
     *            the (destination) fieldName from type B
     * @return
     */
    public ClassMapBuilder<A, B> fieldAToB(String fieldNameA, String fieldNameB) {
        return fieldMap(fieldNameA, fieldNameB).aToB().add();
    }
    
    /**
     * Map a field in one direction only (from fieldNameB to fieldNameA)
     * 
     * @param fieldNameB
     *            the (source) fieldName from type B
     * @param fieldNameA
     *            the (destination) fieldName from type A
     * @return
     */
    public ClassMapBuilder<A, B> fieldBToA(String fieldNameA, String fieldNameB) {
        return fieldMap(fieldNameA, fieldNameB).bToA().add();
    }
    
    /**
     * Create a fieldMap for the particular field (same property name used in
     * both types)
     * 
     * @param a
     * @return
     */
    public FieldMapBuilder<A, B> fieldMap(String a) {
        return fieldMap(a, a);
    }
    
    /**
     * Create a fieldMap for the particular field (same property name used in
     * both types)
     * 
     * @param a
     * @param byDefault
     * @return
     */
    public FieldMapBuilder<A, B> fieldMap(String a, boolean byDefault) {
        return fieldMap(a, a, byDefault);
    }
    
    /**
     * Create a fieldMap for the particular field mapping
     * 
     * @param fieldNameA
     *            the name of the field in type A
     * @param fieldNameB
     *            the name of the field in type B
     * @return
     */
    public FieldMapBuilder<A, B> fieldMap(String fieldNameA, String fieldNameB) {
        return fieldMap(fieldNameA, fieldNameB, false);
    }
    
    /**
     * Create a fieldMap for the particular field mapping
     * 
     * @param fieldNameA
     *            the name of the field in type A
     * @param fieldNameB
     *            the name of the field in type B
     * @param byDefault
     *            whether the field mapping has been provided by default
     * @return
     */
    public FieldMapBuilder<A, B> fieldMap(String fieldNameA, String fieldNameB, boolean byDefault) {
        
        try {
            final FieldMapBuilder<A, B> fieldMapBuilder = new FieldMapBuilder<>(this, fieldNameA, fieldNameB, byDefault,
                    sourcesMappedOnNull, destinationsMappedOnNull, destinationValueRetrievedOnMapping);
            
            return fieldMapBuilder;
        } catch (MappingException e) {
            /*
             * Add more information to the message to help with debugging
             */
            String msg = getClass().getSimpleName() + ".map(" + aType + ", " + bType + ")" + ".field('" + fieldNameA + "', '" + fieldNameB
                    + "'): Error: " + e.getLocalizedMessage();
            throw new MappingException(msg, e);
        }
    }
    
    /**
     * 
     * 
     * @param fieldA
     * @param fieldB
     * @param byDefault
     * @return
     */
    public FieldMapBuilder<A, B> fieldMap(Property fieldA, Property fieldB, boolean byDefault) {
        return new FieldMapBuilder<>(this, fieldA, fieldB, byDefault, sourcesMappedOnNull, destinationsMappedOnNull, destinationValueRetrievedOnMapping);
    }
    
    /**
     * 
     * 
     * @param fieldNameA
     * @param fieldB
     * @param byDefault
     * @return
     */
    public FieldMapBuilder<A, B> fieldMap(String fieldNameA, Property fieldB, boolean byDefault) {
        return new FieldMapBuilder<>(this, resolvePropertyForA(fieldNameA), fieldB, byDefault, sourcesMappedOnNull,
                destinationsMappedOnNull, destinationValueRetrievedOnMapping);
    }
    
    /**
     * 
     * 
     * @param fieldA
     * @param fieldNameB
     * @param byDefault
     * @return
     */
    public FieldMapBuilder<A, B> fieldMap(Property fieldA, String fieldNameB, boolean byDefault) {
        return new FieldMapBuilder<>(this, fieldA, resolvePropertyForB(fieldNameB), byDefault, sourcesMappedOnNull,
                destinationsMappedOnNull, destinationValueRetrievedOnMapping);
    }
    
    /**
     * 
     * 
     * @param fieldA
     * @param fieldB
     * @param byDefault
     * @return
     */
    public FieldMapBuilder<A, B> fieldMap(Property.Builder fieldA, Property.Builder fieldB, boolean byDefault) {
        return new FieldMapBuilder<>(this, fieldA.build((PropertyResolver) propertyResolver),
                fieldB.build((PropertyResolver) propertyResolver), byDefault, sourcesMappedOnNull, destinationsMappedOnNull, destinationValueRetrievedOnMapping);
    }
    
    /**
     * 
     * 
     * @param fieldNameA
     * @param fieldB
     * @param byDefault
     * @return
     */
    public FieldMapBuilder<A, B> fieldMap(String fieldNameA, Property.Builder fieldB, boolean byDefault) {
        return new FieldMapBuilder<>(this, resolvePropertyForA(fieldNameA), fieldB.build((PropertyResolver) propertyResolver),
                byDefault, sourcesMappedOnNull, destinationsMappedOnNull, destinationValueRetrievedOnMapping);
    }
    
    /**
     * 
     * 
     * @param fieldA
     * @param fieldNameB
     * @param byDefault
     * @return
     */
    public FieldMapBuilder<A, B> fieldMap(Property.Builder fieldA, String fieldNameB, boolean byDefault) {
        return new FieldMapBuilder<>(this, fieldA.build((PropertyResolver) propertyResolver), resolvePropertyForB(fieldNameB),
                byDefault, sourcesMappedOnNull, destinationsMappedOnNull, destinationValueRetrievedOnMapping);
    }
    
    /**
     * 
     * 
     * @param fieldA
     * @param fieldB
     * @return
     */
    public ClassMapBuilder<A, B> field(Property fieldA, Property fieldB) {
        return fieldMap(fieldA, fieldB, false).add();
    }
    
    /**
     * 
     * 
     * @param fieldNameA
     * @param fieldB
     * @return
     */
    public ClassMapBuilder<A, B> field(String fieldNameA, Property fieldB) {
        return fieldMap(fieldNameA, fieldB, false).add();
    }
    
    /**
     * 
     * 
     * @param fieldA
     * @param fieldNameB
     * @return
     */
    public ClassMapBuilder<A, B> field(Property fieldA, String fieldNameB) {
        return fieldMap(fieldA, fieldNameB, false).add();
    }
    
    /**
     * 
     * 
     * @param fieldA
     * @param fieldB
     * @return this ClassMapBuilder
     */
    public ClassMapBuilder<A, B> field(Property.Builder fieldA, Property.Builder fieldB) {
        return fieldMap(fieldA, fieldB, false).add();
    }
    
    /**
     * 
     * 
     * @param fieldNameA
     * @param fieldB
     * @return this ClassMapBuilder
     */
    public ClassMapBuilder<A, B> field(String fieldNameA, Property.Builder fieldB) {
        return fieldMap(fieldNameA, fieldB, false).add();
    }
    
    /**
     * Maps 'fieldA' of type A to 'fieldB' of type B.
     * 
     * @param fieldA
     * @param fieldNameB
     * @return this ClassMapBuilder
     */
    public ClassMapBuilder<A, B> field(Property.Builder fieldA, String fieldNameB) {
        return fieldMap(fieldA, fieldNameB, false).add();
    }
    
    /**
     * Exclude the specified field from mapping
     * 
     * @param fieldName
     *            the name of the field/property to exclude
     * @return this ClassMapBuilder
     */
    public ClassMapBuilder<A, B> exclude(String fieldName) {
        if (propertyResolver.existsProperty(getAType(), fieldName) && propertyResolver.existsProperty(getBType(), fieldName)) {
            return fieldMap(fieldName).exclude().add();
        } else {
            return this;
        }
    }
    
    /**
     * Set the custom mapper to use for this mapping.
     * 
     * @param legacyCustomizedMapper
     * @return this ClassMapBuilder
     * @deprecated use {@link #customize(Mapper)} instead
     */
    @Deprecated
    public final ClassMapBuilder<A, B> customize(ma.glasnost.orika.MapperBase<A, B> legacyCustomizedMapper) {
        customize(new ma.glasnost.orika.MapperBase.MapperBaseAdapter<>(legacyCustomizedMapper));
        return this;
    }
    
    /**
     * Set the custom mapper to use for this mapping.
     * 
     * @param customizedMapper
     * @return this ClassMapBuilder
     */
    public ClassMapBuilder<A, B> customize(Mapper<A, B> customizedMapper) {
        this.customizedMapper = customizedMapper;
        return this;
    }
    
    /**
     * Configure this ClassMapBuilder to use an existing mapping (for parent
     * classes) defined from <code>aParentClass</code> to
     * <code>bParentClass</code>.
     * 
     * @param aParentClass
     *            the source class of the parent mapping
     * @param bParentClass
     *            the destination class of the parent mapping
     * @return this ClassMapBuilder
     */
    public <X, Y> ClassMapBuilder<A, B> use(Class<?> aParentClass, Class<?> bParentClass) {
        
        @SuppressWarnings("unchecked")
        Type<Object> aParentType = TypeFactory.valueOf((Class<Object>) aParentClass);
        @SuppressWarnings("unchecked")
        Type<Object> bParentType = TypeFactory.valueOf((Class<Object>) bParentClass);
        
        return use(aParentType, bParentType);
    }
    
    /**
     * Configure this ClassMapBuilder to use an existing mapping (for parent
     * classes) defined from <code>aParentClass</code> to
     * <code>bParentClass</code>.
     * 
     * @param aParentType
     *            the source type of the parent mapping
     * @param bParentType
     *            the destination type of the parent mapping
     * @return this ClassMapBuilder
     */
    public <X, Y> ClassMapBuilder<A, B> use(Type<?> aParentType, Type<?> bParentType) {
        
        if (!aParentType.isAssignableFrom(aType)) {
            throw new MappingException(aType.getSimpleName() + " is not a subclass of " + aParentType.getSimpleName());
        }
        
        if (!bParentType.isAssignableFrom(bType)) {
            throw new MappingException(bType.getSimpleName() + " is not a subclass of " + bParentType.getSimpleName());
        }
        
        usedMappers.add(new MapperKey(aParentType, bParentType));
        
        return this;
    }
    
    /**
     * Configures this class-map builder to employ the default property mapping
     * behavior to any properties that have not already been mapped or excluded;
     * if any DefaultFieldMapper instances are passed, they will be used
     * (instead of those configured on the builder) to attempt a property name
     * match if a direct match is not found.
     * 
     * @param withDefaults
     *            zero or more DefaultFieldMapper instances to apply during the
     *            default mapping; if none are supplied, the configured
     *            DefaultFieldMappers for the builder (if any) should be used.
     * @return this ClassMapBuilder instance
     */
    public ClassMapBuilder<A, B> byDefault(DefaultFieldMapper... withDefaults) {
        return byDefault(MappingDirection.BIDIRECTIONAL, withDefaults);
    }
    
    /**
     * Configures this class-map builder to employ the default property mapping
     * behavior to any properties that have not already been mapped or excluded;
     * if any DefaultFieldMapper instances are passed, they will be used
     * (instead of those configured on the builder) to attempt a property name
     * match if a direct match is not found.
     * 
     * @param direction
     *            the mapping direction to be applied
     * @param withDefaults
     *            zero or more DefaultFieldMapper instances to apply during the
     *            default mapping; if none are supplied, the configured
     *            DefaultFieldMappers for the builder (if any) should be used.
     * @return this ClassMapBuilder instance
     */
    public ClassMapBuilder<A, B> byDefault(MappingDirection direction, DefaultFieldMapper... withDefaults) {
        
        DefaultFieldMapper[] defaults;
        if (withDefaults.length == 0) {
            defaults = getDefaultFieldMappers();
        } else {
            defaults = withDefaults;
        }
        
        for (final String propertyName : getPropertiesForTypeA()) {
            if (!getMappedPropertiesForTypeA().contains(propertyName)) {
                if (getPropertiesForTypeB().contains(propertyName)) {
                    if (!getMappedPropertiesForTypeB().contains(propertyName)) {
                        /*
                         * Don't include the default mapping of Class to Class;
                         * this property is resolved for all types, but can't be
                         * mapped in either direction.
                         */
                        if (!propertyName.equals("class")) {
                            fieldMap(propertyName, true).direction(direction).add();
                        }
                    }
                } else {
                    Property prop = resolvePropertyForA(propertyName);
                    for (DefaultFieldMapper defaulter : defaults) {
                        String suggestion = defaulter.suggestMappedField(propertyName, prop.getType());
                        if (suggestion != null && getPropertiesForTypeB().contains(suggestion)) {
                            if (!getMappedPropertiesForTypeB().contains(suggestion)) {
                                fieldMap(propertyName, suggestion, true).direction(direction).add();
                            }
                        }
                    }
                }
            }
        }
        
        return this;
    }
    
    /**
     * @deprecated use {@link #byDefault(DefaultFieldMapper...)} instead
     * 
     * @param hint0
     *            first hint
     * @param mappingHints
     *            remaining hints
     * @return
     */
    @Deprecated
    public final ClassMapBuilder<A, B> byDefault(ma.glasnost.orika.MappingHint hint0, ma.glasnost.orika.MappingHint... mappingHints) {
        ma.glasnost.orika.MappingHint[] hints = new ma.glasnost.orika.MappingHint[mappingHints.length + 1];
        hints[0] = hint0;
        if (mappingHints.length > 0) {
            System.arraycopy(mappingHints, 0, hints, 1, mappingHints.length);
        }
        return byDefault(hints);
    }
    
    /**
     * @deprecated use {@link #byDefault(DefaultFieldMapper...)} instead
     * 
     * @param mappingHints
     * @return
     */
    @Deprecated
    public final ClassMapBuilder<A, B> byDefault(ma.glasnost.orika.MappingHint[] mappingHints) {
        
        for (final String propertyName : aProperties.keySet()) {
            if (!propertiesCacheA.contains(propertyName)) {
                if (bProperties.containsKey(propertyName)) {
                    if (!propertiesCacheB.contains(propertyName)) {
                        /*
                         * Don't include the default mapping of Class to Class;
                         * this property is resolved for all types, but can't be
                         * mapped in either direction.
                         */
                        if (!propertyName.equals("class")) {
                            fieldMap(propertyName).add();
                        }
                    }
                } else {
                    Property prop = aProperties.get(propertyName);
                    for (ma.glasnost.orika.MappingHint hint : mappingHints) {
                        String suggestion = hint.suggestMappedField(propertyName, prop.getType().getRawType());
                        if (suggestion != null && bProperties.containsKey(suggestion)) {
                            if (!propertiesCacheB.contains(suggestion)) {
                                fieldMap(propertyName, suggestion).add();
                            }
                        }
                    }
                }
            }
        }
        
        return this;
    }
    
    /**
     * Produces a ClassMap instance based on the configurations defined on this
     * ClassMapBuilder. A ClassMap is used by Orika as a runtime descriptor for
     * the details of a mapping between one type and another.
     * 
     * @return a ClassMap as configured by this ClassMapBuilder
     */
    public ClassMap<A, B> toClassMap() {
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ClassMap created:\n\t" + describeClassMap());
        }
        
        return new ClassMap<>(aType, bType, fieldsMapping, customizedMapper, usedMappers, constructorA, constructorB,
                sourcesMappedOnNull, destinationsMappedOnNull, favorsExtension);
    }
    
    /**
     * @param destinationsMappedOnNull
     *            true|false to indicate whether the destination properties of
     *            this class map's fields should be set to null (when mapping in
     *            the forward direction) if the source property's value is null
     * 
     * @return this FieldMapBuilder
     */
    public ClassMapBuilder<A, B> mapNulls(boolean destinationsMappedOnNull) {
        this.destinationsMappedOnNull = destinationsMappedOnNull;
        
        return this;
    }
    
    /**
     * @param sourcesMappedOnNull
     *            true|false to indicate whether the source properties of this
     *            class map's fields should be set to null (when mapping in the
     *            reverse direction) if the destination property's value is null
     * 
     * @return this FieldMapBuilder
     */
    public ClassMapBuilder<A, B> mapNullsInReverse(boolean sourcesMappedOnNull) {
        this.sourcesMappedOnNull = sourcesMappedOnNull;
        
        return this;
    }
    
    /**
     * @param favorsExtension
     *            true|false indicating whether the class-map defined by this
     *            builder should be considered 'abstract', meaning that it is
     *            not usable independently, but can contribute to the definition
     *            of mappers that inherit from it based on the mapped types.
     * 
     * @return
     */
    public ClassMapBuilder<A, B> favorExtension(boolean favorsExtension) {
        this.favorsExtension = favorsExtension;
        
        return this;
    }

    /**
     * @param destinationValueRetrievedOnMapping
     *            true|false to indicate whether the destination properties values of
     *            this class map's fields should be retrieved on mapping (when mapping
     *            in any direction). If false, it will be assumed that the  distination value
     *            is always null. If true, a real destination value will be used.
     *
     * @return this FieldMapBuilder
     */
    public ClassMapBuilder<A, B> retrieveDestinationValueOnMapping(boolean destinationValueRetrievedOnMapping) {
        this.destinationValueRetrievedOnMapping = destinationValueRetrievedOnMapping;

        return this;
    }

    /**
     * Registers the ClassMap defined by this builder with it's initiating
     * MapperFactory
     */
    public void register() {
        if (this.mapperFactory == null) {
            throw new IllegalStateException("register() is not supported from deprecated static ClassMapBuilder.map(..) instances");
        }
        mapperFactory.registerClassMap(this);
    }
    
    /**
     * @return a pseudo-code description of the class map that is created by
     *         this builder
     */
    protected String describeClassMap() {
        StringBuilder output = new StringBuilder();
        output.append(getClass().getSimpleName()).append(".map(").append(aType).append(", ").append(bType).append(")");
        for (FieldMap f : fieldsMapping) {
            if (f.isExcluded()) {
                output.append("\n\t .exclude('").append(f.getSourceName()).append("')");
            } else {
                String dir = "";
                if (f.getDirection() == MappingDirection.A_TO_B) {
                    dir = "AToB";
                } else if (f.getDirection() == MappingDirection.B_TO_A) {
                    dir = "BToA";
                }
                output.append("\n\t .field").append(dir).append("( ").append(f.getSource()).append(", ").append(f.getDestination()).append(" )");
            }
        }
        if (constructorA != null) {
            output.append("\n\t .constructorA(").append(Arrays.toString(constructorA)).append(")");
        }
        if (constructorB != null) {
            output.append("\n\t .constructorB(").append(Arrays.toString(constructorB)).append(")");
        }
        return output.toString();
    }
    
    /**
     * Creates a new ClassMapBuilder configuration for mapping between
     * <code>aType</code> and <code>bType</code>.
     * 
     * @param aType
     * @param bType
     * @return
     * @deprecated use
     *             {@link ma.glasnost.orika.MapperFactory#classMap(Class, Class)}
     *             instead
     */
    public static final <A, B> ClassMapBuilder<A, B> map(Class<A> aType, Class<B> bType) {
        return new ClassMapBuilder<>(TypeFactory.valueOf(aType), TypeFactory.valueOf(bType));
    }
    
    /**
     * @param aType
     * @param bType
     * @return
     * @deprecated use
     *             {@link ma.glasnost.orika.MapperFactory#classMap(Type, Type)}
     *             instead
     */
    public static final <A, B> ClassMapBuilder<A, B> map(Type<A> aType, Type<B> bType) {
        return new ClassMapBuilder<>(aType, bType);
    }
    
    /**
     * @param aType
     * @param bType
     * @return
     * @deprecated use
     *             {@link ma.glasnost.orika.MapperFactory#classMap(Class, Type)}
     *             instead
     */
    public static final <A, B> ClassMapBuilder<A, B> map(Class<A> aType, Type<B> bType) {
        return new ClassMapBuilder<>(TypeFactory.valueOf(aType), bType);
    }
    
    /**
     * @param aType
     * @param bType
     * @return
     * @deprecated use
     *             {@link ma.glasnost.orika.MapperFactory#classMap(Type, Class)}
     *             instead
     */
    public static final <A, B> ClassMapBuilder<A, B> map(Type<A> aType, Class<B> bType) {
        return new ClassMapBuilder<>(aType, TypeFactory.valueOf(bType));
    }
    
    /**
     * Determines whether the provided string is a valid property expression
     * 
     * @param expression
     *            the expression to evaluate
     * @return
     */
    protected boolean isNestedPropertyExpression(String expression) {
        return expression.indexOf('.') != -1;
    }
    
    /**
     * Resolves a property for the particular type, based on the provided
     * property expression
     * 
     * @param type
     *            the type to resolve
     * @param expr
     *            the property expression to resolve
     * @return the Property referenced by the provided expression
     */
    protected Property resolveProperty(java.lang.reflect.Type type, String expr) {
        return propertyResolver.getProperty(type, expr);
    }
    
    /**
     * Resolves a property expression for this builder's 'A' type
     * 
     * @param expr
     *            the property expression
     * @return the Property referenced by the provided expression
     */
    protected Property resolvePropertyForA(String expr) {
        return resolveProperty(aType, expr);
    }
    
    /**
     * Resolves a property expression for this builder's 'B' type
     * 
     * @param expr
     *            the property expression
     * @return the Property referenced by the provided expression
     */
    protected Property resolvePropertyForB(String expr) {
        return resolveProperty(bType, expr);
    }
    
    /**
     * @return the 'A' type for this builder
     */
    public Type<A> getAType() {
        return aType;
    }
    
    /**
     * @return the 'B' type for this builder
     */
    public Type<B> getBType() {
        return bType;
    }
    
    /**
     * @param fieldMap
     */
    protected void addFieldMap(FieldMap fieldMap) {
        getMappedFields().add(fieldMap);
        getMappedPropertiesForTypeA().add(fieldMap.getSourceExpression());
        getMappedPropertiesForTypeB().add(fieldMap.getDestinationExpression());
        
        if (fieldMap.is(aManyToOneElementMap())) {
            getMappedPropertiesForTypeA().add(fieldMap.getSource().getContainer().getExpression());
        } else if (fieldMap.is(aOneToManyElementMap())) {
            getMappedPropertiesForTypeB().add(fieldMap.getDestination().getContainer().getExpression());
        } else if (fieldMap.is(aMultiOccurrenceElementMap())) {
            getMappedPropertiesForTypeA().add(fieldMap.getSource().getContainer().getExpression());
            getMappedPropertiesForTypeB().add(fieldMap.getDestination().getContainer().getExpression());
        }
        
    }
    
    /**
     * @return the mapped properties for type A
     */
    protected Set<String> getMappedPropertiesForTypeA() {
        return propertiesCacheA;
    }
    
    /**
     * @return the mapped properties for type B
     */
    protected Set<String> getMappedPropertiesForTypeB() {
        return propertiesCacheB;
    }
    
    /**
     * @return the mapped fields for this builder
     */
    protected Set<FieldMap> getMappedFields() {
        return fieldsMapping;
    }
    
    /**
     * @return the known properties for type A
     */
    protected Set<String> getPropertiesForTypeA() {
        return aProperties.keySet();
    }
    
    /**
     * @return the known properties for type B
     */
    protected Set<String> getPropertiesForTypeB() {
        return bProperties.keySet();
    }
    
    /**
     * @return the default field mappers (if any) configured for this builder
     */
    protected DefaultFieldMapper[] getDefaultFieldMappers() {
        return defaults;
    }
    
    /**
     * @return the property resolver used by this builder
     */
    protected PropertyResolverStrategy getPropertyResolver() {
        return this.propertyResolver;
    }
    
    /**
     * Declares a constructor to be used for the A type with the specified
     * arguments.
     * 
     * @param args
     *            the arguments identifying the constructor to be used
     * @return this ClassMapBuilder
     */
    public ClassMapBuilder<A, B> constructorA(String... args) {
        this.constructorA = args.clone();
        return this;
    }
    
    /**
     * Declares a constructor to be used for the B type with the specified
     * arguments.
     * 
     * @param args
     *            the arguments identifying the constructor to be used
     * @return this ClassMapBuilder
     */
    public ClassMapBuilder<A, B> constructorB(String... args) {
        this.constructorB = args.clone();
        return this;
    }
    
    public String toString() {
        return getClass().getSimpleName() + "[" + getAType() + ", " + getBType() + "]";
    }
    
}
