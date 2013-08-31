package ma.glasnost.orika.test.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

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