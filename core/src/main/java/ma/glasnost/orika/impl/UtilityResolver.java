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

import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.constructor.SimpleConstructorResolverStrategy;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.converter.DefaultConverterFactory;
import ma.glasnost.orika.impl.generator.CompilerStrategy;
import ma.glasnost.orika.impl.generator.JavassistCompilerStrategy;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.ClassMapBuilderFactory;
import ma.glasnost.orika.property.IntrospectorPropertyResolver;
import ma.glasnost.orika.property.PropertyResolverStrategy;

/**
 * UtilityResolver is used to resolve implementations for the various
 * customizable utility types used in Orika.
 * 
 * @author matt.deboer@gmail.com
 * 
 */
public abstract class UtilityResolver {
    
    /**
     * Provides a default compiler strategy, favoring a type specified in the
     * appropriate system property if found.
     * 
     * @return
     */
    public static CompilerStrategy getDefaultCompilerStrategy() {
        return resolveUtility(OrikaSystemProperties.COMPILER_STRATEGY, JavassistCompilerStrategy.class);
    }
    
    /**
     * Provides a default constructor resolver strategy, favoring a type
     * specified in the appropriate system property if found.
     * 
     * @return
     */
    public static ConverterFactory getDefaultConverterFactory() {
        return resolveUtility(OrikaSystemProperties.CONVERTER_FACTORY, DefaultConverterFactory.class);
    }
    
    /**
     * Provides a default constructor resolver strategy, favoring a type
     * specified in the appropriate system property if found.
     * 
     * @return
     */
    public static ConstructorResolverStrategy getDefaultConstructorResolverStrategy() {
        return resolveUtility(OrikaSystemProperties.CONSTRUCTOR_RESOLVER_STRATEGY, SimpleConstructorResolverStrategy.class);
    }
    
    /**
     * Provides a default constructor resolver strategy, favoring a type
     * specified in the appropriate system property if found.
     * 
     * @return
     */
    public static PropertyResolverStrategy getDefaultPropertyResolverStrategy() {
        return resolveUtility(OrikaSystemProperties.PROPERTY_RESOLVER_STRATEGY, IntrospectorPropertyResolver.class);
        
    }
    
    /**
     * Provides a default ClassMapBuilderFactory instance, favoring a type
     * specified in the appropriate system property if found.
     * 
     * @return
     */
    public static ClassMapBuilderFactory getDefaultClassMapBuilderFactory() {
        return resolveUtility(OrikaSystemProperties.CLASSMAP_BUILDER_FACTORY, ClassMapBuilder.Factory.class);
    }
    
    /**
     * Resolves a utility implementation, given a system property for customized
     * instance, and a default implementation class.
     * 
     * @param systemProperty
     * @param defaultImplementation
     * @return
     */
    private static <U> U resolveUtility(String systemProperty, Class<? extends U> defaultImplementation) {
        
        U utility = null;
        String utilityClassName = System.getProperty(systemProperty);
        if (utilityClassName != null) {
            
            try {
                @SuppressWarnings("unchecked")
                Class<? extends U> utilityClass = (Class<? extends U>) Class.forName(utilityClassName, true, Thread.currentThread()
                        .getContextClassLoader());
                utility = utilityClass.newInstance();
                
            } catch (Exception e) {
                throw new IllegalArgumentException("utility implementation specified for " + systemProperty + " was invalid", e);
            }
            
        } else {
            try {
                utility = defaultImplementation.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return utility;
    }
    
}
