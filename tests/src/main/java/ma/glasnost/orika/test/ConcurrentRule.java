/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ma.glasnost.orika.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * ConcurrentRule is a JUnit Rule which allows a given test method
 * to be executed currently by a specified number of threads concurrently.<br><br>
 * 
 * The first Exception thrown by any of the threads is captured and re-thrown
 * as the overall exception of the method.
 * 
 * TODO: note that the current implementation also invokes <code>@Before</code> and
 * <code>@After</code> methods concurrently; future task is to make this behavior 
 * configurable.
 * 
 * 
 * 
 * @author matt.deboer@gmail.com
 * based on work by: Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class ConcurrentRule implements MethodRule {
    
	/**
	 * Concurrent is used to mark the number of threads which should
	 * be used to invoke the test method simultaneously.
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public @interface Concurrent {
		int value() default 10;
	}
	
	
	/* (non-Javadoc)
	 * @see org.junit.rules.MethodRule#apply(org.junit.runners.model.Statement, org.junit.runners.model.FrameworkMethod, java.lang.Object)
	 */
    public Statement apply(final Statement theStatement, final FrameworkMethod theFrameworkMethod, final Object theObject) {
        return new Statement() {
            
        	private final FrameworkMethod frameworkMethod = theFrameworkMethod;
        	private final Statement statement = theStatement;
        	@SuppressWarnings("unused")
			private final Object object = theObject;
        	
        	@Override
            public void evaluate() throws Throwable {
                Concurrent concurrent = frameworkMethod.getAnnotation(Concurrent.class);
                if (concurrent == null)
                    statement.evaluate();
                else {
                	
                    /*
                     * create a completion service to get jobs in the order they finish, to be able
                     * to cancel remaining jobs as fast as possible if an exception occurs
                     */ 
                    CompletionService<Void> completionService = 
                    		new ExecutorCompletionService<Void>(Executors.newFixedThreadPool(concurrent.value()));
                     
                    /*
                     *  Use a latch to pause all threads at a certain point, then
                     *  cause them all to become eligible for execution at once
                     *  for maximum possibility of contention
                     */  
                    final CountDownLatch simultaneousStart = new CountDownLatch(1);
                    
                    // create the tasks
                    for (int i = 0; i < concurrent.value(); i++) {
                        completionService.submit(new Callable<Void>() {
                            
                            public Void call() throws Exception {
                                try {
                                    simultaneousStart.await();
                                    
                                    statement.evaluate();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                } catch (Throwable throwable) {
                                    if (throwable instanceof Exception) {
                                        throw (Exception) throwable;
                                    } else if (throwable instanceof Error) {
                                        throw (Error) throwable;
                                    }
                                    RuntimeException r = new RuntimeException(throwable.getMessage(), throwable);
                                    r.setStackTrace(throwable.getStackTrace());
                                    throw r;
                                }
                                return null;
                            }
                        });
                    }
                    simultaneousStart.countDown();
                    Throwable throwable = null;
                    for (int i = 0; i < concurrent.value(); i++) {
                        try {
                            completionService.take().get();
                        } catch (ExecutionException e) {
                            // only keep the first exception, but wait for all threads to finish
                            if(throwable == null) {
                                throwable = e.getCause();
                        	}
                        }
                    }
                    if(throwable != null) {
                    	throw throwable;
                    }
                }
            }
        };
    }
}
