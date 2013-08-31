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
package ma.glasnost.orika.examples;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 * 
 */
public class Example2TestCase {
    
    public static class Property {
        public String key;
        public String value;
        
        public Property(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String toString() {
            return "[" + key + "=" + value + "]";
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Property other = (Property) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }
        
    }
    
    public static class A {
        public A1 a1;
        public A2 a2;
        public Property p1;
        public Property p2;
    }
    
    public static class A1 {
        public Property Pa11;
        public Property Pa12;
    }
    
    public static class A2 {
        public Property Pa21;
        public Property Pa22;
    }
    
    public static class B {
        public Property p1;
        public Property p2;
    }
    
    public static class B1 extends B {
        public Property Pb11;
        public Property Pb12;
    }
    
    public static class B2 extends B {
        public Property Pb21;
        public Property Pb22;
    }
    
    /**
     * 
     * How to map the
     * 
     * 1. A.p1 -> b.p1
     * 
     * 2. A.p2 -> b.p2
     * 
     * 3. A.a1.Pa11 -> B1.Pb11.
     * 
     * 4. A.a1.Pa12 -> B1.Pb12
     * 
     * 5. A.a2.Pa11 -> B2.Pb11.
     * 
     * 6. A.a2.Pa12 -> B2.Pb12
     */
    @Test
    public void map() {
        
        /*
         * Construct the mapper factory;
         */
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        
        /*
         * Register mappings for the fields whose names done match; 'byDefault' covers the matching ones
         */
        mapperFactory.classMap(A.class, B1.class)
            .field("a1.Pa11", "Pb11")
            .field("a1.Pa12", "Pb12")
            .byDefault()
            .register();
        
        mapperFactory.classMap(A.class, B2.class)
            .field("a2.Pa21", "Pb21")
            .field("a2.Pa22", "Pb22")
            .byDefault()
            .register();
        
        /*
         * Construct some test object
         */
        A source = new A();
        source.p1 = new Property("p1", "p1.value");
        source.p2 = new Property("p2", "p2.value");
        source.a1 = new A1();
        source.a1.Pa11 = new Property("Pa11", "Pa11.value");
        source.a1.Pa12 = new Property("Pa12", "Pa12.value");
        source.a2 = new A2();
        source.a2.Pa21 = new Property("Pa21", "Pa21.value");
        source.a2.Pa22 = new Property("Pa22", "Pa22.value");
        
        MapperFacade mapper = mapperFactory.getMapperFacade();
        
        Collection<A> collectionA = new ArrayList<A>();
        collectionA.add(source);

        /*
         * Map the collection of A into a collection of B1 using 'mapAsList'
         */
        Collection<B1> collectionB1 = mapper.mapAsList(collectionA, B1.class);
        
        Assert.assertNotNull(collectionB1);
        B1 b1 = collectionB1.iterator().next();
        Assert.assertEquals(source.p1, b1.p1);
        Assert.assertEquals(source.p2, b1.p2);
        Assert.assertEquals(source.a1.Pa11, b1.Pb11);
        Assert.assertEquals(source.a1.Pa12, b1.Pb12);
        
        /*
         * Map the collection of A into a collection of B2 using 'mapAsList'
         */
        Collection<B2> collectionB2 = mapper.mapAsList(collectionA, B2.class);
        
        B2 b2 = collectionB2.iterator().next();
        Assert.assertNotNull(b2);
        Assert.assertEquals(source.p1, b2.p1);
        Assert.assertEquals(source.p2, b2.p2);
        Assert.assertEquals(source.a2.Pa21, b2.Pb21);
        Assert.assertEquals(source.a2.Pa22, b2.Pb22);
    }
    
}
