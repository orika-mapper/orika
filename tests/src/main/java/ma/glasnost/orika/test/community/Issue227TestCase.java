package ma.glasnost.orika.test.community;

import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by dustins on 7/23/17.
 */
public class Issue227TestCase {
    private static Logger LOG = LoggerFactory.getLogger(Issue227TestCase.class);

    public static class Apple {
        public String name;

        public Set<Seed> seeds;
    }

    public static class AppleDto {
        public String name;

        public Map<Long, Seed> seeds;
    }

    public static class Seed {
        public Long id;
        public String name;

        public Seed(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Seed seed = (Seed) o;

            if (!id.equals(seed.id)) return false;
            return name.equals(seed.name);
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }
    }

    @Test
    public void test() {
        DefaultMapperFactory mapperFactory = new DefaultMapperFactory.Builder()
                .compilerStrategy(new EclipseJdtCompilerStrategy())
                .dumpStateOnException(true)
                .build();

        mapperFactory.classMap(Apple.class, AppleDto.class)
                .field("name", "name")
                .field("seeds{id}", "seeds{key}")
                .field("seeds{}", "seeds{value}")
                .register();

        Apple apple = new Apple();
        apple.name = "Apple";
        apple.seeds = new HashSet<Seed>() {{
            add(new Seed(1L, "first"));
            add(new Seed(2L, "second"));
        }};

        AppleDto appleDto = mapperFactory.getMapperFacade().map(apple, AppleDto.class);
        Assert.assertEquals(apple.name, appleDto.name);
        Assert.assertEquals(apple.seeds.size(), appleDto.seeds.size());
        Assert.assertTrue(apple.seeds.containsAll(appleDto.seeds.values()));
        Assert.assertTrue(appleDto.seeds.values().containsAll(apple.seeds));
    }
}
