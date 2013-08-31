package ma.glasnost.orika.test.community;

import java.util.List;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;

import junit.framework.Assert;

import org.junit.Test;

public class LongPathPropertyTestCase {
    
    @Test
    public void testSimpleCase() {
        MapperFacade mapper = new ConfigurableMapper() {
            
            @Override
            protected void configure(final MapperFactory factory) {
                factory.classMap(A.class, D.class).field("second[0].third[0].message", "message").register();
            }
            
        };
        D source = new D();
        source.message = "Hello World";
        
         Assert.assertEquals(source.message, mapper.map(source, A.class).second.get(0).third.get(0).message);
    }
    
    public static class A {
        public List<B> second;
    }
    
    public static class B {
        public List<C> third;
    }
    
    public static class C {
        public String message;
    }
    
    public static class D {
        public String message;
    }
}
