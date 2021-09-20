package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.junit.Test;

/**
 * problem generic types when subclassing.
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/issues/129">https://github.com/orika-mapper/orika/issues</a>
 */
public class Issue129TestCase {
    
    @Test
    public void testCustomMapperOneParameter() {
        new StringConverter();
    }
    
}
abstract class AbstractStringConverter<T> extends BidirectionalConverter<T, String> {}

class StringConverter extends AbstractStringConverter<String> {

    @Override
    public String convertTo(String source, Type<String> destinationType, MappingContext mappingContext) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String convertFrom(String source, Type<String> destinationType, MappingContext mappingContext) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}