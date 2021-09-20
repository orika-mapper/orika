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
package ma.glasnost.orika.test.inheritance;

import org.junit.Assert;
import org.junit.Test;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class InitializeUsedMappersTestCase {

    public static class A1 {
        public String name1;
    }

    public static class A2 extends A1 {
        public String name2;
    }

    public static class A3 extends A2 {
        public String name3;
    }

    public static class B1 {
        public String name1;
    }

    public static class B2 extends B1 {
        public String name2;
    }

    public static class B3 extends B2 {
        public String name3;
    }


    @Test
    public void testParentLookup() {

        MapperFactory factory = MappingUtil.getMapperFactory();

        factory.registerClassMap(
                factory.classMap(B2.class, A2.class)
                    .use(B1.class,A1.class)
                    .byDefault());
        factory.registerClassMap(
                factory.classMap(A1.class, B1.class)
                .byDefault());

        MapperFacade mapper = factory.getMapperFacade();

        A3 a = new A3();
        a.name1 = "a1";
        a.name2 = "a2";
        a.name3 = "a3";


        B3 b = mapper.map(a, B3.class);

        Assert.assertNotNull(b);
        Assert.assertEquals(a.name1, b.name1);
        Assert.assertEquals(a.name2, b.name2);
    }

	public static class ABase {
		private String property;

		public String getProperty() {
			return property;
		}

		public void setProperty(String property) {
			this.property = property;
		}
	}

	public static class ASub extends ABase {
	}

	public static class BBase {
		private String property;
		private int timesSet = 0;

		public String getProperty() {
			return property;
		}

		public void setProperty(String property) {
			this.property = property;
			timesSet++;
		}

		public int getTimesSet() {
			return timesSet;
		}
	}

	public static class BSub extends BBase {
	}

	@Test
	public void testParentPropertiesSetOnce() {

		MapperFactory factory = MappingUtil.getMapperFactory();

		factory.registerClassMap(factory.classMap(ABase.class, BBase.class)
				.byDefault());
		factory.registerClassMap(factory.classMap(ASub.class, BSub.class)
				.byDefault());

		MapperFacade mapper = factory.getMapperFacade();

		ASub a = new ASub();
		a.setProperty("property");

		BSub b = mapper.map(a, BSub.class);

		Assert.assertNotNull(b);
		Assert.assertEquals(a.getProperty(), b.getProperty());
		Assert.assertEquals(1, b.getTimesSet());
	}
}
