package ma.glasnost.orika.test.community;

import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;

/**
 * ClassMapBuilderForMaps class exclude method Does not work
 * <p>
 * 
 * @see <a href=
 *      "https://github.com/orika-mapper/orika/issues/314">https://github.com/orika-mapper/orika/issues</a>
 */
public class Issue314TestCase {

	@Test
	public void exclude_classMapBuilderForMaps() {

		DefaultMapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
		mapperFactory.classMap(Map.class, B.class).exclude("b").byDefault().register();

		Map<String, Object> map = new HashMap<>();
		map.put("a", "a");
		map.put("b", "b");

		B b = mapperFactory.getMapperFacade().map(map, B.class);
		assertNull(b.getB());
	}

	public static class B {
		private String a;
		private String b;

		public String getA() {
			return a;
		}

		public void setA(String a) {
			this.a = a;
		}

		public String getB() {
			return b;
		}

		public void setB(String b) {
			this.b = b;
		}
	}

}
