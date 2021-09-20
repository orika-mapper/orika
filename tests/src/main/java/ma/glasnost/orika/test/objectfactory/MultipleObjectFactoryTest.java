package ma.glasnost.orika.test.objectfactory;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.TypeFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MultipleObjectFactoryTest {
	public static class Base {
	}

	public static class Sub1 extends Base {
	}

	public static class Sub2 extends Base {
	}

	@Test
	public void orikaTest() {
		MapperFactory factory = new DefaultMapperFactory.Builder().build();

		factory.registerObjectFactory(new CustomFactory<>(Sub1.class), TypeFactory.valueOf(Sub1.class));
		factory.registerObjectFactory(new CustomFactory<>(Sub2.class), TypeFactory.valueOf(Sub2.class));
		factory.registerObjectFactory(new CustomFactory<>(Base.class), TypeFactory.valueOf(Base.class));

		MapperFacade mapperFacade = factory.getMapperFacade();
		Base mapped = mapperFacade.map(new Object(), Base.class);
		assertEquals("returned instance is not Base", Base.class, mapped.getClass());
	}
}
