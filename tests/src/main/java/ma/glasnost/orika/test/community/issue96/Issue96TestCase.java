package ma.glasnost.orika.test.community.issue96;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import org.junit.Before;
import org.junit.Test;

public class Issue96TestCase {
	private MapperFacade facade;

	@Before
	public void setUp() {
		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().compilerStrategy(new EclipseJdtCompilerStrategy()).build();
		ClassMapBuilder<Customer, CustomerData> builder = mapperFactory.classMap(Customer.class, CustomerData.class);
		builder.field("name", "name");
		builder.field("address.city", "city");
		mapperFactory.registerClassMap(builder.toClassMap());
		facade = mapperFactory.getMapperFacade();
	}

	@Test
	public void test() {
		AddressData address = new AddressData("city");
		// The next line produces a new mapper for AddresssData and Address
		facade.map(address, Address.class);
		CustomerData customer = new CustomerData();
		customer.setCity("city");
		customer.setName("name");
		facade.map(customer, Customer.class);
	}
}
