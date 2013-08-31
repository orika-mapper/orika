package ma.glasnost.orika.test.unenhance;

import java.util.Collections;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class InaccessibleTypeTestCase {

	
	@Test
    public void accountInformationToSearchInformationEmptyAccountInformationTest() {
        
		MapperFactory mapperFactory = MappingUtil.getMapperFactory();
		MapperFacade mapper = mapperFactory.getMapperFacade();
		
		B info = mapper.map(Collections.<A> emptyList(),B.class);
        Assert.assertNotNull(info);    
    }
	
	public static class A {
		
	}
	
	public static class B {
	    
	}
	
}
