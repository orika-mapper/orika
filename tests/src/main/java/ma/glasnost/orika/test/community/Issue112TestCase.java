package ma.glasnost.orika.test.community;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author: ikrokhmalyov@griddynamics.com
 * @since: 7/1/13
 */


public class Issue112TestCase {
    
    @Test
    public void test() {
        
        DefaultMapperFactory mapperFactory=  new DefaultMapperFactory.Builder().compilerStrategy(new EclipseJdtCompilerStrategy()).build();
        mapperFactory.classMap(Class$1.class, Class$1Binding.class)
                .field("list{name}","list{key}")
                .field("list{strings}", "list{value}")
                .byDefault()
                .register();
        
        BoundMapperFacade<Class$1, Class$1Binding> mapper = mapperFactory.getMapperFacade(Class$1.class, Class$1Binding.class);
        Class$1 inst = new Class$1(Arrays.asList(new Class_2("abc", Arrays.asList("qwe","wer")), new Class_2("dfg", Arrays.asList("sdf","cvb"))));
        
        Class$1Binding result = mapper.map(inst);
        
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getList());

    }

    // Class$1, originally
    public static class Class$1 {

        public Class$1() {
        }

        public Class$1(List<Class_2> list) {
            this.list = list;
        }

        private List<Class_2> list;

        public List<Class_2> getList() {
            return list;
        }

        public void setList(List<Class_2> list) {
            this.list = list;
        }

        @Override
        public String toString() {
            return "Class$1{" +
                    "list=" + list +
                    '}';
        }
    }

    // Class$2, originally
    public static class Class_2 {

        public Class_2() {
        }

        public Class_2(String name, List<String> strings) {
            this.name = name;
            this.strings = strings;
        }

        private String name;
        private List<String> strings;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getStrings() {
            return strings;
        }

        public void setStrings(List<String> strings) {
            this.strings = strings;
        }

        @Override
        public String toString() {
            return "Class$2{" +
                    "name='" + name + '\'' +
                    ", strings=" + strings +
                    '}';
        }
    }

    // Class$1Binding, originally
    public static class Class$1Binding {
        private Map<String, List> list;

        public Map<String, List> getList() {
            return list;
        }

        public void setList(Map<String, List> list) {
            this.list = list;
        }

        @Override
        public String toString() {
            return "Class$1Binding{" +
                    "list=" + list +
                    '}';
        }
    }
}
