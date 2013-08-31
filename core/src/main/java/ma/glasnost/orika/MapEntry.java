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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;


/**
 * MapEntry is a concrete implementation of Map.Entry which is created for
 * use in registering mappings that involve java.util.Map instances.<br><br>
 * 
 * MapEntry should be used as the type when registering a mapping between an
 * element type (iterable or array) and an entry type (map).
 * 
 * @author matt.deboer@gmail.com
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class MapEntry<K, V> implements Map.Entry<K, V> {

    public MapEntry() { }
    
    public MapEntry(K key, V value) {
    	this.key = key;
    	this.value = value;
    }
    
    private MapEntry(Entry<K, V> copy) {
        this.key = copy.getKey();
        this.value = copy.getValue();
    }
    
    private K key;
    public V value;
    
    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
    
    public void setKey(K key) {
        this.key = key;
    }

    public V setValue(V value) {
        final V originalValue = this.value;
        this.value = value;
        return originalValue;
    }
    
    /**
     * Returns the concrete <code>MapEntry&lt;K,V&gt;</code> type that represents the entries of the given map
     * 
     * @param mapType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Type<MapEntry<K, V>> concreteEntryType(Type<? extends Map<K, V>> mapType) {
        
        Type<?> type = TypeFactory.valueOf(MapEntry.class, mapType.getActualTypeArguments()); 
        return (Type<MapEntry<K, V>>)type; 
    }
    
    public String toString() {
        return "[" + getKey() + "=" + getValue() + "]";
    }
    
    /**
     * Returns the <code>Map.Entry&lt;K,V&gt;</code> type that represents the entries of the given map
     * 
     * @param mapType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Type<Map.Entry<K, V>> entryType(Type<? extends Map<K, V>> mapType) {
        
        Type<?> type = TypeFactory.valueOf(Map.Entry.class, mapType.getActualTypeArguments()); 
        return (Type<Map.Entry<K, V>>)type; 
    }
    
    public static <K, V> Set<MapEntry<K, V>> entrySet(Map<K, V> map) {
        return new MapEntrySet<K, V>(map.entrySet());
    }
    
    private static class MapEntrySet<K, V> implements Set<MapEntry<K, V>> {

        private Set<Map.Entry<K, V>> delegate;
        
        private MapEntrySet(Set<Map.Entry<K, V>> delegate) {
            this.delegate = delegate;
        }
        
        public int size() {
            return delegate.size();
        }

        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        public boolean contains(Object o) {
            throw new UnsupportedOperationException();
        }

        public Iterator<MapEntry<K, V>> iterator() {
            return new MapEntryIterator<K, V>(delegate.iterator());
        }

        public Object[] toArray() {
            // TODO Auto-generated method stub
            return null;
        }

        public <T> T[] toArray(T[] a) {
            // TODO Auto-generated method stub
            return null;
        }

        public boolean add(MapEntry<K, V> o) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends MapEntry<K, V>> c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }
    }
    
    private static class MapEntryIterator<K, V> implements Iterator<MapEntry<K, V>> {

        private Iterator<Map.Entry<K, V>> delegate;
        
        private MapEntryIterator(Iterator<Map.Entry<K, V>> delegate) {
            this.delegate = delegate;
        }
        
        public boolean hasNext() {
            return delegate.hasNext();
        }

        public MapEntry<K, V> next() {
            return new MapEntry<K, V>(delegate.next());
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
