package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.TypeFactory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Support for mapping one type to many.
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/issues/176">https://github.com/orika-mapper/orika/issues</a>
 */
public class Issue176WithInterfacesTestCase {
    
    @Test
    public void testIssue176_withInterfaces() throws Exception {
        
        final MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.registerObjectFactory((source, mappingContext) -> {
            A a = (A) source;
            final B b;
            if (a.type.equals("1")) {
                b = new B1();
            } else if (a.type.equals("2")) {
                b = new B2();
            } else {
                throw new IllegalArgumentException("type not supported: " + a.type);
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
        MapperFacade mapper = factory.getMapperFacade();
        A a1 = new A();
        a1.type = "1";
        a1.x = 11;
        A a2 = new A();
        a2.type = "2";
        a2.x = 22;
        
        // run test
        B b1 = mapper.map(a1, B.class);
        B b2 = mapper.map(a2, B.class);
        
        // validate result
        assertThat(b1, is(instanceOf(B1.class)));
        assertThat(((B1) b1).x1, is(11));
        assertThat(b2, is(instanceOf(B2.class)));
        assertThat(((B2) b2).x2, is(22));
        
    }
    
    @Test
    public void testIssue176_compinationOfInterfaceHirarchyAndObjectFactory() throws Exception {
        
        final MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.registerObjectFactory((source, mappingContext) -> {
            A a = (A) source;
            final B b;
            if (a.type.equals("1")) {
                b = new B1();
            } else if (a.type.equals("2")) {
                b = new B2();
            } else {
                throw new IllegalArgumentException("type not supported: " + a.type);
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
        
        factory.classMap(AX.class, BX.class)
                .field("y", "x3")
                .byDefault()
                .register();
        
        factory.classMap(ASuper.class, BSuper.class)
                .byDefault()
                .register();
        
        // run the Test
        A a1 = new A();
        a1.type = "1";
        a1.x = 11;
        A a2 = new A();
        a2.type = "2";
        a2.x = 22;
        AX aX = new AX();
        aX.y = 33;
        
        ASuperContainer container = new ASuperContainer();
        container.elelemts.add(a1);
        container.elelemts.add(a2);
        container.elelemts.add(aX);
        
        // run test
        MapperFacade mapper = factory.getMapperFacade();
        
        BSuperContainer bContainer = mapper.map(container, BSuperContainer.class);
        BSuper b1 = bContainer.elelemts.get(0);
        BSuper b2 = bContainer.elelemts.get(1);
        BSuper b3 = bContainer.elelemts.get(2);
        
        // validate result
        assertThat(b1, is(instanceOf(B1.class)));
        assertThat(((B1) b1).x1, is(11));
        assertThat(b2, is(instanceOf(B2.class)));
        assertThat(((B2) b2).x2, is(22));
        assertThat(b3, is(instanceOf(BX.class)));
        assertThat(((BX) b3).x3, is(33));
        
    }
    
    @Test
    public void testIssue176_hirarchyOnly() throws Exception {
        
        final MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.classMap(A.class, B1.class)
                .field("x", "x1")
                .byDefault()
                .register();
        
        factory.classMap(AX.class, BX.class)
                .field("y", "x3")
                .byDefault()
                .register();
        
        factory.classMap(ASuper.class, BSuper.class)
                .byDefault()
                .register();
        
        // run the Test
        A a1 = new A();
        a1.x = 11;
        AX aX = new AX();
        aX.y = 33;
        
        // run test
        MapperFacade mapper = factory.getMapperFacade();
        
        BSuper b1 = mapper.map(a1, BSuper.class);
        BSuper b3 = mapper.map(aX, BSuper.class);
        
        // validate result
        assertThat(b1, is(instanceOf(B1.class)));
        assertThat(((B1) b1).x1, is(11));
        assertThat(b3, is(instanceOf(BX.class)));
        assertThat(((BX) b3).x3, is(33));
        
    }
    
    // A-Hierarchy:
    public static class ASuperContainer {
        public List<ASuper> elelemts = new ArrayList<>();
    }
    
    public interface ASuper {
        // marker interface
    }
    
    public static class A implements ASuper {
        public String type;
        public int x;
        
    }
    
    public static class AX implements ASuper {
        public int y;
    }
    
    // B-Hierarchy:
    public static class BSuperContainer {
        public List<BSuper> elelemts = new ArrayList<>();
    }
    
    public interface BSuper {
        // marker interface
    }
    
    public interface B extends BSuper {
        // marker interface
    }
    
    public static class B1 implements B {
        public int x1;
    }
    
    public static class B2 implements B {
        public int x2;
    }
    
    public static class BX implements BSuper {
        public int x3;
        
    }
    
}