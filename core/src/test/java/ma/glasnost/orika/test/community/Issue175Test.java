package ma.glasnost.orika.test.community;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.Test;

import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.impl.DefaultMapperFactory;

public class Issue175Test {

    @Test
    public void maps_one_value_to_all_elements_in_collection_and_back() {
        System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES,"true");
        DefaultMapperFactory mapper = new DefaultMapperFactory.Builder().build();

        mapper.classMap(Source.class, Destination.class)
                .field("nested", "nested")
                .field("value", "nested{value}")
                .byDefault()
                .register();

        Source source = new Source();
        source.setValue("some data");
        source.setNested(Arrays.asList(
                aNestedSource("one"),
                aNestedSource("two"),
                aNestedSource("three")
        ));

        Destination destination = mapper.getMapperFacade().map(source, Destination.class);

        assertEquals("some data", destination.getNested().get(0).getValue());
        assertEquals("one", destination.getNested().get(0).getId());
        assertEquals("some data", destination.getNested().get(1).getValue());
        assertEquals("two", destination.getNested().get(1).getId());
        assertEquals("some data", destination.getNested().get(2).getValue());
        assertEquals("three", destination.getNested().get(2).getId());

        Source newSource = mapper.getMapperFacade().map(destination, Source.class);

        assertEquals(source, newSource);
    }

    private NestedSource aNestedSource(String id) {
        NestedSource nested = new NestedSource();
        nested.setId(id);
        return nested;
    }

    public static class Source {

        private String value;
        private List<NestedSource> nested = new ArrayList<>();

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setNested(List<NestedSource> nested) {
            this.nested = nested;
        }

        public List<NestedSource> getNested() {
            return nested;
        }

        @Override
        public boolean equals(Object obj) {
        	return Objects.equals(value, ((Source) obj).value) &&
        			Objects.equals(nested, ((Source) obj).nested);
        }
    }

    public static class Destination {

        private List<NestedDestination> nested;

        public List<NestedDestination> getNested() {
            return nested;
        }

        public void setNested(List<NestedDestination> nested) {
            this.nested = nested;
        }
    }

    public static class NestedSource {

        private String id;

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
        	return Objects.equals(id, ((NestedSource) obj).id);
        }
    }

    public static class NestedDestination {
        private String id;
        private String value;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
