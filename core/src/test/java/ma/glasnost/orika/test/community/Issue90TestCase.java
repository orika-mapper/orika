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

/**
 * Simple mappings fail.
 * <p>
 * I am trying to map type A to C, then B to C. I could reproduce with really simple classes, A, B, C being a class with a single string
 * attribute named "s".
 * <p>
 * This fails with exception "B is an unsupported source class". Looks like Orika uses the first converter: it expects A and gets B instead.
 * <p>
 * The exception goes away when I explicitly register the mappings A->C, B->C. This is tedious because this forces me to register all
 * possible mappings (a lot in my case!) during initialization.
 * <p>
 * Please check out attached test case.
 * <p>
 * I could reproduce the issue with Orika 1.4.1 and 1.4.2-SNAPSHOT.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/90">https://code.google.com/archive/p/orika/</a>
 * 
 */
public class Issue90TestCase {

    public static class A {

        private final String s;

        public A(String s) {
            this.s = s;
        }
        
        public String getS() {
            return s;
        }
    }

    public static class B {

        private final String s;

        public B(String s) {
            this.s = s;
        }
        public String getS() {
            return s;
        }
    }

    public static class C {

        private final String s;

        public C(String s) {
            this.s = s;
        }
        public String getS() {
            return s;
        }
    }

    @Test
    public void reproduce() throws Exception {
                final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        
        // Uncomment registers below to workaround the problem...
                //mapperFactory.classMap(A.class, C.class).byDefault().register();
                //mapperFactory.classMap(B.class, C.class).byDefault().register();
        
                final MapperFacade mapper = mapperFactory.getMapperFacade();
        A a = new A("foo");
        C c = mapper.map(a, C.class);
        B b = new B("bar");
        c = mapper.map(b, C.class);     // This one fails
    }
}
