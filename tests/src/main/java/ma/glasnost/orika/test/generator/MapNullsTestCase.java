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
package ma.glasnost.orika.test.generator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class MapNullsTestCase {
    
    public enum Position {
        FIRST,
        LAST;
    }
    
    public static class Container {
        public long longValue;
        public String stringValue;
        public List<String> listOfString;
        public String[] arrayOfString;
        public int[] arrayOfInt;
        public Map<String, Object> map;
        public Position enumValue;
    }
    
    public static class Container2 {
        public long longValue;
        public String stringValue;
        public List<String> listOfString;
        public String[] arrayOfString;
        public int[] arrayOfInt;
        public Map<String, Object> map;
        public Position enumValue;
    }
    
    @Test
    public void mapNulls_False() {

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().mapNulls(false).build();
        
        Container a = new Container();
        Container b = new Container();

        b.longValue = 1L;
        b.stringValue = "TEST A";
        b.arrayOfString = new String[]{"a", "b", "c"};
        b.arrayOfInt = new int[] {1,2,3};
        b.listOfString = Arrays.asList("l1","l2");
        b.map = Collections.singletonMap("key", (Object)"value");
        b.enumValue = Position.FIRST;
        
        mapperFactory.getMapperFacade().map(a, b);

        Assert.assertNotNull( b.stringValue );
        Assert.assertNotNull( b.arrayOfString );
        Assert.assertNotNull( b.arrayOfInt );
        Assert.assertNotNull( b.listOfString );
        Assert.assertNotNull( b.map );
        Assert.assertNotNull( b.enumValue );
    }
    
    @Test
    public void mapNulls_True() {

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().mapNulls(true).build();
        
        Container a = new Container();
        Container b = new Container();

        b.longValue = 1L;
        b.stringValue = "TEST A";
        b.arrayOfString = new String[]{"a", "b", "c"};
        b.arrayOfInt = new int[] {1,2,3};
        b.listOfString = Arrays.asList("l1","l2");
        b.map = Collections.singletonMap("key", (Object)"value");
        b.enumValue = Position.FIRST;
        
        mapperFactory.getMapperFacade().map(a, b);

        Assert.assertNull( b.stringValue );
        Assert.assertNull( b.arrayOfString );
        Assert.assertNull( b.arrayOfInt );
        Assert.assertNull( b.listOfString );
        Assert.assertNull( b.map );
        Assert.assertNull( b.enumValue );
    }
    
    @Test
    public void mapNulls_True_ClassLevel() {

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().mapNulls(false).build();
        
        mapperFactory.classMap(Container.class, Container2.class)
            .mapNulls(true).byDefault().register();
        
        Container a = new Container();
        Container2 b = new Container2();

        b.longValue = 1L;
        b.stringValue = "TEST A";
        b.arrayOfString = new String[]{"a", "b", "c"};
        b.arrayOfInt = new int[] {1,2,3};
        b.listOfString = Arrays.asList("l1","l2");
        b.map = Collections.singletonMap("key", (Object)"value");
        b.enumValue = Position.FIRST;
        
        mapperFactory.getMapperFacade().map(a, b);

        Assert.assertNull( b.stringValue );
        Assert.assertNull( b.arrayOfString );
        Assert.assertNull( b.arrayOfInt );
        Assert.assertNull( b.listOfString );
        Assert.assertNull( b.map );
        Assert.assertNull( b.enumValue );
        
        
    }
    
    @Test
    public void mapNulls_False_ClassLevel() {

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().mapNulls(true).build();
        
        mapperFactory.classMap(Container.class, Container2.class)
        .mapNulls(false).byDefault().register();
    
        Container a = new Container();
        Container2 b = new Container2();

        b.longValue = 1L;
        b.stringValue = "TEST A";
        b.arrayOfString = new String[]{"a", "b", "c"};
        b.arrayOfInt = new int[] {1,2,3};
        b.listOfString = Arrays.asList("l1","l2");
        b.map = Collections.singletonMap("key", (Object)"value");
        b.enumValue = Position.FIRST;
        
        mapperFactory.getMapperFacade().map(a, b);

        Assert.assertNotNull( b.stringValue );
        Assert.assertNotNull( b.arrayOfString );
        Assert.assertNotNull( b.arrayOfInt );
        Assert.assertNotNull( b.listOfString );
        Assert.assertNotNull( b.map );
        Assert.assertNotNull( b.enumValue );
    }
    
    @Test
    public void mapNulls_FieldLevel() {

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().mapNulls(false).build();
        
        mapperFactory.classMap(Container.class, Container2.class)
            .mapNulls(false)
            .fieldMap("arrayOfString").mapNulls(true).add()
            .byDefault().register();
    
        Container a = new Container();
        Container2 b = new Container2();

        b.longValue = 1L;
        b.stringValue = "TEST A";
        b.arrayOfString = new String[]{"a", "b", "c"};
        b.arrayOfInt = new int[] {1,2,3};
        b.listOfString = Arrays.asList("l1","l2");
        b.map = Collections.singletonMap("key", (Object)"value");
        b.enumValue = Position.FIRST;
        
        mapperFactory.getMapperFacade().map(a, b);

        Assert.assertNotNull( b.stringValue );
        Assert.assertNull( b.arrayOfString );
        Assert.assertNotNull( b.arrayOfInt );
        Assert.assertNotNull( b.listOfString );
        Assert.assertNotNull( b.map );
        Assert.assertNotNull( b.enumValue );
    }
}
