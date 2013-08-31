package ma.glasnost.orika.converter.builtin;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.Type;

/**
 * CopyByReferenceConverter handles conversion of Orika's built-in immutable
 * types, as well as anything-to-Object, and primitive to Wrapper conversions<br>
 * 
 * @author matt.deboer@gmail.com
 * 
 */
public class CopyByReferenceConverter extends CustomConverter<Object, Object> {
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.Converter#canConvert(ma.glasnost.orika.metadata.Type,
     * ma.glasnost.orika.metadata.Type)
     */
    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
        
        return /*destinationType.isAssignableFrom(sourceType)
                || */(ClassUtil.isImmutable(sourceType) && (sourceType.equals(destinationType) || sourceType.isWrapperFor(destinationType) || destinationType.isWrapperFor(sourceType)));
    }
    
    public Object convert(Object source, Type<? extends Object> destinationType) {
        return source;
    }
}
