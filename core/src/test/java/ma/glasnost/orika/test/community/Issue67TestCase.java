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

package ma.glasnost.orika.test.community;

import org.junit.Test;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

/**
 * Property with no getter (null readMethod) is not being excluded/ignored
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/67">https://code.google.com/archive/p/orika/</a>
 * @author matt.deboer@gmail.com
 *
 */
public class Issue67TestCase {

	@Test
	public void simpleCase() {
		MapperFactory factory = new DefaultMapperFactory.Builder().build();
		factory.registerClassMap(factory.classMap(Bean.class, Bean.class)
				.byDefault().toClassMap());
		MapperFacade mapper = factory.getMapperFacade();
		Bean bean = new Bean();
		bean.setSize(20);
		bean.setName("Kidney");
		mapper.map(bean, Bean.class);

		/* If map pass no need to check */
	}

	public static class Bean {

		private String name;
		private int size;

		/*
		 * public int getSize() { return size; }
		 */
		public void setSize(int size) {
			this.size = size;
		}

		public String getName() {
			return name;
		}

		public void setName(String value) {
			this.name = value;
		}
	}
}