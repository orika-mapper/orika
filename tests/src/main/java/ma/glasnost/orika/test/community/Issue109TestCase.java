package ma.glasnost.orika.test.community;

import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class Issue109TestCase {
 
    public static class Element {
        public int id;
        public String name;
    }
    
    public static class ElementDto {
        public int id;
        public String name;
    }
    
    public static class Base {
        public List<Element> elements;
        public String name;
    }
    
    public static class BaseDto {
        public ElementDto element;
        public String name;
    }
    
    @Test
    public void testOrderingOfClassMaps() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.classMap(Base.class, BaseDto.class)
            .fieldAToB("elements[0]", "element")
            .byDefault().register();

        
        MapperFacade mapper = factory.getMapperFacade();
        
        Base source = new Base();
        source.name = "source1";
        source.elements = new ArrayList<Element>();
        Element el = new Element();
        el.id = 1;
        el.name = "element1";
        source.elements.add(el);
        
        BaseDto dest = mapper.map(source, BaseDto.class);
        
        Assert.assertEquals(source.elements.get(0).id, dest.element.id);
    }
}
