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

package ma.glasnost.orika.metadata;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.property.PropertyResolverStrategy;

/**
 * ClassMapBuilderFactory should be used to construct the new instances of
 * ClassMapBuilder needed to register a mapping from one class/type to another
 * class/type.
 * 
 * @author matt.deboer@gmail.com
 * 
 */
public abstract class ClassMapBuilderFactory {
    
    /**
     * The next factory in the chain
     */
    protected ClassMapBuilderFactory nextClassMapBuilderFactory;
    /**
     * The MapperFactory used by this factory
     */
    protected MapperFactory mapperFactory;
    /**
     * The PropertyResolver used by this factory
     */
    protected PropertyResolverStrategy propertyResolver;
    /**
     * The DefaultFieldMappers applied by this factory
     */
    protected DefaultFieldMapper[] defaults;
    
    /**
     * @param mapperFactory
     *            the MapperFactory which will be used to register the ClassMapBuilder
     *            instances via their 'register()' method
     */
    public synchronized void setMapperFactory(MapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
    }

    /**
     * Return true if this implementation of factory is suitable for received types
     * @param aType
     * @param bType
     * @return true if this factory applies to the specified pair of types
     */
    protected <A, B> boolean appliesTo(Type<A> aType, Type<B> bType) {
        return false;
    }

    /**
     * Set the next ClassMapBuilderFactory in the chain
     * 
     * @param classMapBuilderFactory
     */
    public void setChainClassMapBuilderFactory(ClassMapBuilderFactory classMapBuilderFactory) {
        nextClassMapBuilderFactory = classMapBuilderFactory;
    }

    /**
     * Choose suitable ClassMapBuilderFactory for types from factories chain
     * 
     * @param aType
     * @param bType
     * @param <A>
     * @param <B>
     * @return this ClassMapBuilderFactory
     */
    public <A, B> ClassMapBuilderFactory chooseClassMapBuilderFactory(Type<A> aType, Type<B> bType) {
        if (appliesTo(aType, bType))
            return this;
        return nextClassMapBuilderFactory == null ? null :
            nextClassMapBuilderFactory.chooseClassMapBuilderFactory(aType, bType);
    }
    
    /**
     * @param propertyResolver
     *            the PropertyResolverStrategy instance to use when resolving
     *            properties of the mapped types
     */
    public synchronized void setPropertyResolver(PropertyResolverStrategy propertyResolver) {
        this.propertyResolver = propertyResolver;
    }
    
    /**
     * @param defaults
     *            zero or more DefaultFieldMapper instances that should be
     *            applied when the <code>byDefault</code> method of the
     *            ClassMapBuilder is called.
     */
    public synchronized void setDefaultFieldMappers(DefaultFieldMapper... defaults) {
        this.defaults = defaults;
    }
    
    
    
    /**
     * Verifies whether the factory has been properly initialized
     * 
     * @return true if the factory has been initialized
     */
    public synchronized boolean isInitialized() {
        return propertyResolver != null && defaults != null;
    }
    
    /**
     * Generates a new ClassMapBuilder instance
     * 
     * @param aType
     * @param bType
     * @return a new ClassMapBuilder for the provided types
     */
    protected abstract <A, B> ClassMapBuilder<A, B> newClassMapBuilder(Type<A> aType, Type<B> bType, MapperFactory mapperFactory,
            PropertyResolverStrategy propertyResolver, DefaultFieldMapper[] defaults);
    
    /**
     * @param aType
     * @param bType
     * @return
     */
    private synchronized final <A, B> ClassMapBuilder<A, B> getNewClassMapBuilder(Type<A> aType, Type<B> bType) {
        return newClassMapBuilder(aType, bType, mapperFactory, propertyResolver, defaults);
    }
    
    /**
     * Begin a new mapping for the specified types.
     * 
     * @param aType
     * @param bType
     * @return a new ClassMapBuilder instance for the specified types
     */
    public <A, B> ClassMapBuilder<A, B> map(Type<A> aType, Type<B> bType) {
        return getNewClassMapBuilder(aType, bType);
    }
    
    /**
     * Begin a new mapping for the specified class and type.
     * 
     * @param aType
     * @param bType
     * @return a new ClassMapBuilder instance for the specified types
     */
    public <A, B> ClassMapBuilder<A, B> map(Class<A> aType, Type<B> bType) {
        return getNewClassMapBuilder(TypeFactory.<A> valueOf(aType), bType);
    }
    
    /**
     * Begin a new mapping for the specified type and class.
     * 
     * @param aType
     * @param bType
     * @return a new ClassMapBuilder instance for the specified types
     */
    public <A, B> ClassMapBuilder<A, B> map(Type<A> aType, Class<B> bType) {
        return getNewClassMapBuilder(aType, TypeFactory.<B> valueOf(bType));
    }
    
    /**
     * Begin a new mapping for the specified classes.
     * 
     * @param aType
     * @param bType
     * @return a new ClassMapBuilder instance for the specified types
     */
    public <A, B> ClassMapBuilder<A, B> map(Class<A> aType, Class<B> bType) {
        return getNewClassMapBuilder(TypeFactory.<A> valueOf(aType), TypeFactory.<B> valueOf(bType));
    }
    
}
