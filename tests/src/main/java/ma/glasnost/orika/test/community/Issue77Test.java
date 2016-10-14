package ma.glasnost.orika.test.community;

import com.google.common.collect.ImmutableMap;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class Issue77Test {

    @Test
    public void map_with_keys_containing_invalid_characters_for_a_variable_instantiation() {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory(true);

        mapperFactory.classMap(A.class, B.class)
                .field("mapSource['foo//bar']", "targetSet")
                .register();

        MapperFacade mapperFacade = mapperFactory.getMapperFacade();

        Map<String, List<String>> mapSource = ImmutableMap.<String, List<String>>builder()
                .put("foo//bar", asList("one", "two"))
                .build();
        A source = new A();
        source.setMapSource(mapSource);

        B map1 = mapperFacade.map(source, B.class);

        assertEquals(newHashSet("one", "two"), map1.getTargetSet());
    }

    public static class A {
        private Map<String, List<String>> mapSource;

        public Map<String, List<String>> getMapSource() {
            return mapSource;
        }

        public void setMapSource(Map<String, List<String>> mapSource) {
            this.mapSource = mapSource;
        }
    }

    public static class B {
        private Set<String> targetSet;

        public Set<String> getTargetSet() {
            return targetSet;
        }

        public void setTargetSet(Set<String> targetSet) {
            this.targetSet = targetSet;
        }
    }
}
