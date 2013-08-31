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
package ma.glasnost.orika.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A simple sorted collection implementation that allows for duplicates; new
 * items are inserted based on their comparison to existing items; if a new item
 * is found to be less than any item in the list, it is inserted before that
 * item, else it is inserted at the end. <br>
 * <br>
 * The collection is backed by a ConcurrentSkipListMap, with the keys defined as
 * Double, allowing for inserts of items at a specific location with respect to
 * the other items in the map.
 * <p>
 * This class is thread-safe, with the same caveats for ConcurrentSkipListMap.
 * 
 * @author matt.deboer@gmail.com
 * @param <V>
 *            the element type contained in this list
 */
public class SortedCollection<V> implements Collection<V> {

	/**
     * 
     */
	protected final Ordering<V> ordering;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private List<V> items = new ArrayList<V>();

	/**
	 * @param ordering
	 */
	public SortedCollection(Ordering<V> ordering) {
		this.ordering = ordering;
	}

	/**
	 * @param c
	 *            the collection from which to initialize this SortedCollection
	 * @param ordering
	 *            the ordering used for sorting the elements
	 */
	public SortedCollection(Collection<? extends V> c, Ordering<V> ordering) {
		this(ordering);
		addAll(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(V value) {
		try {
			rwl.writeLock().lock();
			items.add(value);
			items = TopologicalSorter.sort(filter(items), ordering);
			return true;
		} finally {
			rwl.writeLock().unlock();
		}
	}

	protected Collection<V> filter(List<V> items) {
		return items;
	}

	public int size() {
		try {
			rwl.readLock().lock();
			return items.size();
		} finally {
			rwl.readLock().unlock();
		}
	}

	public boolean isEmpty() {
		try {
			rwl.readLock().lock();
			return items.isEmpty();
		} finally {
			rwl.readLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<V> iterator() {
		try {
			rwl.readLock().lock();
			return new ArrayList<V>(items).iterator();
		} finally {
			rwl.readLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		try {
			rwl.readLock().lock();
			return items.contains(o);
		} finally {
			rwl.readLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		try {
			rwl.readLock().lock();
			return items.toArray();
		} finally {
			rwl.readLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray(T[])
	 */
	public <T> T[] toArray(T[] a) {
		try {
			rwl.readLock().lock();
			return items.toArray(a);
		} finally {
			rwl.readLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		try {
			rwl.writeLock().lock();
			return items.remove(o);
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		try {
			rwl.readLock().lock();
			return items.containsAll(c);
		} finally {
			rwl.readLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends V> c) {
		try {
			rwl.writeLock().lock();
			items.addAll(c);
			items = TopologicalSorter.sort(filter(items), ordering);
			return true;
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		try {
			rwl.writeLock().lock();
			return items.removeAll(c);
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		try {
			rwl.writeLock().lock();
			return items.retainAll(c);
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		try {
			rwl.writeLock().lock();
			items.clear();
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/**
	 * @return the first item in this collection
	 */
	public V first() {
		try {
			rwl.readLock().lock();
			return items.isEmpty() ? null : items.get(0);
		} finally {
			rwl.readLock().unlock();
		}
	}

	/**
	 * @return the last item in this collection
	 */
	public V last() {
		try {
			rwl.readLock().lock();
			return items.isEmpty() ? null : items.get(items.size() - 1);
		} finally {
			rwl.readLock().unlock();
		}
	}
}
