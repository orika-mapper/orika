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
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.metadata.Type;

import org.junit.Test;

/**
 * 
 */
public class Issue45TestCase {
	
	
	public static class Source {
		public String name;
		public String description;
	}
	
	public static class SourceChild extends Source {}
	
	public static class Dest1 {
		private String name;
		private String description;
		
		public Dest1(Source src) {
			this.name = src.name;
			this.description = src.description;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}
	}
	
	public static class Dest2 {
		public String name;
		public String description;
	}
	
	
	private static class MyMapper extends ConfigurableMapper {
		
		private static class SourceConverter extends CustomConverter<SourceChild, Dest1> {

			public Dest1 convert(SourceChild source,
					Type<? extends Dest1> destinationType) {
				return new Dest1(source);
			}
		}
		
		public void configure(MapperFactory mapperFactory) {
			mapperFactory.getConverterFactory().registerConverter(new SourceConverter());
		}
	}
	/*
	 * We need a type which can be resolved 2 different ways:
	 * 
	 * SourceChild == Source : mapping
	 * SourceChild == SourceChild : converter
	 * 
	 */
	@Test
	public void testResolveTypes2() {
		
		MapperFacade mapper = new MyMapper();
		
		Source src = new Source();
		src.name = "source 1";
		src.description = "source 1 description";
		
		SourceChild srcChild = new SourceChild();
		srcChild.name = "source 1";
		srcChild.description = "source 1 description";
		
		
		/*
		 * Mapping Source to Dest2 causes a mapping to be created
		 */
		Dest2 dest2 = mapper.map(src, Dest2.class);
		/*
		 * SourceChild is able to use this mapping, so the resolved
		 * type in this case for SourceChild is 'Source', which
		 * gets cached in resolvedTypes
		 */
		Dest2 dest2B = mapper.map(srcChild, Dest2.class);
		
		
		Assert.assertNotNull(dest2);
		Assert.assertNotNull(dest2B);
		
		/*
		 * But now, since the resolvedType for 'SourceChild' has
		 * been cached as 'Source', it cannot find the converter
		 * which has been specifically created for 'SourceChild'
		 */
		Dest1 dest1 = mapper.map(srcChild, Dest1.class);
		Assert.assertNotNull(dest1);
	}
}
