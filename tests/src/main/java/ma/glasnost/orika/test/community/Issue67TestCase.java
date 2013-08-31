package ma.glasnost.orika.test.community;

import org.junit.Test;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

public class Issue67TestCase {

	@Test
	public void simpleCase() {
		MapperFactory factory = new DefaultMapperFactory.Builder().build();
		factory.registerClassMap(factory.classMap(Bean.class, Bean.class)
				.byDefault().toClassMap());
		MapperFacade mapper = factory.getMapperFacade();
		Bean bean = new Bean();
		bean.setSize(20);
		bean.setName("Kidney");
		mapper.map(bean, Bean.class);

		/* If map pass no need to check */
	}

	public static class Bean {

		private String name;
		private int size;

		/*
		 * public int getSize() { return size; }
		 */
		public void setSize(int size) {
			this.size = size;
		}

		public String getName() {
			return name;
		}

		public void setName(String value) {
			this.name = value;
		}
	}
}