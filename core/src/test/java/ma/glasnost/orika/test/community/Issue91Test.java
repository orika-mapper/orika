package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * No concrete class mapping defined error mapping a list of interfaces.
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/issues/91">https://github.com/orika-mapper/orika/issues</a>
 */
public class Issue91Test {

    private MapperFacade mapperFacade;

    @Before
    public void setUp() throws Exception {
        System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES,"true");
        final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFacade = mapperFactory.getMapperFacade();
    }

    @Test
    public void test() {
        A a = new A();
        B b = new C();
        b.setName("pippo");
        a.getList().add(b);

        A out = mapperFacade.map(a, A.class);

        assertNotNull(out);
    }

    public static class A {

        private List<B> list  = new ArrayList<>();

        public List<B> getList() {
            return list;
        }

        public void setList(List<B> list) {
            this.list = list;
        }
    }

    public static class C implements B {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public interface B {
        String getName();
        void setName(String name);
    }
}

