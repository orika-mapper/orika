package ma.glasnost.orika.test.filters;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.NullFilter;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class NestedPropertyFilterTest {

    public static class A_Source {

        Integer value;

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }

    public static class A1_Destination {
        A2_Destination a2;

        public A2_Destination getA2() {
            return a2;
        }

        public void setA2(A2_Destination a2) {
            this.a2 = a2;
        }
    }

    public static class A2_Destination {
        A3_Destination a3;

        public A3_Destination getA3() {
            return a3;
        }

        public void setA3(A3_Destination a3) {
            this.a3 = a3;
        }
    }

    public static class A3_Destination {

        A4_Destination a4;

        public A4_Destination getA4() {
            return a4;
        }

        public void setA4(A4_Destination a4) {
            this.a4 = a4;
        }
    }

    public static class A4_Destination {

        Integer value;

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }

    @Test
    public void test() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.registerClassMap(factory.classMap(A_Source.class, A1_Destination.class)
            .field("value", "a2.a3.a4.value")
        );
        factory.registerFilter(new NullFilter<Integer, Integer>());

        MapperFacade mapper = factory.getMapperFacade();

        A_Source a = new A_Source();
        a.setValue(null);

        try {
            A1_Destination a1Destination = mapper.map(a, A1_Destination.class);

            Assert.assertEquals(null, a1Destination.getA2());
        } catch (MappingException e) {
            Assert.fail();
        }

    }
}
