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
