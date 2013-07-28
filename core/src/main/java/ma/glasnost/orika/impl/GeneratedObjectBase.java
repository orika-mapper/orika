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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Filter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategy;
import ma.glasnost.orika.metadata.Type;

public abstract class GeneratedObjectBase {
    
    protected Type<?>[] usedTypes;
    protected Converter<Object, Object>[] usedConverters;
    protected BoundMapperFacade<Object, Object>[] usedMapperFacades;
    protected Filter<Object, Object>[] usedFilters;
    protected MapperFacade mapperFacade;
    protected boolean fromAutoMapping;
    
    public void setMapperFacade(MapperFacade mapper) {
        this.mapperFacade = mapper;
    }
    
    public void setUsedTypes(Type<Object>[] types) {
        this.usedTypes = types;
    }
    
    public void setUsedConverters(Converter<Object, Object>[] usedConverters) {
        this.usedConverters = usedConverters;
    }
    
    public void setUsedMapperFacades(BoundMapperFacade<Object, Object>[] usedMapperFacades) {
        this.usedMapperFacades = usedMapperFacades;
    }
    
    public void setUsedFilters(Filter<Object, Object>[] usedFilters) {
        this.usedFilters = usedFilters;
    }
    
    public boolean isFromAutoMapping() {
        return fromAutoMapping;
    }
    
    public void setFromAutoMapping(boolean fromAutoMapping) {
        this.fromAutoMapping = fromAutoMapping;
    }
    
    protected int min(int[] sizes) {
        
        int min = Integer.MAX_VALUE;
        for (int size : sizes) {
            if (size < min) {
                min = size;
            }
        }
        return min;
    }
    
    protected static <T> List<T> asList(Iterable<T> iterable) {
        ArrayList<T> ts = new ArrayList<T>();
        for (T i : iterable) {
            ts.add(i);
        }
        return ts;
    }
    
    protected static List<Object> asList(Object[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }
    
    protected static List<Object> asList(byte[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }
    
    protected static List<Object> asList(int[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }
    
    protected static List<Object> asList(char[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }
    
    protected static List<Object> asList(long[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }
    
    protected static List<Object> asList(float[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }
    
    protected static List<Object> asList(double[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }
    
    protected static List<Object> asList(boolean[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }
    
    protected static List<Object> asList(short[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }
    
    protected void mapArray(byte[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }
        
        MappingStrategy strategy = null;
        Class<?> entryClass = null;
        int i = 0;
        for (final Object s : source) {
            if (strategy == null || !s.getClass().equals(entryClass)) {
                strategy = mapperFacade.resolveMappingStrategy(s, null, clazz, false, mappingContext);
                entryClass = s.getClass();
            }
            destination[i++] = (Byte) strategy.map(s, null, mappingContext);
        }
    }
    
    protected void mapArray(boolean[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }
        
        MappingStrategy strategy = null;
        Class<?> entryClass = null;
        int i = 0;
        for (final Object s : source) {
            if (strategy == null || !s.getClass().equals(entryClass)) {
                strategy = mapperFacade.resolveMappingStrategy(s, null, clazz, false, mappingContext);
                entryClass = s.getClass();
            }
            destination[i++] = (Boolean) strategy.map(s, null, mappingContext);
        }
        
    }
    
    protected void mapArray(char[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }
        
        MappingStrategy strategy = null;
        Class<?> entryClass = null;
        int i = 0;
        for (final Object s : source) {
            if (strategy == null || !s.getClass().equals(entryClass)) {
                strategy = mapperFacade.resolveMappingStrategy(s, null, clazz, false, mappingContext);
                entryClass = s.getClass();
            }
            destination[i++] = (Character) strategy.map(s, null, mappingContext);
        }
        
    }
    
    protected void mapArray(short[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }
        
        MappingStrategy strategy = null;
        Class<?> entryClass = null;
        int i = 0;
        for (final Object s : source) {
            if (strategy == null || !s.getClass().equals(entryClass)) {
                strategy = mapperFacade.resolveMappingStrategy(s, null, clazz, false, mappingContext);
                entryClass = s.getClass();
            }
            destination[i++] = (Short) strategy.map(s, null, mappingContext);
        }
        
    }
    
    protected void mapArray(int[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }
        
        MappingStrategy strategy = null;
        Class<?> entryClass = null;
        int i = 0;
        for (final Object s : source) {
            if (strategy == null || !s.getClass().equals(entryClass)) {
                strategy = mapperFacade.resolveMappingStrategy(s, null, clazz, false, mappingContext);
                entryClass = s.getClass();
            }
            destination[i++] = (Integer) strategy.map(s, null, mappingContext);
        }
        
    }
    
    protected void mapArray(long[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }
        
        MappingStrategy strategy = null;
        Class<?> entryClass = null;
        int i = 0;
        for (final Object s : source) {
            if (strategy == null || !s.getClass().equals(entryClass)) {
                strategy = mapperFacade.resolveMappingStrategy(s, null, clazz, false, mappingContext);
                entryClass = s.getClass();
            }
            destination[i++] = (Long) strategy.map(s, null, mappingContext);
        }
        
    }
    
    protected void mapArray(float[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }
        
        MappingStrategy strategy = null;
        Class<?> entryClass = null;
        int i = 0;
        for (final Object s : source) {
            if (strategy == null || !s.getClass().equals(entryClass)) {
                strategy = mapperFacade.resolveMappingStrategy(s, null, clazz, false, mappingContext);
                entryClass = s.getClass();
            }
            destination[i++] = (Float) strategy.map(s, null, mappingContext);
        }
        
    }
    
    protected void mapArray(double[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }
        
        MappingStrategy strategy = null;
        Class<?> entryClass = null;
        int i = 0;
        for (final Object s : source) {
            if (strategy == null || !s.getClass().equals(entryClass)) {
                strategy = mapperFacade.resolveMappingStrategy(s, null, clazz, false, mappingContext);
                entryClass = s.getClass();
            }
            destination[i++] = (Double) strategy.map(s, null, mappingContext);
        }
    }
    
    public static boolean[] booleanArray(Collection<Boolean> collection) {
        boolean[] primitives = new boolean[collection.size()];
        int index = -1;
        Iterator<Boolean> iter = collection.iterator();
        while (iter.hasNext()) {
            primitives[++index] = iter.next();
        }
        return primitives;
    }
    
    public static byte[] byteArray(Collection<Byte> collection) {
        byte[] primitives = new byte[collection.size()];
        int index = -1;
        Iterator<Byte> iter = collection.iterator();
        while (iter.hasNext()) {
            primitives[++index] = iter.next().byteValue();
        }
        return primitives;
    }
    
    public static char[] charArray(Collection<Character> collection) {
        char[] primitives = new char[collection.size()];
        int index = -1;
        Iterator<Character> iter = collection.iterator();
        while (iter.hasNext()) {
            primitives[++index] = iter.next().charValue();
        }
        return primitives;
    }
    
    public static short[] shortArray(Collection<Short> collection) {
        short[] primitives = new short[collection.size()];
        int index = -1;
        Iterator<Short> iter = collection.iterator();
        while (iter.hasNext()) {
            primitives[++index] = iter.next().shortValue();
        }
        return primitives;
    }
    
    public static int[] intArray(Collection<Integer> collection) {
        int[] primitives = new int[collection.size()];
        int index = -1;
        Iterator<Integer> iter = collection.iterator();
        while (iter.hasNext()) {
            primitives[++index] = iter.next().intValue();
        }
        return primitives;
    }
    
    public static long[] longArray(Collection<Long> collection) {
        long[] primitives = new long[collection.size()];
        int index = -1;
        Iterator<Long> iter = collection.iterator();
        while (iter.hasNext()) {
            primitives[++index] = iter.next().longValue();
        }
        return primitives;
    }
    
    public static float[] floatArray(Collection<Float> collection) {
        float[] primitives = new float[collection.size()];
        int index = -1;
        Iterator<Float> iter = collection.iterator();
        while (iter.hasNext()) {
            primitives[++index] = iter.next().floatValue();
        }
        return primitives;
    }
    
    public static double[] doubleArray(Collection<Double> collection) {
        double[] primitives = new double[collection.size()];
        int index = -1;
        Iterator<Double> iter = collection.iterator();
        while (iter.hasNext()) {
            primitives[++index] = iter.next().doubleValue();
        }
        return primitives;
    }
    
    /**
     * Function to help with list to Array conversion to support Javassist
     * 
     * @param list the list to convert
     * @param array the array to receive the elements of the list
     */
    public static void listToArray(List<?> list, Object[] array) {
        list.toArray((Object[]) array);
    }
    
    /**
     * Function to help with list to Array conversion to support Javassist
     * 
     * @param list the list to convert
     * @param arrayClass the array type to construct
     * @return an Array with elements of type matching the list
     */
    public static Object listToArray(List<?> list, Class<?> arrayClass) {
        Object array = Array.newInstance(arrayClass.getComponentType(), list.size());
        list.toArray((Object[]) array);
        return array;
    }
    
    /**
     * Function to help with list to Map conversion to support Javassist;<br>
     * 
     * @param entries a list of Map.Entry
     * @param mapClass the type of Map to instantiate
     * @return a new Map created from a list of Map entries
     */
    @SuppressWarnings("unchecked")
    public static <K,V> Map<K,V> listToMap(List<?> entries, Class<?> mapClass) {
        try {
            Map<K, V> map = (Map<K, V>) mapClass.newInstance();
            for (Map.Entry<K, V> entry: (List<Map.Entry<K, V>>)entries) {
                ((Map<K,V>)map).put(entry.getKey(), entry.getValue());
            }
            return map;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    /**
     * Function to help with list to Map conversion to support Javassist;<br>
     * 
     * @param entries a list of Map.Entry
     * @param map the map to receive the entries
     */
    @SuppressWarnings("unchecked")
    public static <K,V> void listToMap(List<?> entries, Map<K, V> map) {
        for (Map.Entry<K, V> entry: (List<Map.Entry<K, V>>)entries) {
            ((Map<K,V>)map).put(entry.getKey(), entry.getValue());
        }
    }
}