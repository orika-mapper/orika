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

package ma.glasnost.orika.test.inheritance;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class NestedInheritanceTestCase {
    
    @Test
    public void testNestedInheritance() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.registerClassMap(factory.classMap(Person.class, PersonDTO.class).byDefault().toClassMap());
        factory.registerClassMap(factory.classMap(Client.class, ClientDTO.class).byDefault().toClassMap());
        factory.registerClassMap(factory.classMap(Subscription.class, SubscriptionDTO.class).field("client", "person").toClassMap());
        
        Client client = new Client();
        client.setName("Khalil Gebran");
        
        Subscription subscription = new Subscription();
        subscription.setClient(client);
        
        SubscriptionDTO dto = factory.getMapperFacade().map(subscription, SubscriptionDTO.class);
        
        Assert.assertNotNull(dto);
        Assert.assertNotNull(dto.getPerson());
        Assert.assertEquals(client.getName(), dto.getPerson().getName());
    }
    
    @Test
    public void testNestedPolymorphicInheritance() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.registerClassMap(factory.classMap(Person.class, PersonDTO.class).byDefault().toClassMap());
        factory.registerClassMap(factory.classMap(Client.class, ClientDTO.class).byDefault().toClassMap());
        factory.registerClassMap(factory.classMap(Employee.class, EmployeeDTO.class).byDefault().toClassMap());
        factory.registerClassMap(factory.classMap(Subscription.class, SubscriptionDTO.class).field("client", "person").toClassMap());
        
        Client client = new Client();
        client.setName("Khalil Gebran");
        
        Employee employee = new Employee();
        employee.setName("Kuipje Anders");
        
        Subscription clientSubscription = new Subscription();
        clientSubscription.setClient(client);
        
        Subscription employeeSubscription = new Subscription();
        employeeSubscription.setClient(employee);
        
        List<Subscription> subscriptions = Arrays.asList(clientSubscription, employeeSubscription);
        
        List<SubscriptionDTO> dto = factory.getMapperFacade().mapAsList(subscriptions, SubscriptionDTO.class);
        
        Assert.assertNotNull(dto);
        Assert.assertEquals(2, dto.size());
        Assert.assertNotNull(dto.get(0).getPerson());
        Assert.assertEquals(client.getName(), dto.get(0).getPerson().getName());
        Assert.assertEquals(employee.getName(), dto.get(1).getPerson().getName());
        Assert.assertEquals(ClientDTO.class, dto.get(0).getPerson().getClass());
        Assert.assertEquals(EmployeeDTO.class, dto.get(1).getPerson().getClass());
    }
    
    public abstract static class Person {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    public static class Client extends Person {
        
    }
    
    public static class Employee extends Person {
        
    }
    
    public static class Subscription {
        
        private Person client;
        
        public Person getClient() {
            return client;
        }
        
        public void setClient(Person client) {
            this.client = client;
        }
        
    }
    
    public static abstract class PersonDTO {
        
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    public static class ClientDTO extends PersonDTO {
        
    }
    
    public static class EmployeeDTO extends PersonDTO {
        
    }
    
    public static class SubscriptionDTO {
        private PersonDTO person;
        
        public PersonDTO getPerson() {
            return person;
        }
        
        public void setPerson(PersonDTO person) {
            this.person = person;
        }
        
    }
}