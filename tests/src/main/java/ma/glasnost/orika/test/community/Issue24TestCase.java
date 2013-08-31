package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Issue 24:    
 *      lookupConcreteDestinationType should return the most specific type, 
 *      not the first that is assignable
 * 
 * @author mattdeboer
 *
 */
public class Issue24TestCase {
    
    @Test
    public void testLookupDestinationGetsMostSpecific() {
        MapperFactory factory = MappingUtil.getMapperFactory();

        factory.registerClassMap(ClassMapBuilder.map(A.class, BSub.class).byDefault().toClassMap());
        factory.registerClassMap(ClassMapBuilder.map(A.class, B.class).byDefault().toClassMap());
        factory.registerClassMap(ClassMapBuilder.map(A.class, BSuper.class).byDefault().toClassMap());
        factory.getMapperFacade();

        Type bsuper = factory.lookupConcreteDestinationType(TypeFactory.valueOf(A.class), TypeFactory.valueOf(BSuper.class), null);
        Assert.assertEquals(BSuper.class, bsuper.getRawType());
        Type b = factory.lookupConcreteDestinationType(TypeFactory.valueOf(A.class), TypeFactory.valueOf(B.class), null);
        Assert.assertEquals(B.class, b.getRawType());
        Type bsub = factory.lookupConcreteDestinationType(TypeFactory.valueOf(A.class), TypeFactory.valueOf(BSub.class), null);
        Assert.assertEquals(BSub.class, bsub.getRawType());
    }

    public static class A {

    }

    public static class BSuper extends B {

    }

    public static class B extends BSub {

    }

    public static class BSub {

    }
}
