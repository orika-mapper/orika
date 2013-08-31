package ma.glasnost.orika.test.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.GeneratedObjectBase;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class MapGenerationTestCase {

	@Test
	public void testMapToMapGeneration() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.registerClassMap(
				factory.classMap(MapWithSetter.class, MapWithSetterDto.class)
				.field("testScores", "scores").byDefault());
		
		MapperFacade mapper = factory.getMapperFacade();
		
		MapWithSetter source = new MapWithSetter();
		Map<String, Integer> testScores = new LinkedHashMap<String, Integer>();

		testScores.put("A", 90);
		testScores.put("B", 80);
		testScores.put("C", 70);
		source.setTestScores(testScores);
		
		
		MapWithSetterDto result = mapper.map(source, MapWithSetterDto.class);
		
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getScores());
		for (Entry<String, Integer> entry: testScores.entrySet()) {
			Assert.assertEquals(entry.getValue(), result.getScores().get(entry.getKey()));
		}
		
	}
	
	@Test
	public void testMapToMapGeneration_noSetter() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.registerClassMap(
				factory.classMap(MapWithSetter.class, MapWithoutSetter.class)
				.field("testScores", "scores").byDefault());
		
		MapperFacade mapper = factory.getMapperFacade();
		
		MapWithSetter source = new MapWithSetter();
		Map<String, Integer> testScores = new LinkedHashMap<String, Integer>();

		testScores.put("A", 90);
		testScores.put("B", 80);
		testScores.put("C", 70);
		source.setTestScores(testScores);
		
		
		MapWithoutSetter result = mapper.map(source, MapWithoutSetter.class);
		
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getScores());
		for (Entry<String, Integer> entry: testScores.entrySet()) {
			Assert.assertEquals(entry.getValue().toString(), result.getScores().get(entry.getKey()));
		}
		
	}
	
	@Test
	public void testMapToArrayGeneration() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.registerClassMap(
				factory.classMap(MapWithSetter.class, GenericDto.class)
				.field("testScores", "stringArray").byDefault());
        
        /*
         * Tell Orika how we should convert the map entries to the result array component type (String)
         */
        factory.getConverterFactory().registerConverter(new CustomConverter<Map.Entry<String, Integer>, String>() {

			public String convert(Map.Entry<String, Integer> source,
					Type<? extends String> destinationType) {
				return source.getKey();
			}});
		
		MapperFacade mapper = factory.getMapperFacade();
		
		MapWithSetter source = new MapWithSetter();
		Map<String, Integer> testScores = new LinkedHashMap<String, Integer>();

		testScores.put("A", 90);
		testScores.put("B", 80);
		testScores.put("C", 70);
		source.setTestScores(testScores);
		
		
		
		GenericDto result = mapper.map(source, GenericDto.class);
		
		Assert.assertNotNull(result.getStringArray());
		
	}
	
	@Test
	public void testMapToListGeneration() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.registerClassMap(
				factory.classMap(MapWithSetter.class, GenericDto.class)
				.field("testScores", "stringList").byDefault());
        
        /*
         * Tell Orika how we should convert the map entries to the result list element type (String)
         */
        factory.getConverterFactory().registerConverter(new CustomConverter<Map.Entry<String, Integer>, String>() {

			public String convert(Map.Entry<String, Integer> source,
					Type<? extends String> destinationType) {
				return source.getKey();
			}});
		
		MapperFacade mapper = factory.getMapperFacade();
		
		MapWithSetter source = new MapWithSetter();
		Map<String, Integer> testScores = new LinkedHashMap<String, Integer>();

		testScores.put("A", 90);
		testScores.put("B", 80);
		testScores.put("C", 70);
		source.setTestScores(testScores);
		
		
		
		GenericDto result = mapper.map(source, GenericDto.class);
		
		Assert.assertNotNull(result.getStringList());
		
	}
	
	@Test
	public void testListToMapGeneration() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.registerClassMap(
				factory.classMap(MapWithoutSetter.class, GenericDto.class)
				.field("scores", "stringList").byDefault());
        
        /*
         * Tell Orika how we should convert the list element type to map entry
         */
        factory.getConverterFactory().registerConverter(new CustomConverter<String,Map.Entry<String, Integer>>() {

        	@SuppressWarnings("serial")
			private Map<String, Map.Entry<String, Integer>> testScores = 
				new LinkedHashMap<String, Map.Entry<String, Integer>>() {{
	        		put("A", new MapEntry<String, Integer>("A",90));
	        		put("B", new MapEntry<String, Integer>("B",80));
	        		put("C", new MapEntry<String, Integer>("C",70));
	        		put("D", new MapEntry<String, Integer>("D",60));
	        		put("F", new MapEntry<String, Integer>("F",50));
	        	}};
        	
			public Map.Entry<String, Integer> convert(String source, Type<? extends Map.Entry<String, Integer>> destinationType) {
				return testScores.get(source);
			}});
		
		MapperFacade mapper = factory.getMapperFacade();
		
		GenericDto source = new GenericDto();
		List<String> testScores = new ArrayList<String>();

		testScores.add("A");
		testScores.add("B");
		testScores.add("C");
		source.setStringList(testScores);

		
		
		MapWithoutSetter result = mapper.map(source, MapWithoutSetter.class);
		
		Assert.assertNotNull(result.getScores());
		
	}
	
	@Test
	public void testArrayToMapGeneration() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.registerClassMap(
				factory.classMap(MapWithoutSetter.class, GenericDto.class)
				.field("scores", "stringArray").byDefault());
        
        /*
         * Tell Orika how we should convert the list element type to map entry
         */
        factory.getConverterFactory().registerConverter(new CustomConverter<String,Map.Entry<String, Integer>>() {

        	@SuppressWarnings("serial")
			private Map<String, Map.Entry<String, Integer>> testScores = 
				new LinkedHashMap<String, Map.Entry<String, Integer>>() {{
	        		put("A", new MyMapEntry<String, Integer>("A",90));
	        		put("B", new MapEntry<String, Integer>("B",80));
	        		put("C", new MapEntry<String, Integer>("C",70));
	        		put("D", new MapEntry<String, Integer>("D",60));
	        		put("F", new MapEntry<String, Integer>("F",50));
	        	}};
        	
			public Map.Entry<String, Integer> convert(String source, Type<? extends Map.Entry<String, Integer>> destinationType) {
				return testScores.get(source);
			}});
		
		MapperFacade mapper = factory.getMapperFacade();
		
		GenericDto source = new GenericDto();
		List<String> testScores = new ArrayList<String>();

		testScores.add("A");
		testScores.add("B");
		testScores.add("C");
		source.setStringArray(testScores.toArray(new String[testScores.size()]));
		
		
		MapWithoutSetter result = mapper.map(source, MapWithoutSetter.class);
		
		Assert.assertNotNull(result.getScores());
		
	}
	
	@Test
	public void testNewSyntax_mapToArrays() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.registerClassMap(
				factory.classMap(MapWithoutSetter.class, GenericDto.class)
				.field("scores{key}", "stringArray{}")
				.field("scores{value}", "intArray{}")
				.byDefault());
        /*
         * Tell Orika how we should convert the list element type to map entry
         */
		MapperFacade mapper = factory.getMapperFacade();
		
		GenericDto source = new GenericDto();
		List<String> testScores = new ArrayList<String>();
		List<Integer> numericScores = new ArrayList<Integer>();
		testScores.add("A");
		numericScores.add(90);
		testScores.add("B");
		numericScores.add(80);
		testScores.add("C");
		numericScores.add(70);
		source.setStringArray(testScores.toArray(new String[testScores.size()]));
		source.setIntArray(GeneratedObjectBase.intArray(numericScores));
		
		MapWithoutSetter result = mapper.map(source, MapWithoutSetter.class);
		
		Assert.assertNotNull(result.getScores());
		
	}
	
	@Test
	public void testNewSyntax_mapToArraysWithUnequalSize() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory(true);
		factory.registerClassMap(
				factory.classMap(MapWithoutSetter.class, GenericDto.class)
				.field("scores{key}", "stringArray{}")
				.field("scores{value}", "intArray{}")
				.byDefault());
		
        /*
         * Tell Orika how we should convert the list element type to map entry
         */
		MapperFacade mapper = factory.getMapperFacade();
		
		GenericDto source = new GenericDto();
		List<String> testScores = new ArrayList<String>();
		List<Integer> numericScores = new ArrayList<Integer>();
		testScores.add("A");
		numericScores.add(90);
		testScores.add("B");
		numericScores.add(80);
		testScores.add("C");
	
		source.setStringArray(testScores.toArray(new String[testScores.size()]));
		source.setIntArray(GeneratedObjectBase.intArray(numericScores));
		
		MapWithoutSetter result = mapper.map(source, MapWithoutSetter.class);
		
		Assert.assertNotNull(result.getScores());
		Assert.assertTrue("90".equals(result.getScores().get("A")));
		Assert.assertTrue("80".equals(result.getScores().get("B")));
		Assert.assertFalse(result.getScores().containsKey("C"));
	}
	
	/**
	 * Demonstrates how a single field can be mapped to more than one destination,
	 * in both directions.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("serial")
	@Test
	public void testNewSyntax_multipleParallel() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory(true);
		factory.registerClassMap(
				factory.classMap(MapWithoutSetter.class, GenericDto.class)
				.field("scores{key}", "stringArray{}")
				.field("scores{value}", "intArray{}")
				.field("scores{key}", "gradeList{letterGrade}")
				.field("scores{value}", "gradeList{minimumScore}")
				.byDefault());
        
		MapWithoutSetter source = new MapWithoutSetter();
		source.setScores(
				new LinkedHashMap<String, String>() {{
        		put("A", "90");
        		put("B", "80");
        		put("C", "70");
        		put("D", "60");
        		put("F", "50");
        	}});
		
        /*
         * Tell Orika how we should convert the list element type to map entry
         */
		MapperFacade mapper = factory.getMapperFacade();
		
		GenericDto result = mapper.map(source, GenericDto.class);
		
		Assert.assertNotNull(result.getGradeList());
		Assert.assertEquals(source.getScores().size(), result.getGradeList().size());
		for (Grade g: result.getGradeList()) {
			Assert.assertTrue(source.getScores().containsKey(""+g.getLetterGrade()));
			Assert.assertTrue(source.getScores().get(""+g.getLetterGrade()).equals(""+g.getMinimumScore()));
		}
		
		MapWithoutSetter mapBack = mapper.map(result, MapWithoutSetter.class);
		Assert.assertTrue(source.getScores().keySet().containsAll(mapBack.getScores().keySet()));
		Assert.assertTrue(mapBack.getScores().keySet().containsAll(source.getScores().keySet()));
	}
	
	
	public static class MyMapEntry<K,V> implements Map.Entry<K, V> {

	    private K key;
	    private V value;
	    
	    public MyMapEntry(K key, V value) {
	        this.key = key;
	        this.value = value;
	    }
	    
        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
	    
	}
	
	
	public static class MapWithSetter {
		
		private Map<String, Integer> testScores;

		public Map<String, Integer> getTestScores() {
		    return Collections.unmodifiableMap(testScores);
			//return testScores;
		}

		public void setTestScores(Map<String, Integer> testScores) {
			this.testScores = testScores;
		}
	}
	
	public static class MapWithSetterDto {
		
		private Map<String, Integer> scores;

		public Map<String, Integer> getScores() {
			return scores;
		}

		public void setScores(Map<String, Integer> scores) {
			this.scores = scores;
		}
	}
	
	public static class MapWithSetterDto2 {
		
		private Map<String, String> scores;

		public Map<String, String> getScores() {
			return scores;
		}

		public void setScores(Map<String, String> scores) {
			this.scores = scores;
		}
	}
	
	public static class MapWithoutSetter {
		
		private Map<String, String> scores;

		public Map<String, String> getScores() {
			return scores;
		}

		public void setScores(Map<String, String> scores) {
			this.scores = scores;
		}
	}
	
	public static class Grade {
		int minimumScore;
		Character letterGrade;
		public int getMinimumScore() {
			return minimumScore;
		}
		public void setMinimumScore(int minimumScore) {
			this.minimumScore = minimumScore;
		}
		public Character getLetterGrade() {
			return letterGrade;
		}
		public void setLetterGrade(Character letterGrade) {
			this.letterGrade = letterGrade;
		}
	}
	
	public static class GenericDto {
		
		private String[] stringArray;
		private List<String> stringList;
		private int[] intArray;
		private long[] longArray;
		private List<Grade> gradeList;
		private Grade[] gradeArray;
		private Map<String, Grade> gradesByLetter;
		private Map<Integer, Grade> gradesByMinScore;
		private Map<Grade, Character> lettersByGrade;
		private Map<Grade, Integer> scoresByGrade;
		public String[] getStringArray() {
			return stringArray;
		}
		public void setStringArray(String[] stringArray) {
			this.stringArray = stringArray;
		}
		public List<String> getStringList() {
			return stringList;
		}
		public void setStringList(List<String> stringList) {
			this.stringList = stringList;
		}
		public int[] getIntArray() {
			return intArray;
		}
		public void setIntArray(int[] intArray) {
			this.intArray = intArray;
		}
		public long[] getLongArray() {
			return longArray;
		}
		public void setLongArray(long[] longArray) {
			this.longArray = longArray;
		}
		public List<Grade> getGradeList() {
			return gradeList;
		}
		public void setGradeList(List<Grade> gradeList) {
			this.gradeList = gradeList;
		}
		public Grade[] getGradeArray() {
			return gradeArray;
		}
		public void setGradeArray(Grade[] gradeArray) {
			this.gradeArray = gradeArray;
		}
		public Map<String, Grade> getGradesByLetter() {
			return gradesByLetter;
		}
		public void setGradesByLetter(Map<String, Grade> gradesByLetter) {
			this.gradesByLetter = gradesByLetter;
		}
		public Map<Integer, Grade> getGradesByMinScore() {
			return gradesByMinScore;
		}
		public void setGradesByMinScore(Map<Integer, Grade> gradesByMinScore) {
			this.gradesByMinScore = gradesByMinScore;
		}
		public Map<Grade, Character> getLettersByGrade() {
			return lettersByGrade;
		}
		public void setLettersByGrade(Map<Grade, Character> lettersByGrade) {
			this.lettersByGrade = lettersByGrade;
		}
		public Map<Grade, Integer> getScoresByGrade() {
			return scoresByGrade;
		}
		public void setScoresByGrade(Map<Grade, Integer> scoresByGrade) {
			this.scoresByGrade = scoresByGrade;
		}
	}
	
}
