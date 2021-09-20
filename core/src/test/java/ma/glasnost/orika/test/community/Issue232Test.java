package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.NullFilter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Issue232Test {

    private MapperFactory mapperFactory;

    @Before
    public void setUp() throws Exception {
        mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory.registerFilter(new NullFilter<Map, Map>());
        mapperFactory.classMap(FirstClassWithMap.class, SecondClassWithMap.class).byDefault().register();
    }

    @Test
    public void test_map_to_map_with_filter() {
        FirstClassWithMap firstMap = new FirstClassWithMap();
        firstMap.setMap(new HashMap<>());
        firstMap.getMap().put("A", "1");

        SecondClassWithMap transformed = mapperFactory.getMapperFacade().map(firstMap, SecondClassWithMap.class);

        assertNotNull(transformed);
        assertNotNull(transformed.getMap());
        assertEquals("1", transformed.getMap().get("A"));
    }

    public static class FirstClassWithMap {

        private Map<String, String> map;

        public Map<String, String> getMap() {
            return map;
        }

        public void setMap(Map<String, String> map) {
            this.map = map;
        }
    }

    public static class SecondClassWithMap {

        private Map<String, String> map;

        public Map<String, String> getMap() {
            return map;
        }

        public void setMap(Map<String, String> map) {
            this.map = map;
        }
    }
}
