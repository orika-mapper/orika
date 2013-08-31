/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
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

package ma.glasnost.orika.test.property;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.NestedProperty;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.property.IntrospectorPropertyResolver;
import ma.glasnost.orika.property.PropertyResolverStrategy;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.property.TestCaseClasses.A;
import ma.glasnost.orika.test.property.TestCaseClasses.B;
import ma.glasnost.orika.test.property.TestCaseClasses.Name;
import ma.glasnost.orika.test.property.TestCaseClasses.Student;

import org.junit.Test;

public class PropertyResolverTestCase {

    private PropertyResolverStrategy propertyResolver = new IntrospectorPropertyResolver();
    
	@Test
	public void testNestedProperty() {
		String np = "start.x";

		NestedProperty p = (NestedProperty) propertyResolver.getProperty(Line.class, np);
		Assert.assertEquals(Integer.TYPE, p.getRawType());
		p = (NestedProperty) propertyResolver.getProperty(Student.class, "favoriteBook.author.name");
	
		Assert.assertEquals(String.class, p.getRawType());
	}
	
	@Test
    public void testGetInvalidNestedProperty() {
        String np = "bogus.x";

        try {
            propertyResolver.getProperty(Line.class, np);
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("could not resolve nested property [" + np + "]"));
        }
    }

	@Test
	public void testBooleanMapping() {
		SpecialCase sc = new SpecialCase();
		sc.setChecked(true);
		sc.totalCost = new BigDecimal("42.50");
		
		MapperFacade mapper = MappingUtil.getMapperFactory().getMapperFacade();
		SpecialCaseDto dto = mapper.map(sc, SpecialCaseDto.class);
		
		Assert.assertEquals(sc.isChecked(), Boolean.valueOf(dto.isChecked()));
		//Assert.assertEquals(sc.totalCost.doubleValue(), dto.getTotalCost(), 0.01d);
	}
	
	@Test
	public void testOverridePropertyDefinition() {
	    
	    Map<String, Property> properties = propertyResolver.getProperties(PostalAddress.class);
	    Property city = properties.get("city");
	    
	    Assert.assertNotNull(city.getSetter());
	}
	
	@Test
    public void testAdHocResolution() {
	    
	    Property prop = propertyResolver.getProperty(A.class, "name:{readTheNameForThisBean|assignTheName}.firstName");
	    
	    Assert.assertNotNull(prop);
	    Assert.assertEquals("firstName", prop.getName());
	    Assert.assertEquals("name.firstName", prop.getExpression());
	    Assert.assertEquals(TypeFactory.valueOf(String.class), prop.getType());
	}
	
	
	@Test
    public void testAdHocResolution_withType() {
        
        Property prop = propertyResolver.getProperty(A.class, "name:{readTheNameForThisBean|assignTheName|type=ma.glasnost.orika.test.property.TestCaseClasses$Name}.firstName");
        
        Assert.assertNotNull(prop);
        Assert.assertEquals("firstName", prop.getName());
        Assert.assertEquals("name.firstName", prop.getExpression());
        Assert.assertEquals(TypeFactory.valueOf(String.class), prop.getType());
    }
	
	
	/**
	 * This test confirms that ad-hoc properties can be defined in-line within field mapping
	 * expressions
	 */
	@Test
    public void testAdHocResolution_integration() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        factory.registerClassMap(
                factory.classMap(A.class, B.class)
                    .field("name:{readTheNameForThisBean|assignTheName}.firstName", "givenName")
                    .field("name:{readTheNameForThisBean|assignTheName}.lastName", "sirName")
                    .field("address.city", "city")
                    .field("address.street", "street")
                    .field("address.postalCode", "postalCode")
                    .field("address.country", "country")
                );
        
        
        MapperFacade mapper = factory.getMapperFacade();
        
        A a = new A();
        Name name = new Name();
        name.setFirstName("Albert");
        name.setLastName("Einstein");
        a.assignTheName(name);
        ma.glasnost.orika.test.property.TestCaseClasses.Address address = new ma.glasnost.orika.test.property.TestCaseClasses.Address();
        address.city = "Somewhere";
        address.country = "Germany";
        address.postalCode = "A1234FG";
        address.street = "1234 Easy St.";
        a.setAddress(address);
        
        
        B b = mapper.map(a, B.class);
        
        Assert.assertNotNull(b);
        
        A mapBack = mapper.map(b, A.class);
        
        Assert.assertEquals(a, mapBack);
        
    }
    
    /**
     * This test case verifies that an ad-hoc property definition can be reused so
     * that it doesn't have to be repeated in subsequent lines
     */
    @Test
    public void testAdHocResolution_integration_reuseName() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        factory.registerClassMap(
                factory.classMap(A.class, B.class)
                    .field("name:{readTheNameForThisBean|assignTheName}.firstName", "givenName")
                    .field("name.lastName", "sirName")
                    .field("address.city", "city")
                    .field("address.street", "street")
                    .field("address.postalCode", "postalCode")
                    .field("address.country", "country")
                );
        
        
        MapperFacade mapper = factory.getMapperFacade();
        
        A a = new A();
        Name name = new Name();
        name.setFirstName("Albert");
        name.setLastName("Einstein");
        a.assignTheName(name);
        ma.glasnost.orika.test.property.TestCaseClasses.Address address = new ma.glasnost.orika.test.property.TestCaseClasses.Address();
        address.city = "Somewhere";
        address.country = "Germany";
        address.postalCode = "A1234FG";
        address.street = "1234 Easy St.";
        a.setAddress(address);
        
        
        B b = mapper.map(a, B.class);
        
        Assert.assertNotNull(b);
        
        A mapBack = mapper.map(b, A.class);
        
        Assert.assertEquals(a, mapBack);
        
    }
	 
    /**
     * This test case verifies that properties can be added through a programmatic
     * builder interface, explicitly defining the properties using the programming API.
     */
    @Test
    public void testAdHocResolution_integration_programmaticPropertyBuilder() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        ClassMapBuilder<Element,PersonDto> builder = factory.classMap(Element.class, PersonDto.class);
        
        {
            Property.Builder employment = 
                    Property.Builder.propertyFor(Element.class, "employment")
                        .type(Element.class)
                        .getter("getAttribute(\"employment\")")
                        .setter("setAttribute(\"employment\", %s)");
           
            
            builder.field(employment.nestedProperty("jobTitle")
                        .type(new TypeBuilder<List<String>>(){}.build())
                        .getter("getAttribute(\"jobTitle\")")
                        .setter("setAttribute(\"jobTitle\", %s)")
                    , "jobTitles");
           
            
            builder.field(employment.nestedProperty("salary")
                        .type(Long.class)
                        .getter("getAttribute(\"salary\")")
                        .setter("setAttribute(\"salary\", %s)")
                    , "salary");
            
            
            Property.Builder name = 
                    Property.Builder.propertyFor(Element.class, "name")
                        .type(Element.class)
                        .getter("getAttribute(\"name\")")
                        .setter("setAttribute(\"name\", %s)");
            
            
            builder.field(name.nestedProperty("first")
                        .type(String.class)
                        .getter("getAttribute(\"first\")")
                        .setter("setAttribute(\"first\", %s)")
                    , "firstName");
            
            builder.field(name.nestedProperty("last")
                        .type(String.class)
                        .getter("getAttribute(\"last\")")
                        .setter("setAttribute(\"last\", %s)")
                    , "lastName");
            
        }
        
        factory.registerClassMap(builder);
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Element person = new Element();
        Element employment = new Element();
        List<String> jobTitles = new ArrayList<String>();
        jobTitles.add("manager");
        jobTitles.add("executive");
        employment.setAttribute("jobTitle", jobTitles);
        employment.setAttribute("salary", 50000L);
        person.setAttribute("employment", employment);
        Element name = new Element();
        name.setAttribute("first", "Chuck");
        name.setAttribute("last", "Testa");
        person.setAttribute("name", name);
        
        PersonDto result = mapper.map(person, PersonDto.class);
        
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("first"), result.firstName);
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("last"), result.lastName);
        Assert.assertEquals(((Element)person.getAttribute("employment")).getAttribute("salary"), result.salary);
        Assert.assertEquals(((Element)person.getAttribute("employment")).getAttribute("jobTitle"), result.jobTitles);
    }
    
    /**
     * Demonstrates that you can declare properties in-line using a custom property descriptor format;
     * like so: "propertyName{getterName|setterName|type=the.type.Name}"
     * setter and type name are optional
     * although those arguments must be constants;
     */
    @Test
    public void testAdHocResolution_integration_declarativeProperties() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        {
            String employmentDef = "employment:{getAttribute(\"employment\")|setAttribute(\"employment\", %s)|type=ma.glasnost.orika.test.property.PropertyResolverTestCase$Element}";
            String jobTitleDef = "jobTitle:{getAttribute(\"jobTitle\")|setAttribute(\"jobTitle\", %s)|type=java.util.List}";
            String salaryDef = "salary:{getAttribute(\"salary\")|setAttribute(\"salary\", %s)|type=java.lang.Long}";
            
            String nameDef = "name:{getAttribute(\"name\")|setAttribute(\"name\",%s)|type=ma.glasnost.orika.test.property.PropertyResolverTestCase$Element}";
            String firstNameDef = "first:{getAttribute(\"first\")|setAttribute(\"first\", %s)|type=java.lang.String}";
            String lastNameDef = "last:{getAttribute(\"last\")|setAttribute(\"last\", %s)|type=java.lang.String}";
            
            factory.classMap(Element.class, PersonDto.class)
                .field(employmentDef + "." + jobTitleDef, "jobTitles")
                .field(employmentDef + "." + salaryDef, "salary")
                .field(nameDef + "." + firstNameDef, "firstName")
                .field(nameDef + "." + lastNameDef, "lastName")
                .register();
        }
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Element person = new Element();
        Element employment = new Element();
        List<String> jobTitles = new ArrayList<String>();
        jobTitles.add("manager");
        jobTitles.add("executive");
        employment.setAttribute("jobTitle", jobTitles);
        employment.setAttribute("salary", 50000L);
        person.setAttribute("employment", employment);
        Element name = new Element();
        name.setAttribute("first", "Chuck");
        name.setAttribute("last", "Testa");
        person.setAttribute("name", name);
        
        PersonDto result = mapper.map(person, PersonDto.class);
        
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("first"), result.firstName);
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("last"), result.lastName);
        Assert.assertEquals(((Element)person.getAttribute("employment")).getAttribute("salary"), result.salary);
        Assert.assertEquals(((Element)person.getAttribute("employment")).getAttribute("jobTitle"), result.jobTitles);
        
        Element mapBack = mapper.map(result, Element.class);
        
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("first"), ((Element)mapBack.getAttribute("name")).getAttribute("first"));
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("last"), ((Element)mapBack.getAttribute("name")).getAttribute("last"));
        Assert.assertEquals(((Element)person.getAttribute("employment")).getAttribute("salary"), ((Element)mapBack.getAttribute("employment")).getAttribute("salary"));
        
        List<?> original = (List<?>) ((Element)person.getAttribute("employment")).getAttribute("jobTitle");
        List<?> mapBackList = (List<?>) ((Element)mapBack.getAttribute("employment")).getAttribute("jobTitle");
        Assert.assertTrue(original.containsAll(mapBackList));
        Assert.assertTrue(mapBackList.containsAll(original));
    }
    
    /**
     * Demonstrates that you can specify the types as parameterized if necessary,
     * such as "List&lt;String&gt;" or "java.util.List&lt;java.lang.String&gt;"
     * 
     */
    @Test
    public void testAdHocResolution_integration_declarativePropertiesNestedTypes() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        {
            String employmentDef = "employment:{getAttribute(\"employment\")|setAttribute(\"employment\", %s)|type=ma.glasnost.orika.test.property.PropertyResolverTestCase$Element}";
            String jobTitleDef = "jobTitle:{getAttribute(\"jobTitle\")|setAttribute(\"jobTitle\", %s)|type=List<String>}";
            String salaryDef = "salary:{getAttribute(\"salary\")|setAttribute(\"salary\", %s)|type=java.lang.Long}";
            
            String nameDef = "name:{getAttribute(\"name\")|setAttribute(\"name\",%s)|type=ma.glasnost.orika.test.property.PropertyResolverTestCase$Element}";
            String firstNameDef = "first:{getAttribute(\"first\")|setAttribute(\"first\", %s)|type=java.lang.String}";
            String lastNameDef = "last:{getAttribute(\"last\")|setAttribute(\"last\", %s)|type=java.lang.String}";
            
            factory.classMap(Element.class, PersonDto.class)
                .field(employmentDef + "." + jobTitleDef, "jobTitles")
                .field("employment." + salaryDef, "salary") // reuse the in-line declaration of 'employment' property
                .field(nameDef + "." + firstNameDef, "firstName")
                .field("name." + lastNameDef, "lastName") // reuses the in-line declaration of 'name' property
                .register();
        }
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Element person = new Element();
        Element employment = new Element();
        List<String> jobTitles = new ArrayList<String>();
        jobTitles.add("manager");
        jobTitles.add("executive");
        employment.setAttribute("jobTitle", jobTitles);
        employment.setAttribute("salary", 50000L);
        person.setAttribute("employment", employment);
        Element name = new Element();
        name.setAttribute("first", "Chuck");
        name.setAttribute("last", "Testa");
        person.setAttribute("name", name);
        
        PersonDto result = mapper.map(person, PersonDto.class);
        
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("first"), result.firstName);
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("last"), result.lastName);
        Assert.assertEquals(((Element)person.getAttribute("employment")).getAttribute("salary"), result.salary);
        Assert.assertEquals(((Element)person.getAttribute("employment")).getAttribute("jobTitle"), result.jobTitles);
        
        Element mapBack = mapper.map(result, Element.class);
        
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("first"), ((Element)mapBack.getAttribute("name")).getAttribute("first"));
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("last"), ((Element)mapBack.getAttribute("name")).getAttribute("last"));
        Assert.assertEquals(((Element)person.getAttribute("employment")).getAttribute("salary"), ((Element)mapBack.getAttribute("employment")).getAttribute("salary"));
        
        List<?> original = (List<?>) ((Element)person.getAttribute("employment")).getAttribute("jobTitle");
        List<?> mapBackList = (List<?>) ((Element)mapBack.getAttribute("employment")).getAttribute("jobTitle");
        Assert.assertTrue(original.containsAll(mapBackList));
        Assert.assertTrue(mapBackList.containsAll(original));
        
    }
    
    /**
     * Demonstrates that you can specify the types as parameterized if necessary,
     * such as "List&lt;String&gt;" or "java.util.List&lt;java.lang.String&gt;",
     * and that single quotes can be used instead of double quotes for convenience.
     */
    @Test
    public void testAdHocResolution_integration_declarativePropertiesNestedTypesSingleQuote() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        {
            String employmentDef = "employment:{getAttribute('employment')|setAttribute('employment', %s)|type=ma.glasnost.orika.test.property.PropertyResolverTestCase.Element}";
            String jobTitleDef = "jobTitle:{getAttribute(\"job's Title\")|setAttribute(\"job's Title\", %s)|type=List<String>}";
            String salaryDef = "salary:{getAttribute(\"'salary'\")|setAttribute(\"'salary'\", %s)|type=java.lang.Long}";
            
            String nameDef = "name:{getAttribute('name')|setAttribute('name',%s)|type=ma.glasnost.orika.test.property.PropertyResolverTestCase.Element}";
            String firstNameDef = "first:{getAttribute('first')|setAttribute('first', %s)|type=java.lang.String}";
            String lastNameDef = "last:{getAttribute('last')|setAttribute('last', %s)|type=java.lang.String}";
            
            factory.classMap(Element.class, PersonDto.class)
                .field(employmentDef + "." + jobTitleDef, "jobTitles")
                .field("employment." + salaryDef, "salary") // reuse the in-line declaration of 'employment' property
                .field(nameDef + "." + firstNameDef, "firstName")
                .field("name." + lastNameDef, "lastName") // reuses the in-line declaration of 'name' property
                .register();
        }
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Element person = new Element();
        Element employment = new Element();
        List<String> jobTitles = new ArrayList<String>();
        jobTitles.add("manager");
        jobTitles.add("executive");
        employment.setAttribute("job's Title", jobTitles);
        employment.setAttribute("'salary'", 50000L);
        person.setAttribute("employment", employment);
        Element name = new Element();
        name.setAttribute("first", "Chuck");
        name.setAttribute("last", "Testa");
        person.setAttribute("name", name);
        
        PersonDto result = mapper.map(person, PersonDto.class);
        
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("first"), result.firstName);
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("last"), result.lastName);
        Assert.assertEquals(((Element)person.getAttribute("employment")).getAttribute("'salary'"), result.salary);
        Assert.assertEquals(((Element)person.getAttribute("employment")).getAttribute("job's Title"), result.jobTitles);
        
        Element mapBack = mapper.map(result, Element.class);
        
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("first"), ((Element)mapBack.getAttribute("name")).getAttribute("first"));
        Assert.assertEquals(((Element)person.getAttribute("name")).getAttribute("last"), ((Element)mapBack.getAttribute("name")).getAttribute("last"));
        Assert.assertEquals(((Element)person.getAttribute("employment")).getAttribute("'salary'"), ((Element)mapBack.getAttribute("employment")).getAttribute("'salary'"));
        
        List<?> original = (List<?>) ((Element)person.getAttribute("employment")).getAttribute("job's Title");
        List<?> mapBackList = (List<?>) ((Element)mapBack.getAttribute("employment")).getAttribute("job's Title");
        Assert.assertTrue(original.containsAll(mapBackList));
        Assert.assertTrue(mapBackList.containsAll(original));
        
    }
    
    public static class Element {
        
        Map<String,Object> attributes = new HashMap<String,Object>();
        
        public Object getAttribute(String name) {
            return attributes.get(name);
        }
        
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }
    }
    
    public static class PersonDto {
        public String firstName;
        public String lastName;
        public List<String> jobTitles;
        public long salary;
    }
    
	public static class Point {
		private int x, y;

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}
	}

	public static class Line {
		private Point start;
		private Point end;

		public Point getStart() {
			return start;
		}

		public void setStart(Point start) {
			this.start = start;
		}

		public Point getEnd() {
			return end;
		}

		public void setEnd(Point end) {
			this.end = end;
		}
	}

	public static class LineDTO {
		private int x0, y0, x1, y1;

		public int getX0() {
			return x0;
		}

		public void setX0(int x0) {
			this.x0 = x0;
		}

		public int getY0() {
			return y0;
		}

		public void setY0(int y0) {
			this.y0 = y0;
		}

		public int getX1() {
			return x1;
		}

		public void setX1(int x1) {
			this.x1 = x1;
		}

		public int getY1() {
			return y1;
		}

		public void setY1(int y1) {
			this.y1 = y1;
		}

	}
	
	public static class SpecialCase {
		
		private Boolean checked;
		public BigDecimal totalCost;
		
		
		public Boolean isChecked() {
        	return checked;
        }

		public void setChecked(Boolean checked) {
        	this.checked = checked;
        }
	}
	
	public static class SpecialCaseDto {
		
		private boolean checked;
		private double totalCost;
		
		public boolean isChecked() {
        	return checked;
        }
		public void setChecked(boolean checked) {
        	this.checked = checked;
        }
		public double getTotalCost() {
        	return totalCost;
        }
		public void setTotalCost(double totalCost) {
        	this.totalCost = totalCost;
        }
	}
	
	public static interface Address {
        public String getStreet();
        public String getCity();
        public String getSubnational();
        public String getPostalCode();
        public String getCountry();
    }
	
	public static class PostalAddress implements Address {
	    
	    private String street;
	    private String city;
	    private String subnational;
	    private String postalCode;
	    private String country;
        public String getStreet() {
            return street;
        }
        public void setStreet(String street) {
            this.street = street;
        }
        public String getCity() {
            return city;
        }
        public void setCity(String city) {
            this.city = city;
        }
        public String getSubnational() {
            return subnational;
        }
        public void setSubnational(String subnational) {
            this.subnational = subnational;
        }
        public String getPostalCode() {
            return postalCode;
        }
        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }
        public String getCountry() {
            return country;
        }
        public void setCountry(String country) {
            this.country = country;
        }
	}
	

}
