package ma.glasnost.orika.test.filters;

import java.math.BigDecimal;

import ma.glasnost.orika.CustomFilter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class FilterTestCase {
    
    @Test
    public void testFiltering() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.classMap(Source.class, Destination.class).byDefault().register();
        factory.registerFilter(new SecurityFilter());
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Source source = new Source();
        source.name = new SourceName();
        source.name.first = "Joe";
        source.name.last = "Smith";
        source.id = 2L;
        source.age = 35;
        source.cost = 12.34d;
        source.creditCardNumber = "5432109876543210";
        
        Destination dest = mapper.map(source, Destination.class);
        
        Assert.assertEquals(source.name.first, dest.name.first);
        Assert.assertEquals(source.name.last, dest.name.last);
        Assert.assertNull(dest.age);
        Assert.assertEquals("************3210", dest.creditCardNumber);
        
    }
    
    public static class SecurityFilter extends CustomFilter<Object, Object> {
        
        private final String MASK = "*************";
        
        public boolean filtersSource() {
            return false;
        }
        
        public boolean filtersDestination() {
            return true;
        }
        
        public boolean shouldMap(final Type<?> sourceType, final String sourceName, final Type<?> destType, final String destName,
                final MappingContext mappingContext) {
            if ("age".equals(sourceName)) {
                return false;
            }
            return true;
        }
        
        public <D> D filterDestination(D destinationValue, final Type<?> sourceType, final String sourceName, final Type<D> destType,
                final String destName, final MappingContext mappingContext) {
            if ("creditCardNumber".equals(sourceName)) {
                String cardMask = (String) destinationValue;
                destinationValue = (D) (MASK.substring(0, cardMask.length() - 4) + cardMask.substring(cardMask.length() - 4));
            }
            return destinationValue;
            
        }
        
        public <S> S filterSource(final S sourceValue, final Type<S> sourceType, final String sourceName, final Type<?> destType,
                final String destName, final MappingContext mappingContext) {
            return sourceValue;
        }
    }
    
    public static class Source {
        public SourceName name;
        public Long id;
        public int age;
        public double cost;
        public String creditCardNumber;
    }
    
    public static class SourceName {
        public String first;
        public String last;
    }
    
    public static class Destination {
        public DestinationName name;
        public Long id;
        public Integer age;
        public BigDecimal cost;
        public String creditCardNumber;
    }
    
    public static class DestinationName {
        public String first;
        public String last;
    }
}
