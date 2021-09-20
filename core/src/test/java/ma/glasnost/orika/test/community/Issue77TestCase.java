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

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * A sub type is not being mapped to the configured mapping type.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/77">https://code.google.com/archive/p/orika/</a>
 *
 */
public class Issue77TestCase {
    
    public static class A1 {

    }

    public static class B1 extends A1 {
        int someProperty;
    }

    public static class Container1 {
        private List<A1>    elements    = new ArrayList<>();
        private A1          singleElement;

        public List<A1> getElements() {
            return elements;
        }

        public void setElements(final List<A1> elements) {
            this.elements = elements;
        }

        public A1 getSingleElement() {
            return singleElement;
        }

        public void setSingleElement(final A1 singleElement) {
            this.singleElement = singleElement;
        }
    }

    public static class A2 {

    }

    public static class B2 extends A2 {
        int someProperty;
    }

    public static class Container2 {
        private List<A2>    elements    = new ArrayList<>();
        private A2          singleElement;

        public List<A2> getElements() {
            return elements;
        }

        public void setElements(final List<A2> elements) {
            this.elements = elements;
        }

        public A2 getSingleElement() {
            return singleElement;
        }

        public void setSingleElement(final A2 singleElement) {
            this.singleElement = singleElement;
        }
    }

    @Test
    public void testMappingSubTypes() {
        // Test data
        final Container1 c1 = new Container1();
        c1.elements.add(new B1());
        c1.singleElement = new B1();

        // mapper
        final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory.classMap(B1.class, B2.class).byDefault().register();
        mapperFactory.classMap(B2.class, B1.class).byDefault().register();
        final MapperFacade mapper = mapperFactory.getMapperFacade();

        // map
        final Container2 c2 = mapper.map(c1, Container2.class);

        // check
        assertThat(c1.singleElement, is(instanceOf(B1.class)));
        assertThat(c2.singleElement, is(instanceOf(B2.class)));
        assertThat(c2.elements.get(0), is(instanceOf(B2.class)));
    }  
}
