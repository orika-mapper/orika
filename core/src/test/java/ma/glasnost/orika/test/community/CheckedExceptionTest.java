package ma.glasnost.orika.test.community;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

public class CheckedExceptionTest {

	@Test
	public void testMappingGetterThrowingCheckedException() throws Throwable {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory
				.classMap(Source.class, Destination.class)
				.byDefault()
				.register();

		MapperFacade mapper = factory.getMapperFacade();

		Destination dest = mapper.map(new Source(), Destination.class);
		Assert.assertEquals("value", dest.getValue());
	}

	public static class Source {
		public String getValue() throws IOException {
			return "value";
		}
	}

	public static class Destination {
		public String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

}
