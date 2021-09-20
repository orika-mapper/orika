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

package ma.glasnost.orika.test.inheritance;

import org.junit.Assert;
import org.junit.Test;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;

public class UserProvidedInheritanceTestCase {
    
    @SuppressWarnings("deprecation")
    @Test
    public void testFail() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.registerClassMap(ClassMapBuilder.map(Base.class, BaseDto.class).customize(new CustomMapper<Base, BaseDto>() {
            @Override
            public void mapAtoB(Base base, BaseDto baseDto, MappingContext context) {
                baseDto.setBaseField(base.getBaseTrickField());
            }
        }).toClassMap());
        factory.registerClassMap(ClassMapBuilder.map(Child.class, ChildDto.class).byDefault().toClassMap());
        
        factory.build();
        
        Child child = new Child();
        child.setChildField("CHILD FIELD");
        child.setBaseTrickField("BASE FIELD");
        
        ChildDto dto = factory.getMapperFacade().map(child, ChildDto.class);
        
        Assert.assertNotNull(dto);
        Assert.assertEquals(child.getChildField(), dto.getChildField());
        Assert.assertEquals(child.getBaseTrickField(), dto.getBaseField());
        
    }
    
    public static class Base {
        private String baseTrickField;
        
        public String getBaseTrickField() {
            return baseTrickField;
        }
        
        public void setBaseTrickField(String baseTrickField) {
            this.baseTrickField = baseTrickField;
        }
    }
    
    public static class BaseDto {
        private String baseField;
        
        public String getBaseField() {
            return baseField;
        }
        
        public void setBaseField(String baseField) {
            this.baseField = baseField;
        }
    }
    
    public static class Child extends Base {
        private String childField;
        
        public String getChildField() {
            return childField;
        }
        
        public void setChildField(String childField) {
            this.childField = childField;
        }
    }
    
    public static class ChildDto extends BaseDto {
        private String childField;
        
        public String getChildField() {
            return childField;
        }
        
        public void setChildField(String childField) {
            this.childField = childField;
        }
    }
}
