package ma.glasnost.orika.test;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;

import org.junit.Ignore;
import org.junit.Test;

public class TestCase {
    
    public static class S {
        public Name name;
        public String description;
    }
    
    public static class D {
        public Name name;
        public String description;
    }
    
    public static class Name {
        public String first;
        public String last;
    }
    
    
    @Test
    @Ignore
    public void test() {
        
        MapperFacade mapper = MappingUtil.getMapperFactory().getMapperFacade();
        
        S source = new S();
        D dest = new D();
        Name n = new Name();
        n.first = "John";
        n.last = "Doe";
        dest.name = n;
        dest.description = "Typical";
        
        mapper.map(source, dest);
        
        Assert.assertNotNull(dest.name);
        Assert.assertNotNull(dest.name.first);
        Assert.assertNotNull(dest.description);
        
    }
    
}
