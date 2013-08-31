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
package ma.glasnost.orika.test.community;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class Issue52bTestCase {
    
    @Test
    public void parentBeforeChild() {

        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        MapperFacade mapper = factory.getMapperFacade();

        A source = new A();
        source.field1 = "one";
        source.field2 = "two";
        
        B dest = mapper.map(source, B.class);
        
        Assert.assertEquals(source.field1, dest.field1);
        Assert.assertEquals(source.field2, dest.field2);

        A1 source2 = new A1();
        source2.field1 = "one";
        source2.field2 = "two";
        source2.field3 = "three";
        
        dest = mapper.map(source2, B.class);
        
        Assert.assertEquals(source2.field1, dest.field1);
        Assert.assertEquals(source2.field2, dest.field2);
        Assert.assertEquals(source2.field3, dest.field3);
        
    }
    
    @Test
    public void childBeforeParent() {

        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        MapperFacade mapper = factory.getMapperFacade();

        
        A1 source2 = new A1();
        source2.field1 = "one";
        source2.field2 = "two";
        source2.field3 = "three";
        
       
        B dest = mapper.map(source2, B.class);
        
        Assert.assertEquals(source2.field1, dest.field1);
        Assert.assertEquals(source2.field2, dest.field2);
        Assert.assertEquals(source2.field3, dest.field3);
        
        A source = new A();
        source.field1 = "one";
        source.field2 = "two";
        
        dest = mapper.map(source, B.class);
        
        Assert.assertEquals(source.field1, dest.field1);
        Assert.assertEquals(source.field2, dest.field2);

    }
    
    public static class A {
        public String field1;
        public String field2;
    }
    
    public static class A1 extends A {
        public String field3;
    }
    
    public static class B {
        public String field1;
        public String field2;
        public String field3;
    }
    
}

