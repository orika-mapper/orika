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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * NPE when mapping a class implementing a map.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/92">https://code.google.com/archive/p/orika/</a>
 * 
 */
public class Issue92TestCase {

        private MapperFactory factory;
        
        @BeforeClass
        public static void setUpBeforeClass() throws Exception {
        }

        @AfterClass
        public static void tearDownAfterClass() throws Exception {
        }

        @Before
        public void setUp() throws Exception {
                factory = new DefaultMapperFactory.Builder().build();
        }

        @After
        public void tearDown() throws Exception {
        }

        @Test
        public void test() {
                factory.classMap(CustomMap.class, CustomClass.class).field("id", "id").register();
                CustomMap customMap = new CustomMap();
                customMap.setId("test");
                CustomClass customClass = factory.getMapperFacade().map(customMap, CustomClass.class);
                assertThat(customClass.getId(),is(equalTo(customMap.getId())));
        }

        static public class CustomMap extends HashMap<String,String> {
                private String id;

                /**
                 * @return the id
                 */
                public final String getId() {
                        return id;
                }

                /**
                 * @param id the id to set
                 */
                public final void setId(String id) {
                        this.id = id;
                }
        }
        
        static public class CustomClass {
                private String id;

                /**
                 * @return the id
                 */
                public final String getId() {
                        return id;
                }

                /**
                 * @param id the id to set
                 */
                public final void setId(String id) {
                        this.id = id;
                }
        }
}
