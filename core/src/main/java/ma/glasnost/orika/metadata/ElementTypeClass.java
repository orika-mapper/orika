package ma.glasnost.orika.metadata;

import ma.glasnost.orika.MapEntry;

import java.util.Collection;
import java.util.Map;

public class ElementTypeClass {

    static Type<?> defaultElementType(final Type<?> type, final Type<?> elementType) {

        if (elementType != null) {
            return elementType;
        }

        if (type.getActualTypeArguments().length > 0 && elementType == null) {
            return (Type<?>) type.getActualTypeArguments()[0];
        } else if (type.isCollection()) {
            Type<?> collectionElementType = elementType;
            Type<?> collection = type.findAncestor(Collection.class);
            if (collection != null) {
                collectionElementType = (Type<?>) collection.getActualTypeArguments()[0];
            }
            return collectionElementType;

        } else if (type.isMap()) {

            Type<?> mapElementType = elementType;
            Type<?> map = type.findAncestor(Map.class);
            if (map != null) {
                @SuppressWarnings("unchecked")
                Type<? extends Map<Object, Object>> mapType = (Type<? extends Map<Object, Object>>) map;
                mapElementType = MapEntry.entryType(mapType);
            }
            return mapElementType;

        } else {
            return elementType;
        }
    }
}
