package ma.glasnost.orika.test.community;

import java.util.List;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

/**
 * NPE on mapping nested field with collection.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/119">https://code.google.com/archive/p/orika/</a>
 */
public class Issue119TestCase {
    
    public static class Source {
        public String id;
        public SourceRef ref;
    }
    
    public static class SourceRef {
        public List<String> identities;
    }
    
    public static class Dest {
        public String id;
        public List<DestReference> references;
    }
    
    public static class DestReference {
        public String identity;
    }
    
    @Test
    public void test() {
        
        MapperFactory factory = MappingUtil.getMapperFactory(true);
        
        factory.classMap(Source.class, Dest.class)
                .mapNulls(false)
                .fieldAToB("id", "id")
                .fieldAToB("ref.identities{}", "references{identity}")
                .register();
        
        Source src = new Source();
        src.id = "myId";
        
        Dest dest = factory.getMapperFacade().map(src, Dest.class);
        
    }
}
