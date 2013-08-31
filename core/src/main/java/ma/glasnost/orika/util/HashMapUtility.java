package ma.glasnost.orika.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import ma.glasnost.orika.MappedTypePair;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap.Builder;

/**
 * HashMapUtility provides an wrapper for obtaining instances of ConcurrentLinkedHashMap
 * and is compatible with both 1.2 and later 1.x versions of this class.
 */
public class HashMapUtility {
    
    /**
     * Generates a new instance of ConcurrentLinkedHashMap with the specified max weighted capacity
     * 
     * @param keyClass
     * @param valClass
     * @param capacity
     * @return a new instance of ConcurrentLinkedHashMap with the specified max weighted capacity
     */
    public static <K, V extends MappedTypePair<Object, Object>> Map<K, V> getConcurrentLinkedHashMap(int capacity) {
        
        Builder<K, V> builder = new ConcurrentLinkedHashMap.Builder<K, V>();
       
        /*
         * Fix for maximumWeightedCapacity change from int to long between 1.2 version
         * and newer 1.x versions; use reflection to detect int or long in the method
         * signature
         */
        try {
            Method maximumWeightedCapacity = ConcurrentLinkedHashMap.Builder.class.getMethod("maximumWeightedCapacity", int.class);
            maximumWeightedCapacity.invoke(builder, capacity);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getTargetException());
        } catch (NoSuchMethodException e) {
            try {
                Method maximumWeightedCapacity = ConcurrentLinkedHashMap.Builder.class.getMethod("maximumWeightedCapacity", long.class);
                maximumWeightedCapacity.invoke(builder, (long)capacity);
            } catch (NoSuchMethodException e1) {
                throw new IllegalStateException(e1);
            } catch (IllegalAccessException e1) {
                throw new IllegalStateException(e1);
            } catch (InvocationTargetException e1) {
                throw new IllegalStateException(e1);
            }
            
        }
        return builder.build();
    }
}
