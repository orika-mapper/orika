/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
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
package ma.glasnost.orika.test.community;

import org.junit.Assert;
import org.junit.Test;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

public class Issue38TestCase {

	@Test
	public void testAvoidEmptyObjectCreation() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		
		factory.registerClassMap(ClassMapBuilder.map(A.class, B.class).field("b.i1", "i1").field("b.i2", "i2").toClassMap());
		
		MapperFacade mapperFacade = factory.getMapperFacade();
		
		
		B b = new B();
		b.i1 = null;
		b.i2 = null;
		
		
		A result = mapperFacade.map(b, A.class);
		
		Assert.assertNull(result.b);
		
		b.i1 = 2;
		b.i2 = null;
	}
	
	@Test
	public void testCreateDestinationIfNotNull() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		
		factory.registerClassMap(ClassMapBuilder.map(A.class, B.class).field("b.i1", "i1").field("b.i2", "i2").toClassMap());
		
		MapperFacade mapperFacade = factory.getMapperFacade();
		
		
		B b = new B();
		b.i1 = 2;
		b.i2 = 3;
		
		
		A result = mapperFacade.map(b, A.class);
		
		Assert.assertNotNull(result.b);
		Assert.assertEquals(b.i1, result.b.i1);
		Assert.assertEquals(b.i2, result.b.i2);
		
	}
	
	public static class A {
		public B b;
	}
	
	public static class B {
		public Integer i1;
		public Integer i2; 
	}
	
	
	
}
