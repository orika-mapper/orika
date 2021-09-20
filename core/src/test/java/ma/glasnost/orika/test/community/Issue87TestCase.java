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
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Nested properties: Allow abstract nested property's fields mapping.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/87">https://code.google.com/archive/p/orika/</a>
 * 
 */
public class Issue87TestCase {

    private MapperFactory mapperFactory;

    @Before
    public void createMapperFactoryAndDefineMapping() {
            mapperFactory = new DefaultMapperFactory.Builder()
                            .useAutoMapping(false).build();
            mapperFactory.classMap(FakeBeanA.class, FakeBeanB.class)
                            .field("fieldA", "fieldB.nestedField").register();
    }

    @Test(expected = MappingException.class)
    public void cannotMapAbstractNestedPropertyWhenConcreteTypeIsNotRegistered() {

            // We expect to get a MappingException. Indeed, Orika doesn't know how
            // to create an
            // instance of AbstractNestedField (the type of FakeBeanB.fieldB)
            createAnInstanceOfFakeBeanAAndMapItToFakeBeanB();
    }

    @Test
    public void mapAbstractNestedPropertyWhenConcreteTypeIsRegistered() {

            // Register concrete type for AbstractNestedType
            mapperFactory.registerConcreteType(AbstractNestedType.class,
                            NestedType.class);

            // Orika should be able to create an instance of FieldB abstract type
            // (as we have explicitly defined above the concrete type to create and
            // the SimpleConstructorResolverStrategy is normally able to create an
            // instance of this concrete type)
            // Therefore, the mapping should work !
            createAnInstanceOfFakeBeanAAndMapItToFakeBeanB();
    }

    private void createAnInstanceOfFakeBeanAAndMapItToFakeBeanB() {

            // Create an instance of FakeBeanA and assign a value to its fieldA
            FakeBeanA fakeBeanA = new FakeBeanA();
            fakeBeanA.fieldA = 42;

            // Try the mapping from fakeBeanA to FakeBeanB
            FakeBeanB fakeBeanB = mapperFactory.getMapperFacade().map(fakeBeanA,
                            FakeBeanB.class);

            // Assert the fieldA has been correctly mapped to fieldB.nestedField
            Assert.assertEquals(fakeBeanA.fieldA, fakeBeanB.fieldB.nestedField);
    }

    public static class FakeBeanA {
            public int fieldA;
    }

    public static class FakeBeanB {
            public AbstractNestedType fieldB;
    }

    public static class NestedType extends AbstractNestedType {
    }

    public static abstract class AbstractNestedType {
            public int nestedField;
    }

}
