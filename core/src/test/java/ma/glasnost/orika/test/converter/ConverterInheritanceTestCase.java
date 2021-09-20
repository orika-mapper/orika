/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.glasnost.orika.test.converter;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConverterInheritanceTestCase {
    
    public static class ListConverter extends CustomConverter<Collection<Person>, PersonGroup> {
        
        public PersonGroup convert(Collection<Person> source, Type<? extends PersonGroup> destinationType, MappingContext context) {
            return new PersonGroup(source);
        }
        
    }
    
    public static class Person {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    public static class PersonGroup {
        
        private final List<Person> people;
        
        public PersonGroup(Collection<Person> people) {
            this.people = new ArrayList<>(people);
        }
        
        public List<Person> getPeople() {
            return people;
        }
    }
    
    /**
     * Note: mapping by raw type doesn't work, because there's no way to know at
     * runtime that an ArrayList has been parameterized by 'Person'; it just
     * looks like an ArrayList of 'Object'
     */
    @Test(expected = MappingException.class)
    public void testConverterInheritanceRaw() {
        ArrayList<Person> people = new ArrayList<>();
        Person person = new Person();
        person.setName("A");
        people.add(person);
        person = new Person();
        person.setName("B");
        people.add(person);
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new ListConverter());
        
        PersonGroup group = factory.getMapperFacade().map(people, PersonGroup.class);
        Assert.assertNotNull(group);
        Assert.assertTrue(people.containsAll(group.getPeople()));
        Assert.assertTrue(group.getPeople().containsAll(people));
    }
    
    @Test
    public void testConverterInheritanceGenerics() {
        ArrayList<Person> people = new ArrayList<>();
        Person person = new Person();
        person.setName("A");
        people.add(person);
        person = new Person();
        person.setName("B");
        people.add(person);
        
        Type<Collection<Person>> personCollectionType = new TypeBuilder<Collection<Person>>() {
        }.build();
        Type<PersonGroup> personGroupType = TypeFactory.valueOf(PersonGroup.class);
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new ListConverter());
        
        PersonGroup group = factory.getMapperFacade().map(people, personCollectionType, personGroupType);
        Assert.assertNotNull(group);
        Assert.assertTrue(people.containsAll(group.getPeople()));
        Assert.assertTrue(group.getPeople().containsAll(people));
    }
    
}
