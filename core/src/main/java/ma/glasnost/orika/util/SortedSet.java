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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A sorted set implementation based on a Comparator (or type's natural
 * ordering) where the comparison function is allowed to return ordering
 * equivalence (0) which doesn't necessarily imply logical equivalence.
 * <p>
 * This allows for the ordering of a set of items where some are less or greater
 * than others, while others are simply not comparable in an ordering sense (in
 * which case, 0 is returned from their comparison).
 * <p>
 * This class is thread-safe, with the same caveats for ConcurrentSkipListMap.
 * 
 * @author matt.deboer@gmail.com
 * @param <V>
 *            the element type
 */
public class SortedSet<V> extends SortedCollection<V> implements Set<V> {

	/**
	 * @param ordering
	 */
	public SortedSet(Ordering<V> ordering) {
		super(ordering);
	}

	/**
	 * @param c
	 *            the collection to initialize this sorted set
	 * @param ordering
	 *            the ordering used to sort this set
	 */
	public SortedSet(Collection<? extends V> c, Ordering<V> ordering) {
		super(c, ordering);
	}

	protected Collection<V> filter(List<V> items) {
		return new LinkedHashSet<V>(items);
	}
}
