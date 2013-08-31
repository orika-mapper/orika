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

import junit.framework.Assert;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

public class Issue19TestCase {
	
	@Test
	public void test() {
		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

		A a = new A();
		a.setAttribute("attribute");
		
		B b = new B();
		mapperFactory.getMapperFacade().map(a, b);
		Assert.assertEquals(a.getAttribute(),b.getAttribute());
		
		B b1 = new B();
		mapperFactory.getMapperFacade().map(a, b1);
		Assert.assertEquals(a.getAttribute(),b1.getAttribute());
	}
	
	static public class A {
		private String attribute;
		
		public String getAttribute() {
			return attribute;
		}
		
		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}
	}
	
	static public class B {
		private String attribute;
		
		public String getAttribute() {
			return attribute;
		}
		
		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}
	}
}
