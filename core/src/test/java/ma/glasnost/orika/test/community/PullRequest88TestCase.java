package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * Do not loose Map instance when mapping MultiOccurenceVariable to a Map.
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/pull/88">https://github.com/orika-mapper/orika</a>
 */
public class PullRequest88TestCase {
    public static final Type<Set<A>> SET = new TypeBuilder<Set<A>>() {}.build();
    public static final Type<Map<String, String>> MAP = new TypeBuilder<Map<String, String>>() {}.build();

    private MapperFacade mapper;

    @Before
    public void setUp() {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        mapperFactory.classMap(SET, MAP)
                .field("{name}", "{key}")
                .field("{name}", "{value}")
                .register();

        mapper = mapperFactory.getMapperFacade();
    }

    @Test
    public void testSetToMap() throws Exception {
        Set<A> set = new HashSet<>(asList(new A("a"), new A("b")));
        Map<String, String> map = mapper.map(set, SET, MAP);
        assertThat(map.keySet(), is(not(empty())));
        assertThat(map, hasEntry("a", "a"));
        assertThat(map, hasEntry("b", "b"));
    }

    public static class A {
        private String name;

        public A(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

