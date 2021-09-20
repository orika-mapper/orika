package ma.glasnost.orika.test.community;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.ObjectFactory;
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
public class Issue176WithSuperClassesTestCase {
    
    private static final ObjectFactory<B> OBJECT_FACTORY = (source, mappingContext) -> {
        A a = (A) source;
        final B b;
        if ("1".equals(a.type)) {
            b = new B1();
        } else if ("2".equals(a.type)) {
            b = new B2();
        } else if (a.type == null || a.type.isEmpty()) {
            b = new B();
        } else {
            throw new IllegalArgumentException("type not supported: " + a.type);
        }
        return b;
    };
    
    @Test
    public void testIssue176_withSuperClasses() throws Exception {
        
        final MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.registerObjectFactory(OBJECT_FACTORY, TypeFactory.valueOf(B.class), TypeFactory.valueOf(A.class));
        
        factory.classMap(A.class, B1.class)
                .field("x", "x1")
                .customize(new CustomMapper<A, B1>() {
                    // custom reverse mapping
                    @Override
                    public void mapBtoA(B1 b, A a, MappingContext context) {
                        a.type = "1";
                    }
                })
                .byDefault()
                .register();
        
        factory.classMap(A.class, B2.class)
                .field("x", "x2")
                .customize(new CustomMapper<A, B2>() {
                    // custom reverse mapping
                    @Override
                    public void mapBtoA(B2 b, A a, MappingContext context) {
                        a.type = "2";
                    }
                })
                .byDefault()
                .register();
        
        // testing auto generation of Mapper (A.class, B.class)
        
        // run the Test
        MapperFacade mapper = factory.getMapperFacade();
        A a0 = new A();
        A a1 = new A();
        a1.type = "1";
        a1.x = 11;
        A a2 = new A();
        a2.type = "2";
        a2.x = 22;
        
        // run test
        B b0 = mapper.map(a0, B.class);
        B b1 = mapper.map(a1, B.class);
        B b2 = mapper.map(a2, B.class);
        
        // validate result
        assertThat(b0, is(instanceOf(B.class)));
        assertThat(b1, is(instanceOf(B1.class)));
        assertThat(((B1) b1).x1, is(11));
        assertThat(b2, is(instanceOf(B2.class)));
        assertThat(((B2) b2).x2, is(22));
        
        // run test reverse mapping
        A aMapped0 = mapper.map(b0, A.class);
        A aMapped1 = mapper.map(b1, A.class);
        A aMapped2 = mapper.map(b2, A.class);
        
        // validate result
        assertThat(aMapped0, is(instanceOf(A.class)));
        assertThat(aMapped1, is(instanceOf(A.class)));
        assertThat(aMapped1.x, is(11));
        assertThat(aMapped1.type, is("1"));
        assertThat(aMapped2, is(instanceOf(A.class)));
        assertThat(aMapped2.x, is(22));
        assertThat(aMapped2.type, is("2"));
    }
    
    @Test
    public void testIssue176_compinationOfSuperClassHirarchyAndObjectFactory() throws Exception {
        
        final MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.registerObjectFactory(OBJECT_FACTORY, TypeFactory.valueOf(B.class), TypeFactory.valueOf(A.class));
        
        factory.classMap(A.class, B1.class)
                .field("x", "x1")
                .customize(new CustomMapper<A, B1>() {
                    // custom reverse mapping
                    @Override
                    public void mapBtoA(B1 b, A a, MappingContext context) {
                        a.type = "1";
                    }
                })
                .byDefault()
                .register();
        
        factory.classMap(A.class, B2.class)
                .field("x", "x2")
                .customize(new CustomMapper<A, B2>() {
                    // custom reverse mapping
                    @Override
                    public void mapBtoA(B2 b, A a, MappingContext context) {
                        a.type = "2";
                    }
                })
                .byDefault()
                .register();
        
        factory.classMap(AX.class, BX.class)
                .field("y", "x3")
                .byDefault()
                .register();
        
        factory.classMap(A.class, B.class)
                .byDefault()
                .register();
        
        factory.classMap(ASuper.class, BSuper.class)
                .byDefault()
                .register();
        
        // run the Test
        A a0 = new A();
        a0.type = "";
        A a1 = new A();
        a1.type = "1";
        a1.x = 11;
        A a2 = new A();
        a2.type = "2";
        a2.x = 22;
        AX aX = new AX();
        aX.y = 33;
        
        ASuperContainer container = new ASuperContainer();
        container.elelemts.add(a0);
        container.elelemts.add(a1);
        container.elelemts.add(a2);
        container.elelemts.add(aX);
        
        // run test
        MapperFacade mapper = factory.getMapperFacade();
        
        BSuperContainer bContainer = mapper.map(container, BSuperContainer.class);
        BSuper b0 = bContainer.elelemts.get(0);
        BSuper b1 = bContainer.elelemts.get(1);
        BSuper b2 = bContainer.elelemts.get(2);
        BSuper b3 = bContainer.elelemts.get(3);
        
        // validate result
        assertThat(b0, is(instanceOf(B.class)));
        assertThat(b1, is(instanceOf(B1.class)));
        assertThat(((B1) b1).x1, is(11));
        assertThat(b2, is(instanceOf(B2.class)));
        assertThat(((B2) b2).x2, is(22));
        assertThat(b3, is(instanceOf(BX.class)));
        assertThat(((BX) b3).x3, is(33));
        
        // run test reverse mapping
        
        ASuperContainer mappedAContainer = mapper.map(bContainer, ASuperContainer.class);
        ASuper aMapped0 = mappedAContainer.elelemts.get(0);
        ASuper aMapped1 = mappedAContainer.elelemts.get(1);
        ASuper aMapped2 = mappedAContainer.elelemts.get(2);
        ASuper aMapped3 = mappedAContainer.elelemts.get(3);
        
        // validate result
        assertThat(aMapped0, is(instanceOf(A.class)));
        assertThat(aMapped1, is(instanceOf(A.class)));
        assertThat(((A) aMapped1).x, is(11));
        assertThat(((A) aMapped1).type, is("1"));
        assertThat(aMapped2, is(instanceOf(A.class)));
        assertThat(((A) aMapped2).x, is(22));
        assertThat(((A) aMapped2).type, is("2"));
        assertThat(aMapped3, is(instanceOf(AX.class)));
        assertThat(((AX) aMapped3).y, is(33));
    }
    
    @Test
    public void testIssue176_withSuperClassHirarchyOnly() throws Exception {
        
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
    
    public static class ASuper {
        // marker interface
    }
    
    public static class A extends ASuper {
        public String type;
        public int x;
        
    }
    
    public static class AX extends ASuper {
        public int y;
    }
    
    // B-Hierarchy:
    public static class BSuperContainer {
        public List<BSuper> elelemts = new ArrayList<>();
    }
    
    public static class BSuper {
        // marker interface
    }
    
    public static class B extends BSuper {
        // marker interface
    }
    
    public static class B1 extends B {
        public int x1;
    }
    
    public static class B2 extends B {
        public int x2;
    }
    
    public static class BX extends BSuper {
        public int x3;
        
    }
    
}