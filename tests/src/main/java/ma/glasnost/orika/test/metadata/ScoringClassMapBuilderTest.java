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
package ma.glasnost.orika.test.metadata;

import static java.util.Arrays.asList;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.ScoringClassMapBuilder;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 * 
 */
public class ScoringClassMapBuilderTest {
    public static class Name {
        public String first;
        public String middle;
        public String last;
    }
    
    public static class Source {
        public String lastName;
        public Integer age;
        public PostalAddress postalAddress;
        public String firstName;
        public String stateOfBirth;
        public String eyeColor;
        public String driversLicenseNumber;
    }
    
    public static class Destination {
        public Name name;
        public Integer currentAge;
        public String streetAddress;
        public String birthState;
        public String countryCode;
        public String favoriteColor;
        public String id;
    }
    
    public static class PostalAddress {
        public String street;
        public String city;
        public String state;
        public String postalCode;
        public Country country;
    }
    
    public static class Country {
        public String name;
        public String alphaCode;
        public int numericCode;
    }
    
    @Test
    public void testClassMapBuilderExtension() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder().classMapBuilderFactory(new ScoringClassMapBuilder.Factory()).build();
        
        ClassMap<Source, Destination> map = factory.classMap(Source.class, Destination.class).byDefault().toClassMap();
        Map<String, String> mapping = new HashMap<String, String>();
        for (FieldMap f : map.getFieldsMapping()) {
            mapping.put(f.getSource().getExpression(), f.getDestination().getExpression());
        }
        
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertEquals("name.first", mapping.get("firstName"));
        Assert.assertEquals("name.last", mapping.get("lastName"));
        Assert.assertEquals("streetAddress", mapping.get("postalAddress.street"));
        Assert.assertEquals("countryCode", mapping.get("postalAddress.country.alphaCode"));
        Assert.assertEquals("currentAge", mapping.get("age"));
        Assert.assertEquals("birthState", mapping.get("stateOfBirth"));
        
        /*
         * Check that properties that we don't expect aren't mapped by accident
         */
        Assert.assertFalse(mapping.containsKey("driversLicenseNumber"));
        Assert.assertFalse(mapping.containsKey("eyeColor"));
        
        
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSplittingWords() throws Throwable {
        Map<String, List<List<String>>> tests = new HashMap<String, List<List<String>>>() {
            private static final long serialVersionUID = 1L;
            {
                put("lowercase", asList(asList("lowercase")));
                put("Class", asList(asList("class")));
                put("MyClass", asList(asList("my", "class")));
                put("HTML", asList(asList("html")));
                put("PDFLoader", asList(asList("pdf", "loader")));
                put("AString", asList(asList("a", "string")));
                put("SimpleXMLParser", asList(asList("Simple", "xml", "parser")));
                put("GL11Version", asList(asList("gl", "11", "version")));
                put("99Bottles", asList(asList("99", "bottles")));
                put("May5", asList(asList("may", "5")));
                put("BFG9000", asList(asList("bfg", "9000")));
                put("SimpleXMLParser", asList(asList("simple", "xml", "parser")));
                put("postalAddress.country", asList(asList("postal", "address"), asList("country")));
                put("aVeryLongWord.name.first", asList(asList("a", "very", "long", "word"), asList("name"), asList("first")));
            }
        };
        
        Method splitIntoWords = ScoringClassMapBuilder.FieldMatchScore.class.getDeclaredMethod("splitIntoLowerCaseWords", String.class);
        splitIntoWords.setAccessible(true);
        
        for (Entry<String, List<List<String>>> test : tests.entrySet()) {
            
            List<List<String>> testValue = test.getValue();
            List<List<String>> result = (List<List<String>>)splitIntoWords.invoke(null, test.getKey());
            Assert.assertEquals(testValue.size(), result.size());
            for (int i=0, len = testValue.size(); i < len; ++i) {
                Assert.assertEquals(testValue.get(i), result.get(i));
            }
        }
        
    }
    
}
