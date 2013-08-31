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
package ma.glasnost.orika.test.community;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class Issue64TestCase {
    
    @Test
    public void mapWithConstructor() {
        
        Supplier supplier = new Supplier();
        supplier.setName("Chester Tester");
        supplier.setEmail("chester@email.com");
        supplier.setAddresses(new ArrayList<Address>());
        Address addr = new Address();
        addr.street = "1234 Test st.";
        addr.city = "Testville";
        addr.country = "USA";
        addr.postalCode = "12354";
        supplier.getAddresses().add(addr);
        Contact ct = new Contact();
        ct.name = "Bob Tester";
        ct.email = "bob@email.com";
        supplier.setContacts(new ArrayList<Contact>());
        supplier.getContacts().add(ct);
        
        MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        MapperFacade mapper = mapperFactory.getMapperFacade();
        
        SupplierDto result = mapper.map(supplier, SupplierDto.class);
        
        Assert.assertNotNull(result);
        Assert.assertEquals(supplier.getAddresses().size(), result.getAddresses().size());
        Assert.assertEquals(supplier.getContacts().size(), result.getContacts().size());
        for (int i=0; i < supplier.getContacts().size(); ++i) {
            Assert.assertEquals(supplier.getContacts().get(i).email, result.getContacts().get(i).email);
            Assert.assertEquals(supplier.getContacts().get(i).name, result.getContacts().get(i).name);
        }
        for (int i=0; i < supplier.getAddresses().size(); ++i) {
            Assert.assertEquals(supplier.getAddresses().get(i).street, result.getAddresses().get(i).street);
            Assert.assertEquals(supplier.getAddresses().get(i).city, result.getAddresses().get(i).city);
            Assert.assertEquals(supplier.getAddresses().get(i).country, result.getAddresses().get(i).country);
            Assert.assertEquals(supplier.getAddresses().get(i).postalCode, result.getAddresses().get(i).postalCode);
        }
        Assert.assertEquals(supplier.getEmail(), result.getEmail());
        Assert.assertEquals(supplier.getName(), result.getName());
    }
    
    public static class Supplier {
        private List<Address> addresses;
        private List<Contact> contacts;
        private String name;
        private String email;
        /**
         * @return the addresses
         */
        public List<Address> getAddresses() {
            return addresses;
        }
        /**
         * @param addresses the addresses to set
         */
        public void setAddresses(List<Address> addresses) {
            this.addresses = addresses;
        }
        /**
         * @return the contacts
         */
        public List<Contact> getContacts() {
            return contacts;
        }
        /**
         * @param contacts the contacts to set
         */
        public void setContacts(List<Contact> contacts) {
            this.contacts = contacts;
        }
        /**
         * @return the name
         */
        public String getName() {
            return name;
        }
        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }
        /**
         * @return the email
         */
        public String getEmail() {
            return email;
        }
        /**
         * @param email the email to set
         */
        public void setEmail(String email) {
            this.email = email;
        }
        
    }
    
    public static class SupplierDto {
        private List<Address> addresses;
        private List<Contact> contacts;
        private String name;
        private String email;
        
        public SupplierDto(List<Address> addresses, List<Contact> contacts, String name, String email) {
            this.addresses = addresses;
            this.contacts = contacts;
            this.name = name;
            this.email = email;
        }
        
        /**
         * @return the addresses
         */
        public List<Address> getAddresses() {
            return addresses;
        }
        /**
         * @param addresses the addresses to set
         */
        public void setAddresses(List<Address> addresses) {
            this.addresses = addresses;
        }
        /**
         * @return the contacts
         */
        public List<Contact> getContacts() {
            return contacts;
        }
        /**
         * @param contacts the contacts to set
         */
        public void setContacts(List<Contact> contacts) {
            this.contacts = contacts;
        }
        /**
         * @return the name
         */
        public String getName() {
            return name;
        }
        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }
        /**
         * @return the email
         */
        public String getEmail() {
            return email;
        }
        /**
         * @param email the email to set
         */
        public void setEmail(String email) {
            this.email = email;
        }
        
        
    }
    
    public static class Address {
        public String street;
        public String city;
        public String country;
        public String postalCode;
    }
    
    public static class AddressDto {
        public String street;
        public String city;
        public String country;
        public String postalCode;
    }
    
    public static class Contact {
        public String name;
        public String email;
    }
    
    public static class ContactDto {
        public String name;
        public String email;
    }
}
