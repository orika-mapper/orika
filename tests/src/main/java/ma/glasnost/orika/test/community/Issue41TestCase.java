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
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.ConcurrentRule.Concurrent;
import ma.glasnost.orika.test.community.issue41.MyEnum;
import ma.glasnost.orika.test.community.issue41.MyEnumConverter;
import ma.glasnost.orika.test.community.issue41.MySourceObject;
import ma.glasnost.orika.test.community.issue41.MyTargetObject;

import org.junit.Test;

public class Issue41TestCase {
    
    @Test
    public void test_converter_string_to_enum_direct_working() {
        
        ConfigurableMapper mapper = new ConfigurableMapper() {
            
            @Override
            public void configure(MapperFactory factory) {
                
                factory.registerClassMap(factory.classMap(MySourceObject.class, MyTargetObject.class).field("e", "directE")
                
                .toClassMap());
                
                factory.getConverterFactory().registerConverter(new MyEnumConverter());
            }
        };
        
        MySourceObject s = new MySourceObject();
        s.setE("un");
        MyTargetObject t = mapper.map(s, MyTargetObject.class);
        Assert.assertEquals(MyEnum.one, t.getDirectE());
    }
    
    @Test
    @Concurrent(200)
    public void test_converter_string_to_string_nested_not_working() {
        
        ConfigurableMapper mapper = new ConfigurableMapper() {
            
            @Override
            public void configure(MapperFactory factory) {
                
                factory.registerClassMap(factory.classMap(MySourceObject.class, MyTargetObject.class)//
                        .field("e", "sub.s")
                        .toClassMap());
                
                factory.getConverterFactory().registerConverter(new MyEnumConverter());
            }
        };
        
        MySourceObject s = new MySourceObject();
        s.setE("un");
        MyTargetObject t = mapper.map(s, MyTargetObject.class);
        Assert.assertEquals("un", t.getSub().getS());
    }
    
    @Test
    public void test_converter_string_to_enum_nested_not_working() {
        
        ConfigurableMapper mapper = new ConfigurableMapper() {
            
            @Override
            public void configure(MapperFactory factory) {
                factory.getConverterFactory().registerConverter(new MyEnumConverter());
                
                factory.registerClassMap( //
                		factory.classMap(MySourceObject.class, MyTargetObject.class)//
                        	.field("e", "sub.e")
                        	.toClassMap());
                
            }
        };
        
        MySourceObject s = new MySourceObject();
        s.setE("un");
        MyTargetObject t = mapper.map(s, MyTargetObject.class);
        Assert.assertEquals(MyEnum.one, t.getSub().getE());
    }
    
}
