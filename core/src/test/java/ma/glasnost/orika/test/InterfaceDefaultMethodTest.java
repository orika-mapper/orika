package ma.glasnost.orika.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;

import ma.glasnost.orika.impl.DefaultMapperFactory;

public class InterfaceDefaultMethodTest {
    
    @Test
    public void defaultInterfaceImplementationsTest() {
        DefaultMapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        
        mapperFactory.classMap(A.class, B.class).byDefault().register();
        
        B b = mapperFactory.getMapperFacade().map(new A(), B.class);
        assertThat(b, notNullValue());
        assertThat(b.getId(), is("test"));
    }
    
    public static interface BaseA {
        
        default String getId() {
            return "test";
        }
    }
    
    public static class A implements BaseA {
        // inherited default methods from Interface
    }
    
    public static class B {
        String id;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
    }
}
