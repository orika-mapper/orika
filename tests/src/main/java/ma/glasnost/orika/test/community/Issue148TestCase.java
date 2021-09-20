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

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Orika fails to map converter result to string (double cast).
 * <p>
 * I am trying to map an object to a string via a custom converter, with the converter returning null (e.g. because the source object is
 * null)
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/148">https://code.google.com/archive/p/orika/</a>
 * @author dbb
 * @since 07.03.14
 */
public class Issue148TestCase {
    
    // one class has a simple string property
    public static class A {
        private String code;
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
    }
    
    // the other class has the same string property, wrapped in an object
    public static class B {
        private WrapperObject code;
        
        public WrapperObject getCode() {
            return code;
        }
        
        public void setCode(WrapperObject code) {
            this.code = code;
        }
    }
    
    private static MapperFacade mapper;
    
    @BeforeClass
    public static void init() {
        
        DefaultMapperFactory.Builder factoryBuilder = new DefaultMapperFactory.Builder();
        MapperFactory factory = factoryBuilder.build();
        
        factory.getConverterFactory().registerConverter(new Converter.Object2String());
        factory.getConverterFactory().registerConverter(new Converter.String2Object());
        
        mapper = factory.getMapperFacade();
    }
    
    @Test
    // map string to wrapper object. expectation: wrapper object should contain
    // string
    public void testA2BwithValue() {
        A a = new A();
        a.code = "x";
        
        B b = mapper.map(a, B.class);
        
        Assert.assertNotNull(b);
        Assert.assertEquals(a.code, b.code.getId());
    }
    
    @Test
    // map wrapper object to string. expectation: string should contain content
    // of wrapper object
    public void testB2AwithValue() {
        B b = new B();
        b.code = new WrapperObject();
        b.code.setId("x");
        
        A a = mapper.map(b, A.class);
        
        Assert.assertNotNull(a);
        Assert.assertEquals(b.code.getId(), a.code);
    }
    
    @Test
    // map null string to wrapper object. expectation: wrapper object should be
    // null
    public void testA2BwithNullValue() {
        B b = new B();
        b.code = new WrapperObject();
        b.code.setId(null);
        
        A a = mapper.map(b, A.class);
        
        Assert.assertNotNull(a);
        Assert.assertNull(a.code);
    }
    
    @Test
    // map null wrapper object to string. expectation: string should be null
    public void testB2AwithNullWrapper() {
        B b = new B();
        b.code = null;
        
        A a = mapper.map(b, A.class);
        
        Assert.assertNotNull(a);
        Assert.assertNull(a.code);
    }
    
    @Test
    // map wrapper object with null content to string. expectation: string
    // should be null
    public void testB2AwithNullValue() {
        B b = new B();
        b.code = new WrapperObject();
        b.code.setId(null);
        
        A a = mapper.map(b, A.class);
        
        Assert.assertNotNull(a);
        Assert.assertNull(a.code);
    }
    
    public static class Converter {
        
        public static class String2Object extends CustomConverter<String, WrapperObject> {
            public WrapperObject convert(String source, Type<? extends WrapperObject> destinationType, MappingContext context) {
                if (source == null) {
                    return null;
                } else {
                    WrapperObject result = new WrapperObject();
                    result.setId(source);
                    return result;
                }
            }
        }
        
        public static class Object2String extends CustomConverter<WrapperObject, String> {
            public String convert(WrapperObject source, Type<? extends String> destinationType, MappingContext context) {
                if (source == null) {
                    return null;
                } else {
                    return source.getId();
                }
            }
        }
    }
    
    public static class WrapperObject {
        private String id;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
    }
    
}
