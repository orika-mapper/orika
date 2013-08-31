package ma.glasnost.orika.test.community;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

public class Issue77TestCase {
    
    public static class A1 {

    }

    public static class B1 extends A1 {
        int someProperty;
    }

    public static class Container1 {
        private List<A1>    elements    = new ArrayList<A1>();
        private A1          singleElement;

        public List<A1> getElements() {
            return elements;
        }

        public void setElements(final List<A1> elements) {
            this.elements = elements;
        }

        public A1 getSingleElement() {
            return singleElement;
        }

        public void setSingleElement(final A1 singleElement) {
            this.singleElement = singleElement;
        }
    }

    public static class A2 {

    }

    public static class B2 extends A2 {
        int someProperty;
    }

    public static class Container2 {
        private List<A2>    elements    = new ArrayList<A2>();
        private A2          singleElement;

        public List<A2> getElements() {
            return elements;
        }

        public void setElements(final List<A2> elements) {
            this.elements = elements;
        }

        public A2 getSingleElement() {
            return singleElement;
        }

        public void setSingleElement(final A2 singleElement) {
            this.singleElement = singleElement;
        }
    }

    @Test
    public void testMappingSubTypes() {
        // Test data
        final Container1 c1 = new Container1();
        c1.elements.add(new B1());
        c1.singleElement = new B1();

        // mapper
        final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory.classMap(B1.class, B2.class).byDefault().register();
        mapperFactory.classMap(B2.class, B1.class).byDefault().register();
        final MapperFacade mapper = mapperFactory.getMapperFacade();

        // map
        final Container2 c2 = mapper.map(c1, Container2.class);

        // check
        assertTrue(c1.singleElement instanceof B1);
        assertTrue(c2.singleElement instanceof B2);
        assertTrue(c2.elements.get(0) instanceof B2);
    }  
}
