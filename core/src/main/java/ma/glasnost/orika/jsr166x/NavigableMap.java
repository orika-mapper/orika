/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package ma.glasnost.orika.jsr166x;

import java.util.*;

/**
 * A {@link SortedMap} extended with navigation methods returning the
 * closest matches for given search targets. Methods
 * <tt>lowerEntry</tt>, <tt>floorEntry</tt>, <tt>ceilingEntry</tt>,
 * and <tt>higherEntry</tt> return <tt>Map.Entry</tt> objects
 * associated with keys respectively less than, less than or equal,
 * greater than or equal, and greater than a given key, returning
 * <tt>null</tt> if there is no such key.  Similarly, methods
 * <tt>lowerKey</tt>, <tt>floorKey</tt>, <tt>ceilingKey</tt>, and
 * <tt>higherKey</tt> return only the associated keys. All of these
 * methods are designed for locating, not traversing entries.
 *
 * <p>A <tt>NavigableMap</tt> may be viewed and traversed in either
 * ascending or descending key order.  The <tt>Map</tt> methods
 * <tt>keySet</tt> and <tt>entrySet</tt> return ascending views, and
 * the additional methods <tt>descendingKeySet</tt> and
 * <tt>descendingEntrySet</tt> return descending views. The
 * performance of ascending traversals is likely to be faster than
 * descending traversals.  Notice that it is possible to perform
 * subrannge traversals in either direction using <tt>SubMap</tt>.
 *
 * <p>This interface additionally defines methods <tt>firstEntry</tt>,
 * <tt>pollFirstEntry</tt>, <tt>lastEntry</tt>, and
 * <tt>pollLastEntry</tt> that return and/or remove the least and
 * greatest mappings, if any exist, else returning <tt>null</tt>.
 *
 * <p> Implementations of entry-returning methods are expected to
 * return <tt>Map.Entry</tt> pairs representing snapshots of mappings
 * at the time they were produced, and thus generally do <em>not</em>
 * support the optional <tt>Entry.setValue</tt> method. Note however
 * that it is possible to change mappings in the associated map using
 * method <tt>put</tt>.
 *
 * @author Doug Lea
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface NavigableMap<K,V> extends SortedMap<K,V> {
    /**
     * Returns a key-value mapping associated with the least key
     * greater than or equal to the given key, or <tt>null</tt> if there is
     * no such entry.
     *
     * @param key the key.
     * @return an Entry associated with ceiling of given key, or <tt>null</tt>
     * if there is no such Entry.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt> and this map
     * does not support <tt>null</tt> keys.
     */
    public Map.Entry<K,V> ceilingEntry(K key);

    /**
     * Returns least key greater than or equal to the given key, or
     * <tt>null</tt> if there is no such key.
     *
     * @param key the key.
     * @return the ceiling key, or <tt>null</tt>
     * if there is no such key.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt> and this map
     * does not support <tt>null</tt> keys.
     */
    public K ceilingKey(K key);

    /**
     * Returns a key-value mapping associated with the greatest
     * key strictly less than the given key, or <tt>null</tt> if there is no
     * such entry.
     *
     * @param key the key.
     * @return an Entry with greatest key less than the given
     * key, or <tt>null</tt> if there is no such Entry.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt> and this map
     * does not support <tt>null</tt> keys.
     */
    public Map.Entry<K,V> lowerEntry(K key);

    /**
     * Returns the greatest key strictly less than the given key, or
     * <tt>null</tt> if there is no such key.
     *
     * @param key the key.
     * @return the greatest key less than the given
     * key, or <tt>null</tt> if there is no such key.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt> and this map
     * does not support <tt>null</tt> keys.
     */
    public K lowerKey(K key);

    /**
     * Returns a key-value mapping associated with the greatest key
     * less than or equal to the given key, or <tt>null</tt> if there
     * is no such entry.
     *
     * @param key the key.
     * @return an Entry associated with floor of given key, or <tt>null</tt>
     * if there is no such Entry.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt> and this map
     * does not support <tt>null</tt> keys.
     */
    public Map.Entry<K,V> floorEntry(K key);

    /**
     * Returns the greatest key
     * less than or equal to the given key, or <tt>null</tt> if there
     * is no such key.
     *
     * @param key the key.
     * @return the floor of given key, or <tt>null</tt> if there is no
     * such key.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt> and this map
     * does not support <tt>null</tt> keys.
     */
    public K floorKey(K key);

    /**
     * Returns a key-value mapping associated with the least key
     * strictly greater than the given key, or <tt>null</tt> if there
     * is no such entry.
     *
     * @param key the key.
     * @return an Entry with least key greater than the given key, or
     * <tt>null</tt> if there is no such Entry.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt> and this map
     * does not support <tt>null</tt> keys.
     */
    public Map.Entry<K,V> higherEntry(K key);

    /**
     * Returns the least key strictly greater than the given key, or
     * <tt>null</tt> if there is no such key.
     *
     * @param key the key.
     * @return the least key greater than the given key, or
     * <tt>null</tt> if there is no such key.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt> and this map
     * does not support <tt>null</tt> keys.
     */
    public K higherKey(K key);

    /**
     * Returns a key-value mapping associated with the least
     * key in this map, or <tt>null</tt> if the map is empty.
     *
     * @return an Entry with least key, or <tt>null</tt>
     * if the map is empty.
     */
    public Map.Entry<K,V> firstEntry();

    /**
     * Returns a key-value mapping associated with the greatest
     * key in this map, or <tt>null</tt> if the map is empty.
     *
     * @return an Entry with greatest key, or <tt>null</tt>
     * if the map is empty.
     */
    public Map.Entry<K,V> lastEntry();

    /**
     * Removes and returns a key-value mapping associated with
     * the least key in this map, or <tt>null</tt> if the map is empty.
     *
     * @return the removed first entry of this map, or <tt>null</tt>
     * if the map is empty.
     */
    public Map.Entry<K,V> pollFirstEntry();

    /**
     * Removes and returns a key-value mapping associated with
     * the greatest key in this map, or <tt>null</tt> if the map is empty.
     *
     * @return the removed last entry of this map, or <tt>null</tt>
     * if the map is empty.
     */
    public Map.Entry<K,V> pollLastEntry();

    /**
     * Returns a set view of the keys contained in this map, in
     * descending key order.  The set is backed by the map, so changes
     * to the map are reflected in the set, and vice-versa.  If the
     * map is modified while an iteration over the set is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The set supports
     * element removal, which removes the corresponding mapping from
     * the map, via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt> <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the add or <tt>addAll</tt>
     * operations.
     *
     * @return a set view of the keys contained in this map.
     */
    Set<K> descendingKeySet();

    /**
     * Returns a set view of the mappings contained in this map, in
     * descending key order.  Each element in the returned set is a
     * <tt>Map.Entry</tt>.  The set is backed by the map, so changes to
     * the map are reflected in the set, and vice-versa.  If the map
     * is modified while an iteration over the set is in progress
     * (except through the iterator's own <tt>remove</tt> operation,
     * or through the <tt>setValue</tt> operation on a map entry
     * returned by the iterator) the results of the iteration are
     * undefined.  The set supports element removal, which removes the
     * corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map.
     */
    Set<Map.Entry<K, V>> descendingEntrySet();

    /**
     * Returns a view of the portion of this map whose keys range from
     * <tt>fromKey</tt>, inclusive, to <tt>toKey</tt>, exclusive.  (If
     * <tt>fromKey</tt> and <tt>toKey</tt> are equal, the returned sorted map
     * is empty.)  The returned sorted map is backed by this map, so changes
     * in the returned sorted map are reflected in this map, and vice-versa.
     *
     * @param fromKey low endpoint (inclusive) of the subMap.
     * @param toKey high endpoint (exclusive) of the subMap.
     *
     * @return a view of the portion of this map whose keys range from
     * <tt>fromKey</tt>, inclusive, to <tt>toKey</tt>, exclusive.
     *
     * @throws ClassCastException if <tt>fromKey</tt> and
     * <tt>toKey</tt> cannot be compared to one another using this
     * map's comparator (or, if the map has no comparator, using
     * natural ordering).
     * @throws IllegalArgumentException if <tt>fromKey</tt> is greater
     * than <tt>toKey</tt>.
     * @throws NullPointerException if <tt>fromKey</tt> or
     * <tt>toKey</tt> is <tt>null</tt> and this map does not support
     * <tt>null</tt> keys.
     */
    public NavigableMap<K,V> subMap(K fromKey, K toKey);

    /**
     * Returns a view of the portion of this map whose keys are strictly less
     * than <tt>toKey</tt>.  The returned sorted map is backed by this map, so
     * changes in the returned sorted map are reflected in this map, and
     * vice-versa.
     * @param toKey high endpoint (exclusive) of the headMap.
     * @return a view of the portion of this map whose keys are strictly
     *                less than <tt>toKey</tt>.
     *
     * @throws ClassCastException if <tt>toKey</tt> is not compatible
     *         with this map's comparator (or, if the map has no comparator,
     *         if <tt>toKey</tt> does not implement <tt>Comparable</tt>).
     * @throws NullPointerException if <tt>toKey</tt> is <tt>null</tt>
     * and this map does not support <tt>null</tt> keys.
     */
    public NavigableMap<K,V> headMap(K toKey);

    /**
     * Returns a view of the portion of this map whose keys are
     * greater than or equal to <tt>fromKey</tt>.  The returned sorted
     * map is backed by this map, so changes in the returned sorted
     * map are reflected in this map, and vice-versa.
     * @param fromKey low endpoint (inclusive) of the tailMap.
     * @return a view of the portion of this map whose keys are greater
     *                than or equal to <tt>fromKey</tt>.
     * @throws ClassCastException if <tt>fromKey</tt> is not compatible
     *         with this map's comparator (or, if the map has no comparator,
     *         if <tt>fromKey</tt> does not implement <tt>Comparable</tt>).
     * @throws NullPointerException if <tt>fromKey</tt> is <tt>null</tt>
     * and this map does not support <tt>null</tt> keys.
     */
    public NavigableMap<K,V>  tailMap(K fromKey);
}
