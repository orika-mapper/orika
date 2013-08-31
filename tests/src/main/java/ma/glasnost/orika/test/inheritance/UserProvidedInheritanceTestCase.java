package ma.glasnost.orika.test.inheritance;

import ma.glasnost.orika.MapperBase;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import org.junit.Assert;
import org.junit.Test;

public class UserProvidedInheritanceTestCase {
    
    @SuppressWarnings("deprecation")
    @Test
    public void testFail() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.registerClassMap(ClassMapBuilder.map(Base.class, BaseDto.class).customize(new MapperBase<Base, BaseDto>() {
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
