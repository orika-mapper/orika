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

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.community.issue26.OrderID;
import ma.glasnost.orika.test.community.issue28.Order;
import ma.glasnost.orika.test.community.issue28.OrderData;
import org.junit.Assert;
import org.junit.Test;

/**
 * StackoverflowException on recursively-defined generic type.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/28">https://code.google.com/archive/p/orika/</a>
 *
 */
public class Issue28TestCase {
    @Test
    public void testMapping() {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        mapperFactory.getConverterFactory().registerConverter(new OrderIdConverter());
        mapperFactory.classMap(Order.class, OrderData.class).field("id", "number").byDefault().register();
        MapperFacade facade = mapperFactory.getMapperFacade();
        OrderData data = new OrderData(1234L);
        Order order = facade.map(data, Order.class);
        Assert.assertEquals(Long.valueOf(1234L), order.getId());
    }
    
    public static class OrderIdConverter extends CustomConverter<Long, OrderID> {
        
        public OrderID convert(Long source, Type<? extends OrderID> destinationType, MappingContext context) {
            return new OrderID(source);
        }
    }
    
}
