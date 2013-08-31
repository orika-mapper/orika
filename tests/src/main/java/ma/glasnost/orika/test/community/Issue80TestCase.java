package ma.glasnost.orika.test.community;

import java.util.HashMap;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class Issue80TestCase {
    
    public static class JsonObject extends HashMap<String,Object> {
        
    }
    
    public static class JavaBean {
        public String name;
        public Integer id;
    }
    
    @Test
    public void test() {
        
        // TODO: determine whether the solution is that
        // 1. we should be able to getNestedType(0) on JsonObject and get String
        // or
        // 2. we should be able to resolve the values of the get and put 
        // methods...
        
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.classMap(JsonObject.class, JavaBean.class)
        .field("name", "name")
        .field("id", "id")
        .register();
        
        JsonObject source = new JsonObject();
        source.put("name", "Joe Smit");
        source.put("id", 22);
        
        MapperFacade mapper = factory.getMapperFacade();
        
        JavaBean dest = mapper.map(source, JavaBean.class);
        
        Assert.assertEquals(source.get("name"), dest.name);
        Assert.assertEquals(source.get("id"), dest.id);
        
    }
}
