package ma.glasnost.orika.test.community;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Mapping of Lists are not functioning correctly with fieldAToB.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/138">https://code.google.com/archive/p/orika/</a>
 */
public class Issue138TestCase {

    @Test
    public void test() {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory(true);
        mapperFactory.classMap(Person1.class, Person2.class)
                .field("firstname", "forename").fieldAToB("lastname", "backname").fieldAToB("phones{number}", "phoneNumbers{}")
                .register();

        BoundMapperFacade<Person1,Person2> mapperFacade = mapperFactory.getMapperFacade(Person1.class, Person2.class);
        
        
        // Test A to B
        Person1 otto1 = new Person1();
        otto1.setFirstname("Otto");
        otto1.setLastname("McAllen");
        
        ArrayList<Phone> phones = new ArrayList<>();
        phones.add(new Phone((long) 15468));
        phones.add(new Phone((long) 99999));
        otto1.setPhones(phones);
        
        Person2 otto2 = mapperFacade.map(otto1);
        assertEquals(otto1.getFirstname(), otto2.getForename());
        assertEquals(otto1.getLastname(), otto2.getBackname());
        assertEquals(otto1.getPhones().get(0).getNumber(), otto2.getPhoneNumbers().get(0));
        
        
        // Test B to A
        Person2 otto3 = new Person2();
        otto3.setForename("FreshOtto");
        otto3.setBackname("McFresh");
        ArrayList<Long> phoneNumbers = new ArrayList<>();
        phoneNumbers.add((long)11111);
        phoneNumbers.add((long)22222);
        otto3.setPhoneNumbers(phoneNumbers);
        
        Person1 otto4 = mapperFacade.mapReverse(otto3);
        
        assertEquals(otto3.getForename(), otto4.getFirstname());
        assertEquals(null, otto4.getLastname());
        assertEquals(null, otto4.getPhones());
    }

    @Test
    public void testBidirectional() {
    	MapperFactory mapperFactory = MappingUtil.getMapperFactory(true);
        mapperFactory.classMap(Person1.class, Person2.class)
                .field("firstname", "forename").field("lastname", "backname").field("phones{number}", "phoneNumbers{}")
                .register();

        BoundMapperFacade<Person1,Person2> mapperFacade = mapperFactory.getMapperFacade(Person1.class, Person2.class);
        
        
        // Test A to B
        Person1 otto1 = new Person1();
        otto1.setFirstname("Otto");
        otto1.setLastname("McAllen");
        
        ArrayList<Phone> phones = new ArrayList<>();
        phones.add(new Phone((long) 15468));
        phones.add(new Phone((long) 99999));
        otto1.setPhones(phones);
        
        Person2 otto2 = mapperFacade.map(otto1);
        assertEquals(otto1.getFirstname(), otto2.getForename());
        assertEquals(otto1.getLastname(), otto2.getBackname());
        assertEquals(otto1.getPhones().get(0).getNumber(), otto2.getPhoneNumbers().get(0));
        
        
        // Test B to A
        Person2 otto3 = new Person2();
        otto3.setForename("FreshOtto");
        otto3.setBackname("McFresh");
        ArrayList<Long> phoneNumbers = new ArrayList<>();
        phoneNumbers.add((long)11111);
        phoneNumbers.add((long)22222);
        otto3.setPhoneNumbers(phoneNumbers);
        
        Person1 otto4 = mapperFacade.mapReverse(otto3);
        
        assertEquals(otto3.getForename(), otto4.getFirstname());
        assertEquals(otto3.getBackname(), otto4.getLastname());
        phoneNumbers = new ArrayList<>();
        for (Phone phone: otto4.getPhones()) {
        	phoneNumbers.add(phone.getNumber());
        }
        assertEquals(otto3.getPhoneNumbers(), phoneNumbers);
    }
    
    public static class Person1 {
        private String firstname;
        
        private String lastname;

		private ArrayList<Phone> phones;

        public String getFirstname() {
            return firstname;
        }
        
        public void setFirstname(String firstname){
        	this.firstname = firstname;
        }
        
        public String getLastname() {
			return lastname;
		}

		public void setLastname(String lastname) {
			this.lastname = lastname;
		}
        
        public void setPhones(ArrayList<Phone> phones){
        	this.phones = phones;
        }
        
        public ArrayList<Phone> getPhones() {
            return this.phones;
        }
    }
    
    public static class Phone {
    	private Long number;
    	
    	public Phone(Long number){
    		this.number = number;
    	}
    	
    	public void setNumber(Long number){
    		this.number = number;
    	}
    	
    	public Long getNumber(){
    		return number;
    	}
    }

    public static class Person2 {
        private String forename;
        
        private String backname;

		private ArrayList<Long> phoneNumbers;

        public String getForename() {
            return forename;
        }
        
        public void setForename(String forename){
        	this.forename = forename;
        }
        
        public String getBackname() {
			return backname;
		}

		public void setBackname(String backname) {
			this.backname = backname;
		}

        public ArrayList<Long> getPhoneNumbers() {
            return phoneNumbers;
        }
        
        public void setPhoneNumbers(ArrayList<Long> phoneNumbers){
        	this.phoneNumbers = phoneNumbers;
        }
    }
}
