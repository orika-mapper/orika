package ma.glasnost.orika.test.community;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Date;
import java.util.Map;

import org.junit.Test;

import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;

public class Issue188TestCase {
    
    @Test
    public void testCase() throws Exception {
        Type<?> type = TypeFactory.valueOf(Interface1.class);
        assertThat(type.toString(), is("Interface1<Interface2<Interface2>>"));
    }
    
    @Test
    public void testCase2() throws Exception {
        Type<?> type = new TypeBuilder<Map<Date, Map<Integer, String>>>() {}.build();
        assertThat(type.toString(), is("Map<Date, Map<Integer, String>>"));
    }
    
    @Test
    public void testCase3() throws Exception {
        Type<?> type = TypeFactory.valueOf(Interface3.class);
        assertThat(type.toString(), is("Interface3<Interface4<Interface3, Interface4>, Interface3>"));
    }
    
    @Test
    public void testCase5() throws Exception {
        Type<?> type = new TypeBuilder<Interface5<Interface5<Interface5<Interface6<Interface6<String>>>>>>() {}.build();
        assertThat(type.toString(), is("Interface5<Interface5<Interface5<Interface6<Interface6<String>>>>>"));
    }
    
    @Test
    public void testCase7() throws Exception {
        Type<?> type = TypeFactory.valueOf(Interface7.class);
        assertThat(type.toString(), is("Interface7<Interface5<Interface6<Interface5<Interface6>>>>"));
    }
    
    public interface Interface1<T extends Interface2<T>> {
    }
    
    public interface Interface2<T extends Interface2<T>> {
    }
    
    public interface Interface3<T extends Interface4<U, T>, U extends Interface3<T, U>> {
    }
    
    public interface Interface4<T extends Interface3<U, T>, U extends Interface4<T, U>> {
    }
    
    public interface Interface5<T> {
    }
    
    public interface Interface6<T> {
    }
    
    public interface Interface7<T extends Interface5<Interface6<T>>> {
    }
}
