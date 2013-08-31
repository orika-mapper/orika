package ma.glasnost.orika.test.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test a class that User: kenton Date: 12/7/12 Time: 10:50 AM
 */
public class ExplicitCollectionTestCase {
    
    @Test
    public void testStringToStringWithSpecifiedGenericType() {
        ExplicitSet set = new ExplicitSet();
        set.add("1");
        set.add("2");
        A source = new A();
        source.setStrings(set);
        
        B destination = MappingUtil.getMapperFactory().getMapperFacade().map(source, B.class);
        
        Assert.assertNotNull(destination.getStrings());
        Assert.assertEquals(set.size(), destination.getStrings().size());
    }
    
    public static class A {
        private ExplicitSet strings;
        
        public ExplicitSet getStrings() {
            return strings;
        }
        
        public void setStrings(ExplicitSet strings) {
            this.strings = strings;
        }
    }
    
    public static class B {
        private Set<String> strings;
        
        public Set<String> getStrings() {
            return strings;
        }
        
        public void setStrings(Set<String> strings) {
            this.strings = strings;
        }
    }
    
    public static class ExplicitSet extends HashSet<String> {
        
        private static final long serialVersionUID = 1L;

        public ExplicitSet(int i) {
            super(i);
        }
        
        public ExplicitSet(int i, float v) {
            super(i, v);
        }
        
        public ExplicitSet(Collection<? extends String> strings) {
            super(strings);
        }
        
        public ExplicitSet() {
        }
    }
}