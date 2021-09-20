package ma.glasnost.orika.test.community.issue131;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Property types are not correctly resolved from generic interfaces.
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/issues/131">https://github.com/orika-mapper/orika/issues</a>
 * 
 */
public class Issue131Test {

    private MapperFacade mapper;

    public static class A {
        private List<Long> content;

        public List<Long> getContent() {
            return content;
        }

        public void setContent(List<Long> content) {
            this.content = content;
        }
    }

    public interface TB<T> {
        Collection<T> getContent();

        void setContent(Collection<T> content);
    }

    public interface BStrings extends TB<String> {
    }

    public interface BLongs extends TB<Long> {
    }

    public static class ConcreteBStrings implements BStrings {
        private Collection<String> content;

        public Collection<String> getContent() {
            return content;
        }

        public void setContent(Collection<String> content) {
            this.content = content;
        }
    }

    public static class ConcreteBLongs implements BLongs {
        private Collection<Long> content;

        @Override
        public Collection<Long> getContent() {
            return content;
        }

        @Override
        public void setContent(Collection<Long> content) {
            this.content = content;
        }
    }

    @Before
    public void setup() {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        mapperFactory.classMap(A.class, TB.class).byDefault().register();
        mapperFactory.classMap(A.class, BStrings.class).byDefault().register();
        mapperFactory.classMap(A.class, BLongs.class).byDefault().register();
        mapperFactory.registerConcreteType(BStrings.class, ConcreteBStrings.class);
        mapperFactory.registerConcreteType(BLongs.class, ConcreteBLongs.class);

        mapper = mapperFactory.getMapperFacade();
    }

    @Test
    public void testCase() {
        A a = new A();
        a.setContent(Arrays.asList(1L, 2L, 3L));
        BLongs longs = mapper.map(a, BLongs.class);
        assertElementOfType(longs.getContent(), Long.class);

        BStrings strings = mapper.map(a, BStrings.class);
        assertElementOfType(strings.getContent(), String.class);

    }

    private static void assertElementOfType(Collection<?> collection, Class<?> expectedType) {
        assertThat(collection, is(not(empty())));
        for (Object e : collection) {
            Class<?> elementType = e.getClass();
            Assert.assertTrue("element '" + e + "' should be assignable to '" + expectedType + " but is of " + elementType,
                              expectedType.isAssignableFrom(elementType));
        }
    }
}
