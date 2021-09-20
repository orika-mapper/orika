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

package ma.glasnost.orika.test.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test a class that User: kenton Date: 12/7/12 Time: 10:50 AM
 */
public class ExplicitCollectionTestCase {
    
    @Test
    public void testStringToStringWithSpecifiedGenericType() {
        ExplicitSet set = new ExplicitSet();
        set.add("1");
        set.add("2");
        A source = new A();
        source.setStrings(set);
        
        B destination = MappingUtil.getMapperFactory().getMapperFacade().map(source, B.class);
        
        Assert.assertNotNull(destination.getStrings());
        Assert.assertEquals(set.size(), destination.getStrings().size());
    }
    
    public static class A {
        private ExplicitSet strings;
        
        public ExplicitSet getStrings() {
            return strings;
        }
        
        public void setStrings(ExplicitSet strings) {
            this.strings = strings;
        }
    }
    
    public static class B {
        private Set<String> strings;
        
        public Set<String> getStrings() {
            return strings;
        }
        
        public void setStrings(Set<String> strings) {
            this.strings = strings;
        }
    }
    
    public static class ExplicitSet extends HashSet<String> {
        
        private static final long serialVersionUID = 1L;

        public ExplicitSet(int i) {
            super(i);
        }
        
        public ExplicitSet(int i, float v) {
            super(i, v);
        }
        
        public ExplicitSet(Collection<? extends String> strings) {
            super(strings);
        }
        
        public ExplicitSet() {
        }
    }
}