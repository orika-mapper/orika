package ma.glasnost.orika.test.community;



import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

public class IssueArrayToListTestCase {

    static {
        //System.setProperty(OrikaSystemProperties.COMPILER_STRATEGY, EclipseJdtCompilerStrategy.class.getName());
    }

    public static class A {
        String[] strings;

        int[] ints;

        Integer[] integers;

        public String[] getStrings() {
            return strings;
        }

        public void setStrings(String[] strings) {
            this.strings = strings;
        }

        public int[] getInts() {
            return ints;
        }

        public void setInts(int[] ints) {
            this.ints = ints;
        }

        public Integer[] getIntegers() {
            return integers;
        }

        public void setIntegers(Integer[] integers) {
            this.integers = integers;
        }
    }

    public static class B {
        List<String> strings;

        List<Integer> integers;

        public List<String> getStrings() {
            return strings;
        }

        public void setStrings(List<String> strings) {
            this.strings = strings;
        }

        public List<Integer> getIntegers() {
            return integers;
        }

        public void setIntegers(List<Integer> integers) {
            this.integers = integers;
        }
    }

    @Test
    public void testStringArrayToListOfString() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

        MapperFacade mapperFacade = mapperFactory.getMapperFacade();

        A a = new A();
        a.setStrings(new String[] { "4" });

        B b = mapperFacade.map(a, B.class);
        assertEquals(asList("4"), b.getStrings());
    }

    @Test
    public void testListOfStringToStringArray() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

        MapperFacade mapperFacade = mapperFactory.getMapperFacade();

        B b = new B();
        b.setStrings(asList("5"));

        A a = mapperFacade.map(b, A.class);
        assertArrayEquals(new String[] { "5" }, a.getStrings());
    }

    @Test
    public void testIntArrayToListOfInteger() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

        mapperFactory.classMap(A.class, B.class).field("ints", "integers").byDefault().register();

        MapperFacade mapperFacade = mapperFactory.getMapperFacade();

        A a = new A();
        a.setInts(new int[] { 4 });

        B b = mapperFacade.map(a, B.class);
        assertNotNull(b.getIntegers());
        assertEquals(1, b.getIntegers().size());
        assertEquals(Integer.class, b.getIntegers().get(0).getClass());
        assertEquals(Integer.valueOf(4), b.getIntegers().get(0));
    }

    @Test
    public void testListOfIntegerToIntArray() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

        mapperFactory.classMap(A.class, B.class).field("ints", "integers").byDefault().register();

        MapperFacade mapperFacade = mapperFactory.getMapperFacade();

        B b = new B();
        b.setIntegers(asList(Integer.valueOf(6)));

        A a = mapperFacade.map(b, A.class);
        assertArrayEquals(new int[] { 6 }, a.getInts());
    }

    @Test
    public void testIntegerArrayToListOfInteger() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

        MapperFacade mapperFacade = mapperFactory.getMapperFacade();

        A a = new A();
        a.setIntegers(new Integer[] { 4 });

        B b = mapperFacade.map(a, B.class);
        assertNotNull(b.getIntegers());
        assertEquals(1, b.getIntegers().size());
        assertEquals(Integer.class, b.getIntegers().get(0).getClass());
        assertEquals(Integer.valueOf(4), b.getIntegers().get(0));
    }

    @Test
    public void testListOfIntegerToIntegerArray() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

        MapperFacade mapperFacade = mapperFactory.getMapperFacade();

        B b = new B();
        b.setIntegers(asList(Integer.valueOf(7)));

        A a = mapperFacade.map(b, A.class);
        assertArrayEquals(new Integer[] { 7 }, a.getIntegers());
    }

}
