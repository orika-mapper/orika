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

package ma.glasnost.orika.test.community;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

/**
 * I'm not able to set map type using orika.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/102">https://code.google.com/archive/p/orika/</a>
 * 
 */
public class Issue102TestCase {
    
    @Test
    public void testWithoutGenerics() {
        Product p = new Product();
        p.setAvailability(true);
        p.setProductDescription("hi product description");
        p.setProductName("product class 1");
        Map map = new HashMap();
        map.put(1, "kiran");
        map.put(2, "pandu");
        map.put(3, "varsha");
        p.setMap(map);
        
        
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory.classMap(Product.class, ProductDto.class)
                .field("productDescription", "description")
                .field("map", "map")
                .byDefault()
                .register();
        MapperFacade mapper = mapperFactory.getMapperFacade();
        ProductDto pd = mapper.map(p, ProductDto.class);
        
        Assert.assertNotNull(pd.getMap());
        Assert.assertEquals(p.getMap().size(), pd.getMap().size());
        for (Object e: p.getMap().entrySet()) {
            Entry<?,?> entry = (Entry<?,?>)e;
            Assert.assertEquals(entry.getValue(), pd.getMap().get(entry.getKey()));
        }
        
    }
    
    public static class Product {
        private String productName;
        
        private String productDescription;

        private Map map;


        private Boolean availability;
        
        public Map getMap() {
            return map;
        }
        
        public void setMap(Map map) {
            this.map = map;
        }
        
        public String getProductDescription() {
            return productDescription;
        }
        
        public void setProductDescription(String productDescription) {
            this.productDescription = productDescription;
        }

        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
        
        public Boolean getAvailability() {
            return availability;
        }
        
        public void setAvailability(Boolean availability) {
            this.availability = availability;
        }
        
    }
    
    public static class ProductDto {
        
        private String productName;
        
        private String description;
        Map map;
        
        public Map getMap() {
            return map;
        }
        
        public void setMap(Map map) {
            this.map = map;
        }
        
        private Boolean availability;
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public Boolean getAvailability() {
            return availability;
        }
        
        public void setAvailability(Boolean availability) {
            this.availability = availability;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
    }
    
}
