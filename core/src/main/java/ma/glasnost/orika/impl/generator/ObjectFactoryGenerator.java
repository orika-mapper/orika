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
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy.ConstructorMapping;
import ma.glasnost.orika.impl.GeneratedObjectFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectFactoryGenerator {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(ObjectFactoryGenerator.class);
    
    private final ConstructorResolverStrategy constructorResolverStrategy;
    private final MapperFactory mapperFactory;
    private final String nameSuffix;
    
    public ObjectFactoryGenerator(MapperFactory mapperFactory, ConstructorResolverStrategy constructorResolverStrategy,
    		CompilerStrategy compilerStrategy) {
        this.mapperFactory = mapperFactory;
        this.nameSuffix = String.valueOf(System.nanoTime());
        this.constructorResolverStrategy = constructorResolverStrategy;
    }
    
    public GeneratedObjectFactory build(Type<?> type, MappingContext context) {
        
        final String className = type.getSimpleName() + "_ObjectFactory" + nameSuffix;
        
        try {
            StringBuilder logDetails;
            if (LOGGER.isDebugEnabled()) {
            	logDetails = new StringBuilder();
            	logDetails.append("Generating new object factory for (" + type +")");
            } else {
            	logDetails = null;
            }
            
            final SourceCodeContext factoryCode = 
                    new SourceCodeContext(className,GeneratedObjectFactory.class, context, logDetails);
            
            UsedTypesContext usedTypes = new UsedTypesContext();
            UsedConvertersContext usedConverters = new UsedConvertersContext();
            UsedMapperFacadesContext usedMapperFacades = new UsedMapperFacadesContext();
            
            addCreateMethod(factoryCode, usedTypes, usedConverters, usedMapperFacades, type, context, logDetails);
            
            GeneratedObjectFactory objectFactory = (GeneratedObjectFactory) factoryCode.getInstance();
            objectFactory.setMapperFacade(mapperFactory.getMapperFacade());
            
            if (logDetails != null) {
            	LOGGER.debug(logDetails.toString());
            }
            
            return objectFactory;
            
        } catch (final Exception e) {
            throw new MappingException("exception while creating object factory for " + type.getName(), e);
        } 
    }
    
    private void addCreateMethod(SourceCodeContext code, UsedTypesContext usedTypes, 
    		UsedConvertersContext usedConverters, UsedMapperFacadesContext usedMappers, 
    		Type<?> type, MappingContext mappingContext, StringBuilder logDetails) throws CannotCompileException {
    	
        final StringBuilder out = new StringBuilder();
        out.append("public Object create(Object s, " + MappingContext.class.getCanonicalName() + " mappingContext) {");
        out.append(format("if(s == null) throw new %s(\"source object must be not null\");", IllegalArgumentException.class.getCanonicalName()));
        
        Set<Type<? extends Object>> sourceClasses = mapperFactory.lookupMappedClasses(type);
        
        if (sourceClasses != null && !sourceClasses.isEmpty()) {
            for (Type<? extends Object> sourceType : sourceClasses) {
                out.append(addSourceClassConstructor(code, type, sourceType, mappingContext, logDetails));
            }
        } else {
            throw new MappingException("Cannot generate ObjectFactory for " + type);
        }
        
        // TODO: can this condition be reached?
        // if object factory generation failed, we should not create the factory
        // which is unable to construct an instance of anything.
        out.append(format("throw new %s(s.getClass().getCanonicalName() + \" is an unsupported source class : \"+s.getClass().getCanonicalName());",
                IllegalArgumentException.class.getCanonicalName()));
        out.append("\n}");
        
        code.addMethod(out.toString());
    }
    
    private String addSourceClassConstructor(SourceCodeContext code, Type<?> type, Type<?> sourceType, MappingContext mappingContext, StringBuilder logDetails) {
        
        MapperKey mapperKey = new MapperKey(type,sourceType);
        ClassMap<Object, Object>  classMap = mapperFactory.getClassMap(mapperKey); 
        
        if (classMap==null) {
        	classMap = mapperFactory.getClassMap(new MapperKey(sourceType,type));
        }
        
        StringBuilder out = new StringBuilder();
        
        if (type.isArray()) {
            out.append(addArrayClassConstructor(code, type, sourceType, classMap.getFieldsMapping().size()));
        } else {
        
            ConstructorMapping<?> constructorMapping = (ConstructorMapping<?>) constructorResolverStrategy.resolve(classMap, type);
            Constructor<?> constructor = constructorMapping.getConstructor();
            
            if (constructor == null) {
                throw new IllegalArgumentException("no constructors found for " + type);
            } else if (logDetails != null) {
            	logDetails.append("\n\tUsing constructor: " + constructor);
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
                out.append(code.mapFields(fieldMap, s, v, type, logDetails));
            }
            
            out.append(format("return new %s(", type.getCanonicalName()));
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
            append(out,
                    "\n} catch (java.lang.Exception e) {\n",
                    "if (e instanceof RuntimeException) {\n",
                    "throw (RuntimeException)e;\n",
                    "} else {",
                    "throw new java.lang.RuntimeException(" +
            		"\"Error while constructing new " + type.getSimpleName() + " instance\", e);",
            		"\n}\n}\n}");
        }
        return out.toString();
    }

    /**
     * @param type
     * @param size
     */
    private String addArrayClassConstructor(SourceCodeContext code, Type<?> type, Type<?> sourceType, int size) {
        return
                format("if (s instanceof %s) {", sourceType.getCanonicalName()) +
                "return new " + type.getRawType().getComponentType().getCanonicalName() + "[" + size + "];" +
                "\n}";
    }
}
