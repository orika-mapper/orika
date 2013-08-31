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

package ma.glasnost.orika;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ma.glasnost.orika.impl.mapping.strategy.MappingStrategy;
import ma.glasnost.orika.metadata.Type;

/**
 * The main runtime interface between a Java application and Orika. This is the
 * central interface abstracting the service of a Java bean mapping. <br>
 * <br>
 * 
 * The main operation of <code>MapperFacade</code> is <code>map()</code> that
 * copy from a deeply structured one object to an other.<br>
 * <br>
 * 
 * MapperFacade manages the state of a mapping operation through MappingContext.<br>
 * The operation of mapping may include : <br>
 * <ul>
 * <li>Creation of new objects : <code>newObject()</code></li>
 * <li>Conversion object to another type: <code>convert()</code></li>
 * <li>Mapping recursively an object to an other class : <code>map()</code></li>
 * </ul>
 * <br>
 * 
 * Example of code to map an instance of <code>Entity</code>(<code>entity</code>
 * ) to <code>DTO</code> class:<br>
 * 
 * <pre>
 * ...
 * DTO newDTO = mapperFacade.map(entity, DTO.class);
 * ...
 * </pre>
 * 
 * @author S.M. El Aatifi
 * 
 */
public interface MapperFacade {
    
    /**
     * Create and return a new instance of type D mapped with the properties of
     * <code>sourceObject</code>.
     * 
     * @param sourceObject
     *            the object to map from
     * @param destinationClass
     *            the type of the new object to return
     * @return a new instance of type D mapped with the properties of
     *         <code>sourceObject</code>
     */
    <S, D> D map(S sourceObject, Class<D> destinationClass);
    
    /**
     * Create and return a new instance of type D mapped with the properties of
     * <code>sourceObject</code>.
     * 
     * @param sourceObject
     *            the object to map from
     * @param destinationClass
     *            the type of the new object to return
     * @param context
     *            the context from the current mapping request
     * @return a new instance of type D mapped with the properties of
     *         <code>sourceObject</code>
     */
    <S, D> D map(S sourceObject, Class<D> destinationClass, MappingContext context);
    
    /**
     * Maps the properties of <code>sourceObject</code> onto
     * <code>destinationObject</code>.
     * 
     * @param sourceObject
     *            the object from which to read the properties
     * @param destinationObject
     *            the object onto which the properties should be mapped
     */
    <S, D> void map(S sourceObject, D destinationObject);
    
    /**
     * Maps the properties of <code>sourceObject</code> onto
     * <code>destinationObject</code>.
     * 
     * @param sourceObject
     *            the object from which to read the properties
     * @param destinationObject
     *            the object onto which the properties should be mapped
     * @param context
     *            the context from the current mapping request
     */
    <S, D> void map(S sourceObject, D destinationObject, MappingContext context);
    
    /**
     * Maps the properties of <code>sourceObject</code> onto
     * <code>destinationObject</code>, using <code>sourceType</code> and
     * <code>destinationType</code> to specify the parameterized types of the
     * source and destination object.
     * 
     * @param sourceObject
     *            the object from which to read the properties
     * @param destinationObject
     *            the object onto which the properties should be mapped
     * @param sourceType
     *            the parameterized type of the source object
     * @param destinationType
     *            the parameterized type of the destination object
     */
    <S, D> void map(S sourceObject, D destinationObject, Type<S> sourceType, Type<D> destinationType);
    
    /**
     * Maps the properties of <code>sourceObject</code> onto
     * <code>destinationObject</code>, using <code>sourceType</code> and
     * <code>destinationType</code> to specify the parameterized types of the
     * source and destination object.
     * 
     * @param sourceObject
     *            the object from which to read the properties
     * @param destinationObject
     *            the object onto which the properties should be mapped
     * @param sourceType
     *            the parameterized type of the source object
     * @param destinationType
     *            the parameterized type of the destination object
     * @param context
     *            the context from the current mapping request
     */
    <S, D> void map(S sourceObject, D destinationObject, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    /**
     * Maps the source iterable into a new Set parameterized by
     * <code>destinationClass</code>.
     * 
     * @param source
     *            the Iterable from which to map
     * @param destinationClass
     *            the type of elements to be contained in the returned Set.
     * @return a new Set containing elements of type
     *         <code>destinationClass</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass);
    
    /**
     * Maps the source iterable into a new Set parameterized by
     * <code>destinationClass</code>.
     * 
     * @param source
     *            the Iterable from which to map
     * @param destinationClass
     *            the type of elements to be contained in the returned Set.
     * @param context
     *            the context from the current mapping request
     * @return a new Set containing elements of type
     *         <code>destinationClass</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass, MappingContext context);
    
    /**
     * Maps the source Array into a new Set parameterized by
     * <code>destinationClass</code>.
     * 
     * @param source
     *            the Array from which to map
     * @param destinationClass
     *            the type of elements to be contained in the returned Set.
     * @return a new Set containing elements of type
     *         <code>destinationClass</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass);
    
    /**
     * Maps the source Array into a new Set parameterized by
     * <code>destinationClass</code>.
     * 
     * @param source
     *            the Array from which to map
     * @param destinationClass
     *            the type of elements to be contained in the returned Set.
     * @param context
     *            the context from the current mapping request
     * @return a new Set containing elements of type
     *         <code>destinationClass</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass, MappingContext context);
    
    /**
     * Maps the source Iterable into a new List parameterized by
     * <code>destinationClass</code>.
     * 
     * @param source
     *            the Iterable from which to map
     * @param destinationClass
     *            the type of elements to be contained in the returned Set.
     * @return a new List containing elements of type
     *         <code>destinationClass</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass);
    
    /**
     * Maps the source Iterable into a new List parameterized by
     * <code>destinationClass</code>.
     * 
     * @param source
     *            the Iterable from which to map
     * @param destinationClass
     *            the type of elements to be contained in the returned Set.
     * @param context
     *            the context from the current mapping request
     * @return a new List containing elements of type
     *         <code>destinationClass</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass, MappingContext context);
    
    /**
     * Maps the source Array into a new List parameterized by
     * <code>destinationClass</code>.
     * 
     * @param source
     *            the Array from which to map
     * @param destinationClass
     *            the type of elements to be contained in the returned Set.
     * @return a new List containing elements of type
     *         <code>destinationClass</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass);
    
    /**
     * Maps the source Array into a new List parameterized by
     * <code>destinationClass</code>.
     * 
     * @param source
     *            the Array from which to map
     * @param destinationClass
     *            the type of elements to be contained in the returned Set.
     * @param context
     *            the context from the current mapping request
     * @return a new List containing elements of type
     *         <code>destinationClass</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass, MappingContext context);
    
    /**
     * Maps the source interable into a new Array of type<code>D</code>.
     * 
     * @param destination
     * 
     * @param source
     *            the Array from which to map
     * @param destinationClass
     *            the type of elements to be contained in the returned Set.
     * @param context
     *            the context from the current mapping request
     * @return a new Array containing elements of type
     *         <code>destinationClass</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass);
    
    /**
     * Maps the source array into a new Array of type<code>D</code>.
     * 
     * @param destination
     *            the destination array which is also returned
     * @param source
     *            the source array
     * @param destinationClass
     *            the destination class
     * @return a new Array containing elements of type
     *         <code>destinationClass</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass);
    
    /**
     * Maps the source Iterable into a new Array of type<code>D</code>.
     * 
     * @param destination
     *            the destination array which is also returned
     * @param source
     *            the source Iterable
     * @param destinationClass
     *            the destination class
     * @param context
     *            the current MappingContext
     * @return a new Array containing elements of type
     *         <code>destinationClass</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass, MappingContext context);
    
    /**
     * Maps the source Array into a new Array of type<code>D</code>.
     * 
     * @param destination
     *            the destination array which is also returned
     * @param source
     *            the source Array
     * @param destinationClass
     *            the destination class
     * @param context
     *            the current MappingContext
     * @return a new Array containing elements of type
     *         <code>destinationClass</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass, MappingContext context);
    
    /**
     * Maps the source Iterable into the destination Collection
     * 
     * 
     * @param source
     *            the source Iterable
     * @param destination
     *            the destination Collection
     * @param destinationClass
     *            the destination class
     */
    <S, D> void mapAsCollection(Iterable<S> source, Collection<D> destination, Class<D> destinationClass);
    
    /**
     * Map an iterable onto an existing collection
     * 
     * @param source
     *            the source iterable
     * @param destination
     *            the destination collection
     * @param destinationClass
     *            the type of elements in the destination
     * @param context
     *            the current mapping context
     */
    <S, D> void mapAsCollection(Iterable<S> source, Collection<D> destination, Class<D> destinationClass, MappingContext context);
    
    /**
     * Map an array onto an existing collection
     * 
     * @param source
     *            the source array
     * @param destination
     *            the destination collection
     * @param destinationClass
     *            the type of elements in the destination
     */
    <S, D> void mapAsCollection(S[] source, Collection<D> destination, Class<D> destinationClass);
    
    /**
     * Map an array onto an existing collection
     * 
     * @param source
     *            the source array
     * @param destination
     *            the destination collection
     * @param destinationClass
     *            the type of elements in the destination
     * @param context
     *            the current mapping context
     */
    <S, D> void mapAsCollection(S[] source, Collection<D> destination, Class<D> destinationClass, MappingContext context);
    
    /**
     * Create and return a new instance of type D mapped with the properties of
     * <code>sourceObject</code>.
     * 
     * @param sourceObject
     *            the object to map from
     * @param sourceType
     *            the type of the source object
     * @param destinationType
     *            the type of the new object to return
     * @return a new instance of type D mapped with the properties of
     *         <code>sourceObject</code>
     */
    <S, D> D map(S sourceObject, Type<S> sourceType, Type<D> destinationType);
    
    /**
     * Create and return a new instance of type D mapped with the properties of
     * <code>sourceObject</code>.
     * 
     * @param sourceObject
     *            the object to map from
     * @param sourceType
     *            the type of the source object
     * @param destinationType
     *            the type of the new object to return
     * @param context
     *            the current mapping context
     * @return a new instance of type D mapped with the properties of
     *         <code>sourceObject</code>
     */
    <S, D> D map(S sourceObject, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    /**
     * Maps the source Iterable into a new Set parameterized by
     * <code>destinationType</code>.
     * 
     * @param source
     *            the Iterable from which to map
     * @param sourceType
     *            the type of the source elements
     * @param destinationType
     *            the type of the new object to return
     * @return a new Set containing elements of type
     *         <code>destinationType</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> Set<D> mapAsSet(Iterable<S> source, Type<S> sourceType, Type<D> destinationType);
    
    /**
     * Maps the source Iterable into a new Set parameterized by
     * <code>destinationType</code>.
     * 
     * @param source
     *            the Iterable from which to map
     * @param sourceType
     *            the type of the source elements
     * @param destinationType
     *            the type of the new object to return
     * @param context
     *            the current mapping context
     * @return a new Set containing elements of type
     *         <code>destinationType</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> Set<D> mapAsSet(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    /**
     * Maps the source Array into a new Set parameterized by
     * <code>destinationType</code>.
     * 
     * @param source
     *            the Array from which to map
     * @param sourceType
     *            the type of the source elements
     * @param destinationType
     *            the type of the new object to return
     * @return a new Set containing elements of type
     *         <code>destinationType</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> Set<D> mapAsSet(S[] source, Type<S> sourceType, Type<D> destinationType);
    
    /**
     * Maps the source Array into a new Set parameterized by
     * <code>destinationType</code>.
     * 
     * @param source
     *            the Array from which to map
     * @param sourceType
     *            the type of the source elements
     * @param destinationType
     *            the type of the new object to return
     * @param context
     *            the current mapping context
     * @return a new Set containing elements of type
     *         <code>destinationType</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> Set<D> mapAsSet(S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    /**
     * Maps the source Iterable into a new List parameterized by
     * <code>destinationType</code>.
     * 
     * @param source
     *            the Iterable from which to map
     * @param sourceType
     *            the type of the source elements
     * @param destinationType
     *            the type of the new object to return
     * @param context
     *            the current mapping context
     * @return a new List containing elements of type
     *         <code>destinationType</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> List<D> mapAsList(Iterable<S> source, Type<S> sourceType, Type<D> destinationType);
    
    /**
     * Maps the source Iterable into a new List parameterized by
     * <code>destinationType</code>.
     * 
     * @param source
     *            the Iterable from which to map
     * @param sourceType
     *            the type of the source elements
     * @param destinationType
     *            the type of the new object to return
     * @param context
     *            the current mapping context
     * @return a new List containing elements of type
     *         <code>destinationType</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> List<D> mapAsList(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    /**
     * Maps the source Array into a new List parameterized by
     * <code>destinationType</code>.
     * 
     * @param source
     *            the Array from which to map
     * @param sourceType
     *            the type of the source elements
     * @param destinationType
     *            the type of the new object to return
     * @return a new List containing elements of type
     *         <code>destinationType</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> List<D> mapAsList(S[] source, Type<S> sourceType, Type<D> destinationType);
    
    /**
     * Maps the source Array into a new List parameterized by
     * <code>destinationType</code>.
     * 
     * @param source
     *            the Array from which to map
     * @param sourceType
     *            the type of the source elements
     * @param destinationType
     *            the type of the new object to return
     * @param context
     *            the current mapping context
     * @return a new List containing elements of type
     *         <code>destinationType</code> mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> List<D> mapAsList(S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    /**
     * Maps the source Array into a new List parameterized by
     * <code>destinationType</code>.
     * 
     * @param destination
     *            the destination Array which is returned
     * @param source
     *            the Array from which to map
     * @param sourceType
     *            the type of the source elements
     * @param destinationType
     *            the type of the new object to return
     * @return the destination Array mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Type<S> sourceType, Type<D> destinationType);
    
    /**
     * Maps the source Array into a new List parameterized by
     * <code>destinationType</code>.
     * 
     * @param destination
     *            the destination Array which is returned
     * @param source
     *            the Array from which to map
     * @param sourceType
     *            the type of the source elements
     * @param destinationType
     *            the type of the new object to return
     * @param context
     *            the current mapping context
     * @return the destination Array mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> D[] mapAsArray(D[] destination, S[] source, Type<S> sourceType, Type<D> destinationType);
    
    /**
     * Maps the source Iterable into the destination Array
     * 
     * @param destination
     *            the destination Array which is returned
     * @param source
     *            the Iterable from which to map
     * @param sourceType
     *            the type of the source elements
     * @param destinationType
     *            the type of the new object to return
     * @param context
     *            the current mapping context
     * @return the destination Array mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    /**
     * Maps the source Array into the destination Array
     * 
     * @param destination
     *            the destination Array which is returned
     * @param source
     *            the Array from which to map
     * @param sourceType
     *            the type of the source elements
     * @param destinationType
     *            the type of the new object to return
     * @param context
     *            the current mapping context
     * @return the destination Array mapped from the elements of
     *         <code>source</code>.
     */
    <S, D> D[] mapAsArray(D[] destination, S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    /**
     * Map an iterable onto an existing collection
     * 
     * @param source
     *            the source iterable
     * @param destination
     *            the destination collection
     * @param sourceType
     *            the type of elements in the source
     * @param destinationType
     *            the type of elements in the destination
     */
    <S, D> void mapAsCollection(Iterable<S> source, Collection<D> destination, Type<S> sourceType, Type<D> destinationType);
    
    /**
     * Map an iterable onto an existing collection
     * 
     * @param source
     *            the source iterable
     * @param destination
     *            the destination collection
     * @param sourceType
     *            the type of elements in the source
     * @param destinationType
     *            the type of elements in the destination
     * @param context
     *            the current mapping context
     */
    <S, D> void mapAsCollection(Iterable<S> source, Collection<D> destination, Type<S> sourceType, Type<D> destinationType,
            MappingContext context);
    
    /**
     * Map an array onto an existing collection
     * 
     * @param source
     *            the source array
     * @param destination
     *            the destination collection
     * @param sourceType
     *            the type of elements in the source
     * @param destinationType
     *            the type of elements in the destination
     */
    <S, D> void mapAsCollection(S[] source, Collection<D> destination, Type<S> sourceType, Type<D> destinationType);
    
    /**
     * Map an array onto an existing collection
     * 
     * @param source
     *            the source array
     * @param destination
     *            the destination collection
     * @param sourceType
     *            the type of elements in the source
     * @param destinationType
     *            the type of elements in the destination
     * @param context
     *            the current mapping context
     */
    <S, D> void mapAsCollection(S[] source, Collection<D> destination, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    /**
     * Convert the source object into the appropriate destination type
     * 
     * @param source
     *            the source object to map
     * @param destinationClass
     *            the type of the destination class to produce
     * @param converterId
     *            the specific converter to use; if null, the first compatible
     *            global converter is used
     * @return an instance of the converted destination type
     */
    <S, D> D convert(S source, Class<D> destinationClass, String converterId);
    
    /**
     * Convert the source object into the appropriate destination type
     * 
     * @param source
     *            the source object to map
     * @param sourceType
     *            the type of the source object
     * @param destinationType
     *            the type of the destination object to produce
     * @param converterId
     *            the specific converter to use; if null, the first compatible
     *            global converter is used
     * @return an instance of the converted destination type
     */
    <S, D> D convert(S source, Type<S> sourceType, Type<D> destinationType, String converterId);
    
    /**
     * Map from one instance of java.util.Map to another.
     * 
     * @param source
     *            the source Map
     * @param sourceType
     *            the parameterized type of the source Map object
     * @param destinationType
     *            the parameterized type of the destination Map object
     * @return a new Map instance, with key and value types as defined by
     *         destinationType
     */
    <Sk, Sv, Dk, Dv> Map<Dk, Dv> mapAsMap(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType,
            Type<? extends Map<Dk, Dv>> destinationType);
    
    /**
     * Map from one instance of java.util.Map to another.
     * 
     * @param source
     * @param sourceType
     * @param destinationType
     * @param context
     *            context the context from the current mapping request
     * @return a new Map instance, with key and value types as defined by
     *         destinationType
     */
    <Sk, Sv, Dk, Dv> Map<Dk, Dv> mapAsMap(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType,
            Type<? extends Map<Dk, Dv>> destinationType, MappingContext context);
    
    /**
     * Map from an Iterable to an instance of java.util.Map.
     * 
     * @param source
     *            the source Map
     * @param sourceType
     *            the parameterized type of the source Map object
     * @param destinationType
     *            the parameterized type of the destination Map object
     * @return a new Map instance, with key and value types as defined by
     *         destinationType
     */
    <S, Dk, Dv> Map<Dk, Dv> mapAsMap(Iterable<S> source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType);
    
    /**
     * Map from an Iterable to an instance of java.util.Map.
     * 
     * @param source
     *            the source Map
     * @param sourceType
     *            the parameterized type of the source Map object
     * @param destinationType
     *            the parameterized type of the destination Map object
     * @param context
     *            the current mapping context
     * @return a new Map instance, with key and value types as defined by
     *         destinationType
     */
    <S, Dk, Dv> Map<Dk, Dv> mapAsMap(Iterable<S> source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType,
            MappingContext context);
    
    /**
     * Map from an Array to an instance of java.util.Map.
     * 
     * @param source
     *            the source Array
     * @param sourceType
     *            the parameterized type of the source Map object
     * @param destinationType
     *            the parameterized type of the destination Map object
     * @return a new Map instance, with key and value types as defined by
     *         destinationType
     */
    <S, Dk, Dv> Map<Dk, Dv> mapAsMap(S[] source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType);
    
    /**
     * Map from an Array to an instance of java.util.Map.
     * 
     * @param source
     *            the source Array
     * @param sourceType
     *            the parameterized type of the source Array element
     * @param destinationType
     *            the parameterized type of the destination Map object
     * @param context
     *            the current mapping context
     * @return a new Map instance, with key and value types as defined by
     *         destinationType
     */
    <S, Dk, Dv> Map<Dk, Dv> mapAsMap(S[] source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType, MappingContext context);
    
    /**
     * Map from a java.util.Map to a List
     * 
     * @param source
     *            the source Map
     * @param sourceType
     *            the parameterized type of the source Map object
     * @param destinationType
     *            the parameterized type of the destination List elements
     * @return a new List instance, with element types as defined by
     *         destinationType
     */
    <Sk, Sv, D> List<D> mapAsList(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType);
    
    /**
     * Map from a java.util.Map to a List
     * 
     * @param source
     *            the source Map
     * @param sourceType
     *            the parameterized type of the source Map object
     * @param destinationType
     *            the parameterized type of the destination List elements
     * @param context
     *            the current mapping context
     * @return a new List instance, with element types as defined by
     *         destinationType
     */
    <Sk, Sv, D> List<D> mapAsList(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType,
            MappingContext context);
    
    /**
     * Map from a java.util.Map to a Set
     * 
     * @param source
     *            the source Map
     * @param sourceType
     *            the parameterized type of the source Map object
     * @param destinationType
     *            the parameterized type of the destination Set elements
     * @return a new Set instance, with element types defined by destinationType
     */
    <Sk, Sv, D> Set<D> mapAsSet(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType);
    
    /**
     * Map from a java.util.Map to a Set
     * 
     * @param source
     *            the source Map
     * @param sourceType
     *            the parameterized type of the source Map object
     * @param destinationType
     *            the parameterized type of the destination Set elements
     * @param context
     *            the current mapping context
     * @return a new Map instance, with element types as defined by
     *         destinationType
     */
    <Sk, Sv, D> Set<D> mapAsSet(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType, MappingContext context);
    
    /**
     * Map from a java.util.Map to a Set
     * 
     * @param destination
     *            the destination Array which is also returned
     * @param source
     *            the source Map
     * @param sourceType
     *            the parameterized type of the source Map object
     * @param destinationType
     *            the parameterized type of the destination Array elements
     * @return the destination Array
     */
    <Sk, Sv, D> D[] mapAsArray(D[] destination, Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType);
    
    /**
     * Map from a java.util.Map to a Set
     * 
     * @param destination
     *            the destination Array which is also returned
     * @param source
     *            the source Map
     * @param sourceType
     *            the parameterized type of the source Map object
     * @param destinationType
     *            the parameterized type of the destination Array elements
     * @param context
     *            the current mapping context
     * @return the destination Array
     */
    <Sk, Sv, D> D[] mapAsArray(D[] destination, Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType,
            MappingContext context);
    
    /**
     * Create new instance of a destination class. <strong>Abstract types are
     * unsupported</code>.
     * 
     * @param source
     *            the source objet
     * @param destinationType
     *            the type of the destination
     * @param context
     *            the current mapping context
     * @return new instance of <code>destinationClass</code>
     */
    <S, D> D newObject(S source, Type<? extends D> destinationType, MappingContext context);
    
    /**
     * @param sourceObject
     *            the source object being mapped
     * @param sourceType
     *            the source type to map
     * @param destinationType
     *            the destination type
     * @param mapInPlace
     *            whether the strategy should map objects in-place
     * @param context
     *            the current MappingContext
     * @return a MappingStrategy appropriate for mapping between the provided
     *         types
     */
    <S, D> MappingStrategy resolveMappingStrategy(final S sourceObject, final java.lang.reflect.Type sourceType,
            final java.lang.reflect.Type destinationType, boolean mapInPlace, final MappingContext context);
    
}