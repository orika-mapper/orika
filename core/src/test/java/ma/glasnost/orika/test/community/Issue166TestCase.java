package ma.glasnost.orika.test.community;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import org.junit.Assert;
import org.junit.Test;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * StackOverflowError exception when mapping enum.
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/issues/166">https://github.com/orika-mapper/orika/issues</a>
 */
public class Issue166TestCase {

    @Test
    public void testIssue166() throws Exception {

        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        MapperFacade beanMapper = factory.getMapperFacade();
        
        SimpleBeanResource sbr = beanMapper.map(SimpleEnumBean.E1, SimpleBeanResource.class);
        Assert.assertEquals(sbr.getName(), SimpleEnumBean.E1.getName());
    }
    
    @Test
    public void testCaseSimplification_withTypeFactoryResolveValueOf() throws Exception {
        
        // readMethod = public final java.lang.Class<E> java.lang.Enum.getDeclaringClass()
        Method readMethod = SimpleEnumBean.class.getMethod("getDeclaringClass");
        // parameterized return type = Class<E>
        ParameterizedType parameterizedType = (ParameterizedType) readMethod.getGenericReturnType();
        
        // start Test
        Type<?> type = TypeFactory.resolveValueOf(parameterizedType, TypeFactory.valueOf(SimpleEnumBean.class));
        
        // validate
        assertThat(type.toString(), is("Class<SimpleEnumBean>"));
        
    }
    
    public static class SimpleBeanResource implements Serializable {
        private static final long serialVersionUID = 1894987353201458022L;
        
        private String code;
        private String name;

        public SimpleBeanResource(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static enum SimpleEnumBean {
        E1("code_e1", "name_e1");
        
        SimpleEnumBean(String code, String name) {
            this.code = code;
            this.name = name;
        }
        
        private final String code;
        
        private final String name;

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

}