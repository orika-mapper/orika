package ma.glasnost.orika.test.array;

import java.util.Arrays;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class ArrayTestCase {

    @Test
    public void testSimplePrimitiveArray() {
        ArrayTestCaseClasses.A source =  new ArrayTestCaseClasses.A();
        byte[] buffer = new byte[]{1,2,3,4};
        source.setBuffer(buffer);

        MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();

        ArrayTestCaseClasses.B destination = mapperFacade.map(source, ArrayTestCaseClasses.B.class);

        Assert.assertArrayEquals(source.getBuffer(), destination.getBuffer());
        
    }

    @Test
    public void testSimplePrimitiveToWrapperArray() {
        ArrayTestCaseClasses.A source =  new ArrayTestCaseClasses.A();
        byte[] buffer = new byte[]{1,2,3,4};
        source.setBuffer(buffer);

        MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();

        ArrayTestCaseClasses.C destination = mapperFacade.map(source, ArrayTestCaseClasses.C.class);

        Assert.assertArrayEquals(new Byte[]{1,2,3,4}, destination.getBuffer());
    }
    
    @Test
    public void testArrayToList() {
    	MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();
    	
    	ArrayTestCaseClasses.A source =  new ArrayTestCaseClasses.A();
        byte[] buffer = new byte[]{1,2,3,4};
        source.setBuffer(buffer);


        ArrayTestCaseClasses.D destination = mapperFacade.map(source, ArrayTestCaseClasses.D.class);

        Assert.assertEquals(Arrays.asList((byte)1,(byte)2,(byte)3,(byte)4), destination.getBuffer());
    	
    }
    
    @Test
        public void testWrapperArrayToList() {
           MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();
           
           ArrayTestCaseClasses.C source =  new ArrayTestCaseClasses.C();
            Byte[] buffer = new Byte[]{1,2,3,4};
             source.setBuffer(buffer);
    
     
             ArrayTestCaseClasses.D destination = mapperFacade.map(source, ArrayTestCaseClasses.D.class);
     
             Assert.assertEquals(Arrays.asList((byte)1,(byte)2,(byte)3,(byte)4), destination.getBuffer());
           
         }
    
    @Test
    public void testListToArray() {
    	MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();
    	
    	ArrayTestCaseClasses.D source =  new ArrayTestCaseClasses.D();
        source.setBuffer(Arrays.asList((byte)1,(byte)2,(byte)3,(byte)4));


        ArrayTestCaseClasses.A destination = mapperFacade.map(source, ArrayTestCaseClasses.A.class);

        Assert.assertArrayEquals(new byte[] {(byte)1,(byte)2,(byte)3,(byte)4}, destination.getBuffer());
    	
    }
    
    @Test
    public void testMappingArrayOfString() {

        Product p = new Product();
        p.setTags(new String[] { "music", "sport" });

        ProductDTO productDTO = MappingUtil.getMapperFactory().getMapperFacade().map(p, ProductDTO.class);

        Assert.assertArrayEquals(p.getTags(), productDTO.getTags());
    }

    public static class Product {

        private String[] tags;

        public String[] getTags() {
            return tags.clone();
        }

        public void setTags(String[] tags) {
            this.tags = tags;
        }

    }

    public static class ProductDTO {

        private String[] tags;

        public String[] getTags() {
            return tags.clone();
        }

        public void setTags(String[] tags) {
            this.tags = tags;
        }

    }
}
