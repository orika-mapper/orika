package ma.glasnost.orika.test.community.issue121.aobjects;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author: Ilya Krokhmalyov YC14IK1
 * @since: 8/23/13
 */

public abstract class AbstractOrderedMap<K, T> implements Iterable<T>, Serializable {

    protected final Map<K, T> resultSet = new LinkedHashMap<>();

    protected AbstractOrderedMap(Map<K, T> map) {
        resultSet.putAll(map);
    }

    public Iterator<T> iterator() {
        return Collections.unmodifiableCollection(resultSet.values()).iterator();
    }

    public T get(K id) {
        return resultSet.get(id);
    }

    public T getSingle() {
        if(resultSet.size() == 1) {
            return resultSet.values().iterator().next();
        } else if (resultSet.size() == 0) {
            return null;
        } else {
            throw new IllegalStateException("The operation is not allowed when collection size is greater then one");
        }
    }

    public Map<K, T> getMap() {
        return Collections.unmodifiableMap(resultSet);
    }

    public int size() {
        return resultSet.size();
    }

    public boolean containsId(K id) {
        return resultSet.containsKey(id);
    }

    public Set<K> idSet() {
        return Collections.unmodifiableSet(resultSet.keySet());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "resultSet=" + resultSet +
                '}';
    }
}

