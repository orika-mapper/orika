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

package ma.glasnost.orika.test.community.issue96;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import org.junit.Before;
import org.junit.Test;

public class Issue96TestCase {
	private MapperFacade facade;

	@Before
	public void setUp() {
		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
		ClassMapBuilder<Customer, CustomerData> builder = mapperFactory.classMap(Customer.class, CustomerData.class);
		builder.field("name", "name");
		builder.field("address.city", "city");
		mapperFactory.registerClassMap(builder.toClassMap());
		facade = mapperFactory.getMapperFacade();
	}

	@Test
	public void test() {
		AddressData address = new AddressData("city");
		// The next line produces a new mapper for AddresssData and Address
		facade.map(address, Address.class);
		CustomerData customer = new CustomerData();
		customer.setCity("city");
		customer.setName("name");
		facade.map(customer, Customer.class);
	}
}
