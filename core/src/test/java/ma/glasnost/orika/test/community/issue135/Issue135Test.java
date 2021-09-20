package ma.glasnost.orika.test.community.issue135;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

public class Issue135Test {

	private MapperFacade mapper;

	@Before
	public void setup() {
		MapperFactory mapperFactory = MappingUtil.getMapperFactory();
		mapperFactory.classMap(Domain.class, Representation.class)
		.mapNulls(true)
		.mapNullsInReverse(true)
		.field("subB", "repA.repB") // this causes NPE if repA is null
		.field("active", "repA.active")
		.field("primitive", "repA.primitive")
		.register();

		mapper = mapperFactory.getMapperFacade();
	}

	@Test
	public void testCase(){
		Domain src = new Domain();
		//throws NPE
		Representation target = mapper.map(src, Representation.class);
		Assert.assertNotNull(target);
	}
}
