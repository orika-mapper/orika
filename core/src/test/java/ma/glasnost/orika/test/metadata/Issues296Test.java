package ma.glasnost.orika.test.metadata;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @Auther: 530827804@qq.com
 * @Date: 2018/12/6
 */
public class Issues296Test {

	@Test
	public void testIssues296() {
		MapperFactory factory = new DefaultMapperFactory.Builder().build();
		final MapperFacade mapperFacade = factory.getMapperFacade();
		A a = new A();
		B b = new B();
		F<B> f = new F();
		f.setG(b);
		a.setF(f);
		final D d = mapperFacade.map(a, D.class);
		final H g = mapperFacade.map(a, H.class);
		Assert.assertEquals(d.getF().getG().getClass(), C.class);
		Assert.assertEquals(g.getF().getG().getClass(), C.class);
	}

	public static class A<E extends B> {

		F<E> f;

		public F<E> getF() {
			return f;
		}

		public void setF(F<E> f) {
			this.f = f;
		}
	}

	public static class B {

	}

	public static class C extends B {

	}

	public static class D extends A<C> {

	}

	public static class F<G> {
		G g;

		public G getG() {
			return g;
		}

		public void setG(G g) {
			this.g = g;
		}
	}

	public static class H extends D {

	}
}
