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

package ma.glasnost.orika.test.inheritance;

import junit.framework.Assert;
import ma.glasnost.orika.MapperBase;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class InheritanceTestCase {
    
    private final MapperFactory factory = MappingUtil.getMapperFactory();
    
    public void setUp() {
        
    }
    
    @Test
    public void testSimpleInheritance() {
        MapperFacade mapper = factory.getMapperFacade();
        
        ChildEntity entity = new ChildEntity();
        entity.setId(1L);
        entity.setName("Khettabi");
        
        ChildDTO dto = mapper.map(entity, ChildDTO.class);
        
        Assert.assertEquals(entity.getId(), dto.getId());
        Assert.assertEquals(entity.getName(), dto.getName());
    }
    
    @Test
    public void resolveConcreteClass() {
        MapperFacade mapper = factory.getMapperFacade();
        factory.registerClassMap(ClassMapBuilder.map(ChildEntity.class, ChildDTO.class).byDefault().toClassMap());
        
        factory.build();
        
        ChildEntity entity = new ChildEntity();
        entity.setId(1L);
        entity.setName("Khettabi");
        
        BaseDTO dto = mapper.map(entity, BaseDTO.class);
        
        Assert.assertTrue(dto instanceof ChildDTO);
        Assert.assertEquals(entity.getName(), ((ChildDTO) dto).getName());
    }
    
    @Test
    public void testDifferentLevelOfInheritance() {
        factory.registerClassMap(ClassMapBuilder.map(ChildEntity.class, Child2DTO.class)
                .customize(new MapperBase<ChildEntity, Child2DTO>() {
                    
                    @Override
                    public void mapAtoB(ChildEntity a, Child2DTO b, MappingContext context) {
                        b.setMessage("Hello " + a.getName());
                    }
                    
                }).byDefault().toClassMap());
        
        factory.build();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        ChildEntity entity = new ChildEntity();
        entity.setId(1L);
        entity.setName("Khettabi");
        
        Child2DTO dto = mapper.map(entity, Child2DTO.class);
        
        Assert.assertEquals(entity.getId(), dto.getId());
        Assert.assertEquals(entity.getName(), dto.getName());
        Assert.assertEquals("Hello Khettabi", dto.getMessage());
    }
    
    public static abstract class BaseEntity {
        private Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
    }
    
    public static abstract class BaseDTO {
        private Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
    }
    
    public static class ChildEntity extends BaseEntity {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class ChildDTO extends BaseDTO {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class Child2DTO extends ChildDTO {
        private String message;
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
