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
package ma.glasnost.orika.test.extensibility;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import junit.framework.Assert;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.test.common.types.TestCaseClasses.Author;
import ma.glasnost.orika.test.common.types.TestCaseClasses.AuthorDTO;
import ma.glasnost.orika.test.common.types.TestCaseClasses.AuthorImpl;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

/**
 * This test verifies (demonstrates) the capability of DefaultMapperFactory
 * to be extended
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class DefaultMapperFactoryExtensibilityTestCase {

	
	public static class MyMapperFactory extends DefaultMapperFactory {
	
		private static final Logger LOGGER = LoggerFactory.getLogger(MyMapperFactory.class);
		private volatile MapperFacade mapperFacadeProxy;
		
		/**
		 * We extend the builder of DefaultMapperFactory for one which
		 * can create instances of our own MapperFactory type.
		 *
		 */
		public static class Builder extends MapperFactoryBuilder<MyMapperFactory, Builder> {
			
			protected boolean traceMethodCalls = true;
			
			protected Builder self() {
				return this;
			}
			
			public Builder traceMethodCalls(boolean trace) {
				this.traceMethodCalls = trace;
				return self();
			}
			
			public MyMapperFactory build() {
				return new MyMapperFactory(this);
			}
		}
		
		/**
		 * Since DefaultMapperFactory uses (some form of) the Builder pattern, we
		 * need to provide a constructor which can accept an appropriate builder
		 * and pass it to the super constructor.
		 * 
		 * @param builder
		 */
		protected MyMapperFactory(Builder builder) {
			super(builder);
		}
		
		/* (non-Javadoc)
		 * @see ma.glasnost.orika.impl.DefaultMapperFactory#getMapperFacade()
		 */
		public MapperFacade getMapperFacade() {
			if (mapperFacadeProxy == null) {
				synchronized(this) {
					if (mapperFacadeProxy == null) {
						/*
						 * Provide our own instance as a method-tracing proxy
						 */
						mapperFacadeProxy = TracingMapperFacade.proxyFor(super.getMapperFacade());
					}
				}
			}
			return mapperFacadeProxy;
		}
		
	
		/**
		 * TracingMapperFacade provides custom wrappers around MapperFacade
		 * which log all method calls against it. 
		 * 
		 * @author matt.deboer@gmail.com
		 *
		 */
		private static class TracingMapperFacade implements InvocationHandler {
	
			private MapperFacade delegate;
			
			public static MapperFacade proxyFor(MapperFacade delegate) {
				return (MapperFacade) Proxy.newProxyInstance(
						TracingMapperFacade.class.getClassLoader(), 
						new Class<?>[]{MapperFacade.class}, 
						new TracingMapperFacade(delegate));
			}
			
			private TracingMapperFacade(MapperFacade delegate) {
				this.delegate = delegate;
			}
			/* (non-Javadoc)
			 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
			 */
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				try {
					/*
					 * Sure, you could probably do this better with AOP, but
					 * we needed an example, right?
					 */
					final String methodName = delegate.getClass().getCanonicalName() + 
							"#" + method.getName();
					long start = System.currentTimeMillis();
					LOGGER.info("\n---BEGIN: " + methodName + "( " + Arrays.toString(args) + ")");
					Object result = method.invoke(delegate, args);
					long elapsed = System.currentTimeMillis() - start;
					LOGGER.info("\n---END: " + methodName + ": elapsed[" +elapsed + "ms]");
					return result;
				} catch (InvocationTargetException e) {
					throw e.getTargetException();
				}
			}
		}
		
	}
	
	
	@Test
	public void testExtendedMapper() {
		MyMapperFactory factory = new MyMapperFactory.Builder().useAutoMapping(true).traceMethodCalls(true).build();
		Author author = new AuthorImpl("Test Author");
		
		AuthorDTO mapped = factory.getMapperFacade().map(author, AuthorDTO.class);
		Assert.assertNotNull(mapped);
		Assert.assertEquals(author.getName(), mapped.getName());
	}
	
}
