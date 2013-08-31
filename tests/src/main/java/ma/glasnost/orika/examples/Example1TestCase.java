/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
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
package ma.glasnost.orika.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.property.IntrospectorPropertyResolver;
import ma.glasnost.orika.property.PropertyResolver;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 * 
 */
public class Example1TestCase {
    
    private static final String NESTED_OPEN = PropertyResolver.ELEMENT_PROPERT_PREFIX;
    private static final String NESTED_CLOSE = PropertyResolver.ELEMENT_PROPERT_SUFFIX;
    
    @Test
    public void resolveProperties() {
        
        PropertyResolver propertyResolver = new IntrospectorPropertyResolver();
        
        Property aliasesFirst = propertyResolver.getProperty(TypeFactory.valueOf(PersonDto.class), "aliases" + NESTED_OPEN + "[0]" + NESTED_CLOSE);
        
        Assert.assertNotNull(aliasesFirst);
        Assert.assertEquals(TypeFactory.valueOf(String.class), aliasesFirst.getType());
        Assert.assertNotNull(aliasesFirst.getContainer());
        Assert.assertEquals(TypeFactory.valueOf(String[][].class), aliasesFirst.getContainer().getType());
    }
    
    /**
     * !
     */
    @Test
    public void nestedElements() {
        
        MapperFactory mapperFactory = MappingUtil.getMapperFactory(true);
        
        mapperFactory.classMap(Person.class, PersonDto.class)
                .field("name.first", "firstName")
                .field("name.last", "lastName")
                .field("knownAliases" + NESTED_OPEN + "first" + NESTED_CLOSE, "aliases" + NESTED_OPEN + "[0]" + NESTED_CLOSE)
                .field("knownAliases" + NESTED_OPEN + "last" + NESTED_CLOSE, "aliases" + NESTED_OPEN + "[1]" + NESTED_CLOSE)
                .byDefault()
                .register();
        
        MapperFacade mapper = mapperFactory.getMapperFacade();
        
        List<Name> aliases = new ArrayList<Name>();
        aliases.add(new Name("Joe", "Williams"));
        aliases.add(new Name("Terry", "Connor"));
        Person source = new Person(new Name("John","Doe"), new Date(), aliases);
        
        PersonDto dest = mapper.map(source, PersonDto.class);
        
        Assert.assertNotNull(dest);
        Assert.assertEquals(source.getName().getFirst(), dest.getFirstName());
        Assert.assertEquals(source.getName().getLast(), dest.getLastName());
        Assert.assertEquals(source.getKnownAliases().get(0).getFirst(), dest.getAliases()[0][0]);
        Assert.assertEquals(source.getKnownAliases().get(0).getLast(), dest.getAliases()[0][1]);
        Assert.assertEquals(source.getKnownAliases().get(1).getFirst(), dest.getAliases()[1][0]);
        Assert.assertEquals(source.getKnownAliases().get(1).getLast(), dest.getAliases()[1][1]);
        
    }
    
    public static class Person {
        private Name name;
        private List<Name> knownAliases;
        private Date birthDate;
        
        public Person(Name name, Date birthDate, List<Name> knownAliases) {
            this.name = name;
            this.birthDate = (Date) birthDate.clone();
            this.knownAliases = new ArrayList<Name>(knownAliases);
        }
        
        public List<Name> getKnownAliases() {
            return Collections.unmodifiableList(knownAliases);
        }
        
        public Name getName() {
            return name;
        }
        
        public Date getBirthDate() {
            return (Date) birthDate.clone();
        }
    }
    
    public static class Name {
        private String first;
        private String last;
        
        public Name(String first, String last) {
            this.first = first;
            this.last = last;
        }
        
        public String getFirst() {
            return first;
        }
        
        public String getLast() {
            return last;
        }
    }
    
    public static class PersonDto {
        private String firstName;
        private String lastName;
        private Date birthDate;
        private String[][] aliases;
        
        public String getFirstName() {
            return firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public Date getBirthDate() {
            return birthDate;
        }
        
        public String[][] getAliases() {
            return aliases;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public void setBirthDate(Date birthDate) {
            this.birthDate = birthDate;
        }
        
        public void setAliases(String[][] aliases) {
            this.aliases = aliases;
        }
    }
}
