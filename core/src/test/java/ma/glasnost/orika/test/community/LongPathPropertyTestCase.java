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

import java.util.List;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;

import org.junit.Assert;

import org.junit.Test;

public class LongPathPropertyTestCase {
    
    @Test
    public void testSimpleCase() {
        MapperFacade mapper = new ConfigurableMapper() {
            
            @Override
            protected void configure(final MapperFactory factory) {
                factory.classMap(A.class, D.class).field("second[0].third[0].message", "message").register();
            }
            
        };
        D source = new D();
        source.message = "Hello World";
        
         Assert.assertEquals(source.message, mapper.map(source, A.class).second.get(0).third.get(0).message);
    }
    
    public static class A {
        public List<B> second;
    }
    
    public static class B {
        public List<C> third;
    }
    
    public static class C {
        public String message;
    }
    
    public static class D {
        public String message;
    }
}
