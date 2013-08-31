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

import static ma.glasnost.orika.metadata.MappingDirection.A_TO_B;
import static ma.glasnost.orika.metadata.MappingDirection.B_TO_A;
import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.CaseInsensitiveClassMapBuilder;
import ma.glasnost.orika.metadata.MappingDirection;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 * 
 */
public class ClassMapBuilderTest {
    
    public static class Source {
        public final String lastName;
        public final String firstName;
        public final Integer age;
        public final SourceName name;
        
        public Source(String firstName, String lastName, Integer age, SourceName name) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.name = name;
        }
    }
    
    public static class Source2 {
        public String lastName;
        public String firstName;
        public Integer age;
        public SourceName name;
    }
    
    public static class SourceName {
        public String first;
        public String last;
    }
    
    public static class Destination {
        public String lastName;
        public String firstName;
        public Integer age;
        public DestinationName name;
    }
    
    public static class DestinationName {
        public String first;
        public String last;
    }
    
    @Test
    public void byDefault() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder()
            .classMapBuilderFactory(new CaseInsensitiveClassMapBuilder.Factory()).build();
        
        factory.classMap(Source.class, Destination.class).byDefault().register();

        MapperFacade mapper = factory.getMapperFacade();
        
        Source s = new Source("Joe", "Smith", 25, null);
        Destination d = mapper.map(s, Destination.class);
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertEquals(s.firstName, d.firstName);
        Assert.assertEquals(s.lastName, d.lastName);
        Assert.assertEquals(s.age, d.age);
    }
    
    @Test
    public void byDefault_AtoB_final() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder()
            .classMapBuilderFactory(new CaseInsensitiveClassMapBuilder.Factory()).build();
        
        factory.classMap(Source.class, Destination.class)
            .byDefault(A_TO_B)
            .register();
        

        MapperFacade mapper = factory.getMapperFacade();
        
        SourceName name = new SourceName();
        name.first = "Joe";
        name.last = "Smith";
        
        Source s = new Source("Joe", "Smith", 25, name);
        
        
        Destination d = mapper.map(s, Destination.class);
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertEquals(s.firstName, d.firstName);
        Assert.assertEquals(s.lastName, d.lastName);
        Assert.assertEquals(s.age, d.age);
        Assert.assertEquals(s.name.first, d.name.first);
        Assert.assertEquals(s.name.last, d.name.last);
    }
    
    @Test
    public void byDefault_AtoB() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder()
            .classMapBuilderFactory(new CaseInsensitiveClassMapBuilder.Factory()).build();
        
        factory.classMap(Source2.class, Destination.class)
            .byDefault(A_TO_B)
            .register();
        

        MapperFacade mapper = factory.getMapperFacade();
        
        SourceName name = new SourceName();
        name.first = "Joe";
        name.last = "Smith";
        
        Source2 s = new Source2();
        s.firstName = "Joe";
        s.lastName = "Smith";
        s.age = 25;
        s.name = name;
        
        Destination d = mapper.map(s, Destination.class);
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertEquals(s.firstName, d.firstName);
        Assert.assertEquals(s.lastName, d.lastName);
        Assert.assertEquals(s.age, d.age);
        Assert.assertEquals(s.name.first, d.name.first);
        Assert.assertEquals(s.name.last, d.name.last);
        
        /*
         * Check that byDefault was only in one direction
         */
        Source2 mapBack = mapper.map(d, Source2.class);
        Assert.assertNull(mapBack.firstName);
        Assert.assertNull(mapBack.lastName);
        Assert.assertNull(mapBack.age);
        Assert.assertNull(mapBack.name);
    }
    
    @Test
    public void byDefault_BtoA_final() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder()
            .classMapBuilderFactory(new CaseInsensitiveClassMapBuilder.Factory()).build();
        
        factory.classMap(Destination.class, Source.class)
            .byDefault(B_TO_A)
            .register();
        

        MapperFacade mapper = factory.getMapperFacade();
        
        SourceName name = new SourceName();
        name.first = "Joe";
        name.last = "Smith";
        
        Source s = new Source("Joe", "Smith", 25, name);
        
        
        Destination d = mapper.map(s, Destination.class);
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertEquals(s.firstName, d.firstName);
        Assert.assertEquals(s.lastName, d.lastName);
        Assert.assertEquals(s.age, d.age);
        Assert.assertEquals(s.name.first, d.name.first);
        Assert.assertEquals(s.name.last, d.name.last);
    }
    
    @Test
    public void byDefault_BtoA() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder()
            .classMapBuilderFactory(new CaseInsensitiveClassMapBuilder.Factory()).build();
        
        factory.classMap(Destination.class, Source2.class)
            .byDefault(B_TO_A)
            .register();
        

        MapperFacade mapper = factory.getMapperFacade();
        
        SourceName name = new SourceName();
        name.first = "Joe";
        name.last = "Smith";
        
        Source2 s = new Source2();
        s.firstName = "Joe";
        s.lastName = "Smith";
        s.age = 25;
        s.name = name;
        
        
        Destination d = mapper.map(s, Destination.class);
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertEquals(s.firstName, d.firstName);
        Assert.assertEquals(s.lastName, d.lastName);
        Assert.assertEquals(s.age, d.age);
        Assert.assertEquals(s.name.first, d.name.first);
        Assert.assertEquals(s.name.last, d.name.last);
        /*
         * Check that byDefault was only in one direction
         */
        Source2 mapBack = mapper.map(d, Source2.class);
        Assert.assertNull(mapBack.firstName);
        Assert.assertNull(mapBack.lastName);
        Assert.assertNull(mapBack.age);
        Assert.assertNull(mapBack.name);
    }
}
