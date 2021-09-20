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

package ma.glasnost.orika.test.generics;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Assert;
import org.junit.Test;

import java.beans.IntrospectionException;

/**
 * Test to show odd behavior with specified generic types
 */
public class SpecifiedGenericsTestCase {
	@Test
	public void testSpecifiedGeneric() throws IntrospectionException {
		MapperFacade mapperFacade = MappingUtil.getMapperFactory()
				.getMapperFacade();
		Source source = new Source();
		source.setFoo("Hello");
		Destination destination = mapperFacade.map(source, Destination.class);
		Assert.assertEquals(source.getFoo(), destination.getFoo());
	}

	public static class Base<T> {
		protected T foo;

		public T getFoo() {
			return this.foo;
		}

	}

	public static class Source extends Base<String> {
		public void setFoo(String t) {
			this.foo = t;
		}
	}

	public static class Destination extends Base<String> {
		public void setFoo(String t) {
			this.foo = t;
		}
	}

}
