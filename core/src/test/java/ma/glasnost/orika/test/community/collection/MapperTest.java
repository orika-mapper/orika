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

package ma.glasnost.orika.test.community.collection;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MapperTest extends AbstractMapperTest {
    private MapperFacade facade;
    
    @Before
    public void setUp() {
        MapperFactory mapperFactory = createMapperFactory();
        mapperFactory.registerClassMap(mapperFactory.classMap(Order.class, OrderData.class).byDefault().toClassMap());
        mapperFactory.registerClassMap(mapperFactory.classMap(Position.class, PositionData.class).byDefault().toClassMap());
        facade = mapperFactory.getMapperFacade();
    }
    
    @Test
    public void test() {
        OrderData data = new OrderData();
        data.setName("asd");
        PositionData positionData = new PositionData();
        positionData.setNumber("1234");
        data.add(positionData);
        positionData = new PositionData();
        positionData.setNumber("2345");
        data.add(positionData);
        Order order = facade.map(data, Order.class);
        Assert.assertEquals("asd", order.getName());
        Assert.assertEquals(2, order.getPositions().size());
        Assert.assertEquals("1234", order.getPositions().iterator().next().getNumber());
        // map it back
        OrderData data2 = facade.map(order, OrderData.class);
        Assert.assertEquals("asd", data2.getName());
        Assert.assertEquals(2, data2.getPositions().size());
        Assert.assertEquals("1234", data2.getPositions().iterator().next().getNumber());
    }
}
