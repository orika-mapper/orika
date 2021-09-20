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

package ma.glasnost.orika.test.collection;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Simple test of maaping collection
 * 
 * @author Dmitriy Khomyakov
 */
public class ObjectCollectionTestCase {
    
    @Test
    public void testMapOfCollection() {
        DefaultMapperFactory.Builder builder = new DefaultMapperFactory.Builder();
        MapperFactory factory = builder.build();
        
        MapperFacade mapperFacade = factory.getMapperFacade();
        
        List<Dto> dtos = new ArrayList<>();
        
        Dto dto = new Dto();
        dto.setId(1L);
        dto.setName("A");
        dtos.add(dto);
        
        Dto dto2 = new Dto();
        dto2.setId(2L);
        dto2.setName("B");
        dtos.add(dto2);
        
        dto = new Dto();
        dto.setId(3L);
        dto.setName("C");
        dtos.add(dto);
        
        DtoHolder source = new DtoHolder();
        source.setEntities(dtos);
        
        final EntityHolder entities = mapperFacade.map(source, EntityHolder.class);
        
        Assert.assertNotNull(entities);
        Assert.assertEquals(3, entities.getEntities().size());
        
    }
    
    public static class Entity {
        
        private Long id;
        private String name;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public boolean isNew() {
            return id == null;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Entity)) {
                return false;
            }
            
            Entity entity = (Entity) o;
            
            if (!Objects.equals(id, entity.id)) {
                return false;
            }
            
            return true;
        }
        
        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }
    }
    
    public static class EntityHolder {
        
        private Collection<Entity> entityList;
        
        public Collection<Entity> getEntities() {
            return entityList;
        }
        
        public void setEntities(Collection<Entity> entityList) {
            this.entityList = entityList;
        }
        
    }
    
    public static class Dto {
        private Long id;
        private String name;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Dto)) {
                return false;
            }
            
            Dto dto = (Dto) o;
            
            if (!Objects.equals(id, dto.id)) {
                return false;
            }
            
            return true;
        }
        
        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }
    }
    
    public static class DtoHolder {
        
        private Collection<Dto> dtoList;
        
        public Collection<Dto> getEntities() {
            return dtoList;
        }
        
        public void setEntities(Collection<Dto> dtoList) {
            this.dtoList = dtoList;
        }
        
    }
    
}