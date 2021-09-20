package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * NPE on VariableRef.isPrimitive() with map of map.
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/issues/67">https://github.com/orika-mapper/orika/issues</a>
 */
public class Issue67Test {

    private MapperFactory mapperFactory;
    private static final Date A_DATE = new Date();

    @Before
    public void setUp() throws Exception {
        mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory.classMap(DateToIntegerToStringMap.class, DateToIntegerToStringMap.class)
                .byDefault()
                .register();
    }

    @Test
    public void clone_a_map_of_map() {
        DateToIntegerToStringMap original = new DateToIntegerToStringMap();
        Map<Integer, String> integerStringMap = new HashMap<>();
        integerStringMap.put(5, "five");
        Map<Date, Map<Integer, String>> dateMapMap = new HashMap<>();
        dateMapMap.put(A_DATE, integerStringMap);
        original.setDateIntegerStringMap(dateMapMap);

        DateToIntegerToStringMap copy = mapperFactory.getMapperFacade().map(original, DateToIntegerToStringMap.class);

        Map<Integer, String> nestedMap = copy.getDateIntegerStringMap().get(A_DATE);
        assertNotNull(nestedMap);
        assertEquals("five", nestedMap.get(5));
    }

    public static class DateToIntegerToStringMap {

        public Map<Date, Map<Integer, String>> getDateIntegerStringMap() {
            return dateIntegerStringMap;
        }

        public void setDateIntegerStringMap(Map<Date, Map<Integer, String>> dateIntegerStringMap) {
            this.dateIntegerStringMap = dateIntegerStringMap;
        }

        private Map<Date, Map<Integer, String>> dateIntegerStringMap;
    }
}
