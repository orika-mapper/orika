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
package ma.glasnost.orika.test.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import junit.framework.Assert;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.ConcurrentRule;
import ma.glasnost.orika.test.ConcurrentRule.Concurrent;
import ma.glasnost.orika.util.Ordering;
import ma.glasnost.orika.util.SortedCollection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

/**
 * This test is an attempt to trigger invalid insertion order
 * in SortedCollection;
 * 
 */
public class SortedCollectionConcurrencyTestCase {
    
    @Rule
    public ConcurrentRule rule = new ConcurrentRule();
    
    private static Queue<MapperKey> mapperKeys;
    private static SortedCollection<MapperKey> sortedCollection;
    private static int initialSize = 0;
    
    @BeforeClass
    public static void setup() {
        
        
        List<MapperKey> keys = new ArrayList<MapperKey>();
        
        keys.add(new MapperKey(TypeFactory.valueOf(A1.class), TypeFactory.valueOf(B1.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A2.class), TypeFactory.valueOf(B2.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A3.class), TypeFactory.valueOf(B3.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A4.class), TypeFactory.valueOf(B4.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A5.class), TypeFactory.valueOf(B5.class)));
        
        keys.add(new MapperKey(TypeFactory.valueOf(A1.class), TypeFactory.valueOf(C1.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A2.class), TypeFactory.valueOf(C2.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A3.class), TypeFactory.valueOf(C3.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A4.class), TypeFactory.valueOf(C4.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A5.class), TypeFactory.valueOf(C5.class)));
        
        keys.add(new MapperKey(TypeFactory.valueOf(A1.class), TypeFactory.valueOf(D1.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A2.class), TypeFactory.valueOf(D2.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A3.class), TypeFactory.valueOf(D3.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A4.class), TypeFactory.valueOf(D4.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A5.class), TypeFactory.valueOf(D5.class)));
        
        keys.add(new MapperKey(TypeFactory.valueOf(B1.class), TypeFactory.valueOf(C1.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B2.class), TypeFactory.valueOf(C2.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B3.class), TypeFactory.valueOf(C3.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B4.class), TypeFactory.valueOf(C4.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B5.class), TypeFactory.valueOf(C5.class)));
        
        keys.add(new MapperKey(TypeFactory.valueOf(B1.class), TypeFactory.valueOf(D1.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B2.class), TypeFactory.valueOf(D2.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B3.class), TypeFactory.valueOf(D3.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B4.class), TypeFactory.valueOf(D4.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B5.class), TypeFactory.valueOf(D5.class)));
        
        keys.add(new MapperKey(TypeFactory.valueOf(C1.class), TypeFactory.valueOf(D1.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(C2.class), TypeFactory.valueOf(D2.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(C3.class), TypeFactory.valueOf(D3.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(C4.class), TypeFactory.valueOf(D4.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(C5.class), TypeFactory.valueOf(D5.class)));
        
        // ~~~~~~~~~~~~~~~~~~
        // Add some mismatched mapper keys, in attempt to confuse the ordering
        // process
        
        keys.add(new MapperKey(TypeFactory.valueOf(A1.class), TypeFactory.valueOf(B5.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A2.class), TypeFactory.valueOf(B4.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A4.class), TypeFactory.valueOf(B2.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A5.class), TypeFactory.valueOf(B1.class)));
        
        keys.add(new MapperKey(TypeFactory.valueOf(A1.class), TypeFactory.valueOf(C5.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A2.class), TypeFactory.valueOf(C4.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A4.class), TypeFactory.valueOf(C2.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A5.class), TypeFactory.valueOf(C1.class)));
        
        keys.add(new MapperKey(TypeFactory.valueOf(A1.class), TypeFactory.valueOf(D5.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A2.class), TypeFactory.valueOf(D4.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A4.class), TypeFactory.valueOf(D2.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(A5.class), TypeFactory.valueOf(D1.class)));
        
        keys.add(new MapperKey(TypeFactory.valueOf(B1.class), TypeFactory.valueOf(C5.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B2.class), TypeFactory.valueOf(C4.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B4.class), TypeFactory.valueOf(C2.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B5.class), TypeFactory.valueOf(C1.class)));
        
        keys.add(new MapperKey(TypeFactory.valueOf(B1.class), TypeFactory.valueOf(D5.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B2.class), TypeFactory.valueOf(D4.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B4.class), TypeFactory.valueOf(D2.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(B5.class), TypeFactory.valueOf(D1.class)));
        
        keys.add(new MapperKey(TypeFactory.valueOf(C1.class), TypeFactory.valueOf(D5.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(C2.class), TypeFactory.valueOf(D4.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(C4.class), TypeFactory.valueOf(D2.class)));
        keys.add(new MapperKey(TypeFactory.valueOf(C5.class), TypeFactory.valueOf(D1.class)));
        
        Collections.shuffle(keys);
        mapperKeys = new ConcurrentLinkedQueue<MapperKey>(keys);
        initialSize = mapperKeys.size();
        sortedCollection = new SortedCollection<MapperKey>(Ordering.MAPPER_KEY);
    }
    
    @Test
    @Concurrent(200)
    public void testOrdering() {
        try {
            while (!mapperKeys.isEmpty())
                sortedCollection.add(mapperKeys.remove());
        } catch (NoSuchElementException e) {
            
        }
    }
    
    @AfterClass
    public static void teardown() {
        Assert.assertEquals(initialSize, sortedCollection.size());
        
        Set<MapperKey> seen = new HashSet<MapperKey>();
        for (MapperKey key : sortedCollection) {
            for (MapperKey seenKey : seen) {
                if ((seenKey.getAType().isAssignableFrom(key.getAType()) && seenKey.getBType().isAssignableFrom(key.getBType()))
                        || (seenKey.getAType().isAssignableFrom(key.getBType()) && seenKey.getBType().isAssignableFrom(key.getAType()))) {
                    Assert.fail("saw a parent " + seenKey + " before it's child " + key);
                }
            }
            seen.add(key);
        } 
    }
    
    public static class A1 {
        
    }
    
    public static class A2 extends A1 {
        
    }
    
    public static class A3 extends A2 {
        
    }
    
    public static class A4 extends A3 {
        
    }
    
    public static class A5 extends A4 {
        
    }
    
    public static class B1 {
        
    }
    
    public static class B2 extends B1 {
        
    }
    
    public static class B3 extends B2 {
        
    }
    
    public static class B4 extends B3 {
        
    }
    
    public static class B5 extends B4 {
        
    }
    
    public static class C1 {
        
    }
    
    public static class C2 extends C1 {
        
    }
    
    public static class C3 extends C2 {
        
    }
    
    public static class C4 extends C3 {
        
    }
    
    public static class C5 extends C4 {
        
    }
    
    public static class D1 {
        
    }
    
    public static class D2 extends D1 {
        
    }
    
    public static class D3 extends D2 {
        
    }
    
    public static class D4 extends D3 {
        
    }
    
    public static class D5 extends D4 {
        
    }
    
}
