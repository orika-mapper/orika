package ma.glasnost.orika.test;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Test;

import java.util.GregorianCalendar;

public class BrokenTest {

    @Test
    public void testConvertingGregorianCalendar() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder()
                .build();

        MapperFacade mapperFacade = mapperFactory.getMapperFacade();
        GregorianCalendar gc = new GregorianCalendar();
        Object dest = mapperFacade.map(gc, Object.class);
    }
}
