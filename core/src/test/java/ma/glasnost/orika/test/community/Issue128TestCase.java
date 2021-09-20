package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapping to Map&lt;String, List&lt;String&gt;&gt; only maps keys. Map&lt;String, ArrayList&lt;String&gt;&gt; works..
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/128">https://code.google.com/archive/p/orika/</a>
 */
public class Issue128TestCase {
    
    public static class A {
        public Map<String, List<String>> x;
    }
    
    public static class B1 {
        public Map<String, List<String>> x;
    }
    
    public static class B2 {
        public Map<String, ArrayList<String>> x;
    }
    
    private A createA() {
        A a = new A();
        a.x = new HashMap<>();
        a.x.put("key1", new ArrayList<>());
        a.x.get("key1").add("value1a");
        a.x.get("key1").add("value1b");
        a.x.get("key1").add("value1c");
        a.x.put("key2", new ArrayList<>());
        a.x.get("key2").add("value2a");
        a.x.get("key2").add("value2b");
        a.x.get("key2").add("value2c");
        return a;
    }
    
    @Test
    public void testGenericList() {
        
        MapperFactory factory = MappingUtil.getMapperFactory(true);
        MapperFacade mapper = factory.getMapperFacade();
        
        A a = createA();
        
        B1 b1 = mapper.map(a, B1.class);
        Assert.assertEquals(a.x, b1.x);
    }
    
    @Test
    public void testConcreteList() {
        
        MapperFactory factory = MappingUtil.getMapperFactory(true);
        MapperFacade mapper = factory.getMapperFacade();
        
        A a = createA();
        
        B2 b2 = mapper.map(a, B2.class);
        Assert.assertEquals(a.x, b2.x);
    }
    
}
