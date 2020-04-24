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

package ma.glasnost.orika.impl.generator;

import ma.glasnost.orika.*;
import ma.glasnost.orika.Properties;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.AggregateFilter;
import ma.glasnost.orika.impl.GeneratedObjectBase;
import ma.glasnost.orika.impl.generator.CompilerStrategy.SourceCodeGenerationException;
import ma.glasnost.orika.impl.generator.Node.NodeList;
import ma.glasnost.orika.impl.generator.UsedMapperFacadesContext.UsedMapperFacadesIndex;
import ma.glasnost.orika.impl.generator.specification.AbstractSpecification;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.*;
import ma.glasnost.orika.property.PropertyResolverStrategy;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.Specifications.aMultiOccurrenceElementMap;

/**
 * SourceCodeContext contains the state information necessary while generating
 * source code for a given mapping object; it also houses various utility
 * methods which can be used to aid in code generation.
 * 
 */
public class SourceCodeContext {
    
    private static final AtomicInteger UNIQUE_CLASS_INDEX = new AtomicInteger();
    
    private StringBuilder sourceBuilder;
    private String classSimpleName;
    private String packageName;
    private String className;
    private CompilerStrategy compilerStrategy;
    private List<String> methods;
    private List<String> fields;
    private Class<?> superClass;
    
    private final UsedTypesContext usedTypes;
    private final UsedConvertersContext usedConverters;
    private final UsedFiltersContext usedFilters;
    private final UsedMapperFacadesContext usedMapperFacades;
    private final MapperFactory mapperFactory;
    private final CodeGenerationStrategy codeGenerationStrategy;
    private final StringBuilder logDetails;
    private final PropertyResolverStrategy propertyResolver;
    private final Map<AggregateSpecification, List<FieldMap>> aggregateFieldMaps;
    private final MappingContext mappingContext;
    private final Collection<Filter<Object, Object>> filters;
    private final boolean shouldCaptureFieldContext;
    
    /**
     * Constructs a new instance of SourceCodeContext
     * 
     * @param baseClassName
     * @param superClass
     * @param mappingContext
     * @param logDetails
     */
    @SuppressWarnings("unchecked")
    public SourceCodeContext(final String baseClassName, Class<?> superClass, MappingContext mappingContext, StringBuilder logDetails) {
        
        this.mapperFactory = (MapperFactory) mappingContext.getProperty(Properties.MAPPER_FACTORY);
        this.codeGenerationStrategy = (CodeGenerationStrategy) mappingContext.getProperty(Properties.CODE_GENERATION_STRATEGY);
        this.compilerStrategy = (CompilerStrategy) mappingContext.getProperty(Properties.COMPILER_STRATEGY);
        this.propertyResolver = (PropertyResolverStrategy) mappingContext.getProperty(Properties.PROPERTY_RESOLVER_STRATEGY);
        this.filters = (Collection<Filter<Object, Object>>) mappingContext.getProperty(Properties.FILTERS);
        this.shouldCaptureFieldContext = (Boolean) mappingContext.getProperty(Properties.CAPTURE_FIELD_CONTEXT);
        
        String safeBaseClassName = baseClassName.replace("[]", "$Array");
        this.sourceBuilder = new StringBuilder();
        this.superClass = superClass;
        
        int namePos = safeBaseClassName.lastIndexOf(".");
        if (namePos > 0) {
            this.packageName = safeBaseClassName.substring(0, namePos);
            this.classSimpleName = safeBaseClassName.substring(namePos + 1);
        } else {
            this.packageName = "ma.glasnost.orika.generated";
            this.classSimpleName = safeBaseClassName;
        }
        
        this.classSimpleName = makeUniqueClassName(this.classSimpleName);
        this.className = this.packageName + "." + this.classSimpleName;
        this.methods = new ArrayList<String>();
        this.fields = new ArrayList<String>();
        
        sourceBuilder.append("package " + packageName + ";\n\n");
        sourceBuilder.append("public class " + classSimpleName + " extends " + superClass.getCanonicalName() + " {\n");
        
        this.usedTypes = new UsedTypesContext();
        this.usedConverters = new UsedConvertersContext();
        this.usedFilters = new UsedFiltersContext();
        
        this.mappingContext = mappingContext;
        this.usedMapperFacades = new UsedMapperFacadesContext();
        this.logDetails = logDetails;
        
        this.aggregateFieldMaps = new LinkedHashMap<AggregateSpecification, List<FieldMap>>();
    }
    
    private String makeUniqueClassName(String name) {
        return name + System.nanoTime() + "$" + UNIQUE_CLASS_INDEX.getAndIncrement();
    }
    
    /**
     * @return true if debug logging is enabled for this context
     */
    public boolean isDebugEnabled() {
        return logDetails != null;
    }
    
    public void debug(String msg) {
        if (isDebugEnabled()) {
            logDetails.append(msg);
        }
    }
    
    public void debugField(FieldMap fieldMap, String msg) {
        if (isDebugEnabled()) {
            logDetails.append(fieldTag(fieldMap));
            logDetails.append(msg);
        }
    }

    public String optionallyMakeDestinationVariableAndGetValue(StringBuilder out, VariableRef destination, boolean getDestinationOnMapping){

        if(!getDestinationOnMapping){
            return "null";
        }

        if(shouldCaptureFieldContext){
            addVariable(out, destination);
            return destination.getGlobalUniqueName();
        }

        return destination.asWrapper();
    }
    
    public String fieldTag(FieldMap fieldMap) {
        return "\n\t Field(" + fieldMap.getSource() + ", " + fieldMap.getDestination() + ") : ";
    }
    
    /**
     * @return the StringBuilder containing the current accumulated source.
     */
    protected StringBuilder getSourceBuilder() {
        return sourceBuilder;
    }
    
    public Class<?> getSuperClass() {
        return superClass;
    }
    
    public String getClassSimpleName() {
        return classSimpleName;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public String getClassName() {
        return className;
    }
    
    List<String> getFields() {
        return fields;
    }
    
    List<String> getMethods() {
        return methods;
    }

    public boolean shouldMapNulls() {
        return (Boolean) mappingContext.getProperty(Properties.SHOULD_MAP_NULLS);
    }

    public boolean shouldGetDestinationOnMapping() {
        return (Boolean) mappingContext.getProperty(Properties.SHOULD_GET_DESTINATION_ON_MAPPING);
    }

    public MappingContext getMappingContext() {
        return mappingContext;
    }
    
    /**
     * Adds a method definition to the class based on the provided source.
     * 
     * @param methodSource
     */
    public void addMethod(String methodSource) {
        sourceBuilder.append("\n" + methodSource + "\n");
        this.methods.add(methodSource);
    }
    
    /**
     * Adds a field definition to the class based on the provided source.
     * 
     * @param fieldSource
     *            the source from which to compile the field
     */
    public void addField(String fieldSource) {
        sourceBuilder.append("\n" + fieldSource + "\n");
        this.fields.add(fieldSource);
    }
    
    /**
     * @return the completed generated java source for the class.
     */
    public String toSourceFile() {
        return sourceBuilder.toString() + "\n}";
    }
    
    /**
     * Compile and return the (generated) class; this will also cause the
     * generated class to be detached from the class-pool, and any (optional)
     * source and/or class files to be written.
     * 
     * @return the (generated) compiled class
     * @throws SourceCodeGenerationException
     */
    protected Class<?> compileClass() throws SourceCodeGenerationException {
        try {
            return compilerStrategy.compileClass(this);
        } catch (SourceCodeGenerationException e) {
            throw e;
        }
    }
    
    /**
     * @return a new instance of the (generated) compiled class
     * @throws SourceCodeGenerationException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public <T extends GeneratedObjectBase> T getInstance() throws SourceCodeGenerationException, InstantiationException,
            IllegalAccessException {
        
        T instance = (T) compileClass().newInstance();
        
        Type<Object>[] usedTypesArray = usedTypes.toArray();
        Converter[] usedConvertersArray = usedConverters.toArray();
        BoundMapperFacade<Object, Object>[] usedMapperFacadesArray = usedMapperFacades.toArray();
        Filter<Object, Object>[] usedFiltersArray = usedFilters.toArray();
        if (logDetails != null) {
            if (usedTypesArray.length > 0) {
                logDetails.append("\n\t" + Type.class.getSimpleName() + "s used: " + Arrays.toString(usedTypesArray));
            }
            if (usedConvertersArray.length > 0) {
                logDetails.append("\n\t" + Converter.class.getSimpleName() + "s used: " + Arrays.toString(usedConvertersArray));
            }
            if (usedMapperFacadesArray.length > 0) {
                logDetails.append("\n\t" + BoundMapperFacade.class.getSimpleName() + "s used: " + Arrays.toString(usedMapperFacadesArray));
            }
            if (usedFiltersArray.length > 0) {
                logDetails.append("\n\t" + Filter.class.getSimpleName() + "s used: " + Arrays.toString(usedFiltersArray));
            }
        }
        instance.setUsedTypes(usedTypesArray);
        instance.setUsedConverters(usedConvertersArray);
        instance.setUsedMapperFacades(usedMapperFacadesArray);
        instance.setUsedFilters(usedFiltersArray);
        
        return instance;
    }
    
    public String usedFilter(Filter<?, ?> filter) {
        int index = usedFilters.getIndex(filter);
        return "((" + Filter.class.getCanonicalName() + ")usedFilters[" + index + "])";
    }
    
    public String usedConverter(Converter<?, ?> converter) {
        int index = usedConverters.getIndex(converter);
        return "((" + Converter.class.getCanonicalName() + ")usedConverters[" + index + "])";
    }
    
    public String usedType(Type<?> type) {
        int index = usedTypes.getIndex(type);
        return "((" + Type.class.getCanonicalName() + ")usedTypes[" + index + "])";
    }
    
    private String usedMapperFacadeCall(Type<?> sourceType, Type<?> destinationType) {
        UsedMapperFacadesIndex usedFacade = usedMapperFacades.getIndex(sourceType, destinationType, mapperFactory);
        String mapInDirection = usedFacade.isReversed ? "mapReverse" : "map";
        return "((" + BoundMapperFacade.class.getCanonicalName() + ")usedMapperFacades[" + usedFacade.index + "])." + mapInDirection + "";
    }
    
    /**
     * @param sourceType
     * @param destinationType
     * @param sourceExpression
     * @param destExpression
     * @return
     */
    public String callMapper(Type<?> sourceType, Type<?> destinationType, String sourceExpression, String destExpression) {
        return usedMapperFacadeCall(sourceType, destinationType) + "(" + sourceExpression + ", " + destExpression + ", mappingContext)";
    }
    
    /**
     * @param sourceType
     * @param destinationType
     * @param sourceExpression
     * @return
     */
    public String callMapper(Type<?> sourceType, Type<?> destinationType, String sourceExpression) {
        return usedMapperFacadeCall(sourceType, destinationType) + "(" + sourceExpression + ", mappingContext)";
    }
    
    /**
     * @param source
     * @param destination
     * @return
     */
    public String callMapper(VariableRef source, VariableRef destination) {
        return callMapper(source.type(), destination.type(), "" + source, "" + destination);
    }
    
    /**
     * @param source
     * @param destination
     * @return
     */
    public String callMapper(VariableRef source, Type<?> destination) {
        return callMapper(source.type(), destination, "" + source);
    }
    
    public String usedMapperFacadeNewObjectCall(VariableRef source, VariableRef destination) {
        return newObjectFromMapper(source.type(), destination.type());
    }
    
    public String newObjectFromMapper(Type<?> sourceType, Type<?> destinationType) {
        UsedMapperFacadesIndex usedFacade = usedMapperFacades.getIndex(sourceType, destinationType, mapperFactory);
        String instantiateMethod = usedFacade.isReversed ? "newObjectReverse" : "newObject";
        return "((" + BoundMapperFacade.class.getCanonicalName() + ")usedMapperFacades[" + usedFacade.index + "])." + instantiateMethod
                + "";
    }
    
    /**
     * Generates a code snippet to generate a new instance of the destination
     * type from a mapper
     * 
     * @param source
     * @param destinationType
     * @return a code snippet to generate a new instance of the destination type
     *         from a mapper
     */
    public String newObjectFromMapper(VariableRef source, Type<?> destinationType) {
        return newObjectFromMapper(source, source.asWrapper(), destinationType);
    }

    public String newObjectFromMapper(VariableRef source, String sourceValue, Type<?> destinationType) {
        return newObjectFromMapper(source.type(), destinationType) + "(" + sourceValue + ", mappingContext)";
    }
    
    /**
     * Generate usedType array index code for the provided variable
     * 
     * @param r
     * @return the code snippet for referencing a used type by it's array index
     */
    public String usedType(VariableRef r) {
        return usedType(r.type());
    }
    
    /**
     * @param source
     * @param destinationType
     * @return the code snippet for generating a new instance, or assigning the
     *         default value in cases of primitive types
     */
    public String newObject(VariableRef source, Type<?> destinationType) {
        return newObject(source, source.asWrapper(), destinationType);
    }

    public String newObject(VariableRef source, String sourceValue, Type<?> destinationType) {
        if (destinationType.isPrimitive()) {
            return VariableRef.getDefaultValue(destinationType.getRawType());
        } else if (destinationType.isString()) {
            return "null";
        } else {
            return newObjectFromMapper(source, sourceValue, destinationType);
        }
    }

    /**
     * Append a statement with assures that the container variable reference
     * has an existing instance; if it does not, a new object is generated
     * using MapperFacade.newObject
     * @param fieldMap
     * @return the code to assure the variable reference's instantiation
     */
    public String assureContainerInstanceExists(FieldMap fieldMap) {
        Property destination = fieldMap.getDestination();
        Property source = fieldMap.getSource();
        if (destination.getContainer() instanceof NestedProperty) {
            VariableRef containerDestination = new VariableRef(destination.getContainer(), "destination");
            VariableRef containerSource = new VariableRef(source.getContainer(), "source");
            return assureInstanceExists(containerDestination, containerSource);
        }
        else {
            return "";
        }
    }
    
    /**
     * Append a statement which assures that the variable reference has an
     * existing instance; if it does not, a new object is generated using
     * MapperFacade.newObject
     * 
     * @param propertyRef
     *            the property or variable reference on which to check for an
     *            instance
     * @param source
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public String assureInstanceExists(VariableRef propertyRef, VariableRef source) {
        
        StringBuilder out = new StringBuilder();
        String end = "";
        if (source.isNullPossible()) {
            out.append(source.ifNotNull());
            out.append("{\n");
            end = "\n}\n";
        }
        for (final VariableRef ref : propertyRef.getPath()) {
            
            if (ref.isAssignable()) {
                append(out, format("if((%s)) { \n", ref.isNull()), ref.assign(newObject(source, ref.type())), "}");
            }
        }
        out.append(end);
        return out.toString();
    }
    
    /**
     * Appends the provided string as a source code statement, ending it with a
     * statement terminator as appropriate.
     * 
     * @param str
     * @param args
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public static String statement(String str, Object... args) {
        if (str != null && !"".equals(str.trim())) {
            String expr = format(str, args);
            String prefix = "";
            String suffix = "";
            if (!expr.startsWith("\n") || expr.startsWith("}")) {
                prefix = "\n";
            }
            String trimmed = expr.trim();
            if (!"".equals(trimmed) && !trimmed.endsWith(";") && !trimmed.endsWith("}") && !trimmed.endsWith("{") && !trimmed.endsWith("(")) {
                suffix = "; ";
            }
            return prefix + expr + suffix;
        } else if (str != null) {
            return str;
        }
        return "";
    }
    
    /**
     * Appends all of the String values provided to the StringBuilder in order,
     * as "statements"
     * 
     * @param out
     * @param statements
     */
    public static void append(StringBuilder out, String... statements) {
        for (String statement : statements) {
            out.append(statement(statement));
        }
    }
    
    /**
     * Join the items in the list together in a String, separated by the
     * provided separator
     * 
     * @param list
     * @param separator
     * @return a String which joins the items of the list
     */
    public static String join(List<?> list, String separator) {
        StringBuilder result = new StringBuilder();
        for (Object item : list) {
            result.append(item + separator);
        }
        return result.length() > 0 ? result.substring(0, result.length() - separator.length()) : "";
    }
    
    /**
     * Creates a VariableRef representing a Set<Map.Entry> for the provided
     * VariableRef (which should be a Map)
     * 
     * @param s
     *            the Map type variable ref
     * @return a new VariableRef corresponding to an EntrySet for the provided
     *         variable ref, which should be a Map type
     */
    public static VariableRef entrySetRef(VariableRef s) {
        return entrySetRef(s, s.toString());
    }

    public static VariableRef entrySetRef(VariableRef s, String value) {
        @SuppressWarnings("unchecked")
        Type<?> sourceEntryType = TypeFactory.valueOf(Set.class, MapEntry.entryType((Type<? extends Map<Object, Object>>) s.type()));
        return new VariableRef(sourceEntryType, value + ".entrySet()");
    }
    
    /**
     * @param source
     * @param dest
     * @param srcNodes
     * @param destNodes
     *            any relevant declared field mappings
     * @return a code snippet suitable to use as an equality comparison test for
     *         the provided source and destination nodes
     */
    public String currentElementComparator(Node source, Node dest, NodeList srcNodes, NodeList destNodes) {
        
        StringBuilder comparator = new StringBuilder();
        
        String or = "";
        Set<FieldMap> fieldMaps = new HashSet<FieldMap>();
        for (Node node : source.children) {
            if (node.value != null) {
                fieldMaps.add(node.value);
            }
        }
        for (Node node : dest.children) {
            if (node.value != null) {
                fieldMaps.add(node.value);
            }
        }
        
        Set<String> comparisons = new HashSet<String>();
        for (FieldMap fieldMap : fieldMaps) {
            if (!(fieldMap.is(aMultiOccurrenceElementMap()) && fieldMap.isByDefault()) && !fieldMap.isExcluded() && !fieldMap.isIgnored()) {
                
                VariableRef sourceRef = getVariableForComparison(fieldMap, srcNodes, true, source);
                VariableRef destRef = getVariableForComparison(fieldMap, destNodes, false, dest);
                
                if (sourceRef != null && destRef != null) {
                    if (!sourceRef.isValidPropertyReference(propertyResolver)) {
                        throw new IllegalStateException(sourceRef + " is not valid!!");
                    } else if (!destRef.isValidPropertyReference(propertyResolver)) {
                        throw new IllegalStateException(destRef + " is not valid!!");
                    }
                    
                    String code = this.compareFields(fieldMap, sourceRef, destRef, dest.elementRef.type(), null);
                    if (!"".equals(code) && comparisons.add(code)) {
                        comparator.append(or + "!(" + code + ")");
                        or = " || ";
                    }
                }
            }
        }
        return comparator.toString();
    }
    
    /**
     * Resolves a VariableRef instance to be used for comparison;
     * 
     * @param fieldMap
     * @param nodes
     * @param useSource
     * @param defaultParent
     * @return
     */
    private VariableRef getVariableForComparison(FieldMap fieldMap, NodeList nodes, boolean useSource, Node defaultParent) {
        
        VariableRef parentRef;
        Node destNode = Node.findFieldMap(fieldMap, nodes, useSource);
        Property prop = useSource ? fieldMap.getSource() : fieldMap.getDestination();
        if (destNode != null && destNode.parent != null) {
            parentRef = destNode.parent.elementRef;
            if (isSelfOrParentComparison(destNode.parent, defaultParent)) {
                return new VariableRef(prop.getElement(), parentRef);
            } else {
                return null;
            }
        } else if (prop.getContainer() != null) {
            parentRef = defaultParent.elementRef;
            return new VariableRef(parentRef.type(), parentRef.name());
        } else {
            /*
             * Need to check whether the defaultParent.elementRef.type() can
             * actually have a property of the specified type; if not, then it
             * must be a reference to the outermost 'source' or 'destination'
             * variables -- though we should have been able to properly detect
             * this earlier...
             */
            parentRef = defaultParent.elementRef;
            if (propertyResolver.existsProperty(parentRef.type(), prop.getExpression())) {
                return new VariableRef(prop, defaultParent.elementRef);
            } else {
                return new VariableRef(prop, useSource ? "source" : "destination");
            }
        }
    }
    
    private boolean isSelfOrParentComparison(Node node, Node reference) {
        Node parent = reference;
        while (parent != null) {
            if (parent.elementRef.equals(node.elementRef)) {
                return true;
            } else {
                parent = parent.parent;
            }
        }
        return false;
    }
    
    private Property root(Property prop) {
        Property root = prop;
        while (root.getContainer() != null) {
            root = root.getContainer();
        }
        return root;
    }
    
    /**
     * Finds all field maps out of the provided set which are associated with
     * the map passed in ( including that map itself); by "associated", we mean
     * any mappings which are connected to the original FieldMap by having a
     * matching source or destination, including transitive associations.
     * 
     * @param fieldMaps
     *            the set of all field maps
     * @param map
     *            the field map from which to start searching for reference
     * @return a Set of FieldMaps which are associated; they must be mapped in
     *         parallel
     */
    public Set<FieldMap> getAssociatedMappings(Collection<FieldMap> fieldMaps, FieldMap map) {
        
        Set<FieldMap> associated = new LinkedHashSet<FieldMap>();
        associated.add(map);
        Set<FieldMap> unprocessed = new LinkedHashSet<FieldMap>(fieldMaps);
        unprocessed.remove(map);
        
        Set<String> nextRoundSources = new LinkedHashSet<String>();
        Set<String> nextRoundDestinations = new LinkedHashSet<String>();
        Set<String> thisRoundSources = Collections.singleton(root(map.getSource()).getExpression());
        Set<String> thisRoundDestinations = Collections.singleton(root(map.getDestination()).getExpression());
        
        while (!unprocessed.isEmpty() && !(thisRoundSources.isEmpty() && thisRoundDestinations.isEmpty())) {
            
            Iterator<FieldMap> iter = unprocessed.iterator();
            while (iter.hasNext()) {
                FieldMap f = iter.next();
                boolean containsSource = thisRoundSources.contains(root(f.getSource()).getExpression());
                boolean containsDestination = thisRoundDestinations.contains(root(f.getDestination()).getExpression());
                if (containsSource && containsDestination) {
                    associated.add(f);
                    iter.remove();
                } else if (containsSource) {
                    associated.add(f);
                    iter.remove();
                    nextRoundDestinations.add(f.getDestination().getName());
                } else if (containsDestination) {
                    associated.add(f);
                    iter.remove();
                    nextRoundSources.add(f.getSource().getName());
                }
            }
            
            thisRoundSources = nextRoundSources;
            thisRoundDestinations = nextRoundDestinations;
            nextRoundSources = new LinkedHashSet<String>();
            nextRoundDestinations = new LinkedHashSet<String>();
        }
        
        return associated;
    }
    
    /**
     * Tests whether any aggregate specifications apply for the specified
     * FieldMap, and if so, adds it to the list of FieldMaps for that spec,
     * returning true. Otherwise, false is returned.
     * 
     * @param fieldMap
     * @return true if aggregate specifications should be applied to the
     *         provided field map
     */
    public boolean aggregateSpecsApply(FieldMap fieldMap) {
        for (AggregateSpecification spec : codeGenerationStrategy.getAggregateSpecifications()) {
            if (spec.appliesTo(fieldMap)) {
                List<FieldMap> fieldMaps = this.aggregateFieldMaps.get(spec);
                if (fieldMaps == null) {
                    fieldMaps = new ArrayList<FieldMap>();
                    this.aggregateFieldMaps.put(spec, fieldMaps);
                }
                fieldMaps.add(fieldMap);
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return the source code generated from applying all aggregated specs with
     *         accumulated FieldMaps to those FieldMap lists.
     */
    public String mapAggregateFields() {
        StringBuilder out = new StringBuilder();
        for (Entry<AggregateSpecification, List<FieldMap>> entry : aggregateFieldMaps.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                out.append(entry.getKey().generateMappingCode(entry.getValue(), this));
            }
        }
        this.aggregateFieldMaps.clear();
        return out.toString();
    }
    
    /**
     * Generate the code necessary to process the provided FieldMap.
     * 
     * @param fieldMap
     *            the FieldMap describing fields to be mapped
     * @param source
     *            a variable reference to the source property
     * @param destination
     *            a variable reference to the destination property
     * @return a reference to <code>this</code> CodeSourceBuilder
     */
    public String mapFields(FieldMap fieldMap, VariableRef source, VariableRef destination) {
        
        StringBuilder out = new StringBuilder();
        StringBuilder closing = new StringBuilder();

        if (destination.isAssignable() || destination.type().isMultiOccurrence() || !destination.type().isImmutable()) {
            
            if (source.isNestedProperty()) {
                out.append(source.ifPathNotNull());
                out.append("{ \n");
                closing.append("\n}");
            }

            boolean mapNulls = AbstractSpecification.shouldMapNulls(fieldMap, this);

            if (destination.isNullPathPossible()) {
                if (!source.isPrimitive()) {
                    if (!mapNulls) {
                        out.append(source.ifNotNull());
                        out.append(" {\n");
                        closing.append("\n}");
                    }
                }
                out.append(assureInstanceExists(destination, source));
            }

            Converter<Object, Object> converter = getConverter(fieldMap, fieldMap.getConverterId());
            source.setConverter(converter);

            boolean getDestinationOnMapping = AbstractSpecification.shouldGetDestinationOnMapping(fieldMap, this);
            addVariable(out, source);
            String sourceValue = source.getGlobalUniqueName();
            String destinationValue = optionallyMakeDestinationVariableAndGetValue(out, destination, getDestinationOnMapping);

            if (shouldCaptureFieldContext) {
                beginCaptureFieldContext(out, fieldMap, sourceValue, destinationValue);
            }
            StringBuilder filterClosing = new StringBuilder();
            VariableRef[] filteredProperties = applyFilters(source, destination, out, filterClosing, sourceValue, destinationValue);

            if(filteredProperties[0] != source){
                source = filteredProperties[0];
                sourceValue = source.asWrapper();
            }
            destination = filteredProperties[1];
            
            for (Specification spec : codeGenerationStrategy.getSpecifications()) {
                if (spec.appliesTo(fieldMap)) {
                    String code = spec.generateMappingCode(fieldMap, source, sourceValue, destination, this);
                    if (code == null || "".equals(code)) {
                        throw new IllegalStateException("empty code returned for spec " + spec + ", sourceProperty = " + source
                                + ", destinationProperty = " + destination);
                    }
                    out.append(code);
                    
                    break;
                }
            }
            out.append(filterClosing);
            if (shouldCaptureFieldContext) {
                endCaptureFieldContext(out);
            }
            out.append(closing.toString());
        }
        return out.toString();
    }

    private void beginCaptureFieldContext(StringBuilder out, FieldMap fieldMap, String sourceValue, String destinationValue) {
        out.append(format("mappingContext.beginMappingField(\"%s\", %s, %s, \"%s\", %s, %s);\n" + "try{\n",
                escapeQuotes(fieldMap.getSource().getExpression()), usedType(fieldMap.getAType()), sourceValue,
                escapeQuotes(fieldMap.getDestination().getExpression()), usedType(fieldMap.getBType()), destinationValue));
    }

    public void addVariable(StringBuilder out, VariableRef variable){
        String sourceVariableType = variable.isPrimitive()? variable.wrapperTypeName() : variable.typeName();
        out.append(sourceVariableType).append(" ").append(variable.getGlobalUniqueName()).append(" = ").append(variable.asWrapper()).append(";\n");
    }
    
    private void endCaptureFieldContext(StringBuilder out) {
        out.append("} finally {\n" + "\tmappingContext.endMappingField();\n" + "}\n");
    }
    
    private String escapeQuotes(String string) {
        return string.replaceAll("(?<!\\\\)\"", "\\\\\"");
    }

    public VariableRef[] applyFilters(VariableRef sourceProperty, VariableRef destinationProperty, StringBuilder out, StringBuilder closing, boolean getDestinationOnMapping) {
        return applyFilters(sourceProperty, destinationProperty, out, closing, sourceProperty.asWrapper(), (getDestinationOnMapping)?destinationProperty.asWrapper():"null");
    }

    public VariableRef[] applyFilters(VariableRef sourceProperty, VariableRef destinationProperty, StringBuilder out, StringBuilder closing,
                                      String sourceValue, String destinationValue) {
        /*
         * TODO: need code which collects all of the applicable filters and adds
         * them into an aggregate filter object
         */
        Filter<Object, Object> filter = getFilter(sourceProperty, destinationProperty);
        if (filter != null) {

            if (destinationProperty.isNestedProperty()) {
                out.append("if (");
                out.append(format("(%s && %s.shouldMap(%s, \"%s\", %s, %s, \"%s\", %s, mappingContext))", destinationProperty.pathNotNull(),
                        usedFilter(filter), usedType(sourceProperty.type()), varPath(sourceProperty), sourceValue,
                        usedType(destinationProperty.type()), varPath(destinationProperty), destinationValue));

                out.append(" || ");

                out.append(format("(%s && %s.shouldMap(%s, \"%s\", %s, %s, \"%s\", null, mappingContext))", destinationProperty.pathNull(),
                        usedFilter(filter), usedType(sourceProperty.type()), varPath(sourceProperty), sourceValue,
                        usedType(destinationProperty.type()), varPath(destinationProperty)));

                out.append(") {");
            } else {
                out.append(format("if (%s.shouldMap(%s, \"%s\", %s, %s, \"%s\", %s, mappingContext)) {", usedFilter(filter),
                        usedType(sourceProperty.type()), varPath(sourceProperty), sourceValue,
                        usedType(destinationProperty.type()), varPath(destinationProperty), destinationValue));
            }

            sourceProperty = getSourceFilter(sourceProperty, destinationProperty, filter);
            destinationProperty = getDestFilter(sourceProperty, destinationProperty, filter);

            // need to set source property
            closing.insert(0, "\n}\n");
        }
        return new VariableRef[] { sourceProperty, destinationProperty };
    }
    
    private static String varPath(VariableRef var) {
        List<VariableRef> path = var.getPath();
        if (path.isEmpty()) {
            return var.validVariableName();
        } else {
            return path.get(path.size() - 1).property().getExpression() + "." + var.validVariableName();
        }
    }
    
    /**
     * Proxies the destination property as necessary for filters that filter
     * destination values.
     * 
     * @param src
     * @param dest
     * @param filter
     * @return
     */
    private VariableRef getDestFilter(final VariableRef src, final VariableRef dest, final Filter<Object, Object> filter) {
        
        if (filter.filtersDestination()) {
            return new VariableRef(dest.property(), dest.owner()) {
                
                private String setter;
                
                @Override
                protected String setter() {
                    if (setter == null) {
                        String destinationValue = "%s";
                        if (dest.isPrimitive()) {
                            destinationValue = ClassUtil.getWrapperType(dest.rawType()).getCanonicalName() + ".valueOf(%s)";
                        }
                        String filteredValue = format("%s.filterDestination(%s, %s, \"%s\", %s, \"%s\", mappingContext)",
                                usedFilter(filter), destinationValue, usedType(src.type()), src.validVariableName(), usedType(dest.type()), dest.validVariableName()).replace(
                                "$$$", "%s");
                        
                        setter = super.setter().replace("%s", dest.cast(filteredValue));
                    }
                    return setter;
                }
            };
        }
        
        return dest;
    }
    
    /**
     * Proxies the source property as necessary for filters that filter source
     * values.
     * 
     * @param src
     * @param dest
     * @param filter
     * @return
     */
    private VariableRef getSourceFilter(final VariableRef src, final VariableRef dest, final Filter<Object, Object> filter) {
        if (filter.filtersSource()) {
            return new VariableRef(src.property(), src.owner()) {
                {
                    setConverter(src.getConverter());
                }
                
                private String getter;
                
                @Override
                protected String getter() {
                    if (getter == null) {
                        String sourceValue = super.getter();
                        if (src.isPrimitive()) {
                            sourceValue = ClassUtil.getWrapperType(src.rawType()).getCanonicalName() + ".valueOf(" + sourceValue + ")";
                        }
                        getter = src.cast(format("%s.filterSource(%s, %s, \"%s\", %s, \"%s\", mappingContext)", usedFilter(filter),
                                sourceValue, usedType(src.type()), src.validVariableName(), usedType(dest.type()), dest.validVariableName()));
                    }
                    return getter;
                }
            };
        }
        
        return src;
    }
    
    /**
     * Locates all of the filters that apply to the specified source and
     * destination properties, and creates a single aggregate filter from them,
     * which is then returned.<br>
     * If no filters apply, then null is returned.
     * 
     * @param sourceProperty
     * @param destinationProperty
     * @return
     */
    private Filter<Object, Object> getFilter(VariableRef sourceProperty, VariableRef destinationProperty) {
        
        List<Filter<Object, Object>> applicableFilters = new ArrayList<Filter<Object, Object>>();
        for (Filter<Object, Object> filter : filters) {
            if (filter.appliesTo(sourceProperty.property(), destinationProperty.property())) {
                applicableFilters.add(filter);
            }
        }
        if (applicableFilters.isEmpty()) {
            return null;
        } else if (applicableFilters.size() == 1) {
            return applicableFilters.get(0);
        } else {
            return new AggregateFilter(applicableFilters);
        }
    }
    
    /**
     * Generates source code for an "equality" comparison of two variables,
     * based on the FieldMap passed
     * 
     * @param fieldMap
     * @param sourceProperty
     * @param destinationProperty
     * @param destinationType
     * @param logDetails
     * @return the source code for equality test of the provided fields
     */
    public String compareFields(FieldMap fieldMap, VariableRef sourceProperty, VariableRef destinationProperty, Type<?> destinationType,
            StringBuilder logDetails) {
        
        StringBuilder out = new StringBuilder();
        
        out.append("(");
        if (sourceProperty.isNestedProperty()) {
            out.append(sourceProperty.pathNotNull());
            out.append(" && ");
        }
        
        if (destinationProperty.isNestedProperty()) {
            if (!sourceProperty.isPrimitive()) {
                out.append(sourceProperty.notNull());
                out.append(" && ");
            }
        }
        
        Converter<Object, Object> converter = getConverter(fieldMap, fieldMap.getConverterId());
        sourceProperty.setConverter(converter);
        
        for (Specification spec : codeGenerationStrategy.getSpecifications()) {
            if (spec.appliesTo(fieldMap)) {
                String code = spec.generateEqualityTestCode(fieldMap, sourceProperty, destinationProperty, this);
                if (code == null || "".equals(code)) {
                    throw new IllegalStateException("empty code returned for spec " + spec + ", sourceProperty = " + sourceProperty
                            + ", destinationProperty = " + destinationProperty);
                }
                out.append(code);
                break;
            }
        }
        
        out.append(")");
        return out.toString();
    }
    
    private Converter<Object, Object> getConverter(FieldMap fieldMap, String converterId) {
        Converter<Object, Object> converter = null;
        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        if (converterId != null) {
            converter = converterFactory.getConverter(converterId);
        } else {
            converter = converterFactory.getConverter(fieldMap.getSource().getType(), fieldMap.getDestination().getType());
        }
        return converter;
    }

}
