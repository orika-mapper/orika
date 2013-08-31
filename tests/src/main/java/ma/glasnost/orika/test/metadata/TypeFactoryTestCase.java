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

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class TypeFactoryTestCase {
    
    @Test
    public void createTypeFromClass() {
        Type<?> type = TypeFactory.valueOf("java.util.List");
        
        Assert.assertEquals(List.class, type.getRawType());
    }
    
    @Test
    public void createTypeFromClass_defaultPackages() {
        Type<?> type = TypeFactory.valueOf("List");
        
        Assert.assertEquals(List.class, type.getRawType());
        
        type = TypeFactory.valueOf("String");
        
        Assert.assertEquals(String.class, type.getRawType());
    }
    
    @Test
    public void createTypeFromNestedClass() {
        Type<?> type = TypeFactory.valueOf("List<Long>");
        
        Assert.assertEquals(List.class, type.getRawType());
        Assert.assertEquals(Long.class, type.getNestedType(0).getRawType());
    }
    
    @Test
    public void createTypeFromMultipleNestedClass() {
        Type<?> type = TypeFactory.valueOf("List<Map<String,Set<Map<String,java.io.File>>>>");
        
        Assert.assertEquals(List.class, type.getRawType());
        Assert.assertEquals(Map.class, type.getNestedType(0).getRawType());
        Assert.assertEquals(String.class, type.getNestedType(0).getNestedType(0).getRawType());
        Assert.assertEquals(Set.class, type.getNestedType(0).getNestedType(1).getRawType());
        Assert.assertEquals(Map.class, type.getNestedType(0).getNestedType(1).getNestedType(0).getRawType());
        Assert.assertEquals(String.class, type.getNestedType(0).getNestedType(1).getNestedType(0).getNestedType(0).getRawType());
        Assert.assertEquals(File.class, type.getNestedType(0).getNestedType(1).getNestedType(0).getNestedType(1).getRawType());
        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void createTypeFromMultipleNestedClass_invalidExpression() {
        TypeFactory.valueOf("List<Map<String,Set<Map<String,java.io.File>>>");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void createTypeFromMultipleNestedClass_invalidType() {
        TypeFactory.valueOf("List<Map<String,Set<Map<String,java.io.FooBar>>>>");
    }
    
}
