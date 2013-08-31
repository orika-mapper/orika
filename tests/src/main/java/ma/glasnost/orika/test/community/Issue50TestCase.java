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
package ma.glasnost.orika.test.community;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 * 
 */
public class Issue50TestCase {

	public static class Source {
		public String id;
		public String type;
		public String name;
		public int age;
	}

	public static class Dest {
		public String id;
		public String type;
		public String name;
		public int age;
	}

	public static class SubSource extends Source {
		public String description;
		public double weight;
	}

	public static class SubDest extends Dest {
		public String description;
		public double weight;
	}
	
	public static class SubSource2 extends SubSource {
		public String taxId;
	}

	public static class SubDest2 extends SubDest {
		public String taxId;
	}

	@Test
	public void testExcludedFields() {

		MapperFactory factory = 
				new DefaultMapperFactory.Builder()
					.compilerStrategy(new EclipseJdtCompilerStrategy())
					.build();

		
		factory.registerClassMap(factory.classMap(Source.class, Dest.class)
				.exclude("id")
				.exclude("type")
				.byDefault());

		factory.registerClassMap(factory
				.classMap(SubSource.class, SubDest.class)
				.use(Source.class, Dest.class).byDefault());
		
		SubSource source = new SubSource();
		source.id = "1";
		source.type = "A";
		source.name = "Bob";
		source.age = 55;

		SubDest destination = factory.getMapperFacade().map(source, SubDest.class);
		Assert.assertNull(destination.id);
		Assert.assertNull(destination.type);
		Assert.assertEquals(source.name, destination.name);
		Assert.assertEquals(source.age, destination.age);
	}

	/**
	 * This test case verifies that an excluded mapping from a used mapper
	 * can be overridden by explicitly specifying the field
	 */
	@Test
	public void testOverrideExcludedFields() {

		MapperFactory factory = 
				new DefaultMapperFactory.Builder()
					.compilerStrategy(new EclipseJdtCompilerStrategy())
					.build();

		
		factory.registerClassMap(factory.classMap(Source.class, Dest.class)
				.exclude("id")
				.exclude("type")
				.byDefault());

		factory.registerClassMap(factory
				.classMap(SubSource2.class, SubDest2.class)
				.field("type", "type")
				.use(Source.class, Dest.class)
				.byDefault());
		
		SubSource2 source = new SubSource2();
		source.id = "1";
		source.type = "A";
		source.name = "Bob";
		source.age = 55;

		SubDest2 destination = factory.getMapperFacade().map(source, SubDest2.class);
		Assert.assertNull(destination.id);
		Assert.assertEquals(source.type, destination.type);
		Assert.assertEquals(source.name, destination.name);
		Assert.assertEquals(source.age, destination.age);
	}
	
}
