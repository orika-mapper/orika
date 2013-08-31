package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Arnaud Jasselette
 * 
 */
public final class Issue113TestCase {
    
    @Test
    public void testIsAssignable() {
        
        Type<SourceBean<String>> sourceType = new TypeBuilder<SourceBean<String>>() {
        }.build();
        
        Type<ChildSourceBean> destType = TypeFactory.valueOf(ChildSourceBean.class);
        
        Assert.assertTrue(sourceType.isAssignableFrom(destType));
        
    }
    
    @Test
    public void testMapping() {
        
        // create a factory without the auto mapping
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        // register a <SourceBean, TargetBean> class map
        // This generates a <SourceBean<Object>, TargetBean> mapper
        factory.classMap(SourceBean.class, TargetBean.class).field("a", "b").register();
        MapperFacade mapper = factory.getMapperFacade();
        ChildSourceBean childSourceBean = new ChildSourceBean();
        // Orika doesn't use the <SourceBean<Object>, TargetBean> mapper because
        // ChildSourceBean 'hides' its generic type in its signature.
        TargetBean targetBean = mapper.map(childSourceBean, TargetBean.class);
        Assert.assertNotNull(targetBean);
        Assert.assertEquals(childSourceBean.a, targetBean.b);
    }
    
    /**
     * A source bean with a generic type
     */
    public static class SourceBean<T> {
        public int a = 2;
    }
    
    /**
     * A child of the SourceBean that defines the generic type of its parent
     */
    public static class ChildSourceBean extends SourceBean<String> {
    }
    
    /*
     * This would work public static class ChildSourceBean<String> extends
     * SourceBean<String> {}
     */
    
    /**
     * A simple target bean
     */
    public static class TargetBean {
        public int b;
    }
}