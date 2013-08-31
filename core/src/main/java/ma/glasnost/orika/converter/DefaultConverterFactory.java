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
package ma.glasnost.orika.converter;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.ConverterKey;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.util.HashMapUtility;
import ma.glasnost.orika.util.Ordering;
import ma.glasnost.orika.util.SortedSet;

/**
 * DefaultConverterFactory is the base implementation of ConverterFactory
 * 
 * @author mattdeboer
 *
 */
public class DefaultConverterFactory implements ConverterFactory {
    
    private static final Integer CACHE_SIZE = 2000;
    private final Map<ConverterKey, Converter<Object, Object>> converterCache;
    private final Set<Converter<Object, Object>> converters;
    private final Map<String, Converter<Object, Object>> convertersMap;
    private MapperFacade mapperFacade;
    
    /**
     * @param converterCache
     * @param converters
     */
    public DefaultConverterFactory(Map<ConverterKey, Converter<Object, Object>> converterCache, Set<Converter<Object, Object>> converters) {
        super();
        this.converterCache = converterCache;
        this.converters = new SortedSet<Converter<Object,Object>>(converters, Ordering.CONVERTER);
        this.convertersMap = new ConcurrentHashMap<String, Converter<Object, Object>>();
    }
    
    /**
     * Constructs a new instance of DefaultConverterFactory using a concurrent linked hash map
     * as the Converter cache, and a linked hashSet holding the converters.
     */
    public DefaultConverterFactory() {
        this(HashMapUtility.<ConverterKey, Converter<Object, Object>>getConcurrentLinkedHashMap(CACHE_SIZE), 
                new LinkedHashSet<Converter<Object, Object>>());
    }
    
    public void setMapperFacade(MapperFacade mapperFacade) {
    	this.mapperFacade = mapperFacade;
    	for (Converter<?,?> converter: converters) {
    		converter.setMapperFacade(mapperFacade);
    	}
    	for (Converter<?,?> converter: convertersMap.values()) {
    		converter.setMapperFacade(mapperFacade);
    	}
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.converter.ConverterFactory#canConvert(java.lang.Class,
     * java.lang.Class)
     */
    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
        boolean canConvert = _canConvert(sourceType, destinationType);
        if (canConvert)
            return true;
        
        // Maybe the converter is registered with wrapper type and meant to be
        // used
        // for primitive one (source or destination)
        // source
        if (sourceType.isPrimitive()) {
            sourceType = TypeFactory.valueOf(ClassUtil.getWrapperType(sourceType.getRawType()));
            canConvert = _canConvert(sourceType, destinationType);
        }
        if (canConvert)
            return true;
        
        // Destination
        if (destinationType.isPrimitive()) {
            destinationType = TypeFactory.valueOf(ClassUtil.getWrapperType(destinationType.getRawType()));
            canConvert = _canConvert(sourceType, destinationType);
        }
        if (canConvert)
            return true;
        
        return false;
    }
    
    @SuppressWarnings("unchecked")
    private boolean _canConvert(Type<?> sourceType, Type<?> destinationType) {
        boolean canConvert = false;
        ConverterKey key = new ConverterKey(sourceType, destinationType);
        if (converterCache.containsKey(key)) {
            return true;
        }
        for (@SuppressWarnings("rawtypes")
        Converter converter : converters) {
            if (converter.canConvert(sourceType, destinationType)) {
                converterCache.put(key, converter);
                canConvert = true;
                break;
            }
        }
        return canConvert;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.converter.ConverterFactory#hasConverter(java.lang.String
     * )
     */
    public boolean hasConverter(String converterId) {
        return convertersMap.containsKey(converterId);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.converter.ConverterFactory#getConverter(java.lang.Class
     * , java.lang.Class)
     */
    public Converter<Object, Object> getConverter(Type<?> sourceClass, Type<?> destinationClass) {
        
        // Step verify if converter exists for sourceClass and destination
        Converter<Object, Object> converter = _converter(sourceClass, destinationClass);
        
        if (converter != null)
            return converter;
        
        // Maybe the converter is registred with wrapper type and meant to be
        // used
        // for primitive one (source or destination)
        // source
        if (sourceClass.isPrimitive()) {
            sourceClass = TypeFactory.valueOf(ClassUtil.getWrapperType(sourceClass.getRawType()));
            converter = _converter(sourceClass, destinationClass);
        }
        if (converter != null)
            return converter;
        
        // Destination
        if (destinationClass.isPrimitive()) {
            destinationClass = TypeFactory.valueOf(ClassUtil.getWrapperType(destinationClass.getRawType()));
            converter = _converter(sourceClass, destinationClass);
        }
        if (converter != null)
            return converter;
        
        return null;
    }
    
    private Converter<Object, Object> _converter(Type<?> sourceClass, Type<?> destinationClass) {
        ConverterKey key = new ConverterKey(sourceClass, destinationClass);
        if (converterCache.containsKey(key)) {
            return converterCache.get(key);
        }
        
        for (Converter<Object, Object> converter : converters) {
            if (converter.canConvert(sourceClass, destinationClass)) {
                converterCache.put(key, converter);
                return converter;
            }
        }
        
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.converter.ConverterFactory#getConverter(java.lang.String
     * )
     */
    public Converter<Object, Object> getConverter(String converterId) {
        return convertersMap.get(converterId);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.converter.ConverterFactory#registerConverter(ma.glasnost
     * .orika.converter.Converter)
     */
    @SuppressWarnings({ "unchecked"})
    public <S, D> void registerConverter(Converter<S, D> converter) {
    	if (this.mapperFacade != null) {
    		converter.setMapperFacade(mapperFacade);
    	}
    	converters.add((Converter<Object, Object>) converter);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.converter.ConverterFactory#registerConverter(java.lang
     * .String, ma.glasnost.orika.converter.Converter)
     */
    @SuppressWarnings({ "unchecked" })
    public <S, D> void registerConverter(String converterId, Converter<S, D> converter) {
    	if (this.mapperFacade != null) {
    		converter.setMapperFacade(mapperFacade);
    	}
    	convertersMap.put(converterId, (Converter<Object, Object>) converter);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.converter.ConverterFactory#registerConverter(ma.glasnost
     * .orika.Converter)
     */
    @Deprecated
    public <S, D> void registerConverter(ma.glasnost.orika.converter.Converter<S, D> converter) {
        
        registerConverter(new ma.glasnost.orika.converter.Converter.LegacyConverter<S, D>(converter));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.converter.ConverterFactory#registerConverter(java.lang
     * .String, ma.glasnost.orika.Converter)
     */
    @Deprecated
    public <S, D> void registerConverter(String converterId, ma.glasnost.orika.converter.Converter<S, D> converter) {
        
        registerConverter(converterId, new ma.glasnost.orika.converter.Converter.LegacyConverter<S, D>(converter));
    }
}
