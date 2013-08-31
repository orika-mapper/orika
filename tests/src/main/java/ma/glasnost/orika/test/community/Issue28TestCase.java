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
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.community.issue26.OrderID;
import ma.glasnost.orika.test.community.issue28.Order;
import ma.glasnost.orika.test.community.issue28.OrderData;

import org.junit.Test;

public class Issue28TestCase {
    @Test
    public void testMapping() {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        mapperFactory.getConverterFactory().registerConverter(new OrderIdConverter());
        ClassMapBuilder<Order, OrderData> builder = ClassMapBuilder.map(Order.class, OrderData.class);
        ClassMap<Order, OrderData> classMap = builder.field("id", "number").byDefault().toClassMap();
        mapperFactory.registerClassMap(classMap);
        MapperFacade facade = mapperFactory.getMapperFacade();
        OrderData data = new OrderData(1234l);
        Order order = facade.map(data, Order.class);
        Assert.assertEquals(Long.valueOf(1234l), order.getId());
    }
    
    public static class OrderIdConverter extends CustomConverter<Long, OrderID> {
        
        public OrderID convert(Long source, Type<? extends OrderID> destinationType) {
            return new OrderID(source);
        }
    }
    
}
