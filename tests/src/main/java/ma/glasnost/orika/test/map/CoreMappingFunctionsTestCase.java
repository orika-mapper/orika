package ma.glasnost.orika.test.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class CoreMappingFunctionsTestCase {
    
    /*
     * Case 1: from a map to another map
     * 
     * we iterate over the entry set, map the key and value, and put into new map
     */
    @Test
    public void testMapToMap_Simple() {
        
        Map<String, Integer> sourceMap = new HashMap<String, Integer>();
        sourceMap.put("A", 1);
        sourceMap.put("B", 2);
        sourceMap.put("C", 3);
        
        MapperFacade mapper = MappingUtil.getMapperFactory().getMapperFacade();
        
        Map<String, Integer> result = mapper.mapAsMap(sourceMap, new TypeBuilder<Map<String, Integer>>(){}.build(), new TypeBuilder<Map<String, Integer>>(){}.build());
        
        Assert.assertNotNull(result);
        Assert.assertNotSame(sourceMap, result);
        
    }
    
    @Test
    public void testMapToMap_WithConversion() {
        
        Map<String, Integer> sourceMap = new HashMap<String, Integer>();
        sourceMap.put("A", 1);
        sourceMap.put("B", 2);
        sourceMap.put("C", 3);
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new CustomConverter<Integer, String>() {

            public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
                return destinationType.getRawType().equals(String.class) && 
                        (sourceType.getRawType().equals(Integer.class) || 
                                sourceType.getRawType().equals(int.class));
            }

            public String convert(Integer source, Type<? extends String> destinationType) {
                return ""+source;
            }
            
        });
        MapperFacade mapper = factory.getMapperFacade();
        
        Map<String, String> result = mapper.mapAsMap(sourceMap, new TypeBuilder<Map<String, Integer>>(){}.build(), new TypeBuilder<Map<String, String>>(){}.build());
        
        Assert.assertNotNull(result);
        Assert.assertNotSame(sourceMap, result);
        for (Entry<String, Integer> entry: sourceMap.entrySet()) {
            Assert.assertNotNull(result.get(entry.getKey()));
            Assert.assertTrue(result.get(entry.getKey()).equals(""+entry.getValue().toString()));
        }
        
    }
    
    public static class Ranking {
        private String name;
        private Integer rank;
         
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Integer getRank() {
            return rank;
        }
        public void setRank(Integer rank) {
            this.rank = rank;
        }
    }
    
    /*
     * Case 2a: from a collection to a map
     * 
     * we iterate over the collection, and attempt to map each element to a Map.Entry;
     * we'll need a special concrete destination type since Map.Entry is not concrete
     */
    @Test
    public void testCollectionToMap_Simple() {
        
        Collection<Ranking> source = new ArrayList<Ranking>();
        Ranking r = new Ranking();
        r.setName("A");
        r.setRank(1);
        source.add(r);
        r = new Ranking();
        r.setName("B");
        r.setRank(2);
        source.add(r);
        r = new Ranking();
        r.setName("C");
        r.setRank(3);
        source.add(r);
        
        /*
         * To make the map work for Collection to Map, we provide a class mapping
         * from the element type in the collection to the special type MapEntry which
         * represents map entries.
         */
        Type<Map<String, Integer>> mapType = new TypeBuilder<Map<String, Integer>>(){}.build();
        Type<MapEntry<String, Integer>> entryType = MapEntry.concreteEntryType(mapType);
        Type<Ranking> rankingType = TypeFactory.valueOf(Ranking.class);
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.registerClassMap(factory.classMap(rankingType, entryType)
                .field("name", "key")
                .field("rank", "value")
                .byDefault().toClassMap());
                
        factory.registerConcreteType(Map.Entry.class, MapEntry.class);
        MapperFacade mapper = factory.getMapperFacade();
        Map<String, Integer> result = mapper.mapAsMap(source, rankingType, mapType);
        
        Assert.assertNotNull(result);
        Assert.assertEquals(source.size(), result.size());
        for (Ranking ranking: source) {
            Assert.assertTrue(result.get(ranking.getName()).equals(ranking.getRank()));
        }
 
    }
    
    
    /*
     * Case 2b: from an array to a map
     * 
     * we iterator over the array, and attempt to map each element to a Map.Entry;
     * we'll need a special concrete destination type since Map.Entry is not concrete
     */
    @Test
    public void testArrayToMap_Simple() {
        
        List<Ranking> tempList = new ArrayList<Ranking>();
        Ranking r = new Ranking();
        r.setName("A");
        r.setRank(1);
        tempList.add(r);
        r = new Ranking();
        r.setName("B");
        r.setRank(2);
        tempList.add(r);
        r = new Ranking();
        r.setName("C");
        r.setRank(3);
        tempList.add(r);
        
        Ranking[] source = tempList.toArray(new Ranking[0]);
        
        /*
         * To make the map work for Collection to Map, we provide a class mapping
         * from the element type in the collection to the special type MapEntry which
         * represents map entries.
         */
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.registerClassMap(factory.classMap(Ranking.class, new TypeBuilder<MapEntry<String, Integer>>(){}.build())
                .field("name", "key")
                .field("rank", "value")
                .byDefault().toClassMap());
                
        MapperFacade mapper = factory.getMapperFacade();
        Map<String, Integer> result = mapper.mapAsMap(source, TypeFactory.valueOf(Ranking.class), new TypeBuilder<Map<String, Integer>>(){}.build());
        
        Assert.assertNotNull(result);
        Assert.assertEquals(source.length, result.size());
        for (Ranking ranking: source) {
            Assert.assertTrue(result.get(ranking.getName()).equals(ranking.getRank()));
        }
 
    }   
    
    /*
     * Case 3a: from a map to a collection
     * 
     * we iterate over the entry set, and map each entry to a collection element
     * 
     */
    @Test
    public void testMapToCollection_Simple() {
        
        Map<String, Integer> source = new HashMap<String, Integer>();
        source.put("A", 1);
        source.put("B", 2);
        source.put("C", 3);
        
        Type<Map<String, Integer>> mapType = new TypeBuilder<Map<String, Integer>>(){}.build();
        Type<MapEntry<String, Integer>> entryType = MapEntry.concreteEntryType(mapType);
        Type<Ranking> rankingType = TypeFactory.valueOf(Ranking.class);
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.registerClassMap(factory.classMap(rankingType, entryType)
                .field("name", "key")
                .field("rank", "value")
                .byDefault().toClassMap());
                
        MapperFacade mapper = factory.getMapperFacade();
        
        List<Ranking> result = mapper.mapAsList(source, mapType, rankingType);
        
        Assert.assertNotNull(result);
        
        for (Ranking ranking: result) {
            Assert.assertTrue(source.get(ranking.getName()).equals(ranking.getRank()));
        }
    }
    /*
     * Case 3b: from a map to an array
     * 
     * we iterate over the entry set, and map each entry to an array element
     */
    @Test
    public void testMapToArray_Simple() {
        
        Map<String, Integer> source = new HashMap<String, Integer>();
        source.put("A", 1);
        source.put("B", 2);
        source.put("C", 3);
        
        Type<Map<String, Integer>> mapType = new TypeBuilder<Map<String, Integer>>(){}.build();
        Type<MapEntry<String, Integer>> entryType = MapEntry.concreteEntryType(mapType);
        Type<Ranking> rankingType = TypeFactory.valueOf(Ranking.class);
        
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.registerClassMap(factory.classMap(rankingType, entryType)
                .field("name", "key")
                .field("rank", "value")
                .byDefault().toClassMap());
                
        MapperFacade mapper = factory.getMapperFacade();
        
        Ranking[] result = mapper.mapAsArray(new Ranking[source.size()], source, mapType, rankingType);
        
        Assert.assertNotNull(result);
        
        for (Ranking ranking: result) {
            Assert.assertTrue(source.get(ranking.getName()).equals(ranking.getRank()));
        }
    }
    
}
