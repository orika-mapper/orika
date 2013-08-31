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

package ma.glasnost.orika.test.custommapper;

import junit.framework.Assert;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class CustomMappingTestCase {
    
    @Test
    public void testCustomMapping() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.classMap(PersonDTO.class, Person.class)
                .customize(
                        new CustomMapper<PersonDTO, Person>() {
                            @Override
                            public void mapBtoA(Person b, PersonDTO a, MappingContext context) {
                                a.setName(b.getFirstName() + " " + b.getLastName());
                            }
                            
                        })
               .register();
        
        Person person = new Person();
        person.setFirstName("Abdelkrim");
        person.setLastName("EL KHETTABI");
        
        PersonDTO dto = factory.getMapperFacade().map(person, PersonDTO.class);
        
        Assert.assertEquals(dto.getName(), person.getFirstName() + " " + person.getLastName());
    }
    
    public static class PersonDTO {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    public static class Person {
        private String firstName;
        private String lastName;
        
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
    }
}
