package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * Exclude() does not work if the field is not present in both classes.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/126">https://code.google.com/archive/p/orika/</a>
 */
public class Issue126TestCase {
    
    public static class A {
        public String id;
        public MyField unmappedField;
        public String fieldA;
        public String fieldB;
    }

    public static class MyField {
        public String value = "default";
        
        public MyField(String blah) {
            this.value = blah;
        }
    }
    

    public static class B {
        public String id;
        public String fieldA;
        public String fieldB;
    }
   
    
    @Test
    public void testExclude() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.classMap(A.class, B.class)
            .field("id", "id")
            .exclude("unmappedField")
            .byDefault()
            .register();
        
        A source = new A();
        source.id = "a";
        source.fieldA = "a";
        source.fieldB = "b";
        source.unmappedField = new MyField("myField");
        
        B dest = factory.getMapperFacade().map(source, B.class);
        Assert.assertNotNull(dest);
        
    }
    
    @Test
    public void testByDefault() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.classMap(A.class, B.class)
            //.field("id", "id")
            .byDefault()
            .register();
        
        A source = new A();
        source.id = "a";
        source.fieldA = "a";
        source.fieldB = "b";
        source.unmappedField = new MyField("myField");
        
        B dest = factory.getMapperFacade().map(source, B.class);
        Assert.assertNotNull(dest);
        
    }
    
}
