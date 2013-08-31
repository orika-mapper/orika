package ma.glasnost.orika.test.generator;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class BeanToArrayGenerationTestCase {

	@Test
	public void testBeanToArrayGeneration() throws Exception {
		
		
	    MapperFactory factory = MappingUtil.getMapperFactory();
	    
        factory.classMap(Student.class, Object[].class)
                .field("grade.letter", "0")
                .field("grade.point", "1")
                .field("grade.percentage", "2")
                .field("name.first", "3")
                .field("name.last", "4")
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
        
        Object[] result = mapper.map(student, Object[].class);
        
        int index = -1;
        Assert.assertEquals(student.grade.letter, result[++index]);
        Assert.assertEquals(student.grade.point, result[++index]);
        Assert.assertEquals(student.grade.percentage, result[++index]);
        Assert.assertEquals(student.name.first, result[++index]);
        Assert.assertEquals(student.name.last, result[++index]);
        Assert.assertEquals(student.id, result[++index]);
        Assert.assertEquals(student.email, result[++index]);
        
        
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
    public void testBeanToStringArrayGeneration() throws Exception {
        
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.classMap(Student.class, String[].class)
                .field("grade.letter", "0")
                .field("grade.point", "1")
                .field("grade.percentage", "2")
                .field("name.first", "3")
                .field("name.last", "4")
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
        
        String[] result = mapper.map(student, String[].class);
        
        int index = -1;
        Assert.assertEquals(student.grade.letter, result[++index]);
        Assert.assertEquals("" + student.grade.point, result[++index]);
        Assert.assertEquals("" + student.grade.percentage, result[++index]);
        Assert.assertEquals(student.name.first, result[++index]);
        Assert.assertEquals(student.name.last, result[++index]);
        Assert.assertEquals(student.id, result[++index]);
        Assert.assertEquals(student.email, result[++index]);
        
        
        Student mapBack = mapper.map(result, Student.class);
        
        Assert.assertEquals(student.id, mapBack.id);
        Assert.assertEquals(student.email, mapBack.email);
        Assert.assertEquals(student.name.first, mapBack.name.first);
        Assert.assertEquals(student.name.last, mapBack.name.last);
        Assert.assertEquals(student.grade.letter, mapBack.grade.letter);
        Assert.assertEquals(student.grade.percentage, mapBack.grade.percentage);
        Assert.assertEquals(student.grade.point, mapBack.grade.point);
        
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
