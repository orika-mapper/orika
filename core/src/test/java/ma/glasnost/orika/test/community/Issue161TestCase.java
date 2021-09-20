package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

/**
 * Unable to map class with self-referencing generics and Comparable return type.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/161">https://code.google.com/archive/p/orika/</a>
 */
public class Issue161TestCase {

	public static class Optional<T> {
	}

	public static class Range<C extends Comparable<C>> {
	}

	public static class SelfReferencingGenericType<T extends SelfReferencingGenericType<T>> {
		public Optional<Range<String>> getOptionalStringRange() {
			return null;
		}
	}

	public static class Foo extends SelfReferencingGenericType<Foo> {
	}

	@Test
	public void mapSelfReferencingGenericType() {
		MapperFactory factory = new DefaultMapperFactory.Builder().build();
		factory.classMap(Foo.class, String.class).byDefault().register();
	}
}
