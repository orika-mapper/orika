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
package ma.glasnost.orika.test.extensibility;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.property.IntrospectorPropertyResolver;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 * 
 */
public class PropertyResolverExtensibilityTestCase {
    
    /**
     * For my next trick, this is a custom Property Resolver which automatically
     * attempts a dynamic definition of type Element (after normal resolution
     * has failed)
     * 
     * @author matt.deboer@gmail.com
     * 
     */
    public static class ElementPropertyResolver extends IntrospectorPropertyResolver {
        
        protected Property getProperty(java.lang.reflect.Type type, String expr, boolean isNestedLookup, Property owner)
                throws MappingException {
            Property property = null;
            try {
                property = super.getProperty(type, expr, isNestedLookup, null);
            } catch (MappingException e) {
                try {
                    property = super.resolveInlineProperty(type, expr + ":{getAttribute(\"" + expr + "\")|setAttribute(\"" + expr
                            + "\",%s)|type=" + (isNestedLookup ? Element.class.getName() : "Object") + "}");
                } catch (MappingException e2) {
                    throw e; // throw the original exception
                }
            }
            return property;
        }
    }
    
    /**
     * This test demonstrates how you might implement a custom property resolver
     * which provides a proprietary definition of properties
     */
    @Test
    public void testAdHocResolution_integration_customResolverUsingDeclarativeProperties() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder().propertyResolverStrategy(new ElementPropertyResolver()).build();
        
        factory.classMap(Element.class, Person.class)
                .field("employment.jobTitle", "jobTitle")
                .field("employment.salary", "salary")
                .field("name.first", "firstName")
                .field("name.last", "lastName")
                .register();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Element person = new Element();
        Element employment = new Element();
        employment.setAttribute("jobTitle", "manager");
        employment.setAttribute("salary", 50000L);
        person.setAttribute("employment", employment);
        Element name = new Element();
        name.setAttribute("first", "Chuck");
        name.setAttribute("last", "Testa");
        person.setAttribute("name", name);
        
        Person result = mapper.map(person, Person.class);
        
        Assert.assertEquals(((Element) person.getAttribute("name")).getAttribute("first"), result.firstName);
        Assert.assertEquals(((Element) person.getAttribute("name")).getAttribute("last"), result.lastName);
        Assert.assertEquals(((Element) person.getAttribute("employment")).getAttribute("salary") + "", result.salary);
        Assert.assertEquals(((Element) person.getAttribute("employment")).getAttribute("jobTitle"), result.jobTitle);
        
        Element mapBack = mapper.map(result, Element.class);
        
        Assert.assertEquals(((Element) person.getAttribute("name")).getAttribute("first"),
                ((Element) mapBack.getAttribute("name")).getAttribute("first"));
        Assert.assertEquals(((Element) person.getAttribute("name")).getAttribute("last"),
                ((Element) mapBack.getAttribute("name")).getAttribute("last"));
        Assert.assertEquals(((Element) person.getAttribute("employment")).getAttribute("salary") + "",
                ((Element) mapBack.getAttribute("employment")).getAttribute("salary"));
        Assert.assertEquals(((Element) person.getAttribute("employment")).getAttribute("jobTitle"),
                ((Element) mapBack.getAttribute("employment")).getAttribute("jobTitle"));
        
    }
    
    public static class Element {
        
        Map<String, Object> attributes = new HashMap<String, Object>();
        
        public Object getAttribute(String name) {
            return attributes.get(name);
        }
        
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }
    }
    
    public static class Person {
        public String firstName;
        public String lastName;
        public String jobTitle;
        public String salary;
    }
    
}
