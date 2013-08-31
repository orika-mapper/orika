package ma.glasnost.orika.converter.builtin;

import java.util.HashSet;
import java.util.Set;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * PassThroughConverter allows configuration of a number of specific types which
 * should be passed through (as-is) without creating a mapped copy.<br><br>
 * 
 * This allows you to declare your own set of types which should be treated by
 * Orika as if they were in the set of immutable types.
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class PassThroughConverter extends CustomConverter<Object, Object> {

	private final Set<Type<?>> passThroughTypes = new HashSet<Type<?>>();
	
	/**
	 * Constructs a new PassThroughConverter configured to treat the provided
	 * list of types as immutable.
	 * 
	 * @param types one or more types that should be treated as immutable
	 */
	public PassThroughConverter(java.lang.reflect.Type...types) {
		for (java.lang.reflect.Type type: types) {
			passThroughTypes.add(TypeFactory.valueOf(type));
		}
	}
	
	private boolean shouldPassThrough(Type<?> type) {
		for (Type<?> registeredType: passThroughTypes) {
			if (registeredType.isAssignableFrom(type)) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see ma.glasnost.orika.Converter#canConvert(ma.glasnost.orika.metadata.Type, ma.glasnost.orika.metadata.Type)
	 */
	public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
	    return shouldPassThrough(sourceType) && sourceType.equals(destinationType);
    }

	public Object convert(Object source, Type<? extends Object> destinationType) {
	    return source;
    }
}
