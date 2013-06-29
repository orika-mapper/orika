package ma.glasnost.orika.test.expectation;

import static ma.glasnost.orika.metadata.FieldSet.all;
import static ma.glasnost.orika.metadata.FieldSet.allExcept;
import static ma.glasnost.orika.metadata.FieldSet.allNonNested;
import static ma.glasnost.orika.metadata.FieldSet.allNonNestedExcept;
import static ma.glasnost.orika.metadata.FieldSet.noneOf;
import static ma.glasnost.orika.metadata.FieldSet.only;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.FailedExpectationException;
import ma.glasnost.orika.metadata.FieldSet;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class ExpectationTestCase {
    
    public static class Source {
        public final String lastName;
        public final String firstName;
        public final Integer age;
        public final SourceName name;
        
        public Source(String firstName, String lastName, Integer age, SourceName name) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.name = name;
        }
    }
    
    public static class Source2 {
        public String lastName;
        public String firstName;
        public Integer age;
        public SourceName name;
    }
    
    public static class SourceName {
        public String first;
        public String last;
    }
    
    public static class Destination {
        public String lastName;
        public String firstName;
        public Integer age;
        public DestinationName name;
    }
    
    public static class DestinationName {
        public String first;
        public String last;
    }
    
    
    @Test
    public void only_met() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.classMap(Source.class, Destination.class)
            .fieldAToB("firstName", "name.first")
            .fieldMap("age").add()
            .expectMappedOnB(only("name.first", "age"))
            .register();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Source s = new Source("Joe", "Smith", 25, null);
        Destination d = mapper.map(s, Destination.class);
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertEquals(s.firstName, d.name.first);
        Assert.assertEquals(s.age, d.age);
    }
    
    @Test(expected=FailedExpectationException.class)
    public void only_failed() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.classMap(Source.class, Destination.class)
            .fieldAToB("firstName", "name.first")
            .fieldAToB("lastName", "name.last")
            .fieldMap("age").add()
            .expectMappedOnB(only("name.first", "age"))
            .register();
        
        factory.getMapperFacade();
    }
    
    @Test
    public void all_met() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.classMap(Source.class, Destination.class)
            .fieldMap("name.first").add()
            .fieldMap("name.last").add()
            .byDefault()
            .expectMappedOnB(all())
            .register();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        SourceName name = new SourceName();
        name.first = "Joey";
        name.last = "Smith";
        Source s = new Source("Joe", "Smith", 25, name);
        Destination d = mapper.map(s, Destination.class);
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertEquals(s.age, d.age);
        Assert.assertEquals(s.firstName, d.firstName);
        Assert.assertEquals(s.lastName, d.lastName);
        Assert.assertEquals(s.name.first, d.name.first);
        Assert.assertEquals(s.name.last, d.name.last);
    }
    
    @Test(expected=FailedExpectationException.class)
    public void all_failed() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.classMap(Source.class, Destination.class)
            .fieldAToB("firstName", "name.first")
            .fieldAToB("lastName", "name.last")
            .fieldMap("age").add()
            .expectMappedOnB(all())
            .register();
        
        factory.getMapperFacade();
    }
    
    @Test
    public void allNonNested_met() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.classMap(Source.class, Destination.class)
            .byDefault()
            .expectMappedOnB(allNonNested())
            .register();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        SourceName name = new SourceName();
        name.first = "Joey";
        name.last = "Smith";
        Source s = new Source("Joe", "Smith", 25, name);
        Destination d = mapper.map(s, Destination.class);
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertEquals(s.age, d.age);
        Assert.assertEquals(s.firstName, d.firstName);
        Assert.assertEquals(s.lastName, d.lastName);
        Assert.assertEquals(s.name.first, d.name.first);
        Assert.assertEquals(s.name.last, d.name.last);
    }
    
    @Test(expected=FailedExpectationException.class)
    public void allNonNested_failed() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.classMap(Source.class, Destination.class)
            .fieldMap("age").add()
            .expectMappedOnB(allNonNested())
            .register();
        
        factory.getMapperFacade();
    }
    
    @Test
    public void allExcept_met() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.classMap(Source.class, Destination.class)
            .exclude("age")
            .fieldAToB("name.first", "name.first")
            .fieldAToB("name.last", "name.last")
            .byDefault()
            .expectMappedOnB(allExcept("age"))
            .register();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        SourceName name = new SourceName();
        name.first = "Joey";
        name.last = "Smith";
        Source s = new Source("Joe", "Smith", 25, name);
        Destination d = mapper.map(s, Destination.class);
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertNull(d.age);
        Assert.assertEquals(s.firstName, d.firstName);
        Assert.assertEquals(s.lastName, d.lastName);
        Assert.assertEquals(s.name.first, d.name.first);
        Assert.assertEquals(s.name.last, d.name.last);
    }
    
    @Test(expected=FailedExpectationException.class)
    public void allExcept_failed() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.classMap(Source.class, Destination.class)
            .byDefault()
            .expectMappedOnB(allExcept("age"))
            .register();
        
        factory.getMapperFacade();
    }
    
    @Test
    public void allNonNestedExcept_met() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.classMap(Source.class, Destination.class)
            .exclude("age")
            .byDefault()
            .expectMappedOnB(allNonNestedExcept("age"))
            .register();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        SourceName name = new SourceName();
        name.first = "Joey";
        name.last = "Smith";
        Source s = new Source("Joe", "Smith", 25, name);
        Destination d = mapper.map(s, Destination.class);
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertNull(d.age);
        Assert.assertEquals(s.firstName, d.firstName);
        Assert.assertEquals(s.lastName, d.lastName);
        Assert.assertEquals(s.name.first, d.name.first);
        Assert.assertEquals(s.name.last, d.name.last);
    }
    
    @Test(expected=FailedExpectationException.class)
    public void allNonNestedExcept_failed() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.classMap(Source.class, Destination.class)
            .byDefault()
            .expectMappedOnB(allNonNestedExcept("age"))
            .register();
        
        factory.getMapperFacade();
    }
    
    @Test
    public void noneOf_met() {
        MapperFactory factory = MappingUtil.getMapperFactory(true);
        
        factory.classMap(Source.class, Destination.class)
            .fieldAToB("lastName", "name.last")
            .exclude("age")
            .byDefault()
            .expectMappedOnB(noneOf("name.first", "age"))
            .register();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Source s = new Source("Joe", "Smith", 25, null);
        Destination d = mapper.map(s, Destination.class);

        
        Assert.assertEquals(s.lastName, d.name.last);
        Assert.assertNull(d.name.first);
        Assert.assertNull(d.age);
    }
    
    @Test(expected=FailedExpectationException.class)
    public void noneOf_failed() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.classMap(Source.class, Destination.class)
            .fieldAToB("firstName", "name.first")
            .fieldAToB("lastName", "name.last")
            .fieldMap("age").add()
            .expectMappedOnB(noneOf("name.first", "age"))
            .register();
        
        factory.getMapperFacade();
    }
}
