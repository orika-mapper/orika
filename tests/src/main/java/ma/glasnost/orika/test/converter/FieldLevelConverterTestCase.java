package ma.glasnost.orika.test.converter;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.converter.builtin.DateToStringConverter;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.converter.FieldLevelConverterClasses.A;
import ma.glasnost.orika.test.converter.FieldLevelConverterClasses.B;
import ma.glasnost.orika.test.converter.FieldLevelConverterClasses.C;

import org.junit.Test;

public class FieldLevelConverterTestCase {
    
    @Test
    public void testDateToString() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        ConverterFactory converterFactory = factory.getConverterFactory();
        converterFactory.registerConverter("dateConverter1", new DateToStringConverter("dd/MM/yyyy"));
        converterFactory.registerConverter("dateConverter2", new DateToStringConverter("dd-MM-yyyy"));
        
        factory.registerClassMap(ClassMapBuilder.map(A.class, B.class).fieldMap("date").converter("dateConverter1").add().toClassMap());
        factory.registerClassMap(ClassMapBuilder.map(A.class, C.class).fieldMap("date").converter("dateConverter2").add().toClassMap());
        
        factory.build();
        
        MapperFacade mapperFacade = factory.getMapperFacade();
        
        C c = new C();
        c.setDate(new Date());
        
        A a = mapperFacade.map(c, A.class);
        
        Assert.assertEquals(new SimpleDateFormat("dd-MM-yyyy").format(c.getDate()), a.getDate());
        
        B b = new B();
        b.setDate(new Date());
        
        a = mapperFacade.map(b, A.class);
        
        Assert.assertEquals(new SimpleDateFormat("dd/MM/yyyy").format(b.getDate()), a.getDate());
        
    }
}
