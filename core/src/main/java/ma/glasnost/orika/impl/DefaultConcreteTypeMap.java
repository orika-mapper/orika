package ma.glasnost.orika.impl;

import ma.glasnost.orika.MapEntry;

import java.util.*;

public class DefaultConcreteTypeMap {

    private static final Map<Class, Class> map;
    static {
        Map<Class, Class> tmpMap = new HashMap<Class, Class>();
        tmpMap.put(Collection.class, ArrayList.class);
        tmpMap.put(List.class, ArrayList.class);
        tmpMap.put(Set.class, LinkedHashSet.class);
        tmpMap.put(Map.class, LinkedHashMap.class);
        tmpMap.put(Map.Entry.class, MapEntry.class);
        tmpMap.put(SortedMap.class, TreeMap.class);
        tmpMap.put(SortedSet.class, TreeSet.class);
        map = Collections.unmodifiableMap(tmpMap);
    }
    public static Set<Map.Entry<Class, Class>> getAll() {
        return map.entrySet();
    }

    public static Class get(Class<?> type) {
        return map.get(type);
    }
}
