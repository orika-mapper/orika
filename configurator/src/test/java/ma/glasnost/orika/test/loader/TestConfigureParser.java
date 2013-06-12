package ma.glasnost.orika.test.loader;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.loader.ConfigurationLoader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestConfigureParser {

    @Test
    public void testConfigureParseAndMapping() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        ConfigurationLoader loader = new ConfigurationLoader();
        loader.load(factory, "config.xml");

        A a = new A();
        a.setA1("11");
        a.setA2(12);
        a.setA3("14");

        B b = factory.getMapperFacade().map(a, B.class);

        assertEquals(a.getA1(), b.getB1().toString());
        assertEquals(a.getA2(), b.getB2());
        assertEquals(a.getA3(), b.getA3());

        loader.load(factory, "config1.xml");

        C c = factory.getMapperFacade().map(a, C.class);

        assertEquals(a.getA1(), c.getB1().toString());
        assertEquals(a.getA2(), c.getB2());
        assertEquals(a.getA3(), c.getA3());
    }
}
