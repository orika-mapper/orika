package ma.glasnost.orika.test.community.collection;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

public abstract class AbstractMapperTest {
    protected MapperFactory createMapperFactory() {
        // MapperFactory mapperFactory = new
        // DefaultMapperFactory.Builder().compilerStrategy(new
        // EclipseJdtCompilerStrategy()).build();
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        return mapperFactory;
    }
}
