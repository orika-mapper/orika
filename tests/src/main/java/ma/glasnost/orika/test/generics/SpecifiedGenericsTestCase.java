package ma.glasnost.orika.test.generics;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Assert;
import org.junit.Test;

import java.beans.IntrospectionException;

/**
 * Test to show odd behavior with specified generic types
 */
public class SpecifiedGenericsTestCase {
	@Test
	public void testSpecifiedGeneric() throws IntrospectionException {
		MapperFacade mapperFacade = MappingUtil.getMapperFactory()
				.getMapperFacade();
		Source source = new Source();
		source.setFoo("Hello");
		Destination destination = mapperFacade.map(source, Destination.class);
		Assert.assertEquals(source.getFoo(), destination.getFoo());
	}

	public static class Base<T> {
		protected T foo;

		public T getFoo() {
			return this.foo;
		}

	}

	public static class Source extends Base<String> {
		public void setFoo(String t) {
			this.foo = t;
		}
	}

	public static class Destination extends Base<String> {
		public void setFoo(String t) {
			this.foo = t;
		}
	}

}
