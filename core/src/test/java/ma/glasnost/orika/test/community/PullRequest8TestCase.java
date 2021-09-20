/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
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

package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.impl.DefaultConstructorObjectFactory;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * lookupObjectFactory should consider superclasses for source type.
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/pull/8">https://github.com/orika-mapper/orika</a>
 */
public class PullRequest8TestCase {
	public static class MySourceType {
	}

	public static class MyType {
	}

	public static class MyObjectFactory<T> extends
			DefaultConstructorObjectFactory<T> {
		public MyObjectFactory(Class<T> type) {
			super(type);
		}
	}

	@Test
	public void test() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.registerObjectFactory(
				new MyObjectFactory<>(MyType.class),
				TypeFactory.valueOf(MyType.class));
		factory.registerClassMap(factory.classMap(MySourceType.class,
				MyType.class).toClassMap());
		ObjectFactory<MyType> myFactory = factory.lookupObjectFactory(
				TypeFactory.valueOf(MyType.class),
				TypeFactory.valueOf(MySourceType.class));
        assertThat(myFactory, is(instanceOf(MyObjectFactory.class)));
	}
}
