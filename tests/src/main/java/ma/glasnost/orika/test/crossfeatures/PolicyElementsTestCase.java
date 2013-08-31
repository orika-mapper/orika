package ma.glasnost.orika.test.crossfeatures;

import java.util.HashSet;
import java.util.Set;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.crossfeatures.PolicyElementsTestCaseClasses.CustomerElement;
import ma.glasnost.orika.test.crossfeatures.PolicyElementsTestCaseClasses.CustomerElementDTO;
import ma.glasnost.orika.test.crossfeatures.PolicyElementsTestCaseClasses.OneOtherElement;
import ma.glasnost.orika.test.crossfeatures.PolicyElementsTestCaseClasses.OneOtherElementDTO;
import ma.glasnost.orika.test.crossfeatures.PolicyElementsTestCaseClasses.OtherElement;
import ma.glasnost.orika.test.crossfeatures.PolicyElementsTestCaseClasses.OtherElementDTO;
import ma.glasnost.orika.test.crossfeatures.PolicyElementsTestCaseClasses.Policy;
import ma.glasnost.orika.test.crossfeatures.PolicyElementsTestCaseClasses.PolicyDTO;
import ma.glasnost.orika.test.crossfeatures.PolicyElementsTestCaseClasses.PolicyElement;
import ma.glasnost.orika.test.crossfeatures.PolicyElementsTestCaseClasses.PolicyElementDTO;
import ma.glasnost.orika.test.crossfeatures.PolicyElementsTestCaseClasses.PolicyElementProxy;
import ma.glasnost.orika.test.crossfeatures.PolicyElementsTestCaseClasses.ProductElement;
import ma.glasnost.orika.test.crossfeatures.PolicyElementsTestCaseClasses.ProductElementDTO;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

import org.junit.Assert;
import org.junit.Test;

public class PolicyElementsTestCase {
    
    @Test
    public void test() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        configureMapperFactory(factory);
        
        Policy policy = new Policy();
        Set<PolicyElement> elements = new HashSet<PolicyElement>();
        elements.add(new CustomerElement());
        elements.add(new ProductElement());
        elements.add(new OtherElement());
        elements.add(new OneOtherElement());
        
        policy.setElements(elements);
        
        PolicyDTO dto = factory.getMapperFacade().map(policy, PolicyDTO.class);
        
        Assert.assertEquals(elements.size(), dto.getElements().size());
    }
    
    private void configureMapperFactory(MapperFactory factory) {
        
        factory.registerClassMap(ClassMapBuilder.map(Policy.class, PolicyDTO.class).byDefault().toClassMap());
        factory.registerClassMap(ClassMapBuilder.map(CustomerElement.class, CustomerElementDTO.class).byDefault().toClassMap());
        factory.registerClassMap(ClassMapBuilder.map(ProductElement.class, ProductElementDTO.class).byDefault().toClassMap());
        factory.registerClassMap(ClassMapBuilder.map(OtherElement.class, OtherElementDTO.class).byDefault().toClassMap());
        factory.registerClassMap(ClassMapBuilder.map(OneOtherElement.class, OneOtherElementDTO.class).byDefault().toClassMap());
    }
    
    @Test
    public void testHibernateProxyLike() {
        MapperFactory factory = new DefaultMapperFactory.Builder().unenhanceStrategy(new UnenhanceStrategy() {
            @SuppressWarnings("unchecked")
            public <T> Type<T> unenhanceType(T object, Type<T> type) {
                if (object instanceof PolicyElementProxy)
                    return (Type<T>) ((PolicyElementProxy) object).getTargetClass();
                return type;
            }
            
            @SuppressWarnings("unchecked")
            public <T> T unenhanceObject(T object, Type<T> type) {
                if (object instanceof PolicyElementProxy)
                    return (T) ((PolicyElementProxy) object).getTarget();
                return object;
            }
            
        }).build();
        configureMapperFactory(factory);
        
        Policy policy = new Policy();
        Set<PolicyElement> elements = new HashSet<PolicyElement>();
        CustomerElement target = new CustomerElement();
        target.setName("Adil");
        elements.add(new PolicyElementProxy(target));
        elements.add(new ProductElement());
        elements.add(new OtherElement());
        elements.add(new OneOtherElement());
        
        policy.setElements(elements);
        
        PolicyDTO dto = factory.getMapperFacade().map(policy, PolicyDTO.class);
        
        Assert.assertEquals(elements.size(), dto.getElements().size());
        
        for (PolicyElementDTO element: dto.getElements()) {
            if (element instanceof CustomerElementDTO) {
                Assert.assertEquals("Adil", ((CustomerElementDTO) element).getName());
            }
        }
        
    }
}
