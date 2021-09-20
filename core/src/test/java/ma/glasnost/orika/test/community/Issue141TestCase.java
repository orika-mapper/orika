package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.CaseInsensitiveClassMapBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Compilation Error when referencing collection on class property.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/141">https://code.google.com/archive/p/orika/</a>
 */
public class Issue141TestCase {

	public static class Clazz {
		private SubClass subClass;

		public SubClass getSubClass() {
			return subClass;
		}

		public void setSubClass(SubClass subClass) {
			this.subClass = subClass;
		}
	}

	public static class SubClass {
		private List<String> strings;

		public List<String> getStrings() {
			return strings;
		}

		public void setStrings(List<String> strings) {
			this.strings = strings;
		}
	}

	public static class Clazz2 {
		private String string;

		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}
	}

	public static class A {
		private B b = new B();

		public B getB() {
			return b;
		}

		public void setB(B b) {
			this.b = b;
		}
	}

	public static class B {
		private List<C> c = new ArrayList<>();

		public List<C> getC() {
			return c;
		}

		public void setC(List<C> c) {
			this.c = c;
		}
	}

	public static class C {
		private D d = new D();

		public D getD() {
			return d;
		}

		public void setD(D d) {
			this.d = d;
		}
	}

	public static class D {
		private List<String> data = new ArrayList<>();

		public List<String> getData() {
			return data;
		}

		public void setData(List<String> data) {
			this.data = data;
		}
	}

	public static class TheBean {
		private String data;

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}
	}

	@Test
	public void test() {
		SubClass subClass = new SubClass();
		subClass.setStrings(new ArrayList<>(Arrays.asList("abc@mail.com")));

		Clazz clazz = new Clazz();
		clazz.setSubClass(subClass);

		Clazz2 clazz2 = new Clazz2();

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder()
				.classMapBuilderFactory(new CaseInsensitiveClassMapBuilder.Factory())
				.build();
		mapperFactory.classMap(Clazz.class, Clazz2.class)
				.field("subClass.strings[0]", "string").register();

		
		
		MapperFacade mapper = mapperFactory.getMapperFacade();
		mapper.map(clazz, clazz2);
	}

	@Test
	public void test2() throws Throwable {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder()
			.useAutoMapping(false)
			.mapNulls(false)
			.build();

		mapperFactory.classMap(TheBean.class, A.class)
				.field("data", "b.c[0].d.data[0]").register();

		MapperFacade mapper = mapperFactory.getMapperFacade();

		TheBean theBean = new TheBean();
		theBean.setData("TEST");

		A a = mapper.map(theBean, A.class);
	}

}
