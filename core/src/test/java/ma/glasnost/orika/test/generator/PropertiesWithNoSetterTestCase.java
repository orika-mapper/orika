package ma.glasnost.orika.test.generator;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;


/**
 * Verify that we can properly map non-assignable properties of an object,
 * so long as they are not immutable
 * 
 *
 */
public class PropertiesWithNoSetterTestCase {

	
	public static class Person {
		public String firstName;
		public String lastName;
	}
	
	public static class SomeObject {
	    
		private final Person person;
		private final String id;
		
	    public SomeObject() {
	        this.person = new Person();
	        this.id = UUID.randomUUID().toString();
	    }

	    public Person getPerson() {
	    	return person;
	    }
	    
	    public String getId() {
	    	return id;
	    }
	    
	}
	
	public static class AnotherObject {
	    
		private final Person person;
		private final String id;
		
	    public AnotherObject() {
	        this.person = new Person();
	        this.id = UUID.randomUUID().toString();
	    }

	    public Person getPerson() {
	    	return person;
	    }
	    
	    public String getId() {
	    	return id;
	    }
	}
	
	@Test
	public void test() {
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		MapperFacade mapper = factory.getMapperFacade();
		
		SomeObject source = new SomeObject();
		source.getPerson().firstName = "Joe";
		source.getPerson().lastName = "Smith";
		
		AnotherObject dest = mapper.map(source, AnotherObject.class);
		Assert.assertEquals(source.getPerson().firstName, dest.getPerson().firstName);
		Assert.assertEquals(source.getPerson().lastName, dest.getPerson().lastName);
		Assert.assertNotEquals(source.getId(), dest.getId());
	}
}
