package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @see <a href="https://github.com/orika-mapper/orika/issues/322">https://github.com/orika-mapper/orika/issues/322</a>
 */
public class Issue322TestCase {

    @Test
    public void compile_error_when_customized_list_element_mapping_int_to_integer() {
        PersonList personList = new PersonList();
        Person person = new Person();
        person.age = 20;
        personList.persons.add(person);

        MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        mapperFactory.classMap(PersonList.class, PersonAgeListDto.class)
                .field("persons{age}", "personAges{}")
                .byDefault()
                .register();

        PersonAgeListDto dest = mapperFactory.getMapperFacade().map(personList, PersonAgeListDto.class);

        assertEquals(1, dest.personAges.size());
        assertEquals(Integer.valueOf(20), dest.personAges.get(0));
    }

    public static class Person {
        public int age;
    }

    public static class PersonList {
        public List<Person> persons = new ArrayList<>();
    }

    public static class PersonAgeListDto {
        public List<Integer> personAges;
    }
}
