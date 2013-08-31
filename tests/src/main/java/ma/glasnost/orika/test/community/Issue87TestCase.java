package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class Issue87TestCase {

    private MapperFactory mapperFactory;

    @Before
    public void createMapperFactoryAndDefineMapping() {
            mapperFactory = new DefaultMapperFactory.Builder()
                            .useAutoMapping(false).build();
            mapperFactory.classMap(FakeBeanA.class, FakeBeanB.class)
                            .field("fieldA", "fieldB.nestedField").register();
    }

    @Test(expected = MappingException.class)
    public void cannotMapAbstractNestedPropertyWhenConcreteTypeIsNotRegistered() {

            // We expect to get a MappingException. Indeed, Orika doesn't know how
            // to create an
            // instance of AbstractNestedField (the type of FakeBeanB.fieldB)
            createAnInstanceOfFakeBeanAAndMapItToFakeBeanB();
    }

    @Test
//    @Ignore
    public void mapAbstractNestedPropertyWhenConcreteTypeIsRegistered() {

            // Register concrete type for AbstractNestedType
            mapperFactory.registerConcreteType(AbstractNestedType.class,
                            NestedType.class);

            // Orika should be able to create an instance of FieldB abstract type
            // (as we have explicitly defined above the concrete type to create and
            // the SimpleConstructorResolverStrategy is normally able to create an
            // instance of this concrete type)
            // Therefore, the mapping should work !
            createAnInstanceOfFakeBeanAAndMapItToFakeBeanB();
    }

    private void createAnInstanceOfFakeBeanAAndMapItToFakeBeanB() {

            // Create an instance of FakeBeanA and assign a value to its fieldA
            FakeBeanA fakeBeanA = new FakeBeanA();
            fakeBeanA.fieldA = 42;

            // Try the mapping from fakeBeanA to FakeBeanB
            FakeBeanB fakeBeanB = mapperFactory.getMapperFacade().map(fakeBeanA,
                            FakeBeanB.class);

            // Assert the fieldA has been correctly mapped to fieldB.nestedField
            Assert.assertEquals(fakeBeanA.fieldA, fakeBeanB.fieldB.nestedField);
    }

    public static class FakeBeanA {
            public int fieldA;
    }

    public static class FakeBeanB {
            public AbstractNestedType fieldB;
    }

    public static class NestedType extends AbstractNestedType {
    }

    public static abstract class AbstractNestedType {
            public int nestedField;
    }

}
