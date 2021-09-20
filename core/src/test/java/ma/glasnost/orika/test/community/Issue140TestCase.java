package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.TypeBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mapAsMap breaks Object graphs.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/140">https://code.google.com/archive/p/orika/</a>
 */
public class Issue140TestCase {
	public static class ParentA {
		public ChildA child;
	}

	public static class ParentB {
		public ChildB child;
	}

	public static class ChildA {
		public ParentA parent;
	}

	public static class ChildB {
		public ParentB parent;
	}

	@Test
	public void test() {
		MapperFactory OMF = new DefaultMapperFactory.Builder().build();
		OMF.classMap(ParentA.class, ParentB.class).byDefault().register();
		OMF.classMap(ChildA.class, ChildB.class).byDefault().register();

		ParentA a = new ParentA();
		a.child = new ChildA();
		a.child.parent = a;

		Map<String, Object> m = new HashMap<>();
		m.put("p", a);
		m.put("c", a.child);

		MapperFacade mapper = OMF.getMapperFacade();

		ParentB b1 = mapper.map(a, ParentB.class);
		
		ma.glasnost.orika.metadata.Type<Map<String, Object>> mapType = new TypeBuilder<Map<String, Object>>() {
		}.build();
		Map<String, Object> m2 = mapper.mapAsMap(m, mapType, mapType);
		
		ParentB b2 = (ParentB) m2.get("p");
		/*
		 * Issue here is that the object graph is not respected when the object is
		 * contained within a Map type...;
		 * the parent's child's parent is not identical, but when the types are mapped
		 * directly, it is identical.
		 */
		Assert.assertSame(b1, b1.child.parent);
		Assert.assertSame(b2, b2.child.parent);

		List<Object> l = new ArrayList<>();
		l.add(a.child);
		l.add(a);

		ma.glasnost.orika.metadata.Type<Object> listType = new TypeBuilder<Object>() {
		}.build();
		List<Object> l2 = mapper.mapAsList(l, listType, listType);
		b2 = (ParentB) l2.get(1);
		Assert.assertSame(b2, b2.child.parent);
	}

}
