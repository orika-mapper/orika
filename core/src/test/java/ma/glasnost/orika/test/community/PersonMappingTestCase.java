package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Test;

import java.util.List;

/**
 * There doesn't appear to be a way to do this mapping using default behavior...
 * 
 * @author keithroberts
 *
 */
public class PersonMappingTestCase {

    /**
     * RUn 3 maps, rely on engine to map per the sub type.
     * Only one map appears to be retained in memory. The cache must key on the source type.
     * This means you cannot have multiple maps with the same source type...
     * @throws Exception
     */
    //@Test
    public void testMultiMap() throws Exception {
        PersonHolderA holderA = new PersonHolderA();
        PersonName name = new PersonName();
        name.setFirstName("George");
        name.setLastName("Goober");
        name.setFullName("George Goober");
        name.setSuffix("II");
        holderA.setPersonNameA(name);
        
        name = new PersonName();
        name.setFirstName("Joe");
        name.setLastName("Poluka");
        name.setFullName("Joe Poluka");
        holderA.setPersonNameB(name);

        name = new PersonName();
        name.setFirstName("Fred");
        name.setLastName("Foo");
        name.setFullName("Fred Foo");
        holderA.setPersonNameC(name);

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder()
                .build();
        mapperFactory
            .classMap(PersonHolderA.class, PersonHolderB.class)
                .field("personNameA","persons:{getPersons()|setPersons(%s)|type=List<" + SuffixPerson.class.getCanonicalName() + ">}[0]") //suffix
                //.field("personNameB","persons[1]") //full
                //.field("personNameC","persons[2]") //full
                .register();
        
        mapperFactory
            .classMap(PersonName.class, SuffixPerson.class)
                .field("firstName","firstName")
                .field("lastName","lastName")
                .field("suffix","suffix")
                .register();

        mapperFactory
            .classMap(PersonName.class, FullPerson.class)
                .field("firstName","firstName")
                .field("lastName","lastName")
                .field("fullName","fullName")
                .register();
        
        //medicaidHousehold[0].householdMemberReference[0].ref:{getRef()|setRef(%s)|type=gov.cms.hix._0_1.hix_core.PersonType}.id

        MapperFacade mapper = mapperFactory.getMapperFacade();
        PersonHolderB resultType = mapper.map(holderA,PersonHolderB.class);
        
        System.out.println("result=" + resultType);
    }

    /**
     * Uses one map only. This has a problem picking up the sub class contained in the list.
     * When the syntax on the EL seems correct, get a stackoverflow exception.
     * @throws Exception
     */
    @Test
    public void testSingleMap() throws Exception {
        PersonHolderA holderA = new PersonHolderA();
        PersonName name = new PersonName();
        name.setFirstName("George");
        name.setLastName("Goober");
        name.setFullName("George Goober");
        name.setSuffix("II");
        holderA.setPersonNameA(name);
        
        name = new PersonName();
        name.setFirstName("Joe");
        name.setLastName("Poluka");
        name.setFullName("Joe Poluka");
        holderA.setPersonNameB(name);

        name = new PersonName();
        name.setFirstName("Fred");
        name.setLastName("Foo");
        name.setFullName("Fred Foo");
        holderA.setPersonNameC(name);

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder()
                .build();
        mapperFactory
            .classMap(PersonHolderA.class, PersonHolderB.class)
                .field("personNameA.suffix","persons:{getPersons()|setPersons(%s)|type=List<" + SuffixPerson.class.getCanonicalName() + ">}[0].suffix") //suffix
                .field("personNameA.firstName","persons[0].firstName") //suffix
                .field("personNameA.lastName","persons[0].lastName") //suffix
                .register();
                
        //medicaidHousehold[0].householdMemberReference[0].ref:{getRef()|setRef(%s)|type=gov.cms.hix._0_1.hix_core.PersonType}.id

        MapperFacade mapper = mapperFactory.getMapperFacade();
        PersonHolderB resultType = mapper.map(holderA,PersonHolderB.class);
        
        System.out.println("result=" + resultType);
    }

    public static class PersonName {

        private String firstName;
        private String lastName;

        // getters/setters omitted
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        private String fullName;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
        
        private String suffix;

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }
        

    }
    public static class PersonBase {

        private String firstName;
        private String lastName;

        // getters/setters omitted
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String toString()
        {
            return " firstName="+firstName+" lastName="+lastName;
        }
    }
    
    public static class FullPerson extends PersonBase {

        private String fullName;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
        
        public String toString()
        {
            return super.toString()+" fullName="+fullName;
        }

    }
    
    public static class SuffixPerson extends PersonBase {

        private String suffix;

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }
        
        public String toString()
        {
            return super.toString()+" suffix="+suffix;
        }
    }

    public static class PersonHolderA {

        private PersonName personNameA;
        private PersonName personNameB;
        private PersonName personNameC;

        public PersonName getPersonNameA() {
            return personNameA;
        }
        public void setPersonNameA(PersonName personNameA) {
            this.personNameA = personNameA;
        }
        public PersonName getPersonNameB() {
            return personNameB;
        }
        public void setPersonNameB(PersonName personNameB) {
            this.personNameB = personNameB;
        }
        public PersonName getPersonNameC() {
            return personNameC;
        }
        public void setPersonNameC(PersonName personNameC) {
            this.personNameC = personNameC;
        }
    }
    
    public static class PersonHolderB {

        private List<PersonBase> persons;

        public List<PersonBase> getPersons() {
            return persons;
        }

        public void setPersons(List<PersonBase> persons) {
            this.persons = persons;
        }
        
        public String toString()
        {
            StringBuffer buffer = new StringBuffer();
            for (PersonBase person : this.persons)
            {
                buffer.append(person.toString()).append("\n");
            }
            return buffer.toString();
        }
    }

}
