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
package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * Mapping for Collection&lt;String&gt; --&gt; String[] is not working.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/17">https://code.google.com/archive/p/orika/</a>
 *
 */
public class Issue17TestCase {
    
    public static class A {
        private Collection<String> list;
        
        public void setList(final Collection<String> list) {
            this.list = list;
        }
        
        public Collection<String> getList() {
            return list;
        }
    }
    
    public static class B {
        private String[] list;
        
        public void setList(final String[] list) {
            this.list = list;
        }
        
        public String[] getList() {
            return list;
        }
    }
    
    public static class C {
        private int[] list;
        
        public int[] getList() {
            return list;
        }
        
        public void setList(int[] list) {
            this.list = list;
        }
    }
    
    public static class D {
        private List<Integer> list;
        
        public List<Integer> getList() {
            return list;
        }
        
        public void setList(List<Integer> list) {
            this.list = list;
        }
    }
    
    @Test
    public void testMappingToStringArray() {
        final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory.classMap(A.class, B.class).byDefault().register();
        
        final MapperFacade facade = mapperFactory.getMapperFacade();
        
        final A a = new A();
        a.setList(Arrays.asList("One", "Two", "Three"));
        final B converted = facade.map(a, B.class);
        
        Assert.assertNotNull(converted);
        Assert.assertNotNull(converted.getList());
        List<String> convertedList = Arrays.asList(converted.getList());
        assertThat(convertedList, contains("One", "Two", "Three"));
    }
    
    @Test
    public void testMappingToStringArray_empty() {
        final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        final ClassMapBuilder<A, B> builder = mapperFactory.classMap(A.class, B.class);
        
        mapperFactory.registerClassMap(builder.byDefault().toClassMap());
        
        final MapperFacade facade = mapperFactory.getMapperFacade();
        
        final A a = new A();
        final B converted = facade.map(a, B.class);
        
        Assert.assertNotNull(converted);
        Assert.assertNull(converted.getList());
    }
    
    @Test
    public void testMappingToStringToPrimitiveArray() {
        
        final MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        
        /*
         * Note: this conversion works if we register a special converter; TODO:
         * we should probably consider built-in converters for this sort of
         * thing...
         */
        mapperFactory.getConverterFactory().registerConverter(new BidirectionalConverter<Integer, String>() {
            
            @Override
            public String convertTo(Integer source, Type<String> destinationType, MappingContext context) {
                return "" + source;
            }
            
            @Override
            public Integer convertFrom(String source, Type<Integer> destinationType, MappingContext context) {
                return source != null ? Integer.parseInt(source) : 0;
            }
        });
        
        final ClassMapBuilder<A, C> builder = mapperFactory.classMap(A.class, C.class);
        
        mapperFactory.registerClassMap(builder.byDefault().toClassMap());
        
        final MapperFacade facade = mapperFactory.getMapperFacade();
        
        final A a = new A();
        a.setList(Arrays.asList("1", "2", "3"));
        final C converted = facade.map(a, C.class);
        
        Assert.assertNotNull(converted);
        Assert.assertNotNull(converted.getList());
        for (int item : converted.getList()) {
            Assert.assertTrue(a.getList().contains("" + item));
        }
        
    }
    
    @Test
    public void testMappingToStringToPrimitiveArray_empty() {
        final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        final ClassMapBuilder<A, C> builder = mapperFactory.classMap(A.class, C.class);
        
        mapperFactory.registerClassMap(builder.byDefault().toClassMap());
        
        final MapperFacade facade = mapperFactory.getMapperFacade();
        
        final A a = new A();
        final C converted = facade.map(a, C.class);
        
        Assert.assertNotNull(converted);
        Assert.assertNull(converted.getList());
    }
    
    @Test
    public void testMappingToPrimitiveArray() {
        
        final MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        
        /*
         * Note: this conversion works if we register a special converter; TODO:
         * we should probably consider built-in converters for this sort of
         * thing...
         */
        mapperFactory.getConverterFactory().registerConverter(new BidirectionalConverter<Integer, String>() {
            
            @Override
            public String convertTo(Integer source, Type<String> destinationType, MappingContext context) {
                return "" + source;
            }
            
            @Override
            public Integer convertFrom(String source, Type<Integer> destinationType, MappingContext context) {
                return source != null ? Integer.parseInt(source) : 0;
            }
        });
        
        final ClassMapBuilder<D, C> builder = mapperFactory.classMap(D.class, C.class);
        
        mapperFactory.registerClassMap(builder.byDefault().toClassMap());
        
        final MapperFacade facade = mapperFactory.getMapperFacade();
        
        final D a = new D();
        a.setList(Arrays.asList(1, 2, 3));
        final C converted = facade.map(a, C.class);
        
        Assert.assertNotNull(converted);
        Assert.assertNotNull(converted.getList());
        for (int item : converted.getList()) {
            Assert.assertTrue(a.getList().contains(item));
        }
        
    }
    
}
