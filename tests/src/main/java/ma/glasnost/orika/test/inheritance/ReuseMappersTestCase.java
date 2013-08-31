package ma.glasnost.orika.test.inheritance;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class ReuseMappersTestCase {
    
    @Test
    public void testReuse() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        {
            ClassMapBuilder<Location, LocationDTO> builder = ClassMapBuilder.map(Location.class, LocationDTO.class);
            builder.field("x", "coordinateX").field("y", "coordinateY");
            factory.registerClassMap(builder.toClassMap());
            
        }
        
        {
            ClassMapBuilder<NamedLocation, NamedLocationDTO> builder = ClassMapBuilder.map(NamedLocation.class, NamedLocationDTO.class);
            builder.use(Location.class, LocationDTO.class).field("name", "label");
            factory.registerClassMap(builder.toClassMap());
        }
        
        {
            ClassMapBuilder<City, CityDTO> builder = ClassMapBuilder.map(City.class, CityDTO.class);
            builder.use(NamedLocation.class, NamedLocationDTO.class).byDefault();
            factory.registerClassMap(builder.toClassMap());
        }
        
        factory.build();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        City city = new City();
        city.setX(5);
        city.setY(7);
        city.setZipCode("78951123");
        
        CityDTO dto = mapper.map(city, CityDTO.class);
        
        Assert.assertEquals(city.getX(), dto.getCoordinateX());
        Assert.assertEquals(city.getY(), dto.getCoordinateY());
        Assert.assertEquals(city.getName(), dto.getLabel());
        Assert.assertEquals(city.getZipCode(), dto.getZipCode());
        
    }
    
    public static abstract class Location {
        private int x, y;
        
        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }
        
        public int getY() {
            return y;
        }
        
        public void setY(int y) {
            this.y = y;
        }
        
    }
    
    public static class NamedLocation extends Location {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    public static class City extends NamedLocation {
        private String zipCode;
        
        public String getZipCode() {
            return zipCode;
        }
        
        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }
        
    }
    
    public static abstract class LocationDTO {
        private int coordinateX, coordinateY;
        
        public int getCoordinateX() {
            return coordinateX;
        }
        
        public void setCoordinateX(int x) {
            this.coordinateX = x;
        }
        
        public int getCoordinateY() {
            return coordinateY;
        }
        
        public void setCoordinateY(int y) {
            this.coordinateY = y;
        }
        
    }
    
    public static class NamedLocationDTO extends LocationDTO {
        private String label;
        
        public String getLabel() {
            return label;
        }
        
        public void setLabel(String name) {
            this.label = name;
        }
        
    }
    
    public static class CityDTO extends NamedLocationDTO {
        private String zipCode;
        
        public String getZipCode() {
            return zipCode;
        }
        
        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }
        
    }
}
