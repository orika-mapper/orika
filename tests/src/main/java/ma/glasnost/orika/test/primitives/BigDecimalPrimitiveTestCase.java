package ma.glasnost.orika.test.primitives;

import java.math.BigDecimal;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class BigDecimalPrimitiveTestCase {
    {
        
    }
    
    @Test
    public void shouldMapBigDecimalToPrimtiveDouble() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new CustomConverter<BigDecimal, Double>() {
            
            public Double convert(BigDecimal source, Type<? extends Double> destinationType) {
                return new Double(source.doubleValue());
            }
            
        });
        
        A source = new A();
        source.setValue(BigDecimal.TEN);
        B dest = factory.getMapperFacade().map(source, B.class);
        
        Assert.assertEquals(new Double(10), (Double) dest.getValue());
        
    }
    
    public static class A {
        private BigDecimal value;
        
        public BigDecimal getValue() {
            return value;
        }
        
        public void setValue(BigDecimal value) {
            this.value = value;
        }
        
    }
    
    public static class B {
        private double value;
        
        public double getValue() {
            return value;
        }
        
        public void setValue(double value) {
            this.value = value;
        }
        
    }
}
