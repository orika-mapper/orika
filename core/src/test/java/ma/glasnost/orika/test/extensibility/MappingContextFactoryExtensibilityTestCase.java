package ma.glasnost.orika.test.extensibility;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingContextFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.community.issue137.CustomFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class MappingContextFactoryExtensibilityTestCase {
    
    @Test
    public void testMappingContext_withCustomMapper() {
        MyMappingContext.Factory contextFactory = new MyMappingContext.Factory();
        final MapperFactory factory = new DefaultMapperFactory.Builder()
                .mappingContextFactory(contextFactory)
                .build();
        
        factory.classMap(Person.class, Person.class).customize(new CustomMapper<Person, Person>() {
            @Override
            public void mapAtoB(Person a, Person b, MappingContext context) {
                b.setName(a.getName());
                b.setDependsOn(factory.getMapperFacade().map(a.getDependsOn(), Person.class, context));
            }
            
            @Override
            public void mapBtoA(Person b, Person a, MappingContext context) {
                super.mapAtoB(b, a, context);
            }
            
        }).register();
        factory.classMap(Man.class, Man.class).use(Person.class, Person.class).byDefault().register();
        factory.classMap(Woman.class, Woman.class).use(Person.class, Person.class).byDefault().register();
        
        Man person = createComplexPerson();
        
        Man mapped = factory.getMapperFacade().map(person, Man.class);
        
        assertComplexPerson(mapped);
    }
    
    @Test
    public void testMappingContext_withDefaultMapper() {
        MyMappingContext.Factory contextFactory = new MyMappingContext.Factory();
        final MapperFactory factory = new DefaultMapperFactory.Builder()
                .mappingContextFactory(contextFactory)
                .build();
        
        factory.classMap(Man.class, Man.class).byDefault().register();
        factory.classMap(Woman.class, Woman.class).byDefault().register();
        
        Person person = createComplexPerson();
        
        Person mapped = factory.getMapperFacade().map(person, Person.class);
        
        assertComplexPerson(mapped);
    }
    
    @Test
    public void testMappingContext_withFieldMapper() {
        MyMappingContext.Factory contextFactory = new MyMappingContext.Factory();
        final MapperFactory factory = new DefaultMapperFactory.Builder()
                .mappingContextFactory(contextFactory)
                .build();
        
        factory.classMap(Person.class, Person.class)
                .field("name", "name")
                .field("dependsOn", "dependsOn")
                .register();
        factory.classMap(Man.class, Man.class).use(Person.class, Person.class).field("children", "children").register();
        factory.classMap(Woman.class, Woman.class).use(Person.class, Person.class).field("children", "children").register();
        
        Man person = createComplexPerson();
        
        Man mapped = factory.getMapperFacade().map(person, Man.class);
        
        assertComplexPerson(mapped);
    }
    
    @Test
    public void testMappingContext_withBoundMapperFacade() {
        MyMappingContext.Factory contextFactory = new MyMappingContext.Factory();
        final MapperFactory factory = new DefaultMapperFactory.Builder()
                .mappingContextFactory(contextFactory)
                .build();
        
        
        factory.getConverterFactory().registerConverter(new CustomConverter<Person, Person>() {
            
            public Person convert(Person source, Type<? extends Person> destType, MappingContext mappingContext) {
                final BoundMapperFacade<Person, Person> boundMapperFacadePerson = factory.getMapperFacade(Person.class, Person.class);
                final BoundMapperFacade<Child, Child> boundMapperFacadeChild = factory.getMapperFacade(Child.class, Child.class);
                Person destination = boundMapperFacadePerson.newObject(source, mappingContext);
                destination.setName(source.getName());
                destination.setDependsOn(boundMapperFacadePerson.map(source.getDependsOn(), mappingContext));
                if (source instanceof Parent && destination instanceof Parent) {
                    Set<Child> srcChildren = ((Parent) source).getChildren();
                    Set<Child> destChildren = ((Parent) destination).getChildren();
                    for (Child srcChild : srcChildren) {
                        destChildren.add(boundMapperFacadeChild.map(srcChild, mappingContext));
                    }
                    
                }
                return destination;
            }
        });
        factory.registerObjectFactory(new CustomFactory<Person>() {
            
            @Override
            public Person create(Object o, MappingContext mappingContext) {
                if (o instanceof Woman) {
                    return new Woman();
                } else if (o instanceof Man) {
                    return new Man();
                } else if (o instanceof Child) {
                    return new Child();
                } else {
                    return null;
                }
            }
        }, TypeFactory.valueOf(Person.class));
        factory.classMap(Man.class, Man.class).byDefault().register();
        factory.classMap(Woman.class, Woman.class).byDefault().register();
        factory.classMap(Parent.class, Parent.class).byDefault().register();
        
        // factory.classMap(Person.class, Person.class).byDefault().register();
        final BoundMapperFacade<Person, Person> boundMapperFacadePerson = factory.getMapperFacade(Person.class, Person.class);
        
        Person person = createComplexPerson();
        
        Person mapped = boundMapperFacadePerson.map(person);
        
        assertComplexPerson(mapped);
    }
    
    @Test
    public void testMappingContext_withCustomConverter() {
        MyMappingContext.Factory contextFactory = new MyMappingContext.Factory();
        final MapperFactory factory = new DefaultMapperFactory.Builder()
                .mappingContextFactory(contextFactory)
                .build();
        
        factory.getConverterFactory().registerConverter(new CustomConverter<Person, Person>() {
            
            public Person convert(Person source, Type<? extends Person> destType, MappingContext mappingContext) {
                Person destination = factory.getMapperFacade().newObject(source, destinationType, mappingContext);
                destination.setName(source.getName());
                destination.setDependsOn(factory.getMapperFacade().map(source.getDependsOn(), Person.class, mappingContext));
                if (source instanceof Parent && destination instanceof Parent) {
                    Parent srcParent = (Parent) source;
                    Parent destParent = (Parent) destination;
                    Type<Child> childType = TypeFactory.valueOf(Child.class);
                    factory.getMapperFacade().mapAsCollection(srcParent.getChildren(), destParent.getChildren(),
                            childType, childType, mappingContext);
                }
                return destination;
            }
        });
        factory.registerObjectFactory(new CustomFactory<Person>() {
            
            @Override
            public Person create(Object o, MappingContext mappingContext) {
                if (o instanceof Woman) {
                    return new Woman();
                } else if (o instanceof Man) {
                    return new Man();
                } else if (o instanceof Child) {
                    return new Child();
                } else {
                    return null;
                }
            }
        }, TypeFactory.valueOf(Person.class));
        factory.classMap(Man.class, Man.class).byDefault().register();
        factory.classMap(Woman.class, Woman.class).byDefault().register();
        
        factory.classMap(Person.class, Person.class).byDefault().register();
        
        Person person = createComplexPerson();
        
        Person mapped = factory.getMapperFacade().map(person, Person.class);
        
        assertComplexPerson(mapped);
    }

    private Man createComplexPerson() {
        Man maxi = new Man("Maxi");
        Woman susi = new Woman("Susi");
        Man hansi = new Man("Hansi");
        Child hensl = new Child("Hensl");
        Child kretl = new Child("Kretl");
        maxi.setDependsOn(susi);
        susi.setDependsOn(hansi);
        susi.getChildren().add(hensl);
        susi.getChildren().add(kretl);
        return maxi;
    }
    
    private void assertComplexPerson(Person mapped) {
        Assert.assertEquals("Maxi", mapped.getName());
        Assert.assertEquals("Susi", mapped.getDependsOn().getName());
        Assert.assertEquals("Hansi", mapped.getDependsOn().getDependsOn().getName());
        Assert.assertTrue("Maxi must be a Man but was: " + mapped.getClass().getSimpleName(), mapped instanceof Man);
        Assert.assertTrue("Susi must be a Woman but was: " + mapped.getDependsOn().getClass().getSimpleName(),
                mapped.getDependsOn() instanceof Woman);
        Assert.assertTrue("Hansi must be a Man but was: " + mapped.getDependsOn().getDependsOn().getClass().getSimpleName(),
                mapped.getDependsOn().getDependsOn() instanceof Man);
        
        Parent susi = (Parent) mapped.getDependsOn();
        assertThat(susi.getChildren(), containsInAnyOrder(new Child("Hensl"), new Child("Kretl")));
    }
    
    public static class MyMappingContext extends MappingContext
    {
        public MyMappingContext(Map<Object, Object> globalProperties) {
            super(globalProperties);
        }
        
        public static class Factory implements MappingContextFactory {
            private final ConcurrentHashMap<Object, Object> globalProperties = new ConcurrentHashMap<>();
            private final ThreadLocal<MappingContext> threadLocalMappingContext = new ThreadLocal<>();
            
            @Override
            public MappingContext getContext() {
                if (threadLocalMappingContext.get() != null) {
                    throw new IllegalStateException("During this Thread only one MappingContext is allowed to be created.");
                }
                MappingContext myMappingContext = new MyMappingContext(globalProperties);
                threadLocalMappingContext.set(myMappingContext);
                return myMappingContext;
            }
            
            @Override
            public void release(MappingContext context) {
                context.reset();
                threadLocalMappingContext.remove();
            }
            
            @Override
            public Map<Object, Object> getGlobalProperties() {
                return globalProperties;
            }
            
        }
        
    }
    
    public static class Person
    {
        private String name;
        private Person dependsOn;
        
        public Person() {
            super();
        }
        
        public Person(String name) {
            super();
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Person getDependsOn() {
            return dependsOn;
        }
        
        public void setDependsOn(Person dependsOn) {
            this.dependsOn = dependsOn;
        }
        
        @Override
        public int hashCode() {
        	return Objects.hash(getName());            
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            Person rhs = (Person) obj;
            
            return Objects.equals(getName(), rhs.getName());            
        }
        
        @Override
        public String toString() {
            return String.format("%s(%s)", getClass().getSimpleName(), getName());
        }
        
    }
    
    public static final class Man extends Person implements Parent {
        private Set<Child> children = new HashSet<>();
        
        public Man() {
            super();
        }
        
        public Man(String name) {
            super(name);
        }
        
        public Set<Child> getChildren() {
            return children;
        }
        
        public void setChildren(Set<Child> children) {
            this.children = children;
        }

    }
    
    public static final class Woman extends Person implements Parent {
        private Set<Child> children = new HashSet<>();
        
        public Woman() {
            super();
        }
        
        public Woman(String name) {
            super(name);
        }
        
        @Override
        public Set<Child> getChildren() {
            return children;
        }
        
        @Override
        public void setChildren(Set<Child> children) {
            this.children = children;
        }
        
    }
    
    public interface Parent {
        
        Set<Child> getChildren();
        
        void setChildren(Set<Child> children);
        
    }
    
    public static final class Child extends Person {
        
        public Child() {
            super();
        }
        
        public Child(String name) {
            super(name);
        }
        
    }
}
