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

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.UtilityResolver;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.property.PropertyResolverStrategy;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class BeanToMapGenerationTestCase {

    private final double DELTA = 0.00001;

	@Test
	public void testBeanToMapGeneration() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory();
	
		factory.classMap(Student.class, Map.class)
				.field("grade.letter", "letterGrade")
				.field("grade.point", "GPA")
				.field("grade.percentage", "gradePercentage")
				.field("name.first", "firstName")
				.field("name.last", "lastName")
				.byDefault()
				.register();
		
		MapperFacade mapper = factory.getMapperFacade();
		
		Student student = new Student();
		student.id = "1";
		student.email = "test@test.com";
		student.name = new Name();
		student.name.first = "Chuck";
		student.name.last = "Testa";
		student.grade = new Grade();
		student.grade.letter = "B-";
		student.grade.percentage = 81.5;
		student.grade.point = 2.7;
		
		
		@SuppressWarnings("unchecked")
        Map<String,Object> result = mapper.map(student, Map.class);
		
		Assert.assertEquals(student.id, result.get("id"));
		Assert.assertEquals(student.email, result.get("email"));
		Assert.assertEquals(student.name.first, result.get("firstName"));
		Assert.assertEquals(student.name.last, result.get("lastName"));
		Assert.assertEquals(student.grade.letter, result.get("letterGrade"));
		Assert.assertEquals(student.grade.percentage, result.get("gradePercentage"));
		Assert.assertEquals(student.grade.point, result.get("GPA"));
		
		Student mapBack = mapper.map(result, Student.class);
		
		Assert.assertEquals(student.id, mapBack.id);
        Assert.assertEquals(student.email, mapBack.email);
        Assert.assertEquals(student.name.first, mapBack.name.first);
        Assert.assertEquals(student.name.last, mapBack.name.last);
        Assert.assertEquals(student.grade.letter, mapBack.grade.letter);
        Assert.assertEquals(student.grade.percentage, mapBack.grade.percentage, DELTA);
        Assert.assertEquals(student.grade.point, mapBack.grade.point, DELTA);
		
	}
	
	@Test
    public void testBeanToCustomMapGeneration() throws Exception {
        
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        Type<Map<String, String>> mapType = new TypeBuilder<Map<String, String>>(){}.build();
        Type<Student> studentType = TypeFactory.valueOf(Student.class);
        
        factory.classMap(Student.class, mapType)
                .field("grade.letter", "letterGrade")
                .field("grade.point", "GPA")
                .field("grade.percentage", "gradePercentage")
                .field("name.first", "firstName")
                .field("name.last", "lastName")
                .byDefault()
                .register();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Student student = new Student();
        student.id = "1";
        student.email = "test@test.com";
        student.name = new Name();
        student.name.first = "Chuck";
        student.name.last = "Testa";
        student.grade = new Grade();
        student.grade.letter = "B-";
        student.grade.percentage = 81.5;
        student.grade.point = 2.7;
        
        Map<String, String> result = mapper.map(student, studentType, mapType);
        
        Assert.assertEquals(student.id, result.get("id"));
        Assert.assertEquals(student.email, result.get("email"));
        Assert.assertEquals(student.name.first, result.get("firstName"));
        Assert.assertEquals(student.name.last, result.get("lastName"));
        Assert.assertEquals(student.grade.letter, result.get("letterGrade"));
        Assert.assertEquals("" + student.grade.percentage, result.get("gradePercentage"));
        Assert.assertEquals("" + student.grade.point, result.get("GPA"));
        
        Student mapBack = mapper.map(result, mapType, studentType);
        
        Assert.assertEquals(student.id, mapBack.id);
        Assert.assertEquals(student.email, mapBack.email);
        Assert.assertEquals(student.name.first, mapBack.name.first);
        Assert.assertEquals(student.name.last, mapBack.name.last);
        Assert.assertEquals(student.grade.letter, mapBack.grade.letter);
        Assert.assertEquals(student.grade.percentage, mapBack.grade.percentage, DELTA);
        Assert.assertEquals(student.grade.point, mapBack.grade.point, DELTA);
        
    }
	
	@Test
	public void testResolveMapKeys() {
	    
	    PropertyResolverStrategy propertyResolver = UtilityResolver.getDefaultPropertyResolverStrategy();
	    Property namesFirst = propertyResolver.getProperty(PersonDto.class, "names['first']");
	    
	    Assert.assertNotNull(namesFirst);
	    Assert.assertEquals(TypeFactory.valueOf(String.class), namesFirst.getType());
	    Assert.assertNull(namesFirst.getContainer());
	    
	    VariableRef ref = new VariableRef(namesFirst, "destination");
	    Assert.assertEquals("((java.lang.String)((java.util.Map)destination.names).get(\"first\"))", ref.toString());
	}
	
	@Test
	public void testMapElementProperties() {
	    
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.classMap(Person.class, PersonDto.class)
                .field("name.first", "names['first']")
                .field("name.last", "names[\"last\"]")
                .register();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Person person = new Person();
        person.name = new Name();
        person.name.first = "Chuck";
        person.name.last = "Testa";
        
        PersonDto result = mapper.map(person, PersonDto.class);
        
        Assert.assertNotNull(result.names);
        Assert.assertEquals(person.name.first, result.names.get("first"));
        Assert.assertEquals(person.name.last, result.names.get("last"));
        
        Person mapBack = mapper.map(result, Person.class);
        
        Assert.assertNotNull(mapBack.name.first);
        Assert.assertEquals(person.name.first, mapBack.name.first);
        Assert.assertEquals(person.name.last, mapBack.name.last);
	        
	}
	
	@Test
    public void testNestedMapElement() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.classMap(Person.class, PersonDto2.class)
                .field("name.first", "names['self'].first")
                .field("name.last", "names['self'].last")
                .field("father.first", "names['father'].first")
                .field("father.last", "names['father'].last")
                .register();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Person person = new Person();
        person.name = new Name();
        person.name.first = "Chuck";
        person.name.last = "Testa";
        person.father = new Name();
        person.father.first = "Buck";
        person.father.last = "Testa";
        
        PersonDto2 result = mapper.map(person, PersonDto2.class);
        
        Assert.assertNotNull(result.names);
        Assert.assertEquals(person.name.first, result.names.get("self").first);
        Assert.assertEquals(person.name.last, result.names.get("self").last);
        Assert.assertEquals(person.father.first, result.names.get("father").first);
        Assert.assertEquals(person.father.last, result.names.get("father").last);
        
        Person mapBack = mapper.map(result, Person.class);
        
        Assert.assertNotNull(mapBack.name.first);
        Assert.assertEquals(person.name.first, mapBack.name.first);
        Assert.assertEquals(person.name.last, mapBack.name.last);
        Assert.assertEquals(person.father.first, mapBack.father.first);
        Assert.assertEquals(person.father.last, mapBack.father.last);
            
    }

    @Test
    public void testNestedMapElementWithIntegerKey() {


        MapperFactory factory = MappingUtil.getMapperFactory();

        Type<Map<Integer, String>> mapType = new TypeBuilder<Map<Integer, String>>(){}.build();
        Type<Name> nameType = TypeFactory.valueOf(Name.class);

        factory.classMap(Name.class, mapType)
                .field("first", "['0']")
                .field("last", "['42']")
                .byDefault()
                .register();

        MapperFacade mapper = factory.getMapperFacade();

        Name name = new Name();
        name.first = "Any First Name";
        name.last = "Any Last Name";

        Map<Integer, String> result = mapper.map(name, nameType, mapType);

        Assert.assertEquals(name.first, result.get(0));
        Assert.assertEquals(name.last, result.get(42));

        Name mapBack = mapper.map(result, mapType, nameType);

        Assert.assertEquals(name.first, mapBack.first);
        Assert.assertEquals(name.last, mapBack.last);
    }

    @Test
    public void testNestedMapElementWithEnumKey() {


        MapperFactory factory = MappingUtil.getMapperFactory();

        Type<Map<NameFields, String>> mapType = new TypeBuilder<Map<NameFields, String>>(){}.build();
        Type<Name> nameType = TypeFactory.valueOf(Name.class);

        factory.classMap(Name.class, mapType)
                .field("first", "['FIRST']")
                .field("last", "['LAST']")
                .byDefault()
                .register();

        MapperFacade mapper = factory.getMapperFacade();

        Name name = new Name();
        name.first = "Any First Name";
        name.last = "Any Last Name";

        Map<NameFields, String> result = mapper.map(name, nameType, mapType);

        Assert.assertEquals(name.first, result.get(NameFields.FIRST));
        Assert.assertEquals(name.last, result.get(NameFields.LAST));

        Name mapBack = mapper.map(result, mapType, nameType);

        Assert.assertEquals(name.first, mapBack.first);
        Assert.assertEquals(name.last, mapBack.last);
    }

	@Test
	public void testDoubleNestedMapElement() {

		MapperFactory factory = MappingUtil.getMapperFactory();

		factory.classMap(Person.class, PersonDto3.class)
				.field("name.first", "names['self']['first']")
				.field("name.last", "names['self']['last']")
				.field("father.first", "names['father']['first']")
				.field("father.last", "names['father']['last']")
				.register();

		MapperFacade mapper = factory.getMapperFacade();

		Person person = new Person();
		person.name = new Name();
		person.name.first = "Chuck";
		person.name.last = "Testa";
		person.father = new Name();
		person.father.first = "Buck";
		person.father.last = "Testa";

		PersonDto3 result = mapper.map(person, PersonDto3.class);

		Assert.assertNotNull(result.names);
		Assert.assertEquals(person.name.first, result.names.get("self").get("first"));
		Assert.assertEquals(person.name.last, result.names.get("self").get("last"));
		Assert.assertEquals(person.father.first, result.names.get("father").get("first"));
		Assert.assertEquals(person.father.last, result.names.get("father").get("last"));

		Person mapBack = mapper.map(result, Person.class);

		Assert.assertNotNull(mapBack.name.first);
		Assert.assertEquals(person.name.first, mapBack.name.first);
		Assert.assertEquals(person.name.last, mapBack.name.last);
		Assert.assertEquals(person.father.first, mapBack.father.first);
		Assert.assertEquals(person.father.last, mapBack.father.last);

	}

	@Test
	public void testMapWithGenericSuperclassImplementation() {

		MapperFactory factory = MappingUtil.getMapperFactory();

		factory.classMap(Person.class, PersonDto4.class)
				.field("name.first", "names['self'].first")
				.register();

		MapperFacade mapper = factory.getMapperFacade();

		Person person = new Person();
		person.name = new Name();
		person.name.first = "Chuck";

		PersonDto4 result = mapper.map(person, PersonDto4.class);

		Assert.assertNotNull(result.names);
		Assert.assertEquals(person.name.first, result.names.get("self").first);

		Person mapBack = mapper.map(result, Person.class);

		Assert.assertNotNull(mapBack.name.first);
		Assert.assertEquals(person.name.first, mapBack.name.first);
	}

    public static enum NameFields {
        FIRST, LAST
	}

    public static class Person {
	    public Name name;
	    public Name father;
	}
	
	public static class PersonDto {
	    public Map<String, String> names = new HashMap<>();
	}
	
	public static class PersonDto2 {
	    public Map<String, Name> names = new HashMap<>();
	}

	public static class PersonDto3 {
		public Map<String, Map<String, String>> names = new HashMap<>();
	}

	public static class PersonDto4 {
		public NameMap names = new NameMap();
	}
	
	public static class Student {
	    public Grade grade;
	    public String id;
	    public String email;
	    public Name name;
	}
	
	public static class Name {
	    public String first;
	    public String last;
	}
	
	public static class Grade {
		public double point;
		public double percentage;
		public String letter;
	}
	
	public static class NameMap extends HashMap<String, Name>{
		
	}

}
