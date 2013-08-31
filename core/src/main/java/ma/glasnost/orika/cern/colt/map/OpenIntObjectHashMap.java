/*
    Copyright © 1999 CERN - European Organization for Nuclear Research.
    Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
    is hereby granted without fee, provided that the above copyright notice appear in all copies and 
    that both that copyright notice and this permission notice appear in supporting documentation. 
    CERN makes no representations about the suitability of this software for any purpose. 
    It is provided "as is" without expressed or implied warranty.
 */
package ma.glasnost.orika.cern.colt.map;


/**
 * Hash map holding (key,value) associations of type <tt>(int-->Object)</tt>;
 * Automatically grows and shrinks as needed; Implemented using open addressing
 * with double hashing. First see the <a href="package-summary.html">package
 * summary</a> and javadoc <a href="package-tree.html">tree view</a> to get the
 * broad picture.
 * 
 * Overrides many methods for performance reasons only.
 * 
 * @author wolfgang.hoschek@cern.ch
 * @author matt.deboer@gmail.com
 *   Modified source to remove unused methods and combined superclass methods
 *   into single class.
 * @version 1.0, 09/24/99
 * @see java.util.HashMap
 */
public class OpenIntObjectHashMap {
    /**
     * The hash table keys.
     * 
     * @serial
     */
    protected int table[];
    
    /**
     * The hash table values.
     * 
     * @serial
     */
    protected Object values[];
    
    /**
     * The state of each hash table entry (FREE, FULL, REMOVED).
     * 
     * @serial
     */
    protected byte state[];
    
    /**
     * The number of table entries in state==FREE.
     * 
     * @serial
     */
    protected int freeEntries;
    /**
     * The number of distinct associations in the map; its "size()".
     */
    protected int distinct;
    /**
     * The minimum load factor for the hashtable.
     */
    protected double minLoadFactor;

    /**
     * The maximum load factor for the hashtable.
     */
    protected double maxLoadFactor;
    
    /**
     * The table capacity c=table.length always satisfies the invariant
     * <tt>c * minLoadFactor <= s <= c * maxLoadFactor</tt>, where s=size() is the number of associations currently contained.
     * The term "c * minLoadFactor" is called the "lowWaterMark", "c * maxLoadFactor" is called the "highWaterMark".
     * In other words, the table capacity (and proportionally the memory used by this class) oscillates within these constraints.
     * The terms are precomputed and cached to avoid recalculating them each time put(..) or removeKey(...) is called.
     */
    private int highWaterMark;
    
    private static final byte FREE = 0;
    private static final byte FULL = 1;
    private static final byte REMOVED = 2;
    
    private static final int defaultCapacity = 277;
    private static final double defaultMinLoadFactor = 0.2;
    private static final double defaultMaxLoadFactor = 0.5;
    
    /**
     * Constructs an empty map with default capacity and default load factors.
     */
    public OpenIntObjectHashMap() {
        this(defaultCapacity);
    }
    
    /**
     * Constructs an empty map with the specified initial capacity and default
     * load factors.
     * 
     * @param initialCapacity
     *            the initial capacity of the map.
     * @throws IllegalArgumentException
     *             if the initial capacity is less than zero.
     */
    public OpenIntObjectHashMap(int initialCapacity) {
        this(initialCapacity, defaultMinLoadFactor, defaultMaxLoadFactor);
    }
    
    /**
     * Constructs an empty map with the specified initial capacity and the
     * specified minimum and maximum load factor.
     * 
     * @param initialCapacity
     *            the initial capacity.
     * @param minLoadFactor
     *            the minimum load factor.
     * @param maxLoadFactor
     *            the maximum load factor.
     * @throws IllegalArgumentException
     *             if
     *             <tt>initialCapacity < 0 || (minLoadFactor < 0.0 || minLoadFactor >= 1.0) || (maxLoadFactor <= 0.0 || maxLoadFactor >= 1.0) || (minLoadFactor >= maxLoadFactor)</tt>
     *             .
     */
    public OpenIntObjectHashMap(int initialCapacity, double minLoadFactor, double maxLoadFactor) {
        setUp(initialCapacity, minLoadFactor, maxLoadFactor);
    }
    
    /**
     * Returns <tt>true</tt> if the receiver contains the specified key.
     * @param key the key to test for
     * @return <tt>true</tt> if the receiver contains the specified key.
     */
    public boolean containsKey(int key) {
        return indexOfKey(key) >= 0;
    }
    
    
    /**
     * Returns the value associated with the specified key. It is often a good
     * idea to first check with {@link #containsKey(int)} whether the given key
     * has a value associated or not, i.e. whether there exists an association
     * for the given key or not.
     * 
     * @param key
     *            the key to be searched for.
     * @return the value associated with the specified key; <tt>null</tt> if no
     *         such key is present.
     */
    public Object get(int key) {
        int i = indexOfKey(key);
        if (i < 0)
            return null; // not contained
        return values[i];
    }
    
    /**
     * @param key
     *            the key to be added to the receiver.
     * @return the index where the key would need to be inserted, if it is not
     *         already contained. Returns -index-1 if the key is already
     *         contained at slot index. Therefore, if the returned index < 0,
     *         then it is already contained at slot -index-1. If the returned
     *         index >= 0, then it is NOT already contained and should be
     *         inserted at slot index.
     */
    protected int indexOfInsertion(int key) {
        final int tab[] = table;
        final byte stat[] = state;
        final int length = tab.length;
        
        final int hash = key & 0x7FFFFFFF;
        int i = hash % length;
        int decrement = hash % (length - 2); // double hashing, see
                                             // http://www.eece.unm.edu/faculty/heileman/hash/node4.html
        // int decrement = (hash / length) % length;
        if (decrement == 0)
            decrement = 1;
        
        // stop if we find a removed or free slot, or if we find the key itself
        // do NOT skip over removed slots (yes, open addressing is like that...)
        while (stat[i] == FULL && tab[i] != key) {
            i -= decrement;
            // hashCollisions++;
            if (i < 0)
                i += length;
        }
        
        if (stat[i] == REMOVED) {
            // stop if we find a free slot, or if we find the key itself.
            // do skip over removed slots (yes, open addressing is like that...)
            // assertion: there is at least one FREE slot.
            int j = i;
            while (stat[i] != FREE && (stat[i] == REMOVED || tab[i] != key)) {
                i -= decrement;
                // hashCollisions++;
                if (i < 0)
                    i += length;
            }
            if (stat[i] == FREE)
                i = j;
        }
        
        if (stat[i] == FULL) {
            // key already contained at slot i.
            // return a negative number identifying the slot.
            return -i - 1;
        }
        // not already contained, should be inserted at slot i.
        // return a number >= 0 identifying the slot.
        return i;
    }
    
    /**
     * @param key
     *            the key to be searched in the receiver.
     * @return the index where the key is contained in the receiver, returns -1
     *         if the key was not found.
     */
    protected int indexOfKey(int key) {
        final int tab[] = table;
        final byte stat[] = state;
        final int length = tab.length;
        
        final int hash = key & 0x7FFFFFFF;
        int i = hash % length;
        int decrement = hash % (length - 2); // double hashing, see
                                             // http://www.eece.unm.edu/faculty/heileman/hash/node4.html
        // int decrement = (hash / length) % length;
        if (decrement == 0)
            decrement = 1;
        
        // stop if we find a free slot, or if we find the key itself.
        // do skip over removed slots (yes, open addressing is like that...)
        while (stat[i] != FREE && (stat[i] == REMOVED || tab[i] != key)) {
            i -= decrement;
            // hashCollisions++;
            if (i < 0)
                i += length;
        }
        
        if (stat[i] == FREE)
            return -1; // not found
        return i; // found, return index where key is contained
    }
    
    /**
     * Removes all entries from this map
     */
    public void clear() {
        setUp(highWaterMark, minLoadFactor, maxLoadFactor);
    }
    
    /**
     * Chooses a new prime table capacity optimized for growing that
     * (approximately) satisfies the invariant
     * <tt>c * minLoadFactor <= size <= c * maxLoadFactor</tt> and has at least
     * one FREE slot for the given size.
     */
    protected int chooseGrowCapacity(int size, double minLoad, double maxLoad) {
        return nextPrime(Math.max(size + 1, (int) ((4 * size / (3 * minLoad + maxLoad)))));
    }
    
    /**
     * Returns a prime number which is <code>&gt;= desiredCapacity</code> and
     * very close to <code>desiredCapacity</code> (within 11% if
     * <code>desiredCapacity &gt;= 1000</code>).
     * 
     * @param desiredCapacity
     *            the capacity desired by the user.
     * @return the capacity which should be used for a hashtable.
     */
    protected int nextPrime(int desiredCapacity) {
        return PrimeFinder.nextPrime(desiredCapacity);
    }
    
    /**
     * Returns new high water mark threshold based on current capacity and
     * maxLoadFactor.
     * 
     * @return int the new threshold.
     */
    protected int chooseHighWaterMark(int capacity, double maxLoad) {
        return Math.min(capacity - 2, (int) (capacity * maxLoad)); // makes sure
                                                                   // there is
                                                                   // always at
                                                                   // least one
                                                                   // FREE slot
    }
    
    /**
     * Associates the given key with the given value. Replaces any old
     * <tt>(key,someOtherValue)</tt> association, if existing.
     * 
     * @param key
     *            the key the value shall be associated with.
     * @param value
     *            the value to be associated.
     * @return <tt>true</tt> if the receiver did not already contain such a key;
     *         <tt>false</tt> if the receiver did already contain such a key -
     *         the new value has now replaced the formerly associated value.
     */
    public boolean put(int key, Object value) {
        int i = indexOfInsertion(key);
        if (i < 0) { // already contained
            i = -i - 1;
            this.values[i] = value;
            return false;
        }
        
        if (this.distinct > this.highWaterMark) {
            int newCapacity = chooseGrowCapacity(this.distinct + 1, this.minLoadFactor, this.maxLoadFactor);
            rehash(newCapacity);
            return put(key, value);
        }
        
        this.table[i] = key;
        this.values[i] = value;
        if (this.state[i] == FREE)
            this.freeEntries--;
        this.state[i] = FULL;
        this.distinct++;
        
        if (this.freeEntries < 1) { // delta
            int newCapacity = chooseGrowCapacity(this.distinct + 1, this.minLoadFactor, this.maxLoadFactor);
            rehash(newCapacity);
        }
        
        return true;
    }
    
    /**
     * Rehashes the contents of the receiver into a new table with a smaller or
     * larger capacity. This method is called automatically when the number of
     * keys in the receiver exceeds the high water mark or falls below the low
     * water mark.
     */
    protected void rehash(int newCapacity) {
        int oldCapacity = table.length;
        // if (oldCapacity == newCapacity) return;
        
        int oldTable[] = table;
        Object oldValues[] = values;
        byte oldState[] = state;
        
        int newTable[] = new int[newCapacity];
        Object newValues[] = new Object[newCapacity];
        byte newState[] = new byte[newCapacity];
        
        this.highWaterMark = chooseHighWaterMark(newCapacity, this.maxLoadFactor);
        
        this.table = newTable;
        this.values = newValues;
        this.state = newState;
        this.freeEntries = newCapacity - this.distinct; // delta
        
        for (int i = oldCapacity; i-- > 0;) {
            if (oldState[i] == FULL) {
                int element = oldTable[i];
                int index = indexOfInsertion(element);
                newTable[index] = element;
                newValues[index] = oldValues[i];
                newState[index] = FULL;
            }
        }
    }
    
    /**
     * Chooses a new prime table capacity optimized for shrinking that
     * (approximately) satisfies the invariant
     * <tt>c * minLoadFactor <= size <= c * maxLoadFactor</tt> and has at least
     * one FREE slot for the given size.
     */
    protected int chooseShrinkCapacity(int size, double minLoad, double maxLoad) {
        return nextPrime(Math.max(size + 1, (int) ((4 * size / (minLoad + 3 * maxLoad)))));
    }
    
    /**
     * Initializes the receiver.
     * 
     * @param initialCapacity
     *            the initial capacity of the receiver.
     * @param minLoadFactor
     *            the minLoadFactor of the receiver.
     * @param maxLoadFactor
     *            the maxLoadFactor of the receiver.
     * @throws IllegalArgumentException
     *             if
     *             <tt>initialCapacity < 0 || (minLoadFactor < 0.0 || minLoadFactor >= 1.0) || (maxLoadFactor <= 0.0 || maxLoadFactor >= 1.0) || (minLoadFactor >= maxLoadFactor)</tt>
     *             .
     */
    protected void setUp(int initialCapacity, double minLoadFactor, double maxLoadFactor) {
        int capacity = initialCapacity;
        
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Initial Capacity must not be less than zero: " + initialCapacity);
        if (minLoadFactor < 0.0 || minLoadFactor >= 1.0)
            throw new IllegalArgumentException("Illegal minLoadFactor: " + minLoadFactor);
        if (maxLoadFactor <= 0.0 || maxLoadFactor >= 1.0)
            throw new IllegalArgumentException("Illegal maxLoadFactor: " + maxLoadFactor);
        if (minLoadFactor >= maxLoadFactor)
            throw new IllegalArgumentException("Illegal minLoadFactor: " + minLoadFactor + " and maxLoadFactor: " + maxLoadFactor);
        
        capacity = nextPrime(capacity);
        if (capacity == 0)
            capacity = 1; // open addressing needs at least one FREE slot at any
                          // time.
            
        this.table = new int[capacity];
        this.values = new Object[capacity];
        this.state = new byte[capacity];
        
        // memory will be exhausted long before this pathological case happens,
        // anyway.
        this.minLoadFactor = minLoadFactor;
        if (capacity == PrimeFinder.largestPrime)
            this.maxLoadFactor = 1.0;
        else
            this.maxLoadFactor = maxLoadFactor;
        
        this.distinct = 0;
        this.freeEntries = capacity; // delta
        
        // lowWaterMark will be established upon first expansion.
        // establishing it now (upon instance construction) would immediately
        // make the table shrink upon first put(...).
        // After all the idea of an "initialCapacity" implies violating
        // lowWaterMarks when an object is young.
        // See ensureCapacity(...)
        this.highWaterMark = chooseHighWaterMark(capacity, this.maxLoadFactor);
    }
}
