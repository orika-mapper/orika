package ma.glasnost.orika.test.util;

import junit.framework.Assert;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.Type;

import org.junit.Test;

public class ConfigurableMapperTestCase {
    
    public static class CustomConfigurableMapper extends ConfigurableMapper {
        
        protected void configure(MapperFactory mapperFactory) {
            mapperFactory.getConverterFactory().registerConverter(new CustomConverter<Address, String>() {
                
                public String convert(Address source, Type<? extends String> destinationType) {
                    return source.getLine1() + " " + source.getLine2();
                }
            });
            
            ClassMapBuilder<Order, OrderDTO> classMapBuilder = ClassMapBuilder.map(Order.class, OrderDTO.class);
            classMapBuilder.fieldMap("customer.address", "shippingAddress").add();
            
            mapperFactory.registerClassMap(classMapBuilder.byDefault().toClassMap());
            
        }
        
    }
    
    private MapperFacade mapper = new CustomConfigurableMapper();
    
    @Test
    public void testConfigurableMapper() {
        
        Address address = new Address();
        address.setLine1("5 rue Blida");
        address.setLine2("Casablanca");
        
        Customer customer = new Customer();
        customer.setName("Sidi Mohammed El Aatifi");
        customer.setAddress(address);
        
        Order order = new Order();
        order.setNumber("CPC6128");
        order.setCustomer(customer);
        
        OrderDTO orderDto = mapper.map(order, OrderDTO.class);
        
        Assert.assertEquals(address.line1 + " " + address.line2, orderDto.getShippingAddress());
        
    }
    
    public static class Address {
        
        private String line1;
        
        private String line2;
        
        public String getLine1() {
            return line1;
        }
        
        public void setLine1(String line1) {
            this.line1 = line1;
        }
        
        public String getLine2() {
            return line2;
        }
        
        public void setLine2(String line2) {
            this.line2 = line2;
        }
        
    }
    
    public static class Customer {
        
        private String name;
        
        private Address address;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Address getAddress() {
            return address;
        }
        
        public void setAddress(Address address) {
            this.address = address;
        }
        
    }
    
    public static class Order {
        
        private String number;
        
        private Customer customer;
        
        public String getNumber() {
            return number;
        }
        
        public void setNumber(String number) {
            this.number = number;
        }
        
        public Customer getCustomer() {
            return customer;
        }
        
        public void setCustomer(Customer customer) {
            this.customer = customer;
        }
        
    }
    
    public static class OrderDTO {
        
        public String getNumber() {
            return number;
        }
        
        public void setNumber(String number) {
            this.number = number;
        }
        
        public String getCustomerName() {
            return customerName;
        }
        
        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }
        
        public String getShippingAddress() {
            return shippingAddress;
        }
        
        public void setShippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
        }
        
        private String number;
        
        private String customerName;
        
        private String shippingAddress;
        
    }
    
}
