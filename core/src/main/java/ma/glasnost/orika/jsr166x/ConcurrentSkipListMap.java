/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package ma.glasnost.orika.jsr166x;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * A scalable {@link ConcurrentNavigableMap} implementation.  This
 * class maintains a map in ascending key order, sorted according to
 * the <i>natural order</i> for the key's class (see {@link
 * Comparable}), or by the {@link Comparator} provided at creation
 * time, depending on which constructor is used.
 *
 * <p>This class implements a concurrent variant of <a
 * href="http://www.cs.umd.edu/~pugh/">SkipLists</a> providing
 * expected average <i>log(n)</i> time cost for the
 * <tt>containsKey</tt>, <tt>get</tt>, <tt>put</tt> and
 * <tt>remove</tt> operations and their variants.  Insertion, removal,
 * update, and access operations safely execute concurrently by
 * multiple threads. Iterators are <i>weakly consistent</i>, returning
 * elements reflecting the state of the map at some point at or since
 * the creation of the iterator.  They do <em>not</em> throw {@link
 * ConcurrentModificationException}, and may proceed concurrently with
 * other operations. Ascending key ordered views and their iterators
 * are faster than descending ones.
 *
 * <p>All <tt>Map.Entry</tt> pairs returned by methods in this class
 * and its views represent snapshots of mappings at the time they were
 * produced. They do <em>not</em> support the <tt>Entry.setValue</tt>
 * method. (Note however that it is possible to change mappings in the
 * associated map using <tt>put</tt>, <tt>putIfAbsent</tt>, or
 * <tt>replace</tt>, depending on exactly which effect you need.)
 *
 * <p>Beware that, unlike in most collections, the <tt>size</tt>
 * method is <em>not</em> a constant-time operation. Because of the
 * asynchronous nature of these maps, determining the current number
 * of elements requires a traversal of the elements.  Additionally,
 * the bulk operations <tt>putAll</tt>, <tt>equals</tt>, and
 * <tt>clear</tt> are <em>not</em> guaranteed to be performed
 * atomically. For example, an iterator operating concurrently with a
 * <tt>putAll</tt> operation might view only some of the added
 * elements.
 *
 * <p>This class and its views and iterators implement all of the
 * <em>optional</em> methods of the {@link Map} and {@link Iterator}
 * interfaces. Like most other concurrent collections, this class does
 * not permit the use of <tt>null</tt> keys or values because some
 * null return values cannot be reliably distinguished from the
 * absence of elements.
 *
 * @author Doug Lea
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentSkipListMap<K,V> extends AbstractMap<K,V>
    implements ConcurrentNavigableMap<K,V>,
               Cloneable,
               java.io.Serializable {
    /*
     * This class implements a tree-like two-dimensionally linked skip
     * list in which the index levels are represented in separate
     * nodes from the base nodes holding data.  There are two reasons
     * for taking this approach instead of the usual array-based
     * structure: 1) Array based implementations seem to encounter
     * more complexity and overhead 2) We can use cheaper algorithms
     * for the heavily-traversed index lists than can be used for the
     * base lists.  Here's a picture of some of the basics for a
     * possible list with 2 levels of index:
     *
     * Head nodes          Index nodes
     * +-+    right        +-+                      +-+
     * |2|---------------->| |--------------------->| |->null
     * +-+                 +-+                      +-+
     *  | down              |                        |
     *  v                   v                        v
     * +-+            +-+  +-+       +-+            +-+       +-+
     * |1|----------->| |->| |------>| |----------->| |------>| |->null
     * +-+            +-+  +-+       +-+            +-+       +-+
     *  v              |    |         |              |         |
     * Nodes  next     v    v         v              v         v
     * +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+
     * | |->|A|->|B|->|C|->|D|->|E|->|F|->|G|->|H|->|I|->|J|->|K|->null
     * +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+
     *
     * The base lists use a variant of the HM linked ordered set
     * algorithm. See Tim Harris, "A pragmatic implementation of
     * non-blocking linked lists"
     * http://www.cl.cam.ac.uk/~tlh20/publications.html and Maged
     * Michael "High Performance Dynamic Lock-Free Hash Tables and
     * List-Based Sets"
     * http://www.research.ibm.com/people/m/michael/pubs.htm.  The
     * basic idea in these lists is to mark the "next" pointers of
     * deleted nodes when deleting to avoid conflicts with concurrent
     * insertions, and when traversing to keep track of triples
     * (predecessor, node, successor) in order to detect when and how
     * to unlink these deleted nodes.
     *
     * Rather than using mark-bits to mark list deletions (which can
     * be slow and space-intensive using AtomicMarkedReference), nodes
     * use direct CAS'able next pointers.  On deletion, instead of
     * marking a pointer, they splice in another node that can be
     * thought of as standing for a marked pointer (indicating this by
     * using otherwise impossible field values).  Using plain nodes
     * acts roughly like "boxed" implementations of marked pointers,
     * but uses new nodes only when nodes are deleted, not for every
     * link.  This requires less space and supports faster
     * traversal. Even if marked references were better supported by
     * JVMs, traversal using this technique might still be faster
     * because any search need only read ahead one more node than
     * otherwise required (to check for trailing marker) rather than
     * unmasking mark bits or whatever on each read.
     *
     * This approach maintains the essential property needed in the HM
     * algorithm of changing the next-pointer of a deleted node so
     * that any other CAS of it will fail, but implements the idea by
     * changing the pointer to point to a different node, not by
     * marking it.  While it would be possible to further squeeze
     * space by defining marker nodes not to have key/value fields, it
     * isn't worth the extra type-testing overhead.  The deletion
     * markers are rarely encountered during traversal and are
     * normally quickly garbage collected. (Note that this technique
     * would not work well in systems without garbage collection.)
     *
     * In addition to using deletion markers, the lists also use
     * nullness of value fields to indicate deletion, in a style
     * similar to typical lazy-deletion schemes.  If a node's value is
     * null, then it is considered logically deleted and ignored even
     * though it is still reachable. This maintains proper control of
     * concurrent replace vs delete operations -- an attempted replace
     * must fail if a delete beat it by nulling field, and a delete
     * must return the last non-null value held in the field. (Note:
     * Null, rather than some special marker, is used for value fields
     * here because it just so happens to mesh with the Map API
     * requirement that method get returns null if there is no
     * mapping, which allows nodes to remain concurrently readable
     * even when deleted. Using any other marker value here would be
     * messy at best.)
     *
     * Here's the sequence of events for a deletion of node n with
     * predecessor b and successor f, initially:
     *
     *        +------+       +------+      +------+
     *   ...  |   b  |------>|   n  |----->|   f  | ...
     *        +------+       +------+      +------+
     *
     * 1. CAS n's value field from non-null to null.
     *    From this point on, no public operations encountering
     *    the node consider this mapping to exist. However, other
     *    ongoing insertions and deletions might still modify
     *    n's next pointer.
     *
     * 2. CAS n's next pointer to point to a new marker node.
     *    From this point on, no other nodes can be appended to n.
     *    which avoids deletion errors in CAS-based linked lists.
     *
     *        +------+       +------+      +------+       +------+
     *   ...  |   b  |------>|   n  |----->|marker|------>|   f  | ...
     *        +------+       +------+      +------+       +------+
     *
     * 3. CAS b's next pointer over both n and its marker.
     *    From this point on, no new traversals will encounter n,
     *    and it can eventually be GCed.
     *        +------+                                    +------+
     *   ...  |   b  |----------------------------------->|   f  | ...
     *        +------+                                    +------+
     *
     * A failure at step 1 leads to simple retry due to a lost race
     * with another operation. Steps 2-3 can fail because some other
     * thread noticed during a traversal a node with null value and
     * helped out by marking and/or unlinking.  This helping-out
     * ensures that no thread can become stuck waiting for progress of
     * the deleting thread.  The use of marker nodes slightly
     * complicates helping-out code because traversals must track
     * consistent reads of up to four nodes (b, n, marker, f), not
     * just (b, n, f), although the next field of a marker is
     * immutable, and once a next field is CAS'ed to point to a
     * marker, it never again changes, so this requires less care.
     *
     * Skip lists add indexing to this scheme, so that the base-level
     * traversals start close to the locations being found, inserted
     * or deleted -- usually base level traversals only traverse a few
     * nodes. This doesn't change the basic algorithm except for the
     * need to make sure base traversals start at predecessors (here,
     * b) that are not (structurally) deleted, otherwise retrying
     * after processing the deletion.
     *
     * Index levels are maintained as lists with volatile next fields,
     * using CAS to link and unlink.  Races are allowed in index-list
     * operations that can (rarely) fail to link in a new index node
     * or delete one. (We can't do this of course for data nodes.)
     * However, even when this happens, the index lists remain sorted,
     * so correctly serve as indices.  This can impact performance,
     * but since skip lists are probabilistic anyway, the net result
     * is that under contention, the effective "p" value may be lower
     * than its nominal value. And race windows are kept small enough
     * that in practice these failures are rare, even under a lot of
     * contention.
     *
     * The fact that retries (for both base and index lists) are
     * relatively cheap due to indexing allows some minor
     * simplifications of retry logic. Traversal restarts are
     * performed after most "helping-out" CASes. This isn't always
     * strictly necessary, but the implicit backoffs tend to help
     * reduce other downstream failed CAS's enough to outweigh restart
     * cost.  This worsens the worst case, but seems to improve even
     * highly contended cases.
     *
     * Unlike most skip-list implementations, index insertion and
     * deletion here require a separate traversal pass occuring after
     * the base-level action, to add or remove index nodes.  This adds
     * to single-threaded overhead, but improves contended
     * multithreaded performance by narrowing interference windows,
     * and allows deletion to ensure that all index nodes will be made
     * unreachable upon return from a public remove operation, thus
     * avoiding unwanted garbage retention. This is more important
     * here than in some other data structures because we cannot null
     * out node fields referencing user keys since they might still be
     * read by other ongoing traversals.
     *
     * Indexing uses skip list parameters that maintain good search
     * performance while using sparser-than-usual indices: The
     * hardwired parameters k=1, p=0.5 (see method randomLevel) mean
     * that about one-quarter of the nodes have indices. Of those that
     * do, half have one level, a quarter have two, and so on (see
     * Pugh's Skip List Cookbook, sec 3.4).  The expected total space
     * requirement for a map is slightly less than for the current
     * implementation of java.util.TreeMap.
     *
     * Changing the level of the index (i.e, the height of the
     * tree-like structure) also uses CAS. The head index has initial
     * level/height of one. Creation of an index with height greater
     * than the current level adds a level to the head index by
     * CAS'ing on a new top-most head. To maintain good performance
     * after a lot of removals, deletion methods heuristically try to
     * reduce the height if the topmost levels appear to be empty.
     * This may encounter races in which it possible (but rare) to
     * reduce and "lose" a level just as it is about to contain an
     * index (that will then never be encountered). This does no
     * structural harm, and in practice appears to be a better option
     * than allowing unrestrained growth of levels.
     *
     * The code for all this is more verbose than you'd like. Most
     * operations entail locating an element (or position to insert an
     * element). The code to do this can't be nicely factored out
     * because subsequent uses require a snapshot of predecessor
     * and/or successor and/or value fields which can't be returned
     * all at once, at least not without creating yet another object
     * to hold them -- creating such little objects is an especially
     * bad idea for basic internal search operations because it adds
     * to GC overhead.  (This is one of the few times I've wished Java
     * had macros.) Instead, some traversal code is interleaved within
     * insertion and removal operations.  The control logic to handle
     * all the retry conditions is sometimes twisty. Most search is
     * broken into 2 parts. findPredecessor() searches index nodes
     * only, returning a base-level predecessor of the key. findNode()
     * finishes out the base-level search. Even with this factoring,
     * there is a fair amount of near-duplication of code to handle
     * variants.
     *
     * For explanation of algorithms sharing at least a couple of
     * features with this one, see Mikhail Fomitchev's thesis
     * (http://www.cs.yorku.ca/~mikhail/), Keir Fraser's thesis
     * (http://www.cl.cam.ac.uk/users/kaf24/), and Hakan Sundell's
     * thesis (http://www.cs.chalmers.se/~phs/).
     *
     * Given the use of tree-like index nodes, you might wonder why
     * this doesn't use some kind of search tree instead, which would
     * support somewhat faster search operations. The reason is that
     * there are no known efficient lock-free insertion and deletion
     * algorithms for search trees. The immutability of the "down"
     * links of index nodes (as opposed to mutable "left" fields in
     * true trees) makes this tractable using only CAS operations.
     *
     * Notation guide for local variables
     * Node:         b, n, f    for  predecessor, node, successor
     * Index:        q, r, d    for index node, right, down.
     *               t          for another index node
     * Head:         h
     * Levels:       j
     * Keys:         k, key
     * Values:       v, value
     * Comparisons:  c
     */

    private static final long serialVersionUID = -8627078645895051609L;

    /**
     * Special value used to identify base-level header
     */
    private static final Object BASE_HEADER = new Object();

    /**
     * The topmost head index of the skiplist.
     */
    private transient volatile HeadIndex<K,V> head;

    /**
     * The Comparator used to maintain order in this Map, or null
     * if using natural order.
     * @serial
     */
    private final Comparator<? super K> comparator;

    /**
     * Seed for simple random number generator.  Not volatile since it
     * doesn't matter too much if different threads don't see updates.
     */
    private transient int randomSeed;

    /** Lazily initialized key set */
    private transient KeySet keySet;
    /** Lazily initialized entry set */
    private transient EntrySet entrySet;
    /** Lazily initialized values collection */
    private transient Values values;
    /** Lazily initialized descending key set */
    private transient DescendingKeySet descendingKeySet;
    /** Lazily initialized descending entry set */
    private transient DescendingEntrySet descendingEntrySet;

    /**
     * Initialize or reset state. Needed by constructors, clone,
     * clear, readObject. and ConcurrentSkipListSet.clone.
     * (Note that comparator must be separately initialized.)
     */
    final void initialize() {
        keySet = null;
        entrySet = null;
        values = null;
        descendingEntrySet = null;
        descendingKeySet = null;
        randomSeed = (int) System.nanoTime();
        head = new HeadIndex<K,V>(new Node<K,V>(null, BASE_HEADER, null),
                                  null, null, 1);
    }

    /** Updater for casHead */
    private static final
        AtomicReferenceFieldUpdater<ConcurrentSkipListMap, HeadIndex>
        headUpdater = AtomicReferenceFieldUpdater.newUpdater
        (ConcurrentSkipListMap.class, HeadIndex.class, "head");

    /**
     * compareAndSet head node
     */
    private boolean casHead(HeadIndex<K,V> cmp, HeadIndex<K,V> val) {
        return headUpdater.compareAndSet(this, cmp, val);
    }

    /* ---------------- Nodes -------------- */

    /**
     * Nodes hold keys and values, and are singly linked in sorted
     * order, possibly with some intervening marker nodes. The list is
     * headed by a dummy node accessible as head.node. The value field
     * is declared only as Object because it takes special non-V
     * values for marker and header nodes.
     */
    static final class Node<K,V> {
        final K key;
        volatile Object value;
        volatile Node<K,V> next;

        /**
         * Creates a new regular node.
         */
        Node(K key, Object value, Node<K,V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        /**
         * Creates a new marker node. A marker is distinguished by
         * having its value field point to itself.  Marker nodes also
         * have null keys, a fact that is exploited in a few places,
         * but this doesn't distinguish markers from the base-level
         * header node (head.node), which also has a null key.
         */
        Node(Node<K,V> next) {
            this.key = null;
            this.value = this;
            this.next = next;
        }

        /** Updater for casNext */
        static final AtomicReferenceFieldUpdater<Node, Node>
            nextUpdater = AtomicReferenceFieldUpdater.newUpdater
            (Node.class, Node.class, "next");

        /** Updater for casValue */
        static final AtomicReferenceFieldUpdater<Node, Object>
            valueUpdater = AtomicReferenceFieldUpdater.newUpdater
            (Node.class, Object.class, "value");

        /**
         * compareAndSet value field
         */
        boolean casValue(Object cmp, Object val) {
            return valueUpdater.compareAndSet(this, cmp, val);
        }

        /**
         * compareAndSet next field
         */
        boolean casNext(Node<K,V> cmp, Node<K,V> val) {
            return nextUpdater.compareAndSet(this, cmp, val);
        }

        /**
         * Return true if this node is a marker. This method isn't
         * actually called in an any current code checking for markers
         * because callers will have already read value field and need
         * to use that read (not another done here) and so directly
         * test if value points to node.
         * @param n a possibly null reference to a node
         * @return true if this node is a marker node
         */
        boolean isMarker() {
            return value == this;
        }

        /**
         * Return true if this node is the header of base-level list.
         * @return true if this node is header node
         */
        boolean isBaseHeader() {
            return value == BASE_HEADER;
        }

        /**
         * Tries to append a deletion marker to this node.
         * @param f the assumed current successor of this node
         * @return true if successful
         */
        boolean appendMarker(Node<K,V> f) {
            return casNext(f, new Node<K,V>(f));
        }

        /**
         * Helps out a deletion by appending marker or unlinking from
         * predecessor. This is called during traversals when value
         * field seen to be null.
         * @param b predecessor
         * @param f successor
         */
        void helpDelete(Node<K,V> b, Node<K,V> f) {
            /*
             * Rechecking links and then doing only one of the
             * help-out stages per call tends to minimize CAS
             * interference among helping threads.
             */
            if (f == next && this == b.next) {
                if (f == null || f.value != f) // not already marked
                    appendMarker(f);
                else
                    b.casNext(this, f.next);
            }
        }

        /**
         * Return value if this node contains a valid key-value pair,
         * else null.
         * @return this node's value if it isn't a marker or header or
         * is deleted, else null.
         */
        V getValidValue() {
            Object v = value;
            if (v == this || v == BASE_HEADER)
                return null;
            return (V)v;
        }

        /**
         * Create and return a new SnapshotEntry holding current
         * mapping if this node holds a valid value, else null
         * @return new entry or null
         */
        SnapshotEntry<K,V> createSnapshot() {
            V v = getValidValue();
            if (v == null)
                return null;
            return new SnapshotEntry(key, v);
        }
    }

    /* ---------------- Indexing -------------- */

    /**
     * Index nodes represent the levels of the skip list.  To improve
     * search performance, keys of the underlying nodes are cached.
     * Note that even though both Nodes and Indexes have
     * forward-pointing fields, they have different types and are
     * handled in different ways, that can't nicely be captured by
     * placing field in a shared abstract class.
     */
    static class Index<K,V> {
        final K key;
        final Node<K,V> node;
        final Index<K,V> down;
        volatile Index<K,V> right;

        /**
         * Creates index node with given values
         */
        Index(Node<K,V> node, Index<K,V> down, Index<K,V> right) {
            this.node = node;
            this.key = node.key;
            this.down = down;
            this.right = right;
        }

        /** Updater for casRight */
        static final AtomicReferenceFieldUpdater<Index, Index>
            rightUpdater = AtomicReferenceFieldUpdater.newUpdater
            (Index.class, Index.class, "right");

        /**
         * compareAndSet right field
         */
        final boolean casRight(Index<K,V> cmp, Index<K,V> val) {
            return rightUpdater.compareAndSet(this, cmp, val);
        }

        /**
         * Returns true if the node this indexes has been deleted.
         * @return true if indexed node is known to be deleted
         */
        final boolean indexesDeletedNode() {
            return node.value == null;
        }

        /**
         * Tries to CAS newSucc as successor.  To minimize races with
         * unlink that may lose this index node, if the node being
         * indexed is known to be deleted, it doesn't try to link in.
         * @param succ the expected current successor
         * @param newSucc the new successor
         * @return true if successful
         */
        final boolean link(Index<K,V> succ, Index<K,V> newSucc) {
            Node<K,V> n = node;
            newSucc.right = succ;
            return n.value != null && casRight(succ, newSucc);
        }

        /**
         * Tries to CAS right field to skip over apparent successor
         * succ.  Fails (forcing a retraversal by caller) if this node
         * is known to be deleted.
         * @param succ the expected current successor
         * @return true if successful
         */
        final boolean unlink(Index<K,V> succ) {
            return !indexesDeletedNode() && casRight(succ, succ.right);
        }
    }

    /* ---------------- Head nodes -------------- */

    /**
     * Nodes heading each level keep track of their level.
     */
    static final class HeadIndex<K,V> extends Index<K,V> {
        final int level;
        HeadIndex(Node<K,V> node, Index<K,V> down, Index<K,V> right, int level) {
            super(node, down, right);
            this.level = level;
        }
    }

    /* ---------------- Map.Entry support -------------- */

    /**
     * An immutable representation of a key-value mapping as it
     * existed at some point in time. This class does <em>not</em>
     * support the <tt>Map.Entry.setValue</tt> method.
     */
    static class SnapshotEntry<K,V> implements Map.Entry<K,V> {
        private final K key;
        private final V value;

        /**
         * Creates a new entry representing the given key and value.
         * @param key the key
         * @param value the value
         */
        SnapshotEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the key corresponding to this entry.
         *
         * @return the key corresponding to this entry.
         */
        public K getKey() {
            return key;
        }

        /**
         * Returns the value corresponding to this entry.
         *
         * @return the value corresponding to this entry.
         */
        public V getValue() {
            return value;
        }

        /**
         * Always fails, throwing <tt>UnsupportedOperationException</tt>.
         * @throws UnsupportedOperationException always.
         */
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        // inherit javadoc
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry)o;
            // As mandated by Map.Entry spec:
            return ((key==null ?
                     e.getKey()==null : key.equals(e.getKey()))  &&
                    (value==null ?
                     e.getValue()==null : value.equals(e.getValue())));
        }


        // inherit javadoc
        public int hashCode() {
            // As mandated by Map.Entry spec:
            return ((key==null   ? 0 : key.hashCode()) ^
                    (value==null ? 0 : value.hashCode()));
        }

        /**
         * Returns a String consisting of the key followed by an
         * equals sign (<tt>"="</tt>) followed by the associated
         * value.
         * @return a String representation of this entry.
         */
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    /* ---------------- Comparison utilities -------------- */

    /**
     * Represents a key with a comparator as a Comparable.
     *
     * Because most sorted collections seem to use natural order on
     * Comparables (Strings, Integers, etc), most internal methods are
     * geared to use them. This is generally faster than checking
     * per-comparison whether to use comparator or comparable because
     * it doesn't require a (Comparable) cast for each comparison.
     * (Optimizers can only sometimes remove such redundant checks
     * themselves.) When Comparators are used,
     * ComparableUsingComparators are created so that they act in the
     * same way as natural orderings. This penalizes use of
     * Comparators vs Comparables, which seems like the right
     * tradeoff.
     */
    static final class ComparableUsingComparator<K> implements Comparable<K> {
        final K actualKey;
        final Comparator<? super K> cmp;
        ComparableUsingComparator(K key, Comparator<? super K> cmp) {
            this.actualKey = key;
            this.cmp = cmp;
        }
        public int compareTo(K k2) {
            return cmp.compare(actualKey, k2);
        }
    }

    /**
     * If using comparator, return a ComparableUsingComparator, else
     * cast key as Comparator, which may cause ClassCastException,
     * which is propagated back to caller.
     */
    private Comparable<K> comparable(Object key) throws ClassCastException {
        if (key == null)
            throw new NullPointerException();
        return (comparator != null)
            ? new ComparableUsingComparator(key, comparator)
            : (Comparable<K>)key;
    }

    /**
     * Compare using comparator or natural ordering. Used when the
     * ComparableUsingComparator approach doesn't apply.
     */
    int compare(K k1, K k2) throws ClassCastException {
        Comparator<? super K> cmp = comparator;
        if (cmp != null)
            return cmp.compare(k1, k2);
        else
            return ((Comparable<K>)k1).compareTo(k2);
    }

    /**
     * Return true if given key greater than or equal to least and
     * strictly less than fence, bypassing either test if least or
     * fence oare null. Needed mainly in submap operations.
     */
    boolean inHalfOpenRange(K key, K least, K fence) {
        if (key == null)
            throw new NullPointerException();
        return ((least == null || compare(key, least) >= 0) &&
                (fence == null || compare(key, fence) <  0));
    }

    /**
     * Return true if given key greater than or equal to least and less
     * or equal to fence. Needed mainly in submap operations.
     */
    boolean inOpenRange(K key, K least, K fence) {
        if (key == null)
            throw new NullPointerException();
        return ((least == null || compare(key, least) >= 0) &&
                (fence == null || compare(key, fence) <= 0));
    }

    /* ---------------- Traversal -------------- */

    /**
     * Return a base-level node with key strictly less than given key,
     * or the base-level header if there is no such node.  Also
     * unlinks indexes to deleted nodes found along the way.  Callers
     * rely on this side-effect of clearing indices to deleted nodes.
     * @param key the key
     * @return a predecessor of key
     */
    private Node<K,V> findPredecessor(Comparable<K> key) {
        for (;;) {
            Index<K,V> q = head;
            for (;;) {
                Index<K,V> d, r;
                if ((r = q.right) != null) {
                    if (r.indexesDeletedNode()) {
                        if (q.unlink(r))
                            continue; // reread r
                        else
                            break;    // restart
                    }
                    if (key.compareTo(r.key) > 0) {
                        q = r;
                        continue;
                    }
                }
                if ((d = q.down) != null)
                    q = d;
                else
                    return q.node;
            }
        }
    }

    /**
     * Return node holding key or null if no such, clearing out any
     * deleted nodes seen along the way.  Repeatedly traverses at
     * base-level looking for key starting at predecessor returned
     * from findPredecessor, processing base-level deletions as
     * encountered. Some callers rely on this side-effect of clearing
     * deleted nodes.
     *
     * Restarts occur, at traversal step centered on node n, if:
     *
     *   (1) After reading n's next field, n is no longer assumed
     *       predecessor b's current successor, which means that
     *       we don't have a consistent 3-node snapshot and so cannot
     *       unlink any subsequent deleted nodes encountered.
     *
     *   (2) n's value field is null, indicating n is deleted, in
     *       which case we help out an ongoing structural deletion
     *       before retrying.  Even though there are cases where such
     *       unlinking doesn't require restart, they aren't sorted out
     *       here because doing so would not usually outweigh cost of
     *       restarting.
     *
     *   (3) n is a marker or n's predecessor's value field is null,
     *       indicating (among other possibilities) that
     *       findPredecessor returned a deleted node. We can't unlink
     *       the node because we don't know its predecessor, so rely
     *       on another call to findPredecessor to notice and return
     *       some earlier predecessor, which it will do. This check is
     *       only strictly needed at beginning of loop, (and the
     *       b.value check isn't strictly needed at all) but is done
     *       each iteration to help avoid contention with other
     *       threads by callers that will fail to be able to change
     *       links, and so will retry anyway.
     *
     * The traversal loops in doPut, doRemove, and findNear all
     * include the same three kinds of checks. And specialized
     * versions appear in doRemoveFirst, doRemoveLast, findFirst, and
     * findLast. They can't easily share code because each uses the
     * reads of fields held in locals occurring in the orders they
     * were performed.
     *
     * @param key the key
     * @return node holding key, or null if no such.
     */
    private Node<K,V> findNode(Comparable<K> key) {
        for (;;) {
            Node<K,V> b = findPredecessor(key);
            Node<K,V> n = b.next;
            for (;;) {
                if (n == null)
                    return null;
                Node<K,V> f = n.next;
                if (n != b.next)                // inconsistent read
                    break;
                Object v = n.value;
                if (v == null) {                // n is deleted
                    n.helpDelete(b, f);
                    break;
                }
                if (v == n || b.value == null)  // b is deleted
                    break;
                int c = key.compareTo(n.key);
                if (c < 0)
                    return null;
                if (c == 0)
                    return n;
                b = n;
                n = f;
            }
        }
    }

    /**
     * Specialized variant of findNode to perform Map.get. Does a weak
     * traversal, not bothering to fix any deleted index nodes,
     * returning early if it happens to see key in index, and passing
     * over any deleted base nodes, falling back to getUsingFindNode
     * only if it would otherwise return value from an ongoing
     * deletion. Also uses "bound" to eliminate need for some
     * comparisons (see Pugh Cookbook). Also folds uses of null checks
     * and node-skipping because markers have null keys.
     * @param okey the key
     * @return the value, or null if absent
     */
    private V doGet(Object okey) {
        Comparable<K> key = comparable(okey);
        K bound = null;
        Index<K,V> q = head;
        for (;;) {
            K rk;
            Index<K,V> d, r;
            if ((r = q.right) != null &&
                (rk = r.key) != null && rk != bound) {
                int c = key.compareTo(rk);
                if (c > 0) {
                    q = r;
                    continue;
                }
                if (c == 0) {
                    Object v = r.node.value;
                    return (v != null) ? (V)v : getUsingFindNode(key);
                }
                bound = rk;
            }
            if ((d = q.down) != null)
                q = d;
            else {
                for (Node<K,V> n = q.node.next; n != null; n = n.next) {
                    K nk = n.key;
                    if (nk != null) {
                        int c = key.compareTo(nk);
                        if (c == 0) {
                            Object v = n.value;
                            return (v != null) ? (V)v : getUsingFindNode(key);
                        }
                        if (c < 0)
                            return null;
                    }
                }
                return null;
            }
        }
    }

    /**
     * Perform map.get via findNode.  Used as a backup if doGet
     * encounters an in-progress deletion.
     * @param key the key
     * @return the value, or null if absent
     */
    private V getUsingFindNode(Comparable<K> key) {
        /*
         * Loop needed here and elsewhere in case value field goes
         * null just as it is about to be returned, in which case we
         * lost a race with a deletion, so must retry.
         */
        for (;;) {
            Node<K,V> n = findNode(key);
            if (n == null)
                return null;
            Object v = n.value;
            if (v != null)
                return (V)v;
        }
    }

    /* ---------------- Insertion -------------- */

    /**
     * Main insertion method.  Adds element if not present, or
     * replaces value if present and onlyIfAbsent is false.
     * @param kkey the key
     * @param value  the value that must be associated with key
     * @param onlyIfAbsent if should not insert if already present
     * @return the old value, or null if newly inserted
     */
    private V doPut(K kkey, V value, boolean onlyIfAbsent) {
        Comparable<K> key = comparable(kkey);
        for (;;) {
            Node<K,V> b = findPredecessor(key);
            Node<K,V> n = b.next;
            for (;;) {
                if (n != null) {
                    Node<K,V> f = n.next;
                    if (n != b.next)               // inconsistent read
                        break;
                    Object v = n.value;
                    if (v == null) {               // n is deleted
                        n.helpDelete(b, f);
                        break;
                    }
                    if (v == n || b.value == null) // b is deleted
                        break;
                    int c = key.compareTo(n.key);
                    if (c > 0) {
                        b = n;
                        n = f;
                        continue;
                    }
                    if (c == 0) {
                        if (onlyIfAbsent || n.casValue(v, value))
                            return (V)v;
                        else
                            break; // restart if lost race to replace value
                    }
                    // else c < 0; fall through
                }

                Node<K,V> z = new Node<K,V>(kkey, value, n);
                if (!b.casNext(n, z))
                    break;         // restart if lost race to append to b
                int level = randomLevel();
                if (level > 0)
                    insertIndex(z, level);
                return null;
            }
        }
    }

    /**
     * Return a random level for inserting a new node.
     * Hardwired to k=1, p=0.5, max 31.
     *
     * This uses a cheap pseudo-random function that according to
     * http://home1.gte.net/deleyd/random/random4.html was used in
     * Turbo Pascal. It seems the fastest usable one here. The low
     * bits are apparently not very random (the original used only
     * upper 16 bits) so we traverse from highest bit down (i.e., test
     * sign), thus hardly ever use lower bits.
     */
    private int randomLevel() {
        int level = 0;
        int r = randomSeed;
        randomSeed = r * 134775813 + 1;
        if (r < 0) {
            while ((r <<= 1) > 0)
                ++level;
        }
        return level;
    }

    /**
     * Create and add index nodes for given node.
     * @param z the node
     * @param level the level of the index
     */
    private void insertIndex(Node<K,V> z, int level) {
        HeadIndex<K,V> h = head;
        int max = h.level;

        if (level <= max) {
            Index<K,V> idx = null;
            for (int i = 1; i <= level; ++i)
                idx = new Index<K,V>(z, idx, null);
            addIndex(idx, h, level);

        } else { // Add a new level
            /*
             * To reduce interference by other threads checking for
             * empty levels in tryReduceLevel, new levels are added
             * with initialized right pointers. Which in turn requires
             * keeping levels in an array to access them while
             * creating new head index nodes from the opposite
             * direction.
             */
            level = max + 1;
            Index<K,V>[] idxs = (Index<K,V>[])new Index[level+1];
            Index<K,V> idx = null;
            for (int i = 1; i <= level; ++i)
                idxs[i] = idx = new Index<K,V>(z, idx, null);

            HeadIndex<K,V> oldh;
            int k;
            for (;;) {
                oldh = head;
                int oldLevel = oldh.level;
                if (level <= oldLevel) { // lost race to add level
                    k = level;
                    break;
                }
                HeadIndex<K,V> newh = oldh;
                Node<K,V> oldbase = oldh.node;
                for (int j = oldLevel+1; j <= level; ++j)
                    newh = new HeadIndex<K,V>(oldbase, newh, idxs[j], j);
                if (casHead(oldh, newh)) {
                    k = oldLevel;
                    break;
                }
            }
            addIndex(idxs[k], oldh, k);
        }
    }

    /**
     * Add given index nodes from given level down to 1.
     * @param idx the topmost index node being inserted
     * @param h the value of head to use to insert. This must be
     * snapshotted by callers to provide correct insertion level
     * @param indexLevel the level of the index
     */
    private void addIndex(Index<K,V> idx, HeadIndex<K,V> h, int indexLevel) {
        // Track next level to insert in case of retries
        int insertionLevel = indexLevel;
        Comparable<K> key = comparable(idx.key);

        // Similar to findPredecessor, but adding index nodes along
        // path to key.
        for (;;) {
            Index<K,V> q = h;
            Index<K,V> t = idx;
            int j = h.level;
            for (;;) {
                Index<K,V> r = q.right;
                if (r != null) {
                    // compare before deletion check avoids needing recheck
                    int c = key.compareTo(r.key);
                    if (r.indexesDeletedNode()) {
                        if (q.unlink(r))
                            continue;
                        else
                            break;
                    }
                    if (c > 0) {
                        q = r;
                        continue;
                    }
                }

                if (j == insertionLevel) {
                    // Don't insert index if node already deleted
                    if (t.indexesDeletedNode()) {
                        findNode(key); // cleans up
                        return;
                    }
                    if (!q.link(r, t))
                        break; // restart
                    if (--insertionLevel == 0) {
                        // need final deletion check before return
                        if (t.indexesDeletedNode())
                            findNode(key);
                        return;
                    }
                }

                if (j > insertionLevel && j <= indexLevel)
                    t = t.down;
                q = q.down;
                --j;
            }
        }
    }

    /* ---------------- Deletion -------------- */

    /**
     * Main deletion method. Locates node, nulls value, appends a
     * deletion marker, unlinks predecessor, removes associated index
     * nodes, and possibly reduces head index level.
     *
     * Index nodes are cleared out simply by calling findPredecessor.
     * which unlinks indexes to deleted nodes found along path to key,
     * which will include the indexes to this node.  This is done
     * unconditionally. We can't check beforehand whether there are
     * index nodes because it might be the case that some or all
     * indexes hadn't been inserted yet for this node during initial
     * search for it, and we'd like to ensure lack of garbage
     * retention, so must call to be sure.
     *
     * @param okey the key
     * @param value if non-null, the value that must be
     * associated with key
     * @return the node, or null if not found
     */
    private V doRemove(Object okey, Object value) {
        Comparable<K> key = comparable(okey);
        for (;;) {
            Node<K,V> b = findPredecessor(key);
            Node<K,V> n = b.next;
            for (;;) {
                if (n == null)
                    return null;
                Node<K,V> f = n.next;
                if (n != b.next)                    // inconsistent read
                    break;
                Object v = n.value;
                if (v == null) {                    // n is deleted
                    n.helpDelete(b, f);
                    break;
                }
                if (v == n || b.value == null)      // b is deleted
                    break;
                int c = key.compareTo(n.key);
                if (c < 0)
                    return null;
                if (c > 0) {
                    b = n;
                    n = f;
                    continue;
                }
                if (value != null && !value.equals(v))
                    return null;
                if (!n.casValue(v, null))
                    break;
                if (!n.appendMarker(f) || !b.casNext(n, f))
                    findNode(key);                  // Retry via findNode
                else {
                    findPredecessor(key);           // Clean index
                    if (head.right == null)
                        tryReduceLevel();
                }
                return (V)v;
            }
        }
    }

    /**
     * Possibly reduce head level if it has no nodes.  This method can
     * (rarely) make mistakes, in which case levels can disappear even
     * though they are about to contain index nodes. This impacts
     * performance, not correctness.  To minimize mistakes as well as
     * to reduce hysteresis, the level is reduced by one only if the
     * topmost three levels look empty. Also, if the removed level
     * looks non-empty after CAS, we try to change it back quick
     * before anyone notices our mistake! (This trick works pretty
     * well because this method will practically never make mistakes
     * unless current thread stalls immediately before first CAS, in
     * which case it is very unlikely to stall again immediately
     * afterwards, so will recover.)
     *
     * We put up with all this rather than just let levels grow
     * because otherwise, even a small map that has undergone a large
     * number of insertions and removals will have a lot of levels,
     * slowing down access more than would an occasional unwanted
     * reduction.
     */
    private void tryReduceLevel() {
        HeadIndex<K,V> h = head;
        HeadIndex<K,V> d;
        HeadIndex<K,V> e;
        if (h.level > 3 &&
            (d = (HeadIndex<K,V>)h.down) != null &&
            (e = (HeadIndex<K,V>)d.down) != null &&
            e.right == null &&
            d.right == null &&
            h.right == null &&
            casHead(h, d) && // try to set
            h.right != null) // recheck
            casHead(d, h);   // try to backout
    }

    /**
     * Version of remove with boolean return. Needed by view classes
     */
    boolean removep(Object key) {
        return doRemove(key, null) != null;
    }

    /* ---------------- Finding and removing first element -------------- */

    /**
     * Specialized variant of findNode to get first valid node
     * @return first node or null if empty
     */
    Node<K,V> findFirst() {
        for (;;) {
            Node<K,V> b = head.node;
            Node<K,V> n = b.next;
            if (n == null)
                return null;
            if (n.value != null)
                return n;
            n.helpDelete(b, n.next);
        }
    }

    /**
     * Remove first entry; return either its key or a snapshot.
     * @param keyOnly if true return key, else return SnapshotEntry
     * (This is a little ugly, but avoids code duplication.)
     * @return null if empty, first key if keyOnly true, else key,value entry
     */
    Object doRemoveFirst(boolean keyOnly) {
        for (;;) {
            Node<K,V> b = head.node;
            Node<K,V> n = b.next;
            if (n == null)
                return null;
            Node<K,V> f = n.next;
            if (n != b.next)
                continue;
            Object v = n.value;
            if (v == null) {
                n.helpDelete(b, f);
                continue;
            }
            if (!n.casValue(v, null))
                continue;
            if (!n.appendMarker(f) || !b.casNext(n, f))
                findFirst(); // retry
            clearIndexToFirst();
            K key = n.key;
            return keyOnly ? key : new SnapshotEntry<K,V>(key, (V)v);
        }
    }

    /**
     * Clear out index nodes associated with deleted first entry.
     * Needed by doRemoveFirst
     */
    private void clearIndexToFirst() {
        for (;;) {
            Index<K,V> q = head;
            for (;;) {
                Index<K,V> r = q.right;
                if (r != null && r.indexesDeletedNode() && !q.unlink(r))
                    break;
                if ((q = q.down) == null) {
                    if (head.right == null)
                        tryReduceLevel();
                    return;
                }
            }
        }
    }

   /**
     * Remove first entry; return key or null if empty.
     */
    K pollFirstKey() {
        return (K)doRemoveFirst(true);
    }

    /* ---------------- Finding and removing last element -------------- */

    /**
     * Specialized version of find to get last valid node
     * @return last node or null if empty
     */
    Node<K,V> findLast() {
        /*
         * findPredecessor can't be used to traverse index level
         * because this doesn't use comparisons.  So traversals of
         * both levels are folded together.
         */
        Index<K,V> q = head;
        for (;;) {
            Index<K,V> d, r;
            if ((r = q.right) != null) {
                if (r.indexesDeletedNode()) {
                    q.unlink(r);
                    q = head; // restart
                }
                else
                    q = r;
            } else if ((d = q.down) != null) {
                q = d;
            } else {
                Node<K,V> b = q.node;
                Node<K,V> n = b.next;
                for (;;) {
                    if (n == null)
                        return b.isBaseHeader() ? null : b;
                    Node<K,V> f = n.next;            // inconsistent read
                    if (n != b.next)
                        break;
                    Object v = n.value;
                    if (v == null) {                 // n is deleted
                        n.helpDelete(b, f);
                        break;
                    }
                    if (v == n || b.value == null)   // b is deleted
                        break;
                    b = n;
                    n = f;
                }
                q = head; // restart
            }
        }
    }


    /**
     * Specialized version of doRemove for last entry.
     * @param keyOnly if true return key, else return SnapshotEntry
     * @return null if empty, last key if keyOnly true, else key,value entry
     */
    Object doRemoveLast(boolean keyOnly) {
        for (;;) {
            Node<K,V> b = findPredecessorOfLast();
            Node<K,V> n = b.next;
            if (n == null) {
                if (b.isBaseHeader())               // empty
                    return null;
                else
                    continue; // all b's successors are deleted; retry
            }
            for (;;) {
                Node<K,V> f = n.next;
                if (n != b.next)                    // inconsistent read
                    break;
                Object v = n.value;
                if (v == null) {                    // n is deleted
                    n.helpDelete(b, f);
                    break;
                }
                if (v == n || b.value == null)      // b is deleted
                    break;
                if (f != null) {
                    b = n;
                    n = f;
                    continue;
                }
                if (!n.casValue(v, null))
                    break;
                K key = n.key;
                Comparable<K> ck = comparable(key);
                if (!n.appendMarker(f) || !b.casNext(n, f))
                    findNode(ck);                  // Retry via findNode
                else {
                    findPredecessor(ck);           // Clean index
                    if (head.right == null)
                        tryReduceLevel();
                }
                return keyOnly ? key : new SnapshotEntry<K,V>(key, (V)v);
            }
        }
    }

    /**
     * Specialized variant of findPredecessor to get predecessor of
     * last valid node. Needed by doRemoveLast. It is possible that
     * all successors of returned node will have been deleted upon
     * return, in which case this method can be retried.
     * @return likely predecessor of last node.
     */
    private Node<K,V> findPredecessorOfLast() {
        for (;;) {
            Index<K,V> q = head;
            for (;;) {
                Index<K,V> d, r;
                if ((r = q.right) != null) {
                    if (r.indexesDeletedNode()) {
                        q.unlink(r);
                        break;    // must restart
                    }
                    // proceed as far across as possible without overshooting
                    if (r.node.next != null) {
                        q = r;
                        continue;
                    }
                }
                if ((d = q.down) != null)
                    q = d;
                else
                    return q.node;
            }
        }
    }

    /**
     * Remove last entry; return key or null if empty.
     */
    K pollLastKey() {
        return (K)doRemoveLast(true);
    }

    /* ---------------- Relational operations -------------- */

    // Control values OR'ed as arguments to findNear

    private static final int EQ = 1;
    private static final int LT = 2;
    private static final int GT = 0; // Actually checked as !LT

    /**
     * Utility for ceiling, floor, lower, higher methods.
     * @param kkey the key
     * @param rel the relation -- OR'ed combination of EQ, LT, GT
     * @return nearest node fitting relation, or null if no such
     */
    Node<K,V> findNear(K kkey, int rel) {
        Comparable<K> key = comparable(kkey);
        for (;;) {
            Node<K,V> b = findPredecessor(key);
            Node<K,V> n = b.next;
            for (;;) {
                if (n == null)
                    return ((rel & LT) == 0 || b.isBaseHeader()) ? null : b;
                Node<K,V> f = n.next;
                if (n != b.next)                  // inconsistent read
                    break;
                Object v = n.value;
                if (v == null) {                  // n is deleted
                    n.helpDelete(b, f);
                    break;
                }
                if (v == n || b.value == null)    // b is deleted
                    break;
                int c = key.compareTo(n.key);
                if ((c == 0 && (rel & EQ) != 0) ||
                    (c <  0 && (rel & LT) == 0))
                    return n;
                if ( c <= 0 && (rel & LT) != 0)
                    return b.isBaseHeader() ? null : b;
                b = n;
                n = f;
            }
        }
    }

    /**
     * Return SnapshotEntry for results of findNear.
     * @param kkey the key
     * @param rel the relation -- OR'ed combination of EQ, LT, GT
     * @return Entry fitting relation, or null if no such
     */
    SnapshotEntry<K,V> getNear(K kkey, int rel) {
        for (;;) {
            Node<K,V> n = findNear(kkey, rel);
            if (n == null)
                return null;
            SnapshotEntry<K,V> e = n.createSnapshot();
            if (e != null)
                return e;
        }
    }

    /**
     * Return ceiling, or first node if key is <tt>null</tt>
     */
    Node<K,V> findCeiling(K key) {
        return (key == null) ? findFirst() : findNear(key, GT|EQ);
    }

    /**
     * Return lower node, or last node if key is <tt>null</tt>
     */
    Node<K,V> findLower(K key) {
        return (key == null) ? findLast() : findNear(key, LT);
    }

    /**
     * Return SnapshotEntry or key for results of findNear ofter screening
     * to ensure result is in given range. Needed by submaps.
     * @param kkey the key
     * @param rel the relation -- OR'ed combination of EQ, LT, GT
     * @param least minimum allowed key value
     * @param fence key greater than maximum allowed key value
     * @param keyOnly if true return key, else return SnapshotEntry
     * @return Key or Entry fitting relation, or <tt>null</tt> if no such
     */
    Object getNear(K kkey, int rel, K least, K fence, boolean keyOnly) {
        K key = kkey;
        // Don't return keys less than least
        if ((rel & LT) == 0) {
            if (compare(key, least) < 0) {
                key = least;
                rel = rel | EQ;
            }
        }

        for (;;) {
            Node<K,V> n = findNear(key, rel);
            if (n == null || !inHalfOpenRange(n.key, least, fence))
                return null;
            K k = n.key;
            V v = n.getValidValue();
            if (v != null)
                return keyOnly ? k : new SnapshotEntry<K,V>(k, v);
        }
    }

    /**
     * Find and remove least element of subrange.
     * @param least minimum allowed key value
     * @param fence key greater than maximum allowed key value
     * @param keyOnly if true return key, else return SnapshotEntry
     * @return least Key or Entry, or <tt>null</tt> if no such
     */
    Object removeFirstEntryOfSubrange(K least, K fence, boolean keyOnly) {
        for (;;) {
            Node<K,V> n = findCeiling(least);
            if (n == null)
                return null;
            K k = n.key;
            if (fence != null && compare(k, fence) >= 0)
                return null;
            V v = doRemove(k, null);
            if (v != null)
                return keyOnly ? k : new SnapshotEntry<K,V>(k, v);
        }
    }

    /**
     * Find and remove greatest element of subrange.
     * @param least minimum allowed key value
     * @param fence key greater than maximum allowed key value
     * @param keyOnly if true return key, else return SnapshotEntry
     * @return least Key or Entry, or <tt>null</tt> if no such
     */
    Object removeLastEntryOfSubrange(K least, K fence, boolean keyOnly) {
        for (;;) {
            Node<K,V> n = findLower(fence);
            if (n == null)
                return null;
            K k = n.key;
            if (least != null && compare(k, least) < 0)
                return null;
            V v = doRemove(k, null);
            if (v != null)
                return keyOnly ? k : new SnapshotEntry<K,V>(k, v);
        }
    }

    /* ---------------- Constructors -------------- */

    /**
     * Constructs a new empty map, sorted according to the keys' natural
     * order.
     */
    public ConcurrentSkipListMap() {
        this.comparator = null;
        initialize();
    }

    /**
     * Constructs a new empty map, sorted according to the given comparator.
     *
     * @param c the comparator that will be used to sort this map.  A
     *        <tt>null</tt> value indicates that the keys' <i>natural
     *        ordering</i> should be used.
     */
    public ConcurrentSkipListMap(Comparator<? super K> c) {
        this.comparator = c;
        initialize();
    }

    /**
     * Constructs a new map containing the same mappings as the given map,
     * sorted according to the keys' <i>natural order</i>.
     *
     * @param  m the map whose mappings are to be placed in this map.
     * @throws ClassCastException if the keys in m are not Comparable, or
     *         are not mutually comparable.
     * @throws NullPointerException if the specified map is <tt>null</tt>.
     */
    public ConcurrentSkipListMap(Map<? extends K, ? extends V> m) {
        this.comparator = null;
        initialize();
        putAll(m);
    }

    /**
     * Constructs a new map containing the same mappings as the given
     * <tt>SortedMap</tt>, sorted according to the same ordering.
     * @param m the sorted map whose mappings are to be placed in this
     * map, and whose comparator is to be used to sort this map.
     * @throws NullPointerException if the specified sorted map is
     * <tt>null</tt>.
     */
    public ConcurrentSkipListMap(SortedMap<K, ? extends V> m) {
        this.comparator = m.comparator();
        initialize();
        buildFromSorted(m);
    }

    /**
     * Returns a shallow copy of this <tt>Map</tt> instance. (The keys and
     * values themselves are not cloned.)
     *
     * @return a shallow copy of this Map.
     */
    public Object clone() {
        ConcurrentSkipListMap<K,V> clone = null;
        try {
            clone = (ConcurrentSkipListMap<K,V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }

        clone.initialize();
        clone.buildFromSorted(this);
        return clone;
    }

    /**
     * Streamlined bulk insertion to initialize from elements of
     * given sorted map.  Call only from constructor or clone
     * method.
     */
    private void buildFromSorted(SortedMap<K, ? extends V> map) {
        if (map == null)
            throw new NullPointerException();

        HeadIndex<K,V> h = head;
        Node<K,V> basepred = h.node;

        // Track the current rightmost node at each level. Uses an
        // ArrayList to avoid committing to initial or maximum level.
        ArrayList<Index<K,V>> preds = new ArrayList<Index<K,V>>();

        // initialize
        for (int i = 0; i <= h.level; ++i)
            preds.add(null);
        Index<K,V> q = h;
        for (int i = h.level; i > 0; --i) {
            preds.set(i, q);
            q = q.down;
        }

        Iterator<? extends Map.Entry<? extends K, ? extends V>> it =
            map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<? extends K, ? extends V> e = it.next();
            int j = randomLevel();
            if (j > h.level) j = h.level + 1;
            K k = e.getKey();
            V v = e.getValue();
            if (k == null || v == null)
                throw new NullPointerException();
            Node<K,V> z = new Node<K,V>(k, v, null);
            basepred.next = z;
            basepred = z;
            if (j > 0) {
                Index<K,V> idx = null;
                for (int i = 1; i <= j; ++i) {
                    idx = new Index<K,V>(z, idx, null);
                    if (i > h.level)
                        h = new HeadIndex<K,V>(h.node, h, idx, i);

                    if (i < preds.size()) {
                        preds.get(i).right = idx;
                        preds.set(i, idx);
                    } else
                        preds.add(idx);
                }
            }
        }
        head = h;
    }

    /* ---------------- Serialization -------------- */

    /**
     * Save the state of the <tt>Map</tt> instance to a stream.
     *
     * @serialData The key (Object) and value (Object) for each
     * key-value mapping represented by the Map, followed by
     * <tt>null</tt>. The key-value mappings are emitted in key-order
     * (as determined by the Comparator, or by the keys' natural
     * ordering if no Comparator).
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out the Comparator and any hidden stuff
        s.defaultWriteObject();

        // Write out keys and values (alternating)
        for (Node<K,V> n = findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null) {
                s.writeObject(n.key);
                s.writeObject(v);
            }
        }
        s.writeObject(null);
    }

    /**
     * Reconstitute the <tt>Map</tt> instance from a stream.
     */
    private void readObject(final java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in the Comparator and any hidden stuff
        s.defaultReadObject();
        // Reset transients
        initialize();

        /*
         * This is nearly identical to buildFromSorted, but is
         * distinct because readObject calls can't be nicely adapted
         * as the kind of iterator needed by buildFromSorted. (They
         * can be, but doing so requires type cheats and/or creation
         * of adaptor classes.) It is simpler to just adapt the code.
         */

        HeadIndex<K,V> h = head;
        Node<K,V> basepred = h.node;
        ArrayList<Index<K,V>> preds = new ArrayList<Index<K,V>>();
        for (int i = 0; i <= h.level; ++i)
            preds.add(null);
        Index<K,V> q = h;
        for (int i = h.level; i > 0; --i) {
            preds.set(i, q);
            q = q.down;
        }

        for (;;) {
            Object k = s.readObject();
            if (k == null)
                break;
            Object v = s.readObject();
            if (v == null)
                throw new NullPointerException();
            K key = (K) k;
            V val = (V) v;
            int j = randomLevel();
            if (j > h.level) j = h.level + 1;
            Node<K,V> z = new Node<K,V>(key, val, null);
            basepred.next = z;
            basepred = z;
            if (j > 0) {
                Index<K,V> idx = null;
                for (int i = 1; i <= j; ++i) {
                    idx = new Index<K,V>(z, idx, null);
                    if (i > h.level)
                        h = new HeadIndex<K,V>(h.node, h, idx, i);

                    if (i < preds.size()) {
                        preds.get(i).right = idx;
                        preds.set(i, idx);
                    } else
                        preds.add(idx);
                }
            }
        }
        head = h;
    }

    /* ------ Map API methods ------ */

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     * @param key key whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the
     *            specified key.
     * @throws ClassCastException if the key cannot be compared with the keys
     *                  currently in the map.
     * @throws NullPointerException if the key is <tt>null</tt>.
     */
    public boolean containsKey(Object key) {
        return doGet(key) != null;
    }

    /**
     * Returns the value to which this map maps the specified key.  Returns
     * <tt>null</tt> if the map contains no mapping for this key.
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *               <tt>null</tt> if the map contains no mapping for the key.
     * @throws ClassCastException if the key cannot be compared with the keys
     *                  currently in the map.
     * @throws NullPointerException if the key is <tt>null</tt>.
     */
    public V get(Object key) {
        return doGet(key);
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     *
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.
     * @throws ClassCastException if the key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if the key or value are <tt>null</tt>.
     */
    public V put(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        return doPut(key, value, false);
    }

    /**
     * Removes the mapping for this key from this Map if present.
     *
     * @param  key key for which mapping should be removed
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.
     *
     * @throws ClassCastException if the key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if the key is <tt>null</tt>.
     */
    public V remove(Object key) {
        return doRemove(key, null);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.  This operation requires time linear in the
     * Map size.
     *
     * @param value value whose presence in this Map is to be tested.
     * @return  <tt>true</tt> if a mapping to <tt>value</tt> exists;
     *          <tt>false</tt> otherwise.
     * @throws  NullPointerException  if the value is <tt>null</tt>.
     */
    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        for (Node<K,V> n = findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null && value.equals(v))
                return true;
        }
        return false;
    }

    /**
     * Returns the number of elements in this map.  If this map
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, it
     * returns <tt>Integer.MAX_VALUE</tt>.
     *
     * <p>Beware that, unlike in most collections, this method is
     * <em>NOT</em> a constant-time operation. Because of the
     * asynchronous nature of these maps, determining the current
     * number of elements requires traversing them all to count them.
     * Additionally, it is possible for the size to change during
     * execution of this method, in which case the returned result
     * will be inaccurate. Thus, this method is typically not very
     * useful in concurrent applications.
     *
     * @return  the number of elements in this map.
     */
    public int size() {
        long count = 0;
        for (Node<K,V> n = findFirst(); n != null; n = n.next) {
            if (n.getValidValue() != null)
                ++count;
        }
        return (count >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int)count;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        return findFirst() == null;
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        initialize();
    }

    /**
     * Returns a set view of the keys contained in this map.  The set is
     * backed by the map, so changes to the map are reflected in the set, and
     * vice-versa.  The set supports element removal, which removes the
     * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     * The view's <tt>iterator</tt> is a "weakly consistent" iterator that
     * will never throw {@link java.util.ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to)
     * reflect any modifications subsequent to construction.
     *
     * @return a set view of the keys contained in this map.
     */
    public Set<K> keySet() {
        /*
         * Note: Lazy intialization works here and for other views
         * because view classes are stateless/immutable so it doesn't
         * matter wrt correctness if more than one is created (which
         * will only rarely happen).  Even so, the following idiom
         * conservatively ensures that the method returns the one it
         * created if it does so, not one created by another racing
         * thread.
         */
        KeySet ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet());
    }

    /**
     * Returns a set view of the keys contained in this map in
     * descending order.  The set is backed by the map, so changes to
     * the map are reflected in the set, and vice-versa.  The set
     * supports element removal, which removes the corresponding
     * mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>,
     * and <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.  The view's
     * <tt>iterator</tt> is a "weakly consistent" iterator that will
     * never throw {@link java.util.ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed
     * to) reflect any modifications subsequent to construction.
     *
     * @return a set view of the keys contained in this map.
     */
    public Set<K> descendingKeySet() {
        /*
         * Note: Lazy intialization works here and for other views
         * because view classes are stateless/immutable so it doesn't
         * matter wrt correctness if more than one is created (which
         * will only rarely happen).  Even so, the following idiom
         * conservatively ensures that the method returns the one it
         * created if it does so, not one created by another racing
         * thread.
         */
        DescendingKeySet ks = descendingKeySet;
        return (ks != null) ? ks : (descendingKeySet = new DescendingKeySet());
    }

    /**
     * Returns a collection view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  The collection
     * supports element removal, which removes the corresponding
     * mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt>, and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.  The
     * view's <tt>iterator</tt> is a "weakly consistent" iterator that
     * will never throw {@link
     * java.util.ConcurrentModificationException}, and guarantees to
     * traverse elements as they existed upon construction of the
     * iterator, and may (but is not guaranteed to) reflect any
     * modifications subsequent to construction.
     *
     * @return a collection view of the values contained in this map.
     */
    public Collection<V> values() {
        Values vs = values;
        return (vs != null) ? vs : (values = new Values());
    }

    /**
     * Returns a collection view of the mappings contained in this
     * map.  Each element in the returned collection is a
     * <tt>Map.Entry</tt>.  The collection is backed by the map, so
     * changes to the map are reflected in the collection, and
     * vice-versa.  The collection supports element removal, which
     * removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.  The view's <tt>iterator</tt> is a
     * "weakly consistent" iterator that will never throw {@link
     * java.util.ConcurrentModificationException}, and guarantees to
     * traverse elements as they existed upon construction of the
     * iterator, and may (but is not guaranteed to) reflect any
     * modifications subsequent to construction. The
     * <tt>Map.Entry</tt> elements returned by
     * <tt>iterator.next()</tt> do <em>not</em> support the
     * <tt>setValue</tt> operation.
     *
     * @return a collection view of the mappings contained in this map.
     */
    public Set<Map.Entry<K,V>> entrySet() {
        EntrySet es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet());
    }

    /**
     * Returns a collection view of the mappings contained in this
     * map, in descending order.  Each element in the returned
     * collection is a <tt>Map.Entry</tt>.  The collection is backed
     * by the map, so changes to the map are reflected in the
     * collection, and vice-versa.  The collection supports element
     * removal, which removes the corresponding mapping from the map,
     * via the <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.  The view's <tt>iterator</tt> is a
     * "weakly consistent" iterator that will never throw {@link
     * java.util.ConcurrentModificationException}, and guarantees to
     * traverse elements as they existed upon construction of the
     * iterator, and may (but is not guaranteed to) reflect any
     * modifications subsequent to construction. The
     * <tt>Map.Entry</tt> elements returned by
     * <tt>iterator.next()</tt> do <em>not</em> support the
     * <tt>setValue</tt> operation.
     *
     * @return a collection view of the mappings contained in this map.
     */
    public Set<Map.Entry<K,V>> descendingEntrySet() {
        DescendingEntrySet es = descendingEntrySet;
        return (es != null) ? es : (descendingEntrySet = new DescendingEntrySet());
    }

    /* ---------------- AbstractMap Overrides -------------- */

    /**
     * Compares the specified object with this map for equality.
     * Returns <tt>true</tt> if the given object is also a map and the
     * two maps represent the same mappings.  More formally, two maps
     * <tt>t1</tt> and <tt>t2</tt> represent the same mappings if
     * <tt>t1.keySet().equals(t2.keySet())</tt> and for every key
     * <tt>k</tt> in <tt>t1.keySet()</tt>, <tt> (t1.get(k)==null ?
     * t2.get(k)==null : t1.get(k).equals(t2.get(k))) </tt>.  This
     * operation may return misleading results if either map is
     * concurrently modified during execution of this method.
     *
     * @param o object to be compared for equality with this map.
     * @return <tt>true</tt> if the specified object is equal to this map.
     */
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Map))
            return false;
        Map<K,V> t = (Map<K,V>) o;
        try {
            return (containsAllMappings(this, t) &&
                    containsAllMappings(t, this));
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    /**
     * Helper for equals -- check for containment, avoiding nulls.
     */
    static <K,V> boolean containsAllMappings(Map<K,V> a, Map<K,V> b) {
        Iterator<Entry<K,V>> it = b.entrySet().iterator();
        while (it.hasNext()) {
            Entry<K,V> e = it.next();
            Object k = e.getKey();
            Object v = e.getValue();
            if (k == null || v == null || !v.equals(a.get(k)))
                return false;
        }
        return true;
    }

    /* ------ ConcurrentMap API methods ------ */

    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     * This is equivalent to
     * <pre>
     *   if (!map.containsKey(key))
     *      return map.put(key, value);
     *   else
     *      return map.get(key);
     * </pre>
     * except that the action is performed atomically.
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.
     *
     * @throws ClassCastException if the key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if the key or value are <tt>null</tt>.
     */
    public V putIfAbsent(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        return doPut(key, value, true);
    }

    /**
     * Remove entry for key only if currently mapped to given value.
     * Acts as
     * <pre>
     *  if ((map.containsKey(key) && map.get(key).equals(value)) {
     *     map.remove(key);
     *     return true;
     * } else return false;
     * </pre>
     * except that the action is performed atomically.
     * @param key key with which the specified value is associated.
     * @param value value associated with the specified key.
     * @return true if the value was removed, false otherwise
     * @throws ClassCastException if the key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if the key or value are <tt>null</tt>.
     */
    public boolean remove(Object key, Object value) {
        if (value == null)
            throw new NullPointerException();
        return doRemove(key, value) != null;
    }

    /**
     * Replace entry for key only if currently mapped to given value.
     * Acts as
     * <pre>
     *  if ((map.containsKey(key) && map.get(key).equals(oldValue)) {
     *     map.put(key, newValue);
     *     return true;
     * } else return false;
     * </pre>
     * except that the action is performed atomically.
     * @param key key with which the specified value is associated.
     * @param oldValue value expected to be associated with the specified key.
     * @param newValue value to be associated with the specified key.
     * @return true if the value was replaced
     * @throws ClassCastException if the key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key, oldValue or newValue are
     * <tt>null</tt>.
     */
    public boolean replace(K key, V oldValue, V newValue) {
        if (oldValue == null || newValue == null)
            throw new NullPointerException();
        Comparable<K> k = comparable(key);
        for (;;) {
            Node<K,V> n = findNode(k);
            if (n == null)
                return false;
            Object v = n.value;
            if (v != null) {
                if (!oldValue.equals(v))
                    return false;
                if (n.casValue(v, newValue))
                    return true;
            }
        }
    }

    /**
     * Replace entry for key only if currently mapped to some value.
     * Acts as
     * <pre>
     *  if ((map.containsKey(key)) {
     *     return map.put(key, value);
     * } else return null;
     * </pre>
     * except that the action is performed atomically.
     * @param key key with which the specified value is associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.
     * @throws ClassCastException if the key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if the key or value are <tt>null</tt>.
     */
    public V replace(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        Comparable<K> k = comparable(key);
        for (;;) {
            Node<K,V> n = findNode(k);
            if (n == null)
                return null;
            Object v = n.value;
            if (v != null && n.casValue(v, value))
                return (V)v;
        }
    }

    /* ------ SortedMap API methods ------ */

    /**
     * Returns the comparator used to order this map, or <tt>null</tt>
     * if this map uses its keys' natural order.
     *
     * @return the comparator associated with this map, or
     * <tt>null</tt> if it uses its keys' natural sort method.
     */
    public Comparator<? super K> comparator() {
        return comparator;
    }

    /**
     * Returns the first (lowest) key currently in this map.
     *
     * @return the first (lowest) key currently in this map.
     * @throws    NoSuchElementException Map is empty.
     */
    public K firstKey() {
        Node<K,V> n = findFirst();
        if (n == null)
            throw new NoSuchElementException();
        return n.key;
    }

    /**
     * Returns the last (highest) key currently in this map.
     *
     * @return the last (highest) key currently in this map.
     * @throws    NoSuchElementException Map is empty.
     */
    public K lastKey() {
        Node<K,V> n = findLast();
        if (n == null)
            throw new NoSuchElementException();
        return n.key;
    }

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
     * @throws ClassCastException if <tt>fromKey</tt> and <tt>toKey</tt>
     *         cannot be compared to one another using this map's comparator
     *         (or, if the map has no comparator, using natural ordering).
     * @throws IllegalArgumentException if <tt>fromKey</tt> is greater than
     *         <tt>toKey</tt>.
     * @throws NullPointerException if <tt>fromKey</tt> or <tt>toKey</tt> is
     *               <tt>null</tt>.
     */
    public ConcurrentNavigableMap<K,V> subMap(K fromKey, K toKey) {
        if (fromKey == null || toKey == null)
            throw new NullPointerException();
        return new ConcurrentSkipListSubMap(this, fromKey, toKey);
    }

    /**
     * Returns a view of the portion of this map whose keys are
     * strictly less than <tt>toKey</tt>.  The returned sorted map is
     * backed by this map, so changes in the returned sorted map are
     * reflected in this map, and vice-versa.
     * @param toKey high endpoint (exclusive) of the headMap.
     * @return a view of the portion of this map whose keys are
     * strictly less than <tt>toKey</tt>.
     *
     * @throws ClassCastException if <tt>toKey</tt> is not compatible
     * with this map's comparator (or, if the map has no comparator,
     * if <tt>toKey</tt> does not implement <tt>Comparable</tt>).
     * @throws NullPointerException if <tt>toKey</tt> is <tt>null</tt>.
     */
    public ConcurrentNavigableMap<K,V> headMap(K toKey) {
        if (toKey == null)
            throw new NullPointerException();
        return new ConcurrentSkipListSubMap(this, null, toKey);
    }

    /**
     * Returns a view of the portion of this map whose keys are
     * greater than or equal to <tt>fromKey</tt>.  The returned sorted
     * map is backed by this map, so changes in the returned sorted
     * map are reflected in this map, and vice-versa.
     * @param fromKey low endpoint (inclusive) of the tailMap.
     * @return a view of the portion of this map whose keys are
     * greater than or equal to <tt>fromKey</tt>.
     * @throws ClassCastException if <tt>fromKey</tt> is not
     * compatible with this map's comparator (or, if the map has no
     * comparator, if <tt>fromKey</tt> does not implement
     * <tt>Comparable</tt>).
     * @throws NullPointerException if <tt>fromKey</tt> is <tt>null</tt>.
     */
    public ConcurrentNavigableMap<K,V>  tailMap(K fromKey) {
        if (fromKey == null)
            throw new NullPointerException();
        return new ConcurrentSkipListSubMap(this, fromKey, null);
    }

    /* ---------------- Relational operations -------------- */

    /**
     * Returns a key-value mapping associated with the least key
     * greater than or equal to the given key, or <tt>null</tt> if
     * there is no such entry. The returned entry does <em>not</em>
     * support the <tt>Entry.setValue</tt> method.
     *
     * @param key the key.
     * @return an Entry associated with ceiling of given key, or
     * <tt>null</tt> if there is no such Entry.
     * @throws ClassCastException if key cannot be compared with the
     * keys currently in the map.
     * @throws NullPointerException if key is <tt>null</tt>.
     */
    public Map.Entry<K,V> ceilingEntry(K key) {
        return getNear(key, GT|EQ);
    }

    /**
     * Returns least key greater than or equal to the given key, or
     * <tt>null</tt> if there is no such key.
     *
     * @param key the key.
     * @return the ceiling key, or <tt>null</tt>
     * if there is no such key.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt>.
     */
    public K ceilingKey(K key) {
        Node<K,V> n = findNear(key, GT|EQ);
        return (n == null) ? null : n.key;
    }

    /**
     * Returns a key-value mapping associated with the greatest
     * key strictly less than the given key, or <tt>null</tt> if there is no
     * such entry. The returned entry does <em>not</em> support
     * the <tt>Entry.setValue</tt> method.
     *
     * @param key the key.
     * @return an Entry with greatest key less than the given
     * key, or <tt>null</tt> if there is no such Entry.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt>.
     */
    public Map.Entry<K,V> lowerEntry(K key) {
        return getNear(key, LT);
    }

    /**
     * Returns the greatest key strictly less than the given key, or
     * <tt>null</tt> if there is no such key.
     *
     * @param key the key.
     * @return the greatest key less than the given
     * key, or <tt>null</tt> if there is no such key.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt>.
     */
    public K lowerKey(K key) {
        Node<K,V> n = findNear(key, LT);
        return (n == null) ? null : n.key;
    }

    /**
     * Returns a key-value mapping associated with the greatest key
     * less than or equal to the given key, or <tt>null</tt> if there
     * is no such entry. The returned entry does <em>not</em> support
     * the <tt>Entry.setValue</tt> method.
     *
     * @param key the key.
     * @return an Entry associated with floor of given key, or <tt>null</tt>
     * if there is no such Entry.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt>.
     */
    public Map.Entry<K,V> floorEntry(K key) {
        return getNear(key, LT|EQ);
    }

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
     * @throws NullPointerException if key is <tt>null</tt>.
     */
    public K floorKey(K key) {
        Node<K,V> n = findNear(key, LT|EQ);
        return (n == null) ? null : n.key;
    }

    /**
     * Returns a key-value mapping associated with the least key
     * strictly greater than the given key, or <tt>null</tt> if there
     * is no such entry. The returned entry does <em>not</em> support
     * the <tt>Entry.setValue</tt> method.
     *
     * @param key the key.
     * @return an Entry with least key greater than the given key, or
     * <tt>null</tt> if there is no such Entry.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt>.
     */
    public Map.Entry<K,V> higherEntry(K key) {
        return getNear(key, GT);
    }

    /**
     * Returns the least key strictly greater than the given key, or
     * <tt>null</tt> if there is no such key.
     *
     * @param key the key.
     * @return the least key greater than the given key, or
     * <tt>null</tt> if there is no such key.
     * @throws ClassCastException if key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException if key is <tt>null</tt>.
     */
    public K higherKey(K key) {
        Node<K,V> n = findNear(key, GT);
        return (n == null) ? null : n.key;
    }

    /**
     * Returns a key-value mapping associated with the least
     * key in this map, or <tt>null</tt> if the map is empty.
     * The returned entry does <em>not</em> support
     * the <tt>Entry.setValue</tt> method.
     *
     * @return an Entry with least key, or <tt>null</tt>
     * if the map is empty.
     */
    public Map.Entry<K,V> firstEntry() {
        for (;;) {
            Node<K,V> n = findFirst();
            if (n == null)
                return null;
            SnapshotEntry<K,V> e = n.createSnapshot();
            if (e != null)
                return e;
        }
    }

    /**
     * Returns a key-value mapping associated with the greatest
     * key in this map, or <tt>null</tt> if the map is empty.
     * The returned entry does <em>not</em> support
     * the <tt>Entry.setValue</tt> method.
     *
     * @return an Entry with greatest key, or <tt>null</tt>
     * if the map is empty.
     */
    public Map.Entry<K,V> lastEntry() {
        for (;;) {
            Node<K,V> n = findLast();
            if (n == null)
                return null;
            SnapshotEntry<K,V> e = n.createSnapshot();
            if (e != null)
                return e;
        }
    }

    /**
     * Removes and returns a key-value mapping associated with
     * the least key in this map, or <tt>null</tt> if the map is empty.
     * The returned entry does <em>not</em> support
     * the <tt>Entry.setValue</tt> method.
     *
     * @return the removed first entry of this map, or <tt>null</tt>
     * if the map is empty.
     */
    public Map.Entry<K,V> pollFirstEntry() {
        return (SnapshotEntry<K,V>)doRemoveFirst(false);
    }

    /**
     * Removes and returns a key-value mapping associated with
     * the greatest key in this map, or <tt>null</tt> if the map is empty.
     * The returned entry does <em>not</em> support
     * the <tt>Entry.setValue</tt> method.
     *
     * @return the removed last entry of this map, or <tt>null</tt>
     * if the map is empty.
     */
    public Map.Entry<K,V> pollLastEntry() {
        return (SnapshotEntry<K,V>)doRemoveLast(false);
    }


    /* ---------------- Iterators -------------- */

    /**
     * Base of ten kinds of iterator classes:
     *   ascending:  {map, submap} X {key, value, entry}
     *   descending: {map, submap} X {key, entry}
     */
    abstract class Iter {
        /** the last node returned by next() */
        Node<K,V> last;
        /** the next node to return from next(); */
        Node<K,V> next;
        /** Cache of next value field to maintain weak consistency */
        Object nextValue;

        Iter() {}

        public final boolean hasNext() {
            return next != null;
        }

        /** initialize ascending iterator for entire range  */
        final void initAscending() {
            for (;;) {
                next = findFirst();
                if (next == null)
                    break;
                nextValue = next.value;
                if (nextValue != null && nextValue != next)
                    break;
            }
        }

        /**
         * initialize ascending iterator starting at given least key,
         * or first node if least is <tt>null</tt>, but not greater or
         * equal to fence, or end if fence is <tt>null</tt>.
         */
        final void initAscending(K least, K fence) {
            for (;;) {
                next = findCeiling(least);
                if (next == null)
                    break;
                nextValue = next.value;
                if (nextValue != null && nextValue != next) {
                    if (fence != null && compare(fence, next.key) <= 0) {
                        next = null;
                        nextValue = null;
                    }
                    break;
                }
            }
        }
        /** advance next to higher entry */
        final void ascend() {
            if ((last = next) == null)
                throw new NoSuchElementException();
            for (;;) {
                next = next.next;
                if (next == null)
                    break;
                nextValue = next.value;
                if (nextValue != null && nextValue != next)
                    break;
            }
        }

        /**
         * Version of ascend for submaps to stop at fence
         */
        final void ascend(K fence) {
            if ((last = next) == null)
                throw new NoSuchElementException();
            for (;;) {
                next = next.next;
                if (next == null)
                    break;
                nextValue = next.value;
                if (nextValue != null && nextValue != next) {
                    if (fence != null && compare(fence, next.key) <= 0) {
                        next = null;
                        nextValue = null;
                    }
                    break;
                }
            }
        }

        /** initialize descending iterator for entire range  */
        final void initDescending() {
            for (;;) {
                next = findLast();
                if (next == null)
                    break;
                nextValue = next.value;
                if (nextValue != null && nextValue != next)
                    break;
            }
        }

        /**
         * initialize descending iterator starting at key less
         * than or equal to given fence key, or
         * last node if fence is <tt>null</tt>, but not less than
         * least, or beginning if lest is <tt>null</tt>.
         */
        final void initDescending(K least, K fence) {
            for (;;) {
                next = findLower(fence);
                if (next == null)
                    break;
                nextValue = next.value;
                if (nextValue != null && nextValue != next) {
                    if (least != null && compare(least, next.key) > 0) {
                        next = null;
                        nextValue = null;
                    }
                    break;
                }
            }
        }

        /** advance next to lower entry */
        final void descend() {
            if ((last = next) == null)
                throw new NoSuchElementException();
            K k = last.key;
            for (;;) {
                next = findNear(k, LT);
                if (next == null)
                    break;
                nextValue = next.value;
                if (nextValue != null && nextValue != next)
                    break;
            }
        }

        /**
         * Version of descend for submaps to stop at least
         */
        final void descend(K least) {
            if ((last = next) == null)
                throw new NoSuchElementException();
            K k = last.key;
            for (;;) {
                next = findNear(k, LT);
                if (next == null)
                    break;
                nextValue = next.value;
                if (nextValue != null && nextValue != next) {
                    if (least != null && compare(least, next.key) > 0) {
                        next = null;
                        nextValue = null;
                    }
                    break;
                }
            }
        }

        public void remove() {
            Node<K,V> l = last;
            if (l == null)
                throw new IllegalStateException();
            // It would not be worth all of the overhead to directly
            // unlink from here. Using remove is fast enough.
            ConcurrentSkipListMap.this.remove(l.key);
        }

    }

    final class ValueIterator extends Iter implements Iterator<V> {
        ValueIterator() {
            initAscending();
        }
        public V next() {
            Object v = nextValue;
            ascend();
            return (V)v;
        }
    }

    final class KeyIterator extends Iter implements Iterator<K> {
        KeyIterator() {
            initAscending();
        }
        public K next() {
            Node<K,V> n = next;
            ascend();
            return n.key;
        }
    }

    class SubMapValueIterator extends Iter implements Iterator<V> {
        final K fence;
        SubMapValueIterator(K least, K fence) {
            initAscending(least, fence);
            this.fence = fence;
        }

        public V next() {
            Object v = nextValue;
            ascend(fence);
            return (V)v;
        }
    }

    final class SubMapKeyIterator extends Iter implements Iterator<K> {
        final K fence;
        SubMapKeyIterator(K least, K fence) {
            initAscending(least, fence);
            this.fence = fence;
        }

        public K next() {
            Node<K,V> n = next;
            ascend(fence);
            return n.key;
        }
    }

    final class DescendingKeyIterator extends Iter implements Iterator<K> {
        DescendingKeyIterator() {
            initDescending();
        }
        public K next() {
            Node<K,V> n = next;
            descend();
            return n.key;
        }
    }

    final class DescendingSubMapKeyIterator extends Iter implements Iterator<K> {
        final K least;
        DescendingSubMapKeyIterator(K least, K fence) {
            initDescending(least, fence);
            this.least = least;
        }

        public K next() {
            Node<K,V> n = next;
            descend(least);
            return n.key;
        }
    }

    /**
     * Entry iterators use the same trick as in ConcurrentHashMap and
     * elsewhere of using the iterator itself to represent entries,
     * thus avoiding having to create entry objects in next().
     */
    abstract class EntryIter extends Iter implements Map.Entry<K,V> {
        /** Cache of last value returned */
        Object lastValue;

        EntryIter() {
        }

        public K getKey() {
            Node<K,V> l = last;
            if (l == null)
                throw new IllegalStateException();
            return l.key;
        }

        public V getValue() {
            Object v = lastValue;
            if (last == null || v == null)
                throw new IllegalStateException();
            return (V)v;
        }

        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        public boolean equals(Object o) {
            // If not acting as entry, just use default.
            if (last == null)
                return super.equals(o);
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry)o;
            return (getKey().equals(e.getKey()) &&
                    getValue().equals(e.getValue()));
        }

        public int hashCode() {
            // If not acting as entry, just use default.
            if (last == null)
                return super.hashCode();
            return getKey().hashCode() ^ getValue().hashCode();
        }

        public String toString() {
            // If not acting as entry, just use default.
            if (last == null)
                return super.toString();
            return getKey() + "=" + getValue();
        }
    }

    final class EntryIterator extends EntryIter
        implements Iterator<Map.Entry<K,V>> {
        EntryIterator() {
            initAscending();
        }
        public Map.Entry<K,V> next() {
            lastValue = nextValue;
            ascend();
            return this;
        }
    }

    final class SubMapEntryIterator extends EntryIter
        implements Iterator<Map.Entry<K,V>> {
        final K fence;
        SubMapEntryIterator(K least, K fence) {
            initAscending(least, fence);
            this.fence = fence;
        }

        public Map.Entry<K,V> next() {
            lastValue = nextValue;
            ascend(fence);
            return this;
        }
    }

    final class DescendingEntryIterator extends EntryIter
        implements Iterator<Map.Entry<K,V>>  {
        DescendingEntryIterator() {
            initDescending();
        }
        public Map.Entry<K,V> next() {
            lastValue = nextValue;
            descend();
            return this;
        }
    }

    final class DescendingSubMapEntryIterator extends EntryIter
        implements Iterator<Map.Entry<K,V>>  {
        final K least;
        DescendingSubMapEntryIterator(K least, K fence) {
            initDescending(least, fence);
            this.least = least;
        }

        public Map.Entry<K,V> next() {
            lastValue = nextValue;
            descend(least);
            return this;
        }
    }

    // Factory methods for iterators needed by submaps and/or
    // ConcurrentSkipListSet

    Iterator<K> keyIterator() {
        return new KeyIterator();
    }

    Iterator<K> descendingKeyIterator() {
        return new DescendingKeyIterator();
    }

    SubMapEntryIterator subMapEntryIterator(K least, K fence) {
        return new SubMapEntryIterator(least, fence);
    }

    DescendingSubMapEntryIterator descendingSubMapEntryIterator(K least, K fence) {
        return new DescendingSubMapEntryIterator(least, fence);
    }

    SubMapKeyIterator subMapKeyIterator(K least, K fence) {
        return new SubMapKeyIterator(least, fence);
    }

    DescendingSubMapKeyIterator descendingSubMapKeyIterator(K least, K fence) {
        return new DescendingSubMapKeyIterator(least, fence);
    }

    SubMapValueIterator subMapValueIterator(K least, K fence) {
        return new SubMapValueIterator(least, fence);
    }

    /* ---------------- Views -------------- */

    class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator();
        }
        public boolean isEmpty() {
            return ConcurrentSkipListMap.this.isEmpty();
        }
        public int size() {
            return ConcurrentSkipListMap.this.size();
        }
        public boolean contains(Object o) {
            return ConcurrentSkipListMap.this.containsKey(o);
        }
        public boolean remove(Object o) {
            return ConcurrentSkipListMap.this.removep(o);
        }
        public void clear() {
            ConcurrentSkipListMap.this.clear();
        }
        public Object[] toArray() {
            Collection<K> c = new ArrayList<K>();
            for (Iterator<K> i = iterator(); i.hasNext(); )
                c.add(i.next());
            return c.toArray();
        }
        public <T> T[] toArray(T[] a) {
            Collection<K> c = new ArrayList<K>();
            for (Iterator<K> i = iterator(); i.hasNext(); )
                c.add(i.next());
            return c.toArray(a);
        }
    }

    class DescendingKeySet extends KeySet {
        public Iterator<K> iterator() {
            return new DescendingKeyIterator();
        }
    }

    final class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator();
        }
        public boolean isEmpty() {
            return ConcurrentSkipListMap.this.isEmpty();
        }
        public int size() {
            return ConcurrentSkipListMap.this.size();
        }
        public boolean contains(Object o) {
            return ConcurrentSkipListMap.this.containsValue(o);
        }
        public void clear() {
            ConcurrentSkipListMap.this.clear();
        }
        public Object[] toArray() {
            Collection<V> c = new ArrayList<V>();
            for (Iterator<V> i = iterator(); i.hasNext(); )
                c.add(i.next());
            return c.toArray();
        }
        public <T> T[] toArray(T[] a) {
            Collection<V> c = new ArrayList<V>();
            for (Iterator<V> i = iterator(); i.hasNext(); )
                c.add(i.next());
            return c.toArray(a);
        }
    }

    class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<K,V> e = (Map.Entry<K,V>)o;
            V v = ConcurrentSkipListMap.this.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<K,V> e = (Map.Entry<K,V>)o;
            return ConcurrentSkipListMap.this.remove(e.getKey(),
                                                     e.getValue());
        }
        public boolean isEmpty() {
            return ConcurrentSkipListMap.this.isEmpty();
        }
        public int size() {
            return ConcurrentSkipListMap.this.size();
        }
        public void clear() {
            ConcurrentSkipListMap.this.clear();
        }

        public Object[] toArray() {
            Collection<Map.Entry<K,V>> c = new ArrayList<Map.Entry<K,V>>();
            for (Map.Entry e : this)
                c.add(new SnapshotEntry(e.getKey(), e.getValue()));
            return c.toArray();
        }
        public <T> T[] toArray(T[] a) {
            Collection<Map.Entry<K,V>> c = new ArrayList<Map.Entry<K,V>>();
            for (Map.Entry e : this)
                c.add(new SnapshotEntry(e.getKey(), e.getValue()));
            return c.toArray(a);
        }
    }

    class DescendingEntrySet extends EntrySet {
        public Iterator<Map.Entry<K,V>> iterator() {
            return new DescendingEntryIterator();
        }
    }

    /**
     * Submaps returned by {@link ConcurrentSkipListMap} submap operations
     * represent a subrange of mappings of their underlying
     * maps. Instances of this class support all methods of their
     * underlying maps, differing in that mappings outside their range are
     * ignored, and attempts to add mappings outside their ranges result
     * in {@link IllegalArgumentException}.  Instances of this class are
     * constructed only using the <tt>subMap</tt>, <tt>headMap</tt>, and
     * <tt>tailMap</tt> methods of their underlying maps.
     */
    static class ConcurrentSkipListSubMap<K,V> extends AbstractMap<K,V>
        implements ConcurrentNavigableMap<K,V>, java.io.Serializable {

        private static final long serialVersionUID = -7647078645895051609L;

        /** Underlying map */
        private final ConcurrentSkipListMap<K,V> m;
        /** lower bound key, or null if from start */
        private final K least;
        /** upper fence key, or null if to end */
        private final K fence;
        // Lazily initialized view holders
        private transient Set<K> keySetView;
        private transient Set<Map.Entry<K,V>> entrySetView;
        private transient Collection<V> valuesView;
        private transient Set<K> descendingKeySetView;
        private transient Set<Map.Entry<K,V>> descendingEntrySetView;

        /**
         * Creates a new submap.
         * @param least inclusive least value, or <tt>null</tt> if from start
         * @param fence exclusive upper bound or <tt>null</tt> if to end
         * @throws IllegalArgumentException if least and fence non-null
         *  and least greater than fence
         */
        ConcurrentSkipListSubMap(ConcurrentSkipListMap<K,V> map,
                                 K least, K fence) {
            if (least != null &&
                fence != null &&
                map.compare(least, fence) > 0)
                throw new IllegalArgumentException("inconsistent range");
            this.m = map;
            this.least = least;
            this.fence = fence;
        }

        /* ----------------  Utilities -------------- */

        boolean inHalfOpenRange(K key) {
            return m.inHalfOpenRange(key, least, fence);
        }

        boolean inOpenRange(K key) {
            return m.inOpenRange(key, least, fence);
        }

        ConcurrentSkipListMap.Node<K,V> firstNode() {
            return m.findCeiling(least);
        }

        ConcurrentSkipListMap.Node<K,V> lastNode() {
            return m.findLower(fence);
        }

        boolean isBeforeEnd(ConcurrentSkipListMap.Node<K,V> n) {
            return (n != null &&
                    (fence == null ||
                     n.key == null || // pass by markers and headers
                     m.compare(fence, n.key) > 0));
        }

        void checkKey(K key) throws IllegalArgumentException {
            if (!inHalfOpenRange(key))
                throw new IllegalArgumentException("key out of range");
        }

        /**
         * Returns underlying map. Needed by ConcurrentSkipListSet
         * @return the backing map
         */
        ConcurrentSkipListMap<K,V> getMap() {
            return m;
        }

        /**
         * Returns least key. Needed by ConcurrentSkipListSet
         * @return least key or <tt>null</tt> if from start
         */
        K getLeast() {
            return least;
        }

        /**
         * Returns fence key. Needed by ConcurrentSkipListSet
         * @return fence key or <tt>null</tt> of to end
         */
        K getFence() {
            return fence;
        }


        /* ----------------  Map API methods -------------- */

        public boolean containsKey(Object key) {
            K k = (K)key;
            return inHalfOpenRange(k) && m.containsKey(k);
        }

        public V get(Object key) {
            K k = (K)key;
            return (!inHalfOpenRange(k)) ? null : m.get(k);
        }

        public V put(K key, V value) {
            checkKey(key);
            return m.put(key, value);
        }

        public V remove(Object key) {
            K k = (K)key;
            return (!inHalfOpenRange(k)) ? null : m.remove(k);
        }

        public int size() {
            long count = 0;
            for (ConcurrentSkipListMap.Node<K,V> n = firstNode();
                 isBeforeEnd(n);
                 n = n.next) {
                if (n.getValidValue() != null)
                    ++count;
            }
            return (count >= Integer.MAX_VALUE) ?
                Integer.MAX_VALUE : (int)count;
        }

        public boolean isEmpty() {
            return !isBeforeEnd(firstNode());
        }

        public boolean containsValue(Object value) {
            if (value == null)
                throw new NullPointerException();
            for (ConcurrentSkipListMap.Node<K,V> n = firstNode();
                 isBeforeEnd(n);
                 n = n.next) {
                V v = n.getValidValue();
                if (v != null && value.equals(v))
                    return true;
            }
            return false;
        }

        public void clear() {
            for (ConcurrentSkipListMap.Node<K,V> n = firstNode();
                 isBeforeEnd(n);
                 n = n.next) {
                if (n.getValidValue() != null)
                    m.remove(n.key);
            }
        }

        /* ----------------  ConcurrentMap API methods -------------- */

        public V putIfAbsent(K key, V value) {
            checkKey(key);
            return m.putIfAbsent(key, value);
        }

        public boolean remove(Object key, Object value) {
            K k = (K)key;
            return inHalfOpenRange(k) && m.remove(k, value);
        }

        public boolean replace(K key, V oldValue, V newValue) {
            checkKey(key);
            return m.replace(key, oldValue, newValue);
        }

        public V replace(K key, V value) {
            checkKey(key);
            return m.replace(key, value);
        }

        /* ----------------  SortedMap API methods -------------- */

        public Comparator<? super K> comparator() {
            return m.comparator();
        }

        public K firstKey() {
            ConcurrentSkipListMap.Node<K,V> n = firstNode();
            if (isBeforeEnd(n))
                return n.key;
            else
                throw new NoSuchElementException();
        }

        public K lastKey() {
            ConcurrentSkipListMap.Node<K,V> n = lastNode();
            if (n != null) {
                K last = n.key;
                if (inHalfOpenRange(last))
                    return last;
            }
            throw new NoSuchElementException();
        }

        public ConcurrentNavigableMap<K,V> subMap(K fromKey, K toKey) {
            if (fromKey == null || toKey == null)
                throw new NullPointerException();
            if (!inOpenRange(fromKey) || !inOpenRange(toKey))
                throw new IllegalArgumentException("key out of range");
            return new ConcurrentSkipListSubMap(m, fromKey, toKey);
        }

        public ConcurrentNavigableMap<K,V> headMap(K toKey) {
            if (toKey == null)
                throw new NullPointerException();
            if (!inOpenRange(toKey))
                throw new IllegalArgumentException("key out of range");
            return new ConcurrentSkipListSubMap(m, least, toKey);
        }

        public  ConcurrentNavigableMap<K,V> tailMap(K fromKey) {
            if (fromKey == null)
                throw new NullPointerException();
            if (!inOpenRange(fromKey))
                throw new IllegalArgumentException("key out of range");
            return new ConcurrentSkipListSubMap(m, fromKey, fence);
        }

        /* ----------------  Relational methods -------------- */

        public Map.Entry<K,V> ceilingEntry(K key) {
            return (SnapshotEntry<K,V>)
                m.getNear(key, m.GT|m.EQ, least, fence, false);
        }

        public K ceilingKey(K key) {
            return (K)
                m.getNear(key, m.GT|m.EQ, least, fence, true);
        }

        public Map.Entry<K,V> lowerEntry(K key) {
            return (SnapshotEntry<K,V>)
                m.getNear(key, m.LT, least, fence, false);
        }

        public K lowerKey(K key) {
            return (K)
                m.getNear(key, m.LT, least, fence, true);
        }

        public Map.Entry<K,V> floorEntry(K key) {
            return (SnapshotEntry<K,V>)
                m.getNear(key, m.LT|m.EQ, least, fence, false);
        }

        public K floorKey(K key) {
            return (K)
                m.getNear(key, m.LT|m.EQ, least, fence, true);
        }


        public Map.Entry<K,V> higherEntry(K key) {
            return (SnapshotEntry<K,V>)
                m.getNear(key, m.GT, least, fence, false);
        }

        public K higherKey(K key) {
            return (K)
                m.getNear(key, m.GT, least, fence, true);
        }

        public Map.Entry<K,V> firstEntry() {
            for (;;) {
                ConcurrentSkipListMap.Node<K,V> n = firstNode();
                if (!isBeforeEnd(n))
                    return null;
                Map.Entry<K,V> e = n.createSnapshot();
                if (e != null)
                    return e;
            }
        }

        public Map.Entry<K,V> lastEntry() {
            for (;;) {
                ConcurrentSkipListMap.Node<K,V> n = lastNode();
                if (n == null || !inHalfOpenRange(n.key))
                    return null;
                Map.Entry<K,V> e = n.createSnapshot();
                if (e != null)
                    return e;
            }
        }

        public Map.Entry<K,V> pollFirstEntry() {
            return (SnapshotEntry<K,V>)
                m.removeFirstEntryOfSubrange(least, fence, false);
        }

        public Map.Entry<K,V> pollLastEntry() {
            return (SnapshotEntry<K,V>)
                m.removeLastEntryOfSubrange(least, fence, false);
        }

        /* ---------------- Submap Views -------------- */

        public Set<K> keySet() {
            Set<K> ks = keySetView;
            return (ks != null) ? ks : (keySetView = new KeySetView());
        }

        class KeySetView extends AbstractSet<K> {
            public Iterator<K> iterator() {
                return m.subMapKeyIterator(least, fence);
            }
            public int size() {
                return ConcurrentSkipListSubMap.this.size();
            }
            public boolean isEmpty() {
                return ConcurrentSkipListSubMap.this.isEmpty();
            }
            public boolean contains(Object k) {
                return ConcurrentSkipListSubMap.this.containsKey(k);
            }
            public Object[] toArray() {
                Collection<K> c = new ArrayList<K>();
                for (Iterator<K> i = iterator(); i.hasNext(); )
                    c.add(i.next());
                return c.toArray();
            }
            public <T> T[] toArray(T[] a) {
                Collection<K> c = new ArrayList<K>();
                for (Iterator<K> i = iterator(); i.hasNext(); )
                    c.add(i.next());
                return c.toArray(a);
            }
        }

        public Set<K> descendingKeySet() {
            Set<K> ks = descendingKeySetView;
            return (ks != null) ? ks : (descendingKeySetView = new DescendingKeySetView());
        }

        class DescendingKeySetView extends KeySetView {
            public Iterator<K> iterator() {
                return m.descendingSubMapKeyIterator(least, fence);
            }
        }

        public Collection<V> values() {
            Collection<V> vs = valuesView;
            return (vs != null) ? vs : (valuesView = new ValuesView());
        }

        class ValuesView extends AbstractCollection<V> {
            public Iterator<V> iterator() {
                return m.subMapValueIterator(least, fence);
            }
            public int size() {
                return ConcurrentSkipListSubMap.this.size();
            }
            public boolean isEmpty() {
                return ConcurrentSkipListSubMap.this.isEmpty();
            }
            public boolean contains(Object v) {
                return ConcurrentSkipListSubMap.this.containsValue(v);
            }
            public Object[] toArray() {
                Collection<V> c = new ArrayList<V>();
                for (Iterator<V> i = iterator(); i.hasNext(); )
                    c.add(i.next());
                return c.toArray();
            }
            public <T> T[] toArray(T[] a) {
                Collection<V> c = new ArrayList<V>();
                for (Iterator<V> i = iterator(); i.hasNext(); )
                    c.add(i.next());
                return c.toArray(a);
            }
        }

        public Set<Map.Entry<K,V>> entrySet() {
            Set<Map.Entry<K,V>> es = entrySetView;
            return (es != null) ? es : (entrySetView = new EntrySetView());
        }

        class EntrySetView extends AbstractSet<Map.Entry<K,V>> {
            public Iterator<Map.Entry<K,V>> iterator() {
                return m.subMapEntryIterator(least, fence);
            }
            public int size() {
                return ConcurrentSkipListSubMap.this.size();
            }
            public boolean isEmpty() {
                return ConcurrentSkipListSubMap.this.isEmpty();
            }
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry))
                    return false;
                Map.Entry<K,V> e = (Map.Entry<K,V>) o;
                K key = e.getKey();
                if (!inHalfOpenRange(key))
                    return false;
                V v = m.get(key);
                return v != null && v.equals(e.getValue());
            }
            public boolean remove(Object o) {
                if (!(o instanceof Map.Entry))
                    return false;
                Map.Entry<K,V> e = (Map.Entry<K,V>) o;
                K key = e.getKey();
                if (!inHalfOpenRange(key))
                    return false;
                return m.remove(key, e.getValue());
            }
            public Object[] toArray() {
                Collection<Map.Entry<K,V>> c = new ArrayList<Map.Entry<K,V>>();
                for (Map.Entry e : this)
                    c.add(new SnapshotEntry(e.getKey(), e.getValue()));
                return c.toArray();
            }
            public <T> T[] toArray(T[] a) {
                Collection<Map.Entry<K,V>> c = new ArrayList<Map.Entry<K,V>>();
                for (Map.Entry e : this)
                    c.add(new SnapshotEntry(e.getKey(), e.getValue()));
                return c.toArray(a);
            }
        }

        public Set<Map.Entry<K,V>> descendingEntrySet() {
            Set<Map.Entry<K,V>> es = descendingEntrySetView;
            return (es != null) ? es : (descendingEntrySetView = new DescendingEntrySetView());
        }

        class DescendingEntrySetView extends EntrySetView {
            public Iterator<Map.Entry<K,V>> iterator() {
                return m.descendingSubMapEntryIterator(least, fence);
            }
        }
    }
}
