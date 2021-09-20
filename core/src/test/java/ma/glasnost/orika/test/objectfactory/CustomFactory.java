package ma.glasnost.orika.test.objectfactory;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.ObjectFactory;

import java.lang.reflect.Constructor;

public class CustomFactory<T> implements ObjectFactory<T> {
	private final Class<T> type;

	public CustomFactory(Class<T> type) {
		this.type = type;
	}

	public T create(Object o, MappingContext mappingContext) {
		try {
			Constructor<T> declaredConstructor = type.getDeclaredConstructor();
			declaredConstructor.setAccessible(true);
			return declaredConstructor.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
