package ma.glasnost.orika.test.capturefieldcontext;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.NullFilter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FieldContextWithFiltersTestCase {

	public static class Person {
		public Name name;
		public Address address;
		public List<Person> contacts;
	}
	
	public static class Name {
		public String first;
		public String middle;
		public String last;
	}
	
	public static class PersonDto {
		public NameDto name;
		public AddressDto address;
		public List<PersonDto> contacts;
	}
	
	public static class NameDto {
		public String first;
		public String middle;
		public String last;
	}
	
	public static class Address {
		public String street;
		public String city;
		public String state;
		public String postalCode;
		public String country;
	}
	
	public static class AddressDto {
		public String street;
		public String city;
		public String state;
		public String zipCode;
		public String country;
	}
	
	/**
	 * In this test, we demonstrate that the fully-qualified source path
	 * can be referenced to make a filtering decision
	 */
	@Test
	public void referenceFieldContextInFilters() throws Throwable {
		
        MapperFactory factory = new DefaultMapperFactory.Builder()
        	.captureFieldContext(true)
        	.build();
        
        factory.classMap(Person.class, PersonDto.class)
               .byDefault()
               .register();

        factory.classMap(Address.class, AddressDto.class)
                .field("postalCode", "zipCode")
                .byDefault()
                .register();
        
        factory.registerFilter(new AddressDepthFilter());
        
        Person source = new Person();
        source.name = new Name();
        source.address = new Address();
        source.contacts = new ArrayList<>();
        source.name.first = "Kermit";
        source.name.middle = "The";
        source.name.last = "Frog";
        source.address.street = "123 Sesame St.";
        source.address.city = "Manhattan";
        source.address.state = "NY";
        source.address.country = "USA";
        source.address.postalCode = "10023";
        
        Person oscar = new Person();
        oscar.name = new Name();
        oscar.name.first = "Oscar";
        oscar.name.middle = "The";
        oscar.name.last = "Grouch";
        oscar.address = new Address();
        oscar.address.street = "123 Sesame St.";
        oscar.address.city = "Manhattan";
        oscar.address.state = "NY";
        oscar.address.country = "USA";
        oscar.address.postalCode = "10023";
        source.contacts.add(oscar);
        
        Person bigbird = new Person();
        bigbird.name = new Name();
        bigbird.name.first = "Oscar";
        bigbird.name.middle = "The";
        bigbird.name.last = "Grouch";
        bigbird.address = new Address();
        bigbird.address.street = "123 Sesame St.";
        bigbird.address.city = "Manhattan";
        bigbird.address.state = "NY";
        bigbird.address.country = "USA";
        bigbird.address.postalCode = "10023";
        source.contacts.add(bigbird);
        
        
        MapperFacade mapper = factory.getMapperFacade();
		
        PersonDto dest = mapper.map(source, PersonDto.class);
        Assert.assertEquals(source.name.first, dest.name.first);
        Assert.assertEquals(source.name.middle, dest.name.middle);
        Assert.assertEquals(source.name.last, dest.name.last);
        Assert.assertEquals(source.address.city, dest.address.city);
        Assert.assertEquals(source.address.state, dest.address.state);
        Assert.assertEquals(source.address.postalCode, dest.address.zipCode);
        Assert.assertEquals(source.address.street, dest.address.street);
        Assert.assertEquals(source.address.country, dest.address.country);
        
        Assert.assertNotNull(dest.contacts.get(0));
        Assert.assertNotNull(dest.contacts.get(0).name);
        Assert.assertNull(dest.contacts.get(0).address);
        
        Assert.assertNotNull(dest.contacts.get(1));
        Assert.assertNotNull(dest.contacts.get(1).name);
        Assert.assertNull(dest.contacts.get(1).address);
        
	}
	
	public static class AddressDepthFilter extends NullFilter<Object, Object> {
        
        public <S, D> boolean shouldMap(final Type<S> sourceType, final String sourceName, final S source, final Type<D> destType, final String destName,
            final D dest, final MappingContext mappingContext) {
            /*
             * Don't map nested addresses
             */
            if ("postalCode".equals(sourceName)) {
                Assert.assertTrue(Arrays.equals(mappingContext.getDestinationExpressionPaths(), new String[]{"address", "zipCode"}));
            }
        	return !"address".equals(sourceName) ||
            		"source.address".equals(mappingContext.getFullyQualifiedSourcePath());
        }
    }
}
