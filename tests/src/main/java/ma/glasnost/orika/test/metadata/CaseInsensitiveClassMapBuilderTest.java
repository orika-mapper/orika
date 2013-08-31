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

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.CaseInsensitiveClassMapBuilder;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 * 
 */
public class CaseInsensitiveClassMapBuilderTest {
    
    public static class Source {
        public String lastNAME;
        public String firstName;
        public Integer age;
        public SourceName NaMe;
    }
    
    public static class SourceName {
        public String FIRST;
        public String LAST;
    }
    
    public static class Destination {
        public String LastName;
        public String fIrStNaMe;
        public Integer AGE;
        public DestinationName nAme;
    }
    
    public static class DestinationName {
        public String fIrSt;
        public String LaSt;
    }
    
    @Test
    public void byDefault() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder()
            .classMapBuilderFactory(new CaseInsensitiveClassMapBuilder.Factory()).build();
        
        factory.classMap(Source.class, Destination.class).byDefault().register();

        MapperFacade mapper = factory.getMapperFacade();
        
        Source s = new Source();
        s.lastNAME = "Smith";
        s.firstName = "Joe";
        s.age = 25;
        
        Destination d = mapper.map(s, Destination.class);
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertEquals(s.firstName, d.fIrStNaMe);
        Assert.assertEquals(s.lastNAME, d.LastName);
        Assert.assertEquals(s.age, d.AGE);
    }
    
    @Test
    public void fieldMap_withoutNestedProperties() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder()
            .classMapBuilderFactory(new CaseInsensitiveClassMapBuilder.Factory()).build();
        
        factory.classMap(Source.class, Destination.class)
            .field("FIRSTname", "FIRSTname")
            .field("lastNAME", "lastNAME")
            .field("aGE", "aGE")
            .register();
        

        MapperFacade mapper = factory.getMapperFacade();
        
        Source s = new Source();
        s.lastNAME = "Smith";
        s.firstName = "Joe";
        s.age = 25;
        
        Destination d = mapper.map(s, Destination.class);
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertEquals(s.firstName, d.fIrStNaMe);
        Assert.assertEquals(s.lastNAME, d.LastName);
        Assert.assertEquals(s.age, d.AGE);
    }
    
    @Test
    public void fieldMap_withNestedProperties() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder()
            .classMapBuilderFactory(new CaseInsensitiveClassMapBuilder.Factory()).build();
        
        factory.classMap(Source.class, Destination.class)
            .field("FIRSTname", "FIRSTname")
            .field("lastNAME", "lastNAME")
            .field("aGE", "aGE")
            .field("name.first", "name.first")
            .field("name.last", "name.last")
            .register();
        

        MapperFacade mapper = factory.getMapperFacade();
        
        Source s = new Source();
        s.lastNAME = "Smith";
        s.firstName = "Joe";
        s.age = 25;
        s.NaMe = new SourceName();
        s.NaMe.FIRST = "Joe";
        s.NaMe.LAST = "Smith";
        
        Destination d = mapper.map(s, Destination.class);
        /*
         * Check that properties we expect were mapped
         */
        Assert.assertEquals(s.firstName, d.fIrStNaMe);
        Assert.assertEquals(s.lastNAME, d.LastName);
        Assert.assertEquals(s.age, d.AGE);
        Assert.assertEquals(s.NaMe.FIRST, d.nAme.fIrSt);
        Assert.assertEquals(s.NaMe.LAST, d.nAme.LaSt);
    }
}
