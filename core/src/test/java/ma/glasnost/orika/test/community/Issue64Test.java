package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Orika does not create intermediate property when mapping lists.
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/issues/64">https://github.com/orika-mapper/orika/issues</a>
 */
public class Issue64Test {

    private final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
    private final Source source = new Source();

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES,"true");
        System.setProperty(OrikaSystemProperties.WRITE_CLASS_FILES,"true");
    }

    @Test
    public void createIntermediateObjectForSingleProperty() {
        mapperFactory.registerClassMap(mapperFactory.classMap(Source.class, Parent.class)
                .fieldAToB("single", "child.nestedSingle")
        );
        source.setSingle("SINGLE");

        Parent target = mapperFactory.getMapperFacade(Source.class, Parent.class).map(source);

        assertEquals("SINGLE", target.getChild().getNestedSingle());
    }

    @Test
    public void createIntermediateObjectForListProperty() {
        mapperFactory.registerClassMap(mapperFactory.classMap(Source.class, Parent.class)
                .fieldAToB("list{}", "child.nestedList{}")
        );

        source.setList(asList("A", "B"));

        Parent target = mapperFactory.getMapperFacade(Source.class, Parent.class).map(source);

        assertEquals(asList("A", "B"), target.getChild().getNestedList());
    }

    @Test
    public void createIntermediateObjectForBothPropertiesWithNonNullSingle() {
        mapperFactory.registerClassMap(mapperFactory.classMap(Source.class, Parent.class)
                .fieldAToB("single", "child.nestedSingle")
                .fieldAToB("list{}", "child.nestedList{}")
        );
        source.setSingle("SINGLE");
        source.setList(asList("A", "B"));

        Parent target = mapperFactory.getMapperFacade(Source.class, Parent.class).map(source);

        assertEquals("SINGLE", target.getChild().getNestedSingle());
        assertEquals(asList("A", "B"), target.getChild().getNestedList());
    }

    @Test
    public void createIntermediateObjectForBothPropertiesWithNullSingle() {
        mapperFactory.registerClassMap(mapperFactory.classMap(Source.class, Parent.class)
                .fieldAToB("list{}", "child.nestedList{}")
                .fieldAToB("single", "child.nestedSingle")
        );
        source.setSingle(null);
        source.setList(asList("A", "B"));

        Parent target = mapperFactory.getMapperFacade(Source.class, Parent.class).map(source);

        assertNull(target.getChild().getNestedSingle());
        assertEquals(asList("A", "B"), target.getChild().getNestedList());
    }

    public static class Source {
        private String single;
        private List<String> list;

        public String getSingle() {
            return single;
        }

        public void setSingle(String single) {
            this.single = single;
        }

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }
    }

    public static class Parent {
        private Child child;

        public Child getChild() {
            return child;
        }

        public void setChild(Child child) {
            this.child = child;
        }
    }

    public static class Child {
        private String nestedSingle;
        private List<String> nestedList = new ArrayList<>();

        public String getNestedSingle() {
            return nestedSingle;
        }

        public void setNestedSingle(String nestedSingle) {
            this.nestedSingle = nestedSingle;
        }

        public List<String> getNestedList() {
            return nestedList;
        }

        public void setNestedList(List<String> nestedList) {
            this.nestedList = nestedList;
        }
    }

}
