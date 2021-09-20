package ma.glasnost.orika.test.community;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * MapperFacade.map does not map into destination instance when context has already mapped the source Object.
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/issues/160">https://github.com/orika-mapper/orika/issues</a>
 */
public class Issue160TestCase {

	@Test
    public void testIssue160() {
        final MapperFactory factory = new DefaultMapperFactory.Builder().build();
        factory.classMap(A.class, X.class)
                .customize(new CustomMapper<A, X>() {
                    
                    @Override
                    public void mapAtoB(A a, X b, MappingContext context) {
                        factory.getMapperFacade().map(a, b.getY1(), context);
                        factory.getMapperFacade().map(a, b.getY2(), context);
                    }
                })
                .byDefault()
                .register();
        
        A a = new A();
        a.setValue1("testValue1");
        a.setValue2("testValue2");
        
        BoundMapperFacade<A, X> mapperFacade = factory.getMapperFacade(A.class, X.class);
        X mappedX = mapperFacade.map(a);
        
        assertThat(mappedX.getValue1(), is("testValue1"));
        assertThat(mappedX.getY1().getValue2(), is("testValue2"));
        assertThat(mappedX.getY2().getValue2(), is("testValue2"));
	}
    
    @Test
    public void testIssue160WithCycle() {
        
        final MapperFactory factory = new DefaultMapperFactory.Builder().build();
        factory.classMap(A.class, ZCycle.class)
                .customize(new CustomMapper<A, ZCycle>() {
                    
                    @Override
                    public void mapAtoB(A a, ZCycle b, MappingContext context) {
                        factory.getMapperFacade().map(a, b.getZcycle(), context);
                    }
                })
                .byDefault()
                .register();
        
        A a = new A();
        a.setValue1("testValue1");
        a.setValue2("testValue2");
        
        BoundMapperFacade<A, ZCycle> mapperFacade = factory.getMapperFacade(A.class, ZCycle.class);
        ZCycle mappedZ = mapperFacade.map(a);
        
        assertThat(mappedZ.getValue1(), is("testValue1"));
        assertThat(mappedZ.getValue2(), is("testValue2"));
        assertThat(mappedZ.getZcycle().getValue1(), is("testValue1"));
        assertThat(mappedZ.getZcycle().getValue2(), is("testValue2"));
        assertThat(mappedZ.getZcycle().getZcycle().getValue1(), is("testValue1"));
        assertThat(mappedZ.getZcycle().getZcycle().getValue2(), is("testValue2"));
    }
    public static class A {
        private String value1;
        private String value2;
        
        public String getValue1() {
            return value1;
        }
        
        public void setValue1(String value1) {
            this.value1 = value1;
        }
        
        public String getValue2() {
            return value2;
        }
        
        public void setValue2(String value2) {
            this.value2 = value2;
        }
        
    }
    
    public static class X {
        private String value1;
        private final Y y1 = new Y();
        private final Y y2 = new Y();
        
        public String getValue1() {
            return value1;
        }
        
        public void setValue1(String value1) {
            this.value1 = value1;
        }
        
        public Y getY1() {
            return y1;
        }
        
        public Y getY2() {
            return y2;
        }
        
    }
    
    public static class Y {
        private String value2;
        
        public String getValue2() {
            return value2;
        }
        
        public void setValue2(String value2) {
            this.value2 = value2;
        }
        
    }
    
    public static class ZCycle {
        private String value1;
        private String value2;
        
        private ZCycle zcycle;
        
        public String getValue1() {
            return value1;
        }
        
        public void setValue1(String value1) {
            this.value1 = value1;
        }
        
        public String getValue2() {
            return value2;
        }
        
        public void setValue2(String value2) {
            this.value2 = value2;
        }
        
        public ZCycle getZcycle() {
            if (zcycle == null) {
                zcycle = this;
            }
            return zcycle;
        }
        
    }
}
