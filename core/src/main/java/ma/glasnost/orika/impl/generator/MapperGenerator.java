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

package ma.glasnost.orika.impl.generator;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.append;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javassist.CannotCompileException;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.GeneratedMapperBase;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.FieldMapRecorder;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mattdeboer
 *
 */
public final class MapperGenerator {
    
    private static Logger LOGGER = LoggerFactory.getLogger(MapperGenerator.class);
    
    private final MapperFactory mapperFactory;
    private final CompilerStrategy compilerStrategy;
    
    private final Comparator<FieldMap> FIELD_MAP_COMPARATOR = new Comparator<FieldMap>() {
        public int compare(FieldMap f0, FieldMap f1) {
            int comparison = f0.getDestination().getPath().length - f1.getDestination().getPath().length;
            if (comparison == 0) {
                comparison = f0.getDestinationExpression().compareTo(f1.getDestinationExpression());
            }
            return comparison;
        }
    };
    
    
    /**
     * @param mapperFactory
     * @param compilerStrategy
     */
    public MapperGenerator(MapperFactory mapperFactory, CompilerStrategy compilerStrategy) {
        this.mapperFactory = mapperFactory;
        this.compilerStrategy = compilerStrategy;
    }
    
    /**
     * Builds the Mapper as defined by the provided ClassMap
     * 
     * @param classMap
     * @param context
     * @return the newly generated instance of GeneratedMapperBase
     */
    public GeneratedMapperBase build(ClassMap<?, ?> classMap, MappingContext context) {
        
        try {
            compilerStrategy.assureTypeIsAccessible(classMap.getAType().getRawType());
            compilerStrategy.assureTypeIsAccessible(classMap.getBType().getRawType());
            
            FieldMapRecorder recorder = new FieldMapRecorder(classMap, LOGGER.isDebugEnabled());
            if (LOGGER.isDebugEnabled()) {
            	recorder.getLogDetails().append("Generating new mapper for (" + classMap.getAType()+", " + classMap.getBTypeName() +")");
            }
            
            final SourceCodeContext mapperCode = new SourceCodeContext(
                    classMap.getMapperClassName(), GeneratedMapperBase.class, context, recorder);
            
            addMapMethod(mapperCode, true, classMap, recorder);
            recorder.flip();
            addMapMethod(mapperCode, false, classMap, recorder);
            
            GeneratedMapperBase instance = mapperCode.getInstance();
            instance.setAType(classMap.getAType());
            instance.setBType(classMap.getBType());
            
            if (recorder != null) {
            	LOGGER.debug(recorder.getLogDetails().toString());
            }
            
            return instance;
            
        } catch (final Exception e) {
            throw new MappingException(e);
        }
    }
    
    /**
     * Adds the mapping method in the specified direction for the current ClassMap
     * 
     * @param code
     * @param aToB
     * @param classMap
     * @param recorder
     * @throws CannotCompileException
     */
    private void addMapMethod(SourceCodeContext code, boolean aToB, ClassMap<?, ?> classMap, FieldMapRecorder recorder) throws CannotCompileException {
        
    	if (LOGGER.isDebugEnabled()) {
        	if (aToB) {
        		recorder.getLogDetails().append("\n\t" +code.getClassSimpleName() + ".mapAToB("+ classMap.getAType()+", " + classMap.getBTypeName() +") {");
        	} else {
        		recorder.getLogDetails().append("\n\t" +code.getClassSimpleName() + ".mapBToA("+ classMap.getBType()+", " + classMap.getATypeName() +") {");
        	}
        }
    	
    	final StringBuilder out = new StringBuilder();
        final String mapMethod = "map" + (aToB ? "AtoB" : "BtoA");
        out.append("\tpublic void ");
        out.append(mapMethod);
        out.append(format("(java.lang.Object a, java.lang.Object b, %s mappingContext) {\n\n", MappingContext.class.getCanonicalName()));
        
        VariableRef source;
        VariableRef destination;
        if (aToB) {
            source = new VariableRef(classMap.getAType(), "source");
            destination = new VariableRef(classMap.getBType(), "destination"); 
        } else {
            source = new VariableRef(classMap.getBType(), "source");
            destination = new VariableRef(classMap.getAType(), "destination");
        }
         
        append(out,
                format("super.%s(a, b, mappingContext);", mapMethod),
                "\n\n",
                source.declare("a"),
                destination.declare("b"),
                "\n\n");
        
        Set<FieldMap> orderedFieldMaps = orderFieldMaps(classMap.getFieldsMapping(), !aToB);
        for (FieldMap fieldMap : orderedFieldMaps) {
            
            if (fieldMap.isExcluded()) {
                recorder.excludeWithReason(fieldMap, "it was marked excluded in the class-map");
                continue;
            }
            
            if (isAlreadyExistsInUsedMappers(fieldMap, classMap)) {
            	recorder.excludeWithReason(fieldMap, "it is already handled by another mapper in this hierarchy");
            	continue;
            }
            
            if (code.aggregateSpecsApply(fieldMap)) {
                continue;
            }
            
            if (!fieldMap.isIgnored()) {
                try {
                    beginMappingField(out, fieldMap, code);
                    
                    String sourceCode = generateFieldMapCode(code, fieldMap, classMap, destination, recorder);
                    out.append(sourceCode);
                    
                    endMappingField(out, fieldMap, code);
                    
                } catch (final Exception e) {
                    MappingException me = new MappingException(e);
                    me.setSourceProperty(fieldMap.getSource());
                    me.setDestinationProperty(fieldMap.getDestination());
                    me.setSourceType(source.type());
                    me.setDestinationType(destination.type());
                    throw me;
                }
            } else {
                recorder.excludeWithReason(fieldMap, "it is ignored for this mapping direction");
            }
        }
        
        out.append(code.mapAggregateFields());
                
        out.append("\n\t\tif(customMapper != null) { \n\t\t\t customMapper.")
                .append(mapMethod)
                .append("(source, destination, mappingContext);\n\t\t}");
        
        out.append("\n\t}");
        
        if (LOGGER.isDebugEnabled()) {
            recorder.getLogDetails().append("\n\t}");
        }
        
        code.addMethod(out.toString());
    }
    
    /**
     * Generates a comment marking the beginning of code for mapping the specified field
     * 
     * @param out the source code output
     * @param fieldMap the FieldMap for which code is generated
     * @param code the current context
     */
    private void beginMappingField(StringBuilder out, FieldMap fieldMap, SourceCodeContext code) {
        if (code.writesSourceFiles()) {
            out.append("\n/* -------------- BEGIN --| " + fieldMap.getSourceExpression() + " => " + 
                    fieldMap.getDestinationExpression() + " -- */\n");
        }
    }
    
    /**
     * Generates a comment marking the end of code for mapping the specified field
     * 
     * @param out the source code output
     * @param fieldMap the FieldMap for which code is generated
     * @param code the current context
     */
    private void endMappingField(StringBuilder out, FieldMap fieldMap, SourceCodeContext code) {
        if (code.writesSourceFiles()) {
            out.append("\n/* -------------- END ----| " + fieldMap.getSourceExpression() + " => " + 
                    fieldMap.getDestinationExpression() + " -- */\n");
        }
    }
    
    /**
     * Orders the fieldsMaps based on their destination type, assuring that
     * nested destination fields are ordered last.<p>
     * The direction of the fields is reversed when requested.
     * 
     * @param fieldMaps the FieldMap instances to be ordered
     * @param reverse specifies whether the direction of the FieldMaps should be reversed
     * @return
     */
    private Set<FieldMap> orderFieldMaps(Set<FieldMap> fieldMaps, boolean reverse) {
        Set<FieldMap> ordered = new TreeSet<FieldMap>(FIELD_MAP_COMPARATOR);
        if (reverse) {
            for (FieldMap fieldMap: fieldMaps) {
                ordered.add(fieldMap.flip());
            }
        } else {
            ordered.addAll(fieldMaps);
        }
        return ordered;
    }

    private boolean isAlreadyExistsInUsedMappers(FieldMap fieldMap, ClassMap<?, ?> classMap) {
        
        Set<ClassMap<Object, Object>> usedClassMapSet = mapperFactory.lookupUsedClassMap(new MapperKey(classMap.getAType(),
                classMap.getBType()));
        
        if (!fieldMap.isByDefault()) {
        	return false;
        }
        
        for (ClassMap<Object, Object> usedClassMap : usedClassMapSet) {
            for(FieldMap usedFieldMap: usedClassMap.getFieldsMapping()) {
            	if (usedFieldMap.getSource().equals(fieldMap.getSource())
            			&& usedFieldMap.getDestination().equals(fieldMap.getDestination())) {
            		return true;
            	}
            }
        }
        
        return false;
    }
    
    private String generateFieldMapCode(SourceCodeContext code, FieldMap fieldMap, ClassMap<?, ?> classMap, VariableRef destination, FieldMapRecorder recorder) throws Exception {
        
        final VariableRef sourceProperty = new VariableRef(fieldMap.getSource(), "source");
        final VariableRef destinationProperty = new VariableRef(fieldMap.getDestination(), "destination");
        destinationProperty.setOwner(destination);
        
        if (!sourceProperty.isReadable() || ((!destinationProperty.isAssignable()) && !destinationProperty.isCollection() && !destinationProperty.isArray() && !destinationProperty.isMap())) {
            if (recorder.isDebugEnabled()) {
                
    			if (!sourceProperty.isReadable()) {
    			    Type<?> sourceType = classMap.getAType().equals(destination.type()) ? classMap.getBType() : classMap.getAType();
    			    recorder.excludeWithReason(fieldMap, sourceType + "." + fieldMap.getSource().getName() + "(" + fieldMap.getSource().getType() + ") is not readable");
    			} else {
    				// TODO: this brings up an important case: sometimes the destination is not assignable, 
    				// but it's properties can still be mapped in-place. Should we handle it?
    			    // We should be able to test whether the field is immutable
    			    
    			    recorder.excludeWithReason(fieldMap, destination.type() + "." + fieldMap.getDestination().getName() + "(" + fieldMap.getDestination().getType() + ") is neither assignable nor an array, collection, or map");
    			}		
            }
        	return "";
        }
        
        // Make sure the source and destination types are accessible to the builder
        compilerStrategy.assureTypeIsAccessible(sourceProperty.rawType());
        compilerStrategy.assureTypeIsAccessible(destinationProperty.rawType());

        return code.mapFields(fieldMap, sourceProperty, destinationProperty, destination.type(), recorder);
    }
    
}
