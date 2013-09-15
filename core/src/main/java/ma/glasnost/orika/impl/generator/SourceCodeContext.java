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

import static java.lang.String.format;
import static ma.glasnost.orika.impl.Specifications.aMultiOccurrenceElementMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javassist.CannotCompileException;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Filter;
import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.Properties;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.AggregateFilter;
import ma.glasnost.orika.impl.GeneratedObjectBase;
import ma.glasnost.orika.impl.generator.CompilerStrategy.SourceCodeGenerationException;
import ma.glasnost.orika.impl.generator.Node.NodeList;
import ma.glasnost.orika.impl.generator.UsedMapperFacadesContext.UsedMapperFacadesIndex;
import ma.glasnost.orika.impl.generator.specification.AbstractSpecification;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.property.PropertyResolverStrategy;

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
        
        String safeBaseClassName = baseClassName.replace("[]", "$Array");
        this.sourceBuilder = new StringBuilder();
        this.superClass = superClass;
        
        int namePos = safeBaseClassName.lastIndexOf(".");
        if (namePos > 0) {
            this.packageName = safeBaseClassName.substring(0, namePos - 1);
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
     * @throws CannotCompileException
     * @throws IOException
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
     * @throws CannotCompileException
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public <T extends GeneratedObjectBase> T getInstance() throws SourceCodeGenerationException, InstantiationException,
            IllegalAccessException {
        
        T instance = (T) compileClass().newInstance();
        
        Type<Object>[] usedTypesArray = usedTypes.toArray();
        Converter<Object, Object>[] usedConvertersArray = usedConverters.toArray();
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
        return newObjectFromMapper(source.type(), destinationType) + "(" + source.asWrapper() + ", mappingContext)";
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
        if (destinationType.isPrimitive()) {
            return VariableRef.getDefaultValue(destinationType.getRawType());
        } else if (destinationType.isString()) {
            return "null";
        } else {
            return newObjectFromMapper(source, destinationType);
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
        @SuppressWarnings("unchecked")
        Type<?> sourceEntryType = TypeFactory.valueOf(Set.class, MapEntry.entryType((Type<? extends Map<Object, Object>>) s.type()));
        return new VariableRef(sourceEntryType, s + ".entrySet()");
    }
    
    /**
     * @param source
     * @param dest
     * @param srcNodes
     * @param destNodes
     * @return a code snippet suitable to use as an equality comparison test for
     *         the provided source and destination nodes
     */
    public String currentElementComparator(Node source, Node dest, NodeList srcNodes, NodeList destNodes) {
        
        StringBuilder comparator = new StringBuilder();
        
        MapperKey key = new MapperKey(source.elementRef.type(), dest.elementRef.type());
        ClassMap<?, ?> classMap = mapperFactory.getClassMap(key);
        if (classMap == null) {
            classMap = mapperFactory.classMap(key.getAType(), key.getBType()).byDefault().toClassMap();
        }
        
        String or = "";
        Set<FieldMap> fieldMaps = new HashSet<FieldMap>(classMap.getFieldsMapping());
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
                
                Node srcNode = Node.findFieldMap(fieldMap, srcNodes, true);
                if (srcNode != null && srcNode.parent != null) {
                    srcNode = srcNode.parent;
                } else {
                    srcNode = source;
                }
                
                Node destNode = Node.findFieldMap(fieldMap, destNodes, false);
                if (destNode != null && destNode.parent != null) {
                    destNode = destNode.parent;
                } else {
                    destNode = dest;
                }
                
                Type<?> sourceType = source.elementRef.type();
                Type<?> destType = dest.elementRef.type();
                
                try {
                    propertyResolver.getProperty(sourceType, fieldMap.getSource().getName());
                    propertyResolver.getProperty(destType, fieldMap.getDestination().getName());
                    
                    VariableRef s = new VariableRef(fieldMap.getSource(), srcNode.elementRef);
                    VariableRef d = new VariableRef(fieldMap.getDestination(), destNode.elementRef);
                    String code = this.compareFields(fieldMap, s, d, key.getBType(), null);
                    if (!"".equals(code) && comparisons.add(code)) {
                        comparator.append(or + "!(" + code + ")");
                        or = " || ";
                    }
                } catch (Exception e) {
                    
                }
            }
        }
        return comparator.toString();
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
     * @return true if aggregate specifications should be applied to the provided field map
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
     * @param sourceProperty
     *            a variable reference to the source property
     * @param destinationProperty
     *            a variable reference to the destination property
     * @param destinationType
     *            the destination's type
     * @param logDetails
     *            a StringBuilder to contain the debug output
     * @return a reference to <code>this</code> CodeSourceBuilder
     */
    public String mapFields(FieldMap fieldMap, VariableRef sourceProperty, VariableRef destinationProperty, Type<?> destinationType,
            StringBuilder logDetails) {
        
        StringBuilder out = new StringBuilder();
        StringBuilder closing = new StringBuilder();
        
        if (destinationProperty.isAssignable() || destinationProperty.type().isMultiOccurrence()) {
            
            if (sourceProperty.isNestedProperty()) {
                out.append(sourceProperty.ifPathNotNull());
                out.append("{ \n");
                closing.append("\n}");
            }
            
            boolean mapNulls = AbstractSpecification.shouldMapNulls(fieldMap, this);
            
            if (destinationProperty.isNestedProperty()) {
                if (!sourceProperty.isPrimitive()) {
                    if (!mapNulls) {
                        out.append(sourceProperty.ifNotNull());
                        out.append(" {\n");
                        closing.append("\n}");
                    }
                }
                out.append(assureInstanceExists(destinationProperty, sourceProperty));
            }
            
            Converter<Object, Object> converter = getConverter(fieldMap, fieldMap.getConverterId());
            sourceProperty.setConverter(converter);
            
            VariableRef[] filteredProperties = applyFilters(sourceProperty, destinationProperty, out, closing);
            sourceProperty = filteredProperties[0];
            destinationProperty = filteredProperties[1];
            
            for (Specification spec : codeGenerationStrategy.getSpecifications()) {
                if (spec.appliesTo(fieldMap)) {
                    String code = spec.generateMappingCode(fieldMap, sourceProperty, destinationProperty, this);
                    if (code == null || "".equals(code)) {
                        throw new IllegalStateException("empty code returned for spec " + spec + ", sourceProperty = " + sourceProperty
                                + ", destinationProperty = " + destinationProperty);
                    }
                    out.append(code);
                    break;
                }
            }
            
            out.append(closing.toString());
        }
        return out.toString();
    }

    public VariableRef[] applyFilters(VariableRef sourceProperty, VariableRef destinationProperty, StringBuilder out, StringBuilder closing) {
        /*
         * TODO: need code which collects all of the applicable filters and
         * adds them into an aggregate filter object
         */
        Filter<Object, Object> filter = getFilter(sourceProperty, destinationProperty);
        if (filter != null) {
            out.append(format("if (%s.shouldMap(%s, \"%s\", %s, %s, \"%s\", mappingContext)) {",
                    usedFilter(filter),
                    usedType(sourceProperty.type()),
                    varPath(sourceProperty),
                    sourceProperty.asWrapper(),
                    usedType(destinationProperty.type()),
                    varPath(destinationProperty)));
            
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
            return var.name();
        } else {
            return path.get(path.size() - 1).property().getExpression() + "." + var.name();
        }
    }
    
    /**
     * Proxies the destination property as necessary for filters that filter
     * destination values.
     * 
     * @param destinationProperty
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
                        String filteredValue = format("%s.filterDestination(%s, %s, \"%s\", %s, \"%s\", mappingContext)", usedFilter(filter),
                                destinationValue, usedType(src.type()), src.name(), usedType(dest.type()), dest.name()).replace("$$$", "%s");
                        
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
     * @param sourceProperty
     * @param filter
     * @return
     */
    private VariableRef getSourceFilter(final VariableRef src, final VariableRef dest, final Filter<Object, Object> filter) {
        if (filter.filtersSource()) {
            return new VariableRef(src.property(), src.owner()) {
                
                private String getter;
                
                @Override
                protected String getter() {
                    if (getter == null) {
                        getter = src.cast(format("%s.filterSource(%s, %s, \"%s\", %s, \"%s\", mappingContext)", usedFilter(filter),
                                super.asWrapper(), usedType(src.type()), src.name(), usedType(dest.type()), dest.name()));
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
            out.append(sourceProperty.ifPathNotNull());
            out.append(" && ");
        }
        
        if (destinationProperty.isNestedProperty()) {
            if (!sourceProperty.isPrimitive()) {
                out.append(sourceProperty.notNull());
                out.append(" && ");
            }
            out.append(assureInstanceExists(destinationProperty, sourceProperty));
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
