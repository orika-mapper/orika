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

package ma.glasnost.orika.test.converter;

import org.junit.Assert;
import org.junit.Test;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

public class ConverterTestCase {
    
    @Test
    public void testConvertLongString() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.getConverterFactory().registerConverter(new LongToStringConverter());
        
        factory.classMap(A.class, B.class).field("id", "string")
               .register();

        
        A source = new A();
        source.setId(42L);
        
        B destination = factory.getMapperFacade().map(source, B.class);
        
        Assert.assertEquals("42", destination.getString());
        
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
}
