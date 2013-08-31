package ma.glasnost.orika.test.community;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.metadata.Type;

import org.junit.Test;

public class Issue44TestCase {
    
    @Test
    public void shouldMapCollection() {
        ConfigurableMapper mapper = new ConfigurableMapper() {
            @Override
            protected void configure(MapperFactory factory) {
                factory.classMap(Order.class, OrderDto.class).byDefault().register();
                factory.classMap(Product.class, ProductDto.class).byDefault().register();
            }
        };
        Order order = new Order();
        Product product = new Product();
        product.setName("myName");
        order.setProducts(asList(product));
        OrderDto orderDto = mapper.map(order, OrderDto.class);
        assertThat(orderDto.getProducts(), hasSize(1));
        assertThat(orderDto.getProducts().get(0).getName(), is(equalTo("myName")));
    }
    
    @Test
    public void shouldMapCollectionWithConverter() {
        ConfigurableMapper mapper = new ConfigurableMapper() {
            @Override
            protected void configure(MapperFactory factory) {
                factory.getConverterFactory().registerConverter("productToName", new CustomConverter<List<Product>, List<String>>() {
                    
                    public List<String> convert(List<Product> source, Type<? extends List<String>> destinationType) {
                        ArrayList<String> list = new ArrayList<String>(source.size());
                        for (Product product : source) {
                            list.add(product.getName());
                        }
                        return list;
                    }
                });
                factory.classMap(Order.class, OrderDto.class)
                        .fieldMap("products", "productNames")
                        .converter("productToName")
                        .add()
                        .register();
                factory.classMap(Product.class, ProductDto.class).byDefault().register();
            }
        };
        Order order = new Order();
        Product product = new Product();
        product.setName("myName");
        order.setProducts(asList(product));
        OrderDto orderDto = mapper.map(order, OrderDto.class);
        assertThat(orderDto.getProductNames(), hasSize(1));
        assertThat(orderDto.getProductNames().get(0), is(equalTo("myName")));
    }
    
    @Test
    public void shouldMapCollectionWithElementConverter_ToCollection() {
        ConfigurableMapper mapper = new ConfigurableMapper() {
            @Override
            protected void configure(MapperFactory factory) {
                factory.getConverterFactory().registerConverter("productToName", new CustomConverter<Product, String>() {
                    
                    public String convert(Product source, Type<? extends String> destinationType) {
                        return source.getName();
                    }
                });
                factory.classMap(Order.class, OrderDto.class)
                        .fieldMap("products", "productNames")
                        .converter("productToName")
                        .add()
                        .register();
                factory.classMap(Product.class, ProductDto.class).byDefault().register();
            }
        };
        
        Order order = new Order();
        Product product = new Product();
        product.setName("myName");
        order.setProducts(asList(product));
        OrderDto orderDto = mapper.map(order, OrderDto.class);
        assertThat(orderDto.getProductNames(), hasSize(1));
        assertThat(orderDto.getProductNames().get(0), is(equalTo("myName")));
    }
    
    @Test
    public void shouldMapCollectionWithElementConverter_ToArray() {
        ConfigurableMapper mapper = new ConfigurableMapper() {
            @Override
            protected void configure(MapperFactory factory) {
                factory.getConverterFactory().registerConverter("productToName", new CustomConverter<Product, String>() {
                    
                    public String convert(Product source, Type<? extends String> destinationType) {
                        return source.getName();
                    }
                });
                factory.classMap(Order.class, OrderDto2.class)
                        .fieldMap("products", "productNames")
                        .converter("productToName")
                        .add()
                        .register();
                factory.classMap(Product.class, ProductDto.class).byDefault().register();
            }
        };
        
        Order order = new Order();
        Product product = new Product();
        product.setName("myName");
        order.setProducts(asList(product));
        OrderDto2 orderDto = mapper.map(order, OrderDto2.class);
        assertThat(orderDto.getProductNames(), arrayWithSize(1));
        assertThat(orderDto.getProductNames()[0], is(equalTo("myName")));
    }
    
    public static class Product {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class Order {
        private List<Product> products;
        
        public List<Product> getProducts() {
            return products;
        }
        
        public void setProducts(List<Product> products) {
            this.products = products;
        }
    }
    
    public static class ProductDto {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class OrderDto {
        private List<ProductDto> products;
        private List<String> productNames;
        
        public List<ProductDto> getProducts() {
            return products;
        }
        
        public void setProducts(List<ProductDto> products) {
            this.products = products;
        }
        
        public List<String> getProductNames() {
            return productNames;
        }
        
        public void setProductNames(List<String> productNames) {
            this.productNames = productNames;
        }
    }
    
    public static class OrderDto2 {
        private List<ProductDto> products;
        private String[] productNames;
        
        public List<ProductDto> getProducts() {
            return products;
        }
        
        public void setProducts(List<ProductDto> products) {
            this.products = products;
        }
        
        public String[] getProductNames() {
            return productNames;
        }
        
        public void setProductNames(String[] productNames) {
            this.productNames = productNames;
        }
    }
}