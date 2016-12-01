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
package ma.glasnost.orika.test.metadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class TypeFactoryTestCase {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createTypeFromClass() {
        Type<?> type = TypeFactory.valueOf("java.util.List");
        
        Assert.assertEquals(List.class, type.getRawType());
    }
    
    @Test
    public void createTypeFromClass_defaultPackages() {
        Type<?> type = TypeFactory.valueOf("List");
        
        Assert.assertEquals(List.class, type.getRawType());
        
        type = TypeFactory.valueOf("String");
        
        Assert.assertEquals(String.class, type.getRawType());
    }
    
    @Test
    public void createTypeFromNestedClass() {
        Type<?> type = TypeFactory.valueOf("List<Long>");
        
        Assert.assertEquals(List.class, type.getRawType());
        Assert.assertEquals(Long.class, type.getNestedType(0).getRawType());
    }
    
    @Test
    public void createTypeFromMultipleNestedClass() {
        Type<?> type = TypeFactory.valueOf("List<Map<String,Set<Map<String,java.io.File>>>>");
        
        Assert.assertEquals(List.class, type.getRawType());
        Assert.assertEquals(Map.class, type.getNestedType(0).getRawType());
        Assert.assertEquals(String.class, type.getNestedType(0).getNestedType(0).getRawType());
        Assert.assertEquals(Set.class, type.getNestedType(0).getNestedType(1).getRawType());
        Assert.assertEquals(Map.class, type.getNestedType(0).getNestedType(1).getNestedType(0).getRawType());
        Assert.assertEquals(String.class, type.getNestedType(0).getNestedType(1).getNestedType(0).getNestedType(0).getRawType());
        Assert.assertEquals(File.class, type.getNestedType(0).getNestedType(1).getNestedType(0).getNestedType(1).getRawType());
        
    }

    @Test(expected=IllegalArgumentException.class)
    public void createTypeFromMultipleNestedClass_invalidExpression() {
        TypeFactory.valueOf("List<Map<String,Set<Map<String,java.io.File>>>");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void createTypeFromMultipleNestedClass_invalidType() {
        TypeFactory.valueOf("List<Map<String,Set<Map<String,java.io.FooBar>>>>");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void createTypeWithMultibleBounds() {
        Type<MyObjectWithMultibleBound> valueOf = TypeFactory.valueOf(MyObjectWithMultibleBound.class);
        Assert.assertEquals("MyObjectWithMultibleBound<Set<String>>", valueOf.toString());

    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void createTypeFromClassHirarchy() {
        Type type = TypeFactory.valueOf(MyObject2.class);
        Type<MyInterface> interfaceType = TypeFactory.valueOf(MyInterface.class);

        TypeFactory.valueOf(interfaceType);
        java.lang.reflect.Type firstArgType = type.findInterface(interfaceType).getActualTypeArguments()[0];
        Type firstType = TypeFactory.valueOf(firstArgType);
        Assert.assertEquals(String.class, firstType.getRawType());

        java.lang.reflect.Type secondArgType = type.findInterface(interfaceType).getActualTypeArguments()[1];
        Type secondType = TypeFactory.valueOf(secondArgType);
        Assert.assertEquals(Collection.class, secondType.getRawType());

    }

    @Test
    public void testRefineBoundsSuccess() throws Exception {
        testRefineBoundsSuccess(Long.class, Long.class, Object.class);
        testRefineBoundsSuccess(HashSet.class, Set.class, HashSet.class, Object.class);
    }

    @Test
    public void testRefineBoundsFail() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(containsString("Long")); // it can be "Long and String" or "String and Long"
        thrown.expectMessage(containsString("String"));
        thrown.expectMessage(containsString("are not comparable"));

        testRefineBoundsSuccess(HashSet.class, String.class, Long.class);
    }

    @SuppressWarnings("rawtypes")
    public void testRefineBoundsSuccess(Class<?> expetedClass, Class<?>... boundsClass) throws Exception {
        Set<Type<?>> bounds = new HashSet<Type<?>>();
        for (Class<?> clazz : boundsClass) {
            bounds.add(TypeFactory.valueOf(clazz));
        }
        assertThat(refineBounds(bounds), is((Type) TypeFactory.valueOf(expetedClass)));
    }

    @SuppressWarnings("rawtypes")
    private static Type refineBounds(Set<Type<?>> bounds) throws Exception {
        // call private TypeFactory.refineBounds() per reflection:
        Class<TypeFactory> typeFactoryClass = TypeFactory.class;
        Method refineMethod = typeFactoryClass.getDeclaredMethod("refineBounds", Set.class);
        refineMethod.setAccessible(true);
        try {
            return (Type) refineMethod.invoke(null, bounds);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            }
            throw e;
        }
    }

    public static class MyObject2 implements MyInterface<String, Collection<? super Long>> {
        // test Class
    }

    @SuppressWarnings("unused")
    public static interface MyInterface<A, B> {
        // test Class
    }

    @SuppressWarnings("unused")
    public static class MyObjectWithMultibleBound<T extends Object & Collection<String> & Set<String>> {
        // test Class
    }
}
