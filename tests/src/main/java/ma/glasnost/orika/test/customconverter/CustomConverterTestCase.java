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

package ma.glasnost.orika.test.customconverter;

import junit.framework.Assert;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class CustomConverterTestCase {
    
    public static class MyCustomConverter extends CustomConverter<Long, String> {

        /* (non-Javadoc)
         * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
         */
        public String convert(Long source, Type<? extends String> destinationType) {
            return "long{" + source + "}";
        }
    }
    
    public static class MyCustomConverter2 extends CustomConverter<String, String> {
        /* (non-Javadoc)
         * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
         */
        public String convert(String source, Type<? extends String> destinationType) {
            return "string: " + source;
        }
    }
    
    @Test
    public void testConvertLongString() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new MyCustomConverter());
        factory.classMap(A.class, B.class).field("id", "string").register();
        
        
        A source = new A();
        source.setId(42L);
        
        B destination = factory.getMapperFacade().map(source, B.class);
        
        Assert.assertEquals("long{42}", destination.getString());
        
    }
    
    @Test
    public void testConvertStringToString() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new MyCustomConverter2());
        factory.classMap(A.class, B.class).field("id", "string").register();
        
        B source = new B();
        source.setString("hello");
        
        C destination = factory.getMapperFacade().map(source, C.class);
        
        Assert.assertEquals("string: hello", destination.string);
        
    }
    
    public static class A {
        private Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
    }
    
    public static class B {
        private String string;
        
        public String getString() {
            return string;
        }
        
        public void setString(String string) {
            this.string = string;
        }
        
    }
    
    public static class C {
        public String string;
    }
}
