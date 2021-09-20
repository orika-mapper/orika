package ma.glasnost.orika.test.optional;

import com.google.common.base.Optional;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.GuavaOptionalConverter;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class GuavaOptionalTestCase {

    @Test
    public void testMappingIgnoresEmptyOptionalInDestination() {
        final String expected = "initial";

        final Source source = new Source();
        source.setS(Optional.of(expected));

        final Destination actual = getMapperFacade().map(source, Destination.class);

        assertEquals(expected, actual.getS().get());
    }

    @Test
    public void testMappingMapEmptyToEmpty() {
        final Destination actual = getMapperFacade().map(new Source(), Destination.class);

        assertFalse(actual.getS().isPresent());
    }

    private MapperFacade getMapperFacade() {
        final MapperFactory mapperFactory = MappingUtil.getMapperFactory(true);
        mapperFactory.getConverterFactory()
                .registerConverter(new GuavaOptionalConverter<>(TypeFactory.valueOf(String.class), TypeFactory.valueOf(String.class)));
        return mapperFactory.getMapperFacade();
    }

    public static class Source {
        private Optional<String> s = Optional.absent();

        public Optional<String> getS() {
            return s;
        }

        public void setS(final Optional<String> s) {
            this.s = s;
        }
    }

    public static class Destination {
        private Optional<String> s = Optional.absent();

        public Optional<String> getS() {
            return s;
        }

        public void setS(final Optional<String> s) {
            this.s = s;
        }
    }

}
