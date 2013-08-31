package ma.glasnost.orika.test.generics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.junit.Assert;
import org.junit.Test;

public class GenericCollectionsTestCase {
    
    /* Abstract class for person */
    public abstract static class Person {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public boolean equals(Object that) {
            return EqualsBuilder.reflectionEquals(this, that);
        }
        
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }
    
    /* Concrete class for employee */
    public static class Employee extends Person {
    }
    
    /* Task class for some Layer 1 */
    public static class TaskLayer1<P extends Person> {
        private List<P> workers = new ArrayList<P>();
        
        public List<P> getWorkers() {
            return workers;
        }
        
        public void setWorkers(List<P> workers) {
            this.workers = workers;
        }
    }
    
    /* Task class for some Layer 2 (mapped from TaskLayer1) */
    public static class TaskLayer2<P extends Person> {
        private List<P> workers = new ArrayList<P>();
        
        public List<P> getWorkers() {
            return workers;
        }
        
        public void setWorkers(List<P> workers) {
            this.workers = workers;
        }
    }
    
    /**
     * This test uses the older Class-based mapping method, which is unable
     * to determine the proper type hierarchy from the raw types
     */
    @Test
    public void testParameterizedCollection_rawTypes() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        ClassMapBuilder<?, ?> builder = factory.classMap(TaskLayer1.class, TaskLayer2.class).byDefault();
        ClassMap<?, ?> classMap = builder.toClassMap();
        factory.registerClassMap(classMap);
        
        /*
         * Let Orika know that it's okay to copy Employee or Person by reference...
         */
        factory.getConverterFactory().registerConverter(new PassThroughConverter(Employee.class, Person.class));
        
        
        Employee e = new Employee();
        e.setName("Name");
        TaskLayer1<Employee> t1 = new TaskLayer1<Employee>();
        t1.setWorkers(Arrays.asList(e));
        
       
    	TaskLayer2<?> t2 = factory.getMapperFacade().map(t1, TaskLayer2.class);
    	Assert.assertNotNull(t2);
        Assert.assertTrue(t1.getWorkers().containsAll(t2.getWorkers()));
        Assert.assertTrue(t2.getWorkers().containsAll(t1.getWorkers())); 
    }
    
    /**
     * This test attempts the same mapping using the newer type-based methods
     * which allow passing in the exact runtime types.
     */
    @Test
    public void testParameterizedCollection_genericTypes() {
       
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        Employee e = new Employee();
        e.setName("Name");
        TaskLayer1<Employee> t1 = new TaskLayer1<Employee>();
        t1.setWorkers(Arrays.asList(e));
        
        Type<TaskLayer1<Employee>> sourceType = new TypeBuilder<TaskLayer1<Employee>>(){}.build();
        Type<TaskLayer2<Employee>> targetType = new TypeBuilder<TaskLayer2<Employee>>(){}.build();
        
        factory.registerClassMap(
                factory.classMap(sourceType, targetType).byDefault());
        
        TaskLayer2<Employee> t2 = factory.getMapperFacade().map(t1, sourceType, targetType);
        Assert.assertNotNull(t2);
        Assert.assertTrue(t1.getWorkers().containsAll(t2.getWorkers()));
        Assert.assertTrue(t2.getWorkers().containsAll(t1.getWorkers())); 
    }
}
