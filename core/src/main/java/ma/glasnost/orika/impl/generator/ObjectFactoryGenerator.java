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
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.util.ClassHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.append;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;

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
     * @param type
     * @param sourceType
     * @param context
     * @return an instance of the newly generated ObjectFactory
     */
    public GeneratedObjectFactory build(Type<?> type, Type<?> sourceType, MappingContext context) {

        String className = type.getSimpleName() + "_" + sourceType.getSimpleName() + "_ObjectFactory" + nameSuffix;
        try {
            StringBuilder logDetails;
            if (LOGGER.isDebugEnabled()) {
                logDetails = new StringBuilder();
                logDetails.append("Generating new object factory for (").append(type).append(")");
            } else {
                logDetails = null;
            }

            Class<?> packageNeighbour = ClassHelper.getPackageNeighbour(type, sourceType);
            final SourceCodeContext factoryCode = new SourceCodeContext(className,
                    packageNeighbour,
                    GeneratedObjectFactory.class, context, logDetails);

            UsedTypesContext usedTypes = new UsedTypesContext();
            UsedConvertersContext usedConverters = new UsedConvertersContext();
            UsedMapperFacadesContext usedMapperFacades = new UsedMapperFacadesContext();

            addCreateMethod(factoryCode, usedTypes, usedConverters, usedMapperFacades, type, sourceType, context, logDetails);

            GeneratedObjectFactory objectFactory = (GeneratedObjectFactory) factoryCode.getInstance();
            objectFactory.setMapperFacade(mapperFactory.getMapperFacade());

            if (logDetails != null) {
                LOGGER.debug(logDetails.toString());
            }

            return objectFactory;

        } catch (final Exception e) {
            if (e instanceof MappingException) {
                throw (MappingException) e;
            } else {
                throw new MappingException("exception while creating object factory for " + type.getName(), e);
            }
        }
    }

    private static String getPackageName(Type<?> type) {
        Package typePackage = type.getRawType().getPackage();
        return typePackage == null ? "" : typePackage.getName();
    }

    private void addCreateMethod(SourceCodeContext code, UsedTypesContext usedTypes, UsedConvertersContext usedConverters,
                                 UsedMapperFacadesContext usedMappers, Type<?> type, Type<?> sourceType, MappingContext mappingContext, StringBuilder logDetails) {

        final StringBuilder out = new StringBuilder();
        out.append("public Object create(Object s, ").append(MappingContext.class.getCanonicalName()).append(" mappingContext) {");
        out.append(format("if(s == null) throw new %s(\"source object must be not null\");",
                IllegalArgumentException.class.getCanonicalName()));

        out.append(addSourceClassConstructor(code, type, sourceType, mappingContext, logDetails));
        out.append(addUnmatchedSourceHandler(code, type, sourceType, mappingContext, logDetails));

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
    private String addSourceClassConstructor(SourceCodeContext code, Type<?> destinationType, Type<?> sourceType,
                                             MappingContext mappingContext, StringBuilder logDetails) {

        MapperKey mapperKey = new MapperKey(sourceType, destinationType);
        ClassMap<Object, Object> classMap = mapperFactory.getClassMap(mapperKey);

        if (classMap == null) {
            classMap = mapperFactory.getClassMap(new MapperKey(destinationType, sourceType));
        }

        StringBuilder out = new StringBuilder();
        if (classMap != null) {
            if (destinationType.isArray()) {
                out.append(addArrayClassConstructor(code, destinationType, sourceType, classMap.getFieldsMapping().size()));
            } else {

                out.append(format("if (s instanceof %s) {", sourceType.getCanonicalName()));
                out.append(format("%s source = (%s) s;", sourceType.getCanonicalName(), sourceType.getCanonicalName()));
                out.append("\ntry {\n");

                ConstructorMapping<?> constructorMapping = (ConstructorMapping<?>) constructorResolverStrategy.resolve(classMap,
                        destinationType);
                Constructor<?> constructor = constructorMapping.getConstructor();

                if (constructor == null) {
                    throw new IllegalArgumentException("no suitable constructors found for " + destinationType);
                } else if (logDetails != null) {
                    logDetails.append("\n\tUsing constructor: ").append(constructor);
                }

                List<FieldMap> properties = constructorMapping.getMappedFields();
                Type<?>[] constructorArguments = constructorMapping.getParameterTypes();

                if (constructorArguments == null || properties.size() != constructorArguments.length) {
                    throw new MappingException("While attempting to generate ObjectFactory using constructor '" + constructor
                            + "', an automatic mapping of the source type ('" + sourceType
                            + "') to this constructor call could not be determined. Please "
                            + "register a custom ObjectFactory implementation which is able to create an instance of '" + destinationType
                            + "' from an instance of '" + sourceType + "'.");
                }

                int argIndex = 0;

                argIndex = 0;

                for (FieldMap fieldMap : properties) {
                    VariableRef v = new VariableRef(constructorArguments[argIndex], "arg" + argIndex++);
                    VariableRef s = new VariableRef(fieldMap.getSource(), "source");
                    VariableRef destOwner = new VariableRef(fieldMap.getDestination(), "");
                    v.setOwner(destOwner);
                    out.append(statement(v.declare()));
                    out.append(code.mapFields(fieldMap, s, v));
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
                 * Any exceptions thrown calling constructors should be
                 * propagated
                 */
                append(out, "\n} catch (java.lang.Exception e) {\n", "if (e instanceof RuntimeException) {\n",
                        "throw (RuntimeException)e;\n", "} else {", "throw new java.lang.RuntimeException("
                                + "\"Error while constructing new " + destinationType.getSimpleName() + " instance\", e);", "\n}\n}\n}");
            }
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
    private String addUnmatchedSourceHandler(SourceCodeContext code, Type<?> type, Type<?> sourceType, MappingContext mappingContext,
                                             StringBuilder logDetails) {
        StringBuilder out = new StringBuilder();
        for (Constructor<?> constructor : type.getRawType().getConstructors()) {
            if (constructor.getParameterTypes().length == 0 && Modifier.isPublic(constructor.getModifiers())) {
                out.append(format("return new %s();", type.getCanonicalName()));
                break;
            }
        }

        /*
         * If no default constructor field exists, attempt to locate and call a
         * constructor which takes a single argument of source type
         */
        if (out.length() == 0) {
            for (Constructor<?> constructor : type.getRawType().getConstructors()) {
                if (constructor.getParameterTypes().length == 1 && Modifier.isPublic(constructor.getModifiers())) {
                    Type<?> argType = TypeFactory.valueOf(constructor.getGenericParameterTypes()[0]);
                    if (argType.isAssignableFrom(sourceType)) {
                        out.append(format("return new %s((%s)s);", type.getCanonicalName(), sourceType.getCanonicalName()));
                        break;
                    }
                }
            }
        }

        if (out.length() == 0) {

            out.append(format(
                    "throw new %s(s.getClass().getCanonicalName() + \" is an unsupported source class for constructing instances of "
                            + type.getCanonicalName() + "\");", IllegalArgumentException.class.getCanonicalName()));
        }

        return out.toString();
    }

    /**
     * @param type
     * @param size
     */
    private String addArrayClassConstructor(SourceCodeContext code, Type<?> type, Type<?> sourceType, int size) {
        return format("if (s instanceof %s) {", sourceType.getCanonicalName()) + "return new "
                + type.getRawType().getComponentType().getCanonicalName() + "[" + size + "];" + "\n}";
    }
}