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

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.impl.GeneratedMapperBase;
import ma.glasnost.orika.impl.MultipleMapperWrapper;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.test.TestUtil;
import ma.glasnost.orika.test.community.Issue24TestCase;
import ma.glasnost.orika.test.community.issue121.Issue121TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class MultipleMapperWrapperTest {

    /**
     * Simple Hirarchy Test
     * 
     * @see Issue24TestCase
     * @see <a href="https://code.google.com/archive/p/orika/issues/24">https://code.google.com/archive/p/orika/</a>
     */
    @Test
    public void testFindMapper_withSimpleHirarchy() {
        Exception expectedException;

        // create MultipleMapperWrapper instance to Test:
        final MultipleMapperWrapper multipleMapper = createMultipleMapperWrapper(
                new TypeBuilder<A>(){}.build(), // aTypeMapper
                new TypeBuilder<BSub>(){}.build(), // bTypeMapper
                new TypeBuilder<A>(){}.build(), // aTypeWrapper
                new TypeBuilder<B>(){}.build()); // bTypeWrapper
        
        // validation: If there is no Exception it's OK (the only Mapper where found)
        multipleMapper.mapAtoB(new A(), new BSub(), null);
        multipleMapper.mapAtoB(new A(), new BSubSub(), null);
        multipleMapper.mapAtoB(new ASub(), new BSub(), null);
        multipleMapper.mapAtoB(new ASub2(), new BSub(), null);
        multipleMapper.mapAtoB(new ASubSub(), new BSub(), null);

        // validation ExceptionCases: No Mapper should be found:
        
        // ExceptionCase 1: BSuper will be expected, but MultipleMapperWrapper knows that it
        // always generates at least the more specific class B:
        expectedException = TestUtil.expectException(() -> multipleMapper.mapAtoB(new A(), new BSuper(), null));
        assertThat(expectedException.getMessage(), containsString("No matching Mapper found for A <-> B\n"));
        assertThat(expectedException.getMessage(), containsString("Existing Mapper: A <-> BSub\n"));

        // ExceptionCase 2: BSub2 will be expected, but no Mapper is included:
        expectedException = TestUtil.expectException(() -> multipleMapper.mapAtoB(new A(), new BSub2(), null));
        assertThat(expectedException.getMessage(), containsString("No matching Mapper found for A <-> BSub2\n"));
        assertThat(expectedException.getMessage(), containsString("Existing Mapper: A <-> BSub\n"));
        
    }
    
    /**
     * With generic-List example
     * 
     * @see Issue121TestCase
     * @see <a href="https://code.google.com/archive/p/orika/issues/121">https://code.google.com/archive/p/orika/</a>
     */
    @Test
    public void testFindMapper_withGenericList() {
        Exception expectedException;

        // create MultipleMapperWrapper instance to Test:
        final MultipleMapperWrapper multipleMapper = createMultipleMapperWrapper(
                new TypeBuilder<List<A>>(){}.build(), // aTypeMapper
                new TypeBuilder<List<BSub>>(){}.build(), // bTypeMapper
                new TypeBuilder<List<A>>(){}.build(), // aTypeWrapper
                new TypeBuilder<List<B>>(){}.build()); // bTypeWrapper
        
        // validation: If there is no Exception it's OK (the only Mapper where found)
        multipleMapper.mapAtoB(new ArrayList<A>(), new ArrayList<BSub>(), null);
        multipleMapper.mapAtoB(new ArrayList<A>(), new ArrayList<BSubSub>(), null);
        multipleMapper.mapAtoB(new ArrayList<ASub>(), new ArrayList<BSub>(), null);
        multipleMapper.mapAtoB(new ArrayList<ASub2>(), new ArrayList<BSub>(), null);
        multipleMapper.mapAtoB(new ArrayList<ASubSub>(), new ArrayList<BSub>(), null);
        multipleMapper.mapAtoB(new ArrayList<ASubSub>(), new ArrayList<BSuper>(), null);
        multipleMapper.mapAtoB(Collections.unmodifiableList(new ArrayList<A>()), new ArrayList<BSub>(), null);

        // validation ExceptionCases: No Mapper should be found:
        // ExceptionCase 1: A HashSet is not enough as Source, Generics get lost for error-Message.
        expectedException = TestUtil.expectException(() -> multipleMapper.mapAtoB(new HashSet<A>(), new ArrayList<BSub>(), null));
        assertThat(expectedException.getMessage(), containsString("No matching Mapper found for HashSet<Object> <-> ArrayList<Object>\n"));
        assertThat(expectedException.getMessage(), containsString("Existing Mapper: List<A> <-> List<BSub>\n"));
        
        // ExceptionCase 2: A HashSet is not enough as Target.
        expectedException = TestUtil.expectException(() -> multipleMapper.mapAtoB(new ArrayList<A>(), new HashSet<BSub>(), null));
        assertThat(expectedException.getMessage(), containsString("No matching Mapper found for ArrayList<Object> <-> HashSet<Object>\n"));
        assertThat(expectedException.getMessage(), containsString("Existing Mapper: List<A> <-> List<BSub>\n"));
        
    }
    
    private MultipleMapperWrapper createMultipleMapperWrapper(Type<?> aTypeMapper, Type<?> bTypeMapper, Type<?> aTypeWrapper,
            Type<?> bTypeWrapper) {
        GeneratedMapperBase generatedMapper = new GeneratedMapperBase() {
        };
        generatedMapper.setAType(aTypeMapper);
        generatedMapper.setBType(bTypeMapper);
        
        List<Mapper<Object, Object>> mappers = Collections.singletonList((Mapper<Object, Object>) generatedMapper);
        @SuppressWarnings("unchecked")
        MultipleMapperWrapper multipleMapper = new MultipleMapperWrapper((Type<Object>) aTypeWrapper, (Type<Object>) bTypeWrapper, mappers);
        return multipleMapper;
    }
    
    public static class ASuper {
        
    }
    
    public static class A extends ASuper {
        
    }
    
    public static class ASub extends A {
        
    }
    
    public static class ASub2 extends A {
        
    }
    
    public static class ASubSub extends ASub {
        
    }
    
    public static class BSuper {
        
    }
    
    public static class B extends BSuper {
        
    }
    
    public static class BSub extends B {
        
    }
    
    public static class BSub2 extends B {
        
    }
    
    public static class BSubSub extends BSub {
        
    }
}
