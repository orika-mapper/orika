package ma.glasnost.orika.test.community;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

public class Issue102TestCase {
    
    @Test
    public void testWithoutGenerics() {
        System.out.println("pandu");
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
        
        // private Double price;
        Map map;
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
        
        /*
         * public Double getPrice() { return price; }
         * 
         * public void setPrice(Double price) { this.price = price; }
         */
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
        // private BigDecimal price;
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
        
        /*
         * public BigDecimal getPrice() { return price; }
         * 
         * public void setPrice(BigDecimal price) { this.price = price; }
         */
        
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
