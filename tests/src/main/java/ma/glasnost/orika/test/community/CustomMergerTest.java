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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.UtilityResolver;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 */
public class CustomMergerTest {
    
    @Test
    public void testMergingWithCustomMapper() {
        MapperFacade mapper = createMapperFacade();
        
        List<Dto> dtos = new ArrayList<Dto>();
        
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
        
        Type<?> typeOf_DtoHolder = TypeFactory.valueOf(DtoHolder.class);
        UtilityResolver.getDefaultPropertyResolverStrategy().getProperties(typeOf_DtoHolder);
        
        final EntityHolder entities = mapper.map(source, EntityHolder.class);
        
        Assert.assertNotNull(entities);
        Assert.assertEquals(3, entities.getEntities().size());
        
        final EntityHolder originalEntities = entities;
        source.getEntities().remove(dto2);
        dto2.setName("B-Changed");
        source.getEntities().add(dto2);
        
        mapper.map(source, entities);
        
        Assert.assertEquals(entities.getEntities().size(), originalEntities.getEntities().size());
        
        Iterator<Entity> entitiesIter = entities.getEntities().iterator();
        Iterator<Entity> originalIter = originalEntities.getEntities().iterator();
        while (entitiesIter.hasNext()) {
            Entity e = entitiesIter.next();
            Entity o = originalIter.next();
            Assert.assertSame(e, o);
        }
    }
    
    private static MapperFacade createMapperFacade() {
        DefaultMapperFactory.Builder builder = new DefaultMapperFactory.Builder();
        builder.compilerStrategy(new EclipseJdtCompilerStrategy());
        MapperFactory factory = builder.build();
        
        factory.registerMapper(new MergingMapper());
        factory.registerConcreteType(new TypeBuilder<Collection<Entity>>() {}.build(), new TypeBuilder<ArrayList<Entity>>() {}.build());
        return factory.getMapperFacade();
    }
    
    @Test
    public void testMergingWithCustomMapperForChildrenSetToSet() {
        MapperFacade mapper = createMapperFacade();
        
        Set<ChildDto> dtos = new HashSet<ChildDto>();
        
        ChildDto dto = new ChildDto();
        dto.setId(1L);
        dto.setName("A");
        dtos.add(dto);
        
        ChildDto dto2 = new ChildDto();
        dto2.setId(2L);
        dto2.setName("B");
        dtos.add(dto2);
        
        dto = new ChildDto();
        dto.setId(3L);
        dto.setName("C");
        dtos.add(dto);
        
        AnotherDtoHolder dtoHolder = new AnotherDtoHolder();
        dtoHolder.setEntities(dtos);
        
        final AnotherEntityHolder entityHolder = mapper.map(dtoHolder, AnotherEntityHolder.class);
        ChildEntity next = entityHolder.getEntities().iterator().next();
        Assert.assertEquals(next.getClass(), ChildEntity.class);
        
        Assert.assertNotNull(entityHolder);
        Assert.assertEquals(3, entityHolder.getEntities().size());
        
        final AnotherEntityHolder originalEntity = entityHolder;
        Collection<ChildEntity> originalEntities = originalEntity.getEntities();
        dtoHolder.getEntities().remove(dto2);
        dto2.setName("B-Changed");
        dtoHolder.getEntities().add(dto2);
        
        mapper.map(dtoHolder, entityHolder);
        
        Assert.assertSame(originalEntities, entityHolder.getEntities());
        Assert.assertEquals(entityHolder.getEntities().size(), originalEntity.getEntities().size());
        
        Iterator<ChildEntity> entitiesIter = entityHolder.getEntities().iterator();
        Iterator<ChildEntity> originalIter = originalEntity.getEntities().iterator();
        while (entitiesIter.hasNext()) {
            Entity e = entitiesIter.next();
            Entity o = originalIter.next();
            Assert.assertSame(e, o);
        }
    }
    
    public static class MergingMapper extends CustomMapper<Collection<Dto>, Collection<Entity>> {
        
        public void mapAtoB(Collection<Dto> a, Collection<Entity> b, MappingContext context) {
            merge(a, b, context);
        }
        
        private Collection<Entity> merge(Collection<Dto> srcDtos, Collection<Entity> dstEntities, MappingContext context) {
            
            Set<Long> ids = new HashSet<Long>(srcDtos.size());
            
            Type<Dto> sourceType = context.getResolvedSourceType().getNestedType(0);
            Type<Entity> destinationType = context.getResolvedDestinationType().getNestedType(0);
            
            for (Dto memberDto : srcDtos) {
                Entity memberEntity = findEntity(dstEntities, memberDto.getId());
                if (memberEntity == null) {
                    
                    dstEntities.add((Entity) mapperFacade.map(memberDto, sourceType, destinationType, context)); // Class
                                                                                // of
                                                                                // entity
                                                                                // destination
                                                                                // is
                                                                                // unknown
                                                                                // in
                                                                                // merge
                } else {
                    mapperFacade.map(memberDto, memberEntity);
                }
                ids.add(memberDto.getId());
            }
            
            for (Iterator<Entity> iterator = dstEntities.iterator(); iterator.hasNext();) {
                Entity dstEntity = iterator.next();
                if (!dstEntity.isNew() && !ids.contains(dstEntity.getId())) {
                    iterator.remove();
                }
            }
            
            return dstEntities;
            
        }
        
        private Entity findEntity(Collection<Entity> dstEntities, Long id) {
            for (Entity dstEntity : dstEntities) {
                if (id.equals(dstEntity.getId())) {
                    return dstEntity;
                }
            }
            return null;
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
    
    public static class DtoHolder {
        
        private Collection<Dto> dtoList;
        
        public Collection<Dto> getEntities() {
            return dtoList;
        }
        
        public void setEntities(Collection<Dto> dtoList) {
            this.dtoList = dtoList;
        }
        
    }
    
    public static class AnotherEntityHolder {
        private Set<ChildEntity> entityList;
        
        public Set<ChildEntity> getEntities() {
            return entityList;
        }
        
        public void setEntities(Set<ChildEntity> entityList) {
            this.entityList = entityList;
        }
        
    }
    
    public static class AnotherDtoHolder {
        private Set<ChildDto> dtoList;
        
        public Set<ChildDto> getEntities() {
            return dtoList;
        }
        
        public void setEntities(Set<ChildDto> dtoList) {
            this.dtoList = dtoList;
        }
        
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
            
            if (id != null ? !id.equals(entity.id) : entity.id != null) {
                return false;
            }
            
            return true;
        }
        
        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }
    }
    
    public static class ChildEntity extends Entity {
        
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
            
            if (id != null ? !id.equals(dto.id) : dto.id != null) {
                return false;
            }
            
            return true;
        }
        
        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }
    }
    
    public static class ChildDto extends Dto {
        
    }
    
    public static class DesiredMergingMapper extends CustomMapper
    
    {
        
        public void mapAtoB(Collection srcDtos, Collection dstEntities, Class<? extends Dto> srcDtoClass,
                Class<? extends Entity> dstEntityClass, MappingContext context) {
            merge(srcDtos, dstEntities, srcDtoClass, dstEntityClass);
        }
        
        private Collection merge(Collection srcDtos, Collection dstEntities, Class<? extends Dto> srcDtoClass,
                Class<? extends Entity> dstEntityClass) {
            
            Set<Long> ids = new HashSet<Long>(srcDtos.size());
            for (Iterator iterator1 = srcDtos.iterator(); iterator1.hasNext();) {
                Dto memberDto = (Dto) iterator1.next();
                
                Entity memberEntity = findEntity(dstEntities, memberDto.getId());
                if (memberEntity == null) {
                    Entity newEntity = mapperFacade.map(memberDto, dstEntityClass);
                    dstEntities.add(newEntity);
                } else {
                    mapperFacade.map(memberEntity, memberDto);
                }
                ids.add(memberDto.getId());
            }
            
            for (Iterator<? extends Entity> iterator = dstEntities.iterator(); iterator.hasNext();) {
                Entity dstEntity = iterator.next();
                if (!dstEntity.isNew() && !ids.contains(dstEntity.getId())) {
                    iterator.remove();
                }
            }
            
            return dstEntities;
            
        }
        
        private Entity findEntity(Collection dstEntities, Long id) {
            for (Iterator iterator = dstEntities.iterator(); iterator.hasNext();) {
                Entity dstEntity = (Entity) iterator.next();
                if (id.equals(dstEntity.getId())) {
                    return dstEntity;
                }
            }
            return null;
        }
        
    }
    
}
