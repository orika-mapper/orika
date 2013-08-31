package ma.glasnost.orika.test.community;

import java.util.HashMap;
import java.util.Map;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

/**
 * @author conleym
 * 
 */
public final class Issue85TestCase {
    /**
     * @param args
     */
    @Test
    public void test() {
        MapperFactory f = MappingUtil.getMapperFactory();
        f.classMap(MapContainer.class, Map.class).field("map{value}", "{key}").register();
        
        final MapperFacade facade = f.getMapperFacade();
        final Map<Object, Object> dest = new HashMap<Object, Object>();
        final Map<Object, Object> src = new HashMap<Object, Object>();
        src.put("xyz", "123456");
        facade.map(new MapContainer<Object, Object>(src), dest);
        System.out.println(dest);
    }
    
    
    public final static class MapContainer<X, Y> {
        public final Map<X, Y> map;
        
        public MapContainer(final Map<X, Y> map) {
            this.map = map;
        }
    }
    
}
