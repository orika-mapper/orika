package ma.glasnost.orika.test.community;

import java.util.List;

import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

/**
 * NPE on mapNulls in 1.4.3.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/114">https://code.google.com/archive/p/orika/</a>
 */
public class Issue114TestCase {
    
    
    public static class Class1 {
        private List<Long> longs;

        public List<Long> getLongs() {
            return longs;
        }

        public void setLongs(List<Long> longs) {
            this.longs = longs;
        }
    }

    public static class Class1Binding {
        private Class2Binding class2;

        public Class2Binding getClass2() {
            return class2;
        }

        public void setClass2(Class2Binding class2) {
            this.class2 = class2;
        }

        @Override
        public String toString() {
            return "Class1Binding{" +
                    "class2=" + class2 +
                    '}';
        }
    }

    public static class Class2Binding {
        private List<Long> longs;

        public List<Long> getLongs() {
            return longs;
        }

        public void setLongs(List<Long> longs) {
            this.longs = longs;
        }

        @Override
        public String toString() {
            return "Class2Binding{" +
                    "longs=" + longs +
                    '}';
        }
    }
    
    @Test
    public void test() {
        
        DefaultMapperFactory mapperFactory = 
                new DefaultMapperFactory.Builder()
                .build();
        
        mapperFactory.classMap(Class1.class, Class1Binding.class)
                .field("longs","class2.longs")
                .byDefault()
                .register();

        Class1 class1 = new Class1();
        Class1Binding class1Binding = mapperFactory.getMapperFacade(Class1.class, Class1Binding.class).map(class1);
        
        System.out.println(class1Binding);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}
