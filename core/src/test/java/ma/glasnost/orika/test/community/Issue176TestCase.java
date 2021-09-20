package ma.glasnost.orika.test.community;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Support for mapping one type to many.
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/issues/176">https://github.com/orika-mapper/orika/issues</a>
 */
public class Issue176TestCase {
    
    @Test
    public void testIssue176() throws Exception {
        
        final MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.registerObjectFactory((source, mappingContext) -> {
            A a = (A) source;
            final B b;
            if (a.getType().equals("1")) {
                b = new B1();
            } else if (a.getType().equals("2")) {
                b = new B2();
            } else {
                throw new IllegalArgumentException("type not supported: " + a.getType());
            }
            return b;
        }, TypeFactory.valueOf(B.class), TypeFactory.valueOf(A.class));
        
        factory.classMap(A.class, B1.class)
                .field("x", "x1")
                .byDefault()
                .register();
        
        factory.classMap(A.class, B2.class)
                .field("x", "x2")
                .byDefault()
                .register();
        
        // run the Test
        runTest(factory.getMapperFacade());
        
    }
    
    @Test
    public void testIssue176_Workaround() throws Exception {
        
        final MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.getConverterFactory().registerConverter(new CustomConverter<A, B>() {
            
            public B convert(A a, Type<? extends B> destType, MappingContext mappingContext) {
                B cachedObject = mappingContext.getMappedObject(a, destType);
                if (cachedObject != null) {
                    return cachedObject;
                }
                final B b;
                if (a.getType().equals("1")) {
                    b = new B1();
                } else if (a.getType().equals("2")) {
                    b = new B2();
                } else {
                    throw new IllegalArgumentException("type not supported: " + a.getType());
                }
                
                mappingContext.cacheMappedObject(a, (Type) destType, b);
                
                mappingContext.beginMapping(sourceType, a, destType, b);
                try {
                    factory.getMapperFacade().map(a, b, mappingContext);
                } finally {
                    mappingContext.endMapping();
                }
                
                return b;
            }
            
        });
        factory.classMap(A.class, B1.class)
                .field("x", "x1")
                .byDefault()
                .register();
        
        factory.classMap(A.class, B2.class)
                .field("x", "x2")
                .byDefault()
                .register();
        
        // run the Test
        runTest(factory.getMapperFacade());
        
    }
    
    private void runTest(final MapperFacade mapper) {
        
        A a1 = new A();
        a1.setType("1");
        a1.setX(11);
        A a2 = new A();
        a2.setType("2");
        a2.setX(22);
        
        // run test
        B b1 = mapper.map(a1, B.class);
        B b2 = mapper.map(a2, B.class);
        
        // validate result
        assertThat(b1, is(instanceOf(B1.class)));
        assertThat(((B1) b1).getX1(), is(11));
        assertThat(b2, is(instanceOf(B2.class)));
        assertThat(((B2) b2).getX2(), is(22));
    }
    
    public static class A {
        private String type;
        private int x;
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }
        
    }
    
    public interface B {
        // marker interface
    }
    
    public static class B1 implements B {
        private int x1;
        
        public int getX1() {
            return x1;
        }
        
        public void setX1(int x1) {
            this.x1 = x1;
        }
        
    }
    
    public static class B2 implements B {
        private int x2;
        
        public int getX2() {
            return x2;
        }
        
        public void setX2(int x2) {
            this.x2 = x2;
        }
        
    }
    
}