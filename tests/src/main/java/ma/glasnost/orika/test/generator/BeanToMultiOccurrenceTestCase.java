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

package ma.glasnost.orika.test.generator;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.GeneratedObjectBase;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Before;
import org.junit.Test;

public class BeanToMultiOccurrenceTestCase {

	
	private DatatypeFactory dataTypeFactory;
	
	@Before
	public void setUp() {
	    try {
            dataTypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
	}
    
	@Test
	public void unequalSize() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.registerClassMap(
				factory.classMap(MapOfScores.class, GenericDto.class)
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
		
		MapOfScores result = mapper.map(source, MapOfScores.class);
		
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
	@Test
	public void parallelWithConvertedTypes() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory(true);
		factory.registerClassMap(
				factory.classMap(MapOfPeople.class, GenericDto.class)
				.field("people{value.birthDate}", "dateArray{}")
				.field("people{value.name}", "stringArray{}")
				.byDefault());
        
		MapOfPeople peopleMap = new MapOfPeople();
		Person p = new Person();
		p.birthDate = DatatypeFactory.newInstance().newXMLGregorianCalendar("2001-02-03");
		p.name = "Jim";
		peopleMap.people.put(new Date(), p);
		
		p = new Person();
        p.birthDate = DatatypeFactory.newInstance().newXMLGregorianCalendar("2005-07-14");
        p.name = "Sue";
        peopleMap.people.put(new Date(System.currentTimeMillis() + 20000L), p);
		
        p = new Person();
        p.birthDate = DatatypeFactory.newInstance().newXMLGregorianCalendar("2003-07-14");
        p.name = "Sally";
        peopleMap.people.put(new Date(System.currentTimeMillis() + 9000000L), p);
        
        /*
         * Tell Orika how we should convert the list element type to map entry
         */
		MapperFacade mapper = factory.getMapperFacade();
		
		GenericDto result = mapper.map(peopleMap, GenericDto.class);
		
		Assert.assertNotNull(result.dateArray);
		Assert.assertNotNull(result.getStringArray());
		Assert.assertEquals(peopleMap.people.size(), result.dateArray.length);
		Assert.assertEquals(peopleMap.people.size(), result.getStringArray().length);
		int i = -1;
		for (Person person: peopleMap.people.values()) {
		    ++i;
			Assert.assertEquals(person.name, result.getStringArray()[i]);
			Assert.assertTrue(toXMLGregorianCalendar(result.dateArray[i], dataTypeFactory).toXMLFormat()
			        .startsWith(person.birthDate.toXMLFormat()));
		}
		
		MapOfPeople mapBack = mapper.map(result, MapOfPeople.class);
	}
	
	/**
	 * Verifies that we're able to map from a single to field to
	 * each of the individual elements of a multi-occurrence item
	 * 
	 * @throws Exception
	 */
	@Test
	public void oneToMany() throws Exception {
	    MapperFactory factory = MappingUtil.getMapperFactory(true);
        factory.registerClassMap(
                factory.classMap(GroupOfStudents.class, MapOfStudents.class)
                .field("names{}", "students{key.name}")
                .field("defaultLetterGrade", "students{value.letterGrade}")
                .field("defaultScore", "students{value.minimumScore}")
                
                .byDefault());
        
        GroupOfStudents source = new GroupOfStudents();
        source.names = new ArrayList<String>();
        source.names.add("Bob");
        source.names.add("Jim");
        source.names.add("Sally");
        source.defaultLetterGrade = 'C';
        source.defaultScore = 70;
        
        
        /*
         * Tell Orika how we should convert the list element type to map entry
         */
        MapperFacade mapper = factory.getMapperFacade();
        MapOfStudents result = mapper.map(source, MapOfStudents.class);
        
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.students);
        Assert.assertFalse(result.students.isEmpty());
        Assert.assertEquals(source.names.size(), result.students.size());
        for (Grade grade: result.students.values()) {
            Assert.assertEquals(source.defaultLetterGrade, grade.getLetterGrade().charValue());
            Assert.assertEquals(source.defaultScore, grade.getMinimumScore());
        }
        
        GroupOfStudents mapBack = mapper.map(result, GroupOfStudents.class);
        
        Assert.assertEquals(source.defaultLetterGrade, mapBack.defaultLetterGrade);
        Assert.assertEquals(source.defaultScore, mapBack.defaultScore);
        Assert.assertEquals(source.names, mapBack.names);
        Assert.assertEquals(source.grades, mapBack.grades);
        
	}
	
	@SuppressWarnings("serial")
    @Test
    public void multipleParallel() throws Exception {
        
        
        MapperFactory factory = MappingUtil.getMapperFactory(true);
        factory.registerClassMap(
                factory.classMap(MapOfScores.class, GenericDto.class)
                .field("scores{key}", "stringArray{}")
                .field("scores{value}", "intArray{}")
                .field("scores{key}", "gradeList{letterGrade}")
                .field("scores{value}", "gradeList{minimumScore}")
                .byDefault());
        
        MapOfScores source = new MapOfScores();
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
        
        MapOfScores mapBack = mapper.map(result, MapOfScores.class);
        Assert.assertTrue(source.getScores().keySet().containsAll(mapBack.getScores().keySet()));
        Assert.assertTrue(mapBack.getScores().keySet().containsAll(source.getScores().keySet()));
    }
	
	
	private static XMLGregorianCalendar toXMLGregorianCalendar(
            Date source, DatatypeFactory factory) {
        
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(source);
        
        return factory.newXMLGregorianCalendar(c);
        
    }
	
	public static class MapOfScores {
		
		private Map<String, String> scores;

		public Map<String, String> getScores() {
			return scores;
		}

		public void setScores(Map<String, String> scores) {
			this.scores = scores;
		}
	}
	
	public static class MapOfPeople {
	    public Map<Date, Person> people = new LinkedHashMap<Date, Person>(); 
	}
	
	public static class Person {
	    public XMLGregorianCalendar birthDate;
	    public String name;
	}
	
	public static class MapOfStudents {
	    public Map<Person, Grade> students = new LinkedHashMap<Person, Grade>();
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
	
	public static class GroupOfStudents {
	    public List<String> names;
	    public List<Integer> grades;
	    public int defaultScore;
	    public char defaultLetterGrade;
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
		
		public Date[] dateArray;
		
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
