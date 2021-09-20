/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.glasnost.orika.test.unenhance;

import java.util.Collections;

import org.junit.Assert;
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
