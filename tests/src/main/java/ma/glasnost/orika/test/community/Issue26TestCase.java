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
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.community.issue26.Order;
import ma.glasnost.orika.test.community.issue26.OrderData;
import ma.glasnost.orika.test.community.issue26.OrderID;
import ma.glasnost.orika.test.community.issue26.OrderIDConverter;

import org.junit.Test;

public class Issue26TestCase {

	@Test
	public void testMapping() {
		MapperFactory mapperFactory = MappingUtil.getMapperFactory();
		
		mapperFactory.registerClassMap(
		        mapperFactory.classMap(Order.class, OrderData.class)
				.field("entityID", "orderId").byDefault().toClassMap());
	
		mapperFactory.getConverterFactory().registerConverter(new OrderIDConverter());
		MapperFacade facade = mapperFactory.getMapperFacade();
		
		OrderData data = new OrderData(1234l);
		Order order = facade.map(data, Order.class);
		Assert.assertEquals(new OrderID(1234l), order.getEntityID());
	}
}
