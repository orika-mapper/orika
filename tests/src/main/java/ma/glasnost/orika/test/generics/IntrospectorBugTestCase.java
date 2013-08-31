package ma.glasnost.orika.test.generics;

import java.beans.IntrospectionException;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for to work around http://bugs.sun.com/view_bug.do?bug_id=6788525
 */
public class IntrospectorBugTestCase {
    
    @Test
    public void testIntrospectorBugWorkaround() throws IntrospectionException {
        MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();
        Source source = new Source();
        source.setFoo("Hello World");
        Destination destination = mapperFacade.map(source, Destination.class);
        Assert.assertEquals("Hello World", destination.getFoo());
    }
    
    public static class Base<T> {
        private T foo;
        
        public T getFoo() {
            return this.foo;
        }
        
        public void setFoo(T t) {
            this.foo = t;
        }
    }
    
    public static class Source extends Base<String> {
    }
    
    public static class Destination {
        private String foo;
        
        public String getFoo() {
            return foo;
        }
        
        public void setFoo(String foo) {
            this.foo = foo;
        }
    }
    
}