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

package ma.glasnost.orika.test.fieldmap;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class OverrideFieldNameTestCase {
    
    @Test
    public void testSimpleFieldMap() {
        ClassMap<Address, AddressDTO> classMap = ClassMapBuilder.map(Address.class, AddressDTO.class).field("country", "countryName")
                .field("city", "cityName").toClassMap();
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.registerClassMap(classMap);
        
        factory.build();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Address adress = new Address();
        adress.setCountry("Morocco");
        adress.setCity("Marrakesh");
        
        AddressDTO adressDTO = mapper.map(adress, AddressDTO.class);
        
        Assert.assertEquals(adress.getCountry(), adressDTO.getCountryName());
        Assert.assertEquals(adress.getCity(), adressDTO.getCityName());
    }
    
    public static class Address {
        private String country;
        private String city;
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
    }
    
    public static class AddressDTO {
        private String countryName;
        private String cityName;
        
        public String getCountryName() {
            return countryName;
        }
        
        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }
        
        public String getCityName() {
            return cityName;
        }
        
        public void setCityName(String cityName) {
            this.cityName = cityName;
        }
        
    }
    
}
