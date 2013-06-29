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
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import javassist.CannotCompileException;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy.ConstructorMapping;
import ma.glasnost.orika.impl.GeneratedObjectFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.FieldMapRecorder;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ObjectFactoryGenerator generates source code which implements an
 * ObjectFactory capable of instantiating a given target type.
 */
public class ObjectFactoryGenerator {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(ObjectFactoryGenerator.class);
    
    private final ConstructorResolverStrategy constructorResolverStrategy;
    private final MapperFactory mapperFactory;
    private final String nameSuffix;
    
    /**
     * Creates a new ObjectFactoryGenerator instance
     * 
     * @param mapperFactory
     * @param constructorResolverStrategy
     * @param compilerStrategy
     */
    public ObjectFactoryGenerator(MapperFactory mapperFactory, ConstructorResolverStrategy constructorResolverStrategy,
            CompilerStrategy compilerStrategy) {
        this.mapperFactory = mapperFactory;
        this.nameSuffix = String.valueOf(System.nanoTime());
        this.constructorResolverStrategy = constructorResolverStrategy;
    }
    
    /**
     * @param destinationType the destination type for which the ObjectFactory will generate
     * instances
     * @param sourceType
     * @param context
     * @return an instance of the newly generated ObjectFactory
     */
    public GeneratedObjectFactory build(Type<?> destinationType, Type<?> sourceType, MappingContext context) {
        
        final String className = destinationType.getSimpleName() + "_" + sourceType.getSimpleName() + "_ObjectFactory" + nameSuffix;
        
        try {
            MapperKey mapperKey = new MapperKey(sourceType, destinationType);
            ClassMap<Object, Object> classMap = mapperFactory.getClassMap(mapperKey);
            
            if (classMap == null) {
                classMap = mapperFactory.getClassMap(new MapperKey(destinationType, sourceType));
            }
            if (!sourceType.equals(classMap.getAType())) {
                classMap = classMap.flip();
            }
            
            
            FieldMapRecorder recorder = new FieldMapRecorder(classMap, LOGGER.isDebugEnabled());
            if (recorder.isDebugEnabled()) {
                recorder.getLogDetails().append("Generating new object factory for (" + destinationType + ")");
            } 
            
            final SourceCodeContext factoryCode = new SourceCodeContext(className, GeneratedObjectFactory.class, context, recorder);
            
            UsedTypesContext usedTypes = new UsedTypesContext();
            UsedConvertersContext usedConverters = new UsedConvertersContext();
            UsedMapperFacadesContext usedMapperFacades = new UsedMapperFacadesContext();
            
            addCreateMethod(factoryCode, usedTypes, usedConverters, usedMapperFacades, classMap, context, recorder);
            
            GeneratedObjectFactory objectFactory = (GeneratedObjectFactory) factoryCode.getInstance();
            objectFactory.setMapperFacade(mapperFactory.getMapperFacade());
            
            if (recorder.isDebugEnabled()) {
                LOGGER.debug(recorder.getLogDetails().toString());
            }
            
            return objectFactory;
            
        } catch (final Exception e) {
            throw new MappingException("exception while creating object factory for " + destinationType.getName(), e);
        }
    }
    
    private void addCreateMethod(SourceCodeContext code, UsedTypesContext usedTypes, UsedConvertersContext usedConverters,
            UsedMapperFacadesContext usedMappers, ClassMap<?, ?> classMap, MappingContext mappingContext, FieldMapRecorder recorder)
            throws CannotCompileException {
        
        final StringBuilder out = new StringBuilder();
        out.append("public Object create(Object s, " + MappingContext.class.getCanonicalName() + " mappingContext) {");
        out.append(format("if(s == null) throw new %s(\"source object must be not null\");",
                IllegalArgumentException.class.getCanonicalName()));
        
        out.append(addSourceClassConstructor(code, classMap, mappingContext, recorder));
        
        out.append(addUnmatchedSourceHandler(code, classMap.getBType(), mappingContext, recorder));
        
        out.append("\n}");
        
        code.addMethod(out.toString());
    }
    
    /**
     * @param code
     * @param destinationType
     * @param sourceType
     * @param mappingContext
     * @param logDetails
     * @return
     */
    private String addSourceClassConstructor(SourceCodeContext code, ClassMap<?, ?> classMap, MappingContext mappingContext,
            FieldMapRecorder recorder) {
        
        StringBuilder out = new StringBuilder();
        Type<?> sourceType = classMap.getAType();
        Type<?> destinationType = classMap.getBType();
        
        if (destinationType.isArray()) {
            out.append(addArrayClassConstructor(code, classMap, classMap.getFieldsMapping().size()));
        } else {
            
            ConstructorMapping<?> constructorMapping = (ConstructorMapping<?>) constructorResolverStrategy.resolve(classMap, destinationType);
            Constructor<?> constructor = constructorMapping.getConstructor();
            
            if (constructor == null) {
                throw new IllegalArgumentException("no suitable constructors found for " + destinationType);
            } else if (recorder.isDebugEnabled()) {
                recorder.append("\n\tUsing constructor: " + constructor);
            }
            
            List<FieldMap> properties = constructorMapping.getMappedFields();
            Type<?>[] constructorArguments = constructorMapping.getParameterTypes();
            
            int argIndex = 0;
            
            out.append(format("if (s instanceof %s) {", sourceType.getCanonicalName()));
            out.append(format("%s source = (%s) s;", sourceType.getCanonicalName(), sourceType.getCanonicalName()));
            out.append("\ntry {\n");
            argIndex = 0;
            
            for (FieldMap fieldMap : properties) {
                VariableRef v = new VariableRef(constructorArguments[argIndex], "arg" + argIndex++);
                VariableRef s = new VariableRef(fieldMap.getSource(), "source");
                VariableRef destOwner = new VariableRef(fieldMap.getDestination(), "");
                v.setOwner(destOwner);
                out.append(statement(v.declare()));
                out.append(code.mapFields(fieldMap, s, v, destinationType, recorder));
            }
            
            out.append(format("return new %s(", destinationType.getCanonicalName()));
            for (int i = 0; i < properties.size(); i++) {
                out.append(format("arg%d", i));
                if (i < properties.size() - 1) {
                    out.append(",");
                }
            }
            out.append(");");
            
            /*
             * Any exceptions thrown calling constructors should be propagated
             */
            append(out, "\n} catch (java.lang.Exception e) {\n", "if (e instanceof RuntimeException) {\n", "throw (RuntimeException)e;\n",
                    "} else {", "throw new java.lang.RuntimeException(" + "\"Error while constructing new " + destinationType.getSimpleName()
                            + " instance\", e);", "\n}\n}\n}");
        }
        return out.toString();
    }
    
    /**
     * Adds a default constructor call (where possible) as fail-over case when
     * no specific source type has been matched.
     * 
     * @param code
     * @param type
     * @param mappingContext
     * @param logDetails
     * @return
     */
    private String addUnmatchedSourceHandler(SourceCodeContext code, Type<?> type, MappingContext mappingContext, FieldMapRecorder recorder) {
        StringBuilder out = new StringBuilder();
        for (Constructor<?> constructor : type.getRawType().getConstructors()) {
            if (constructor.getParameterTypes().length == 0 && Modifier.isPublic(constructor.getModifiers())) {
                out.append(format("return new %s();", type.getCanonicalName()));
                break;
            }
        }
        
        if (out.length() == 0) {
            // TODO: can this condition be reached?
            // if object factory generation failed, we should not create the factory
            // which is unable to construct an instance of anything.
            out.append(format(
                    "throw new %s(s.getClass().getCanonicalName() + \" is an unsupported source class : \"+s.getClass().getCanonicalName());",
                    IllegalArgumentException.class.getCanonicalName()));
        }
        
        return out.toString();
    }
    
    /**
     * @param code
     * @param classMap
     * @param size
     * @return
     */
    private String addArrayClassConstructor(SourceCodeContext code, ClassMap<?, ?> classMap, int size) {
        return format("if (s instanceof %s) {", classMap.getAType().getCanonicalName()) + "return new "
                + classMap.getBType().getRawType().getComponentType().getCanonicalName() + "[" + size + "];" + "\n}";
    }
}
