package ma.glasnost.orika.test.generator;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
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

import org.junit.Test;

public class BeanToMapGenerationTestCase {

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
        Assert.assertEquals(student.grade.percentage, mapBack.grade.percentage);
        Assert.assertEquals(student.grade.point, mapBack.grade.point);
		
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
        Assert.assertEquals(student.grade.percentage, mapBack.grade.percentage);
        Assert.assertEquals(student.grade.point, mapBack.grade.point);
        
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
	
	public static class Person {
	    public Name name;
	    public Name father;
	}
	
	public static class PersonDto {
	    public Map<String, String> names = new HashMap<String, String>();
	}
	
	public static class PersonDto2 {
	    public Map<String, Name> names = new HashMap<String, Name>();
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

}
