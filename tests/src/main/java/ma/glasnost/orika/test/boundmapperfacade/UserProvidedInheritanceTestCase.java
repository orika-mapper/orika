package ma.glasnost.orika.test.boundmapperfacade;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Assert;
import org.junit.Test;

public class UserProvidedInheritanceTestCase {
    
    @Test
    public void testFail() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        
        factory.registerClassMap(factory.classMap(Base.class, BaseDto.class).customize(new CustomMapper<Base, BaseDto>() {
            @Override
            public void mapAtoB(Base base, BaseDto baseDto, MappingContext context) {
                baseDto.setBaseField(base.getBaseTrickField());
            }
        }).toClassMap());
        factory.registerClassMap(factory.classMap(Child.class, ChildDto.class).byDefault().toClassMap());
        
        Child child = new Child();
        child.setChildField("CHILD FIELD");
        child.setBaseTrickField("BASE FIELD");
        
        ChildDto dto = factory.getMapperFacade(Child.class, ChildDto.class).map(child);
        
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
