package ma.glasnost.orika.test.community.issue121;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.community.issue121.aobjects.AMapAObjects1;
import ma.glasnost.orika.test.community.issue121.aobjects.AObject1;
import ma.glasnost.orika.test.community.issue121.aobjects.AObject2;
import ma.glasnost.orika.test.community.issue121.bobjects.BContainerListBObject1;
import ma.glasnost.orika.test.community.issue121.bobjects.BObject1;
import ma.glasnost.orika.test.community.issue121.bobjects.BObject2Container;
import ma.glasnost.orika.test.community.issue121.util.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Ilya Krokhmalyov YC14IK1
 * @since: 8/23/13
 */

public class Issue121TestCase {
    
    public BoundMapperFacade<AMapAObjects1, BContainerListBObject1> getFacade(MapperFactory mapperFactory) {
        return mapperFactory.getMapperFacade(AMapAObjects1.class, BContainerListBObject1.class);
    }
    
    public AMapAObjects1 getInstance() {
        Map<Integer, AObject1> map = new HashMap<>();
        map.put(RandomUtils.randomInt(), AObject1.instance());
        map.put(RandomUtils.randomInt(), AObject1.instance());
        map.put(RandomUtils.randomInt(), AObject1.instance());
        AMapAObjects1 instance = new AMapAObjects1(map);
        return instance;
    }
    
    @Test
    public void test1() throws Throwable {
        
        MapperFactory mapperFactory = MappingUtil.getMapperFactory(true);
        mapperFactory.classMap(AMapAObjects1.class, BContainerListBObject1.class)
            .fieldAToB("map", "list").register();
        mapperFactory.classMap(TypeFactory.valueOf(Map.class, Integer.class, AObject1.class),
                TypeFactory.valueOf(List.class, BObject1.class))
                // Custom mapper
            .field("{key}", "{key}")
            .field("{value}", "{}")
            .byDefault()
            .register();
        
        BoundMapperFacade<AMapAObjects1, BContainerListBObject1> mapper = getFacade(mapperFactory);
        
        AMapAObjects1 instance = getInstance();
        BContainerListBObject1 result = mapper.map(instance);
        
        Assert.assertEquals("Not equals count of mapped objects", result.getList().size(), instance.size());
        Assert.assertEquals(result.getList().get(0).getKey(), instance.getMap().entrySet().iterator().next().getKey());
        // Assert in this point, because seconds class mapper
        // not found
        Integer firstResultId = result.getList().get(0).getId();
        Integer firstInstanceId = instance.getMap()
                .entrySet().iterator().next().getValue().getId();
        Assert.assertEquals("Bug here. Value empty", firstInstanceId, firstResultId);
        Assert.assertEquals(result.getList().get(0).getName(), instance.getMap().entrySet().iterator().next().getValue().getName());
    }
    
    @Test
    public void test2() throws Throwable {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory(true);
        
        mapperFactory.classMap(AMapAObjects1.class, BContainerListBObject1.class).fieldAToB("map{value}", "list{}").register();
        mapperFactory.classMap(AObject1.class, BObject1.class).byDefault().register();
        
        BoundMapperFacade<AMapAObjects1, BContainerListBObject1> mapper = getFacade(mapperFactory);
        
        AMapAObjects1 instance = getInstance();
        BContainerListBObject1 result = mapper.map(instance);
        
        Assert.assertEquals("Bug here!!! Not equals count of mapped objects. You can see, that in result only last element.",
                result.getList().size(), instance.size());
    }
    
    @Test
    public void test3() throws Throwable {
        
        MapperFactory mapperFactory = MappingUtil.getMapperFactory(true);
        
        mapperFactory.classMap(AMapAObjects1.class, BContainerListBObject1.class)
                .fieldAToB("map{value}", "list{}")
                .fieldAToB("map{key}", "list{key}")
                .register();
        mapperFactory.classMap(AObject1.class, BObject1.class).field("list", "container.list").byDefault().register();
        
        BoundMapperFacade<AMapAObjects1, BContainerListBObject1> mapper = getFacade(mapperFactory);
        
        AMapAObjects1 instance = getInstance();
        mapper.map(instance);
        
    }
    
    @Test
    public void test4() throws Throwable {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory(true);
        
        mapperFactory.classMap(AMapAObjects1.class, BContainerListBObject1.class)
                .fieldAToB("map{value}", "list{}")
                .fieldAToB("map{key}", "list{key}")
                .register();
        mapperFactory.classMap(AObject1.class, BObject1.class).field("list{}", "container.list{}").byDefault().register();
        
    }
    
    @Test
    public void test5() throws Throwable {
        
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().mapNulls(true).build();
        
        mapperFactory.classMap(AMapAObjects1.class, BContainerListBObject1.class)
                .fieldAToB("map{value}", "list{}")
                .fieldAToB("map{key}", "list{key}")
                .register();
        mapperFactory.classMap(AObject1.class, BObject1.class).field("list", "container").register();
        mapperFactory.classMap(TypeFactory.valueOf(List.class, AObject2.class), BObject2Container.class)
                .field("{}", "list{}")
                .byDefault()
                .register();
        
        BoundMapperFacade<AMapAObjects1, BContainerListBObject1> mapper = getFacade(mapperFactory);
        
        AMapAObjects1 instance = getInstance();
        BContainerListBObject1 result = mapper.map(instance);
        
        Assert.assertNotNull("Result is null", result);
        Assert.assertNotNull("Inner List is null", result.getList());
    }
}
