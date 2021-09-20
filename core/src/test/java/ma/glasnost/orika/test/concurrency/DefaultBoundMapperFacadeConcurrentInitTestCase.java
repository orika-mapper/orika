package ma.glasnost.orika.test.concurrency;

import com.google.common.collect.Lists;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.concurrency.model.ModelWithNestedObjDest;
import ma.glasnost.orika.test.concurrency.model.ModelWithNestedObjSource;
import ma.glasnost.orika.test.concurrency.model.NestedModel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Checks that instantiation and cache inside of <{@link ma.glasnost.orika.impl.DefaultBoundMapperFacade} is thread safe.
 */
public class DefaultBoundMapperFacadeConcurrentInitTestCase {

    private MapperFacade mapper;

    private final int threads = 20;
    private ExecutorService executor;

    @Before
    public void setup() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.classMap(ModelWithNestedObjSource.class, ModelWithNestedObjDest.class)
            .byDefault()
            .register();

        mapper = factory.getMapperFacade();

        executor = Executors.newFixedThreadPool(threads);
    }

    @After
    public void cleanup() {
        if (executor != null) {
            executor.shutdownNow();
        } 
    }

    @Test
    public void testCase() {
        CountDownLatch readyLatch = new CountDownLatch(threads);

        List<Future<?>> futures = IntStream.range(0, threads).mapToObj(i -> 
            executor.submit(() -> {
                ModelWithNestedObjSource model = i % 2 == 0 
                    ? new ModelWithNestedObjSource(Lists.newArrayList(new NestedModel("str")))
                    : new ModelWithNestedObjSource(Lists.newArrayList(new NestedModel(1)));
                
                readyLatch.countDown();
                try {
                    if (readyLatch.await(1, TimeUnit.SECONDS)) {
                        Assert.assertEquals(model.getValue(), mapper.map(model, ModelWithNestedObjDest.class).getValue());
                    } else {
                        throw new RuntimeException("Latch timeout");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            })
        ).collect(Collectors.toList());

        // check that no exception thrown inside futures
        for (Future<?> future : futures) {
            try {
                future.get(1, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
