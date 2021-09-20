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
package ma.glasnost.orika.test.map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.MultipleMapperWrapper;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.DynamicSuite;
import ma.glasnost.orika.test.DynamicSuite.Scenario;
import ma.glasnost.orika.test.DynamicSuite.TestCasePattern;

/**
 * Run All *TestCase.java Unit-Tests, but use always the {@link MultipleMapperWrapper} even if no ObjectFactory is used or only one Mapper
 * where found.
 * <p>
 * This should covering all special cases.
 *
 */
@RunWith(DynamicSuite.class)
@TestCasePattern(".*TestCase")
@Scenario(name = "alwaysCreateMultipleMapperWrapper")
public class MultipleMapperWrapperTestSuite {
    
    @BeforeClass
    public static void alwaysCreateMultipleMapperWrapper() {
        System.setProperty("ma.glasnost.orika.alwaysCreateMultipleMapperWrapper", "true");
        // validate basic pre-requirements for this TestSuite (be sure that the property is set and evaluated right):
        final MapperFactory factory = new DefaultMapperFactory.Builder().build();
        Type<A> aType = TypeFactory.valueOf(A.class);
        Mapper<Object, Object> mapper = factory.lookupMapper(new MapperKey(aType, aType));
        assertThat(mapper, is(instanceOf(MultipleMapperWrapper.class)));
        
    }
    
    @AfterClass
    public static void tearDown() {
        System.clearProperty("ma.glasnost.orika.alwaysCreateMultipleMapperWrapper");
    }
    
    public static class A {
    }
}
