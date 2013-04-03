package ma.glasnost.orika.loader;

import java.util.Arrays;

/**
 * @author ikozar
 * date     25.02.13
 *
 * Contain nodes value by enum of node types
 */
public class EnumMap<T> {
    private T[] array;
    private Class<? extends Enum> enumClass;

    public EnumMap(Class<? extends Enum> enClass) {
        enumClass = enClass;
        array = (T[]) new Object[enClass.getEnumConstants().length];
    }

    public void put(Enum<? extends Enum> en, T value) {
        array[en.ordinal()] = value;
    }

    public T get(Enum<? extends Enum> en) {
        return array[en.ordinal()];
    }

    public void clear() {
        Arrays.fill(array, null);
    }
}
