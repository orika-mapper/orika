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

package ma.glasnost.orika.test.generics;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.UtilityResolver;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.property.PropertyResolverStrategy;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class GenericsTestCase {
    
    @Test
    public void testTypeErasure() {
        MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();
        EntityLong entity = new EntityLong();
        entity.setId(42L);
        
        new EntityGeneric<String>().setId("Hello");
        new EntityGeneric<Integer>().setId(42);
        EntityLong clone = mapperFacade.map(entity, EntityLong.class);
        
        Assert.assertEquals(Long.valueOf(42L), clone.getId());
    }
    
    @Test
    public void testTypeErasure2() {
        MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();
        EntityLong entity = new EntityLong();
        entity.setId(42L);
        
        new EntityGeneric<String>().setId("Hello");
        EntityGeneric<Long> sourceObject = new EntityGeneric<Long>();
        sourceObject.setId(42L);
        EntityLong clone = mapperFacade.map(sourceObject, EntityLong.class);
        
        Assert.assertEquals(Long.valueOf(42L), clone.getId());
    }
    
    @Test
    public void testGenericsWithNestedParameterizedTypes() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        try {
            /*
             * Note that the normal lookup of nested property doesn't work,
             * since EntityGeneric doesn't have actualized type arguments
             */
            factory.classMap(EntityGeneric.class, EntityLong.class).field("id.key", "id").toClassMap();
            Assert.fail("should throw exception for unresolvable nested property");
        } catch (Exception e) {
            Assert.assertTrue(e.getLocalizedMessage().contains("could not resolve nested property [id.key]"));
        }
        
        // If we explicitly declare the generic type for the source object,
        // we can successfully register the class map
        Type<EntityGeneric<NestedKey<Long>>> sourceType = new TypeBuilder<EntityGeneric<NestedKey<Long>>>() {}.build();
        factory.registerClassMap(factory.classMap(sourceType, EntityLong.class).field("id.key", "id").toClassMap());
        
        MapperFacade mapperFacade = factory.getMapperFacade();
        
        EntityGeneric<NestedKey<Long>> sourceObject = new EntityGeneric<NestedKey<Long>>();
        
        NestedKey<Long> key = new NestedKey<Long>();
        key.setKey(42L);
        sourceObject.setId(key);
        Type<EntityLong> _Entity_Long = TypeFactory.valueOf(EntityLong.class);
        
        EntityLong clone = mapperFacade.map(sourceObject, sourceType, _Entity_Long);
        
        Assert.assertEquals(Long.valueOf(42L), clone.getId());
    }
    
    @Test
    public void testParameterizedPropertyUtil() {
        
        Type<?> t = new TypeBuilder<TestEntry<Holder<Long>, Holder<String>>>() {}.build();
        PropertyResolverStrategy propertyResolver = UtilityResolver.getDefaultPropertyResolverStrategy();
        Property p = propertyResolver.getNestedProperty(t, "key.held");
        Assert.assertEquals(p.getType().getRawType(), Long.class);
        Assert.assertEquals(p.getType(), TypeFactory.valueOf(Long.class));
        
        Map<String, Property> properties = propertyResolver.getProperties(t);
        Assert.assertTrue(properties.containsKey("key"));
        Assert.assertEquals(properties.get("key").getType(), new TypeBuilder<Holder<Long>>() {}.build());
    }
    
    @Test
    public void testMappingParameterizedTypes() {
        
        Type<TestEntry<Holder<Long>, Holder<String>>> fromType = new TypeBuilder<TestEntry<Holder<Long>, Holder<String>>>() {}.build();
        Type<OtherTestEntry<Container<String>, Container<String>>> toType = new TypeBuilder<OtherTestEntry<Container<String>, Container<String>>>() {}.build();
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        TestEntry<Holder<Long>, Holder<String>> fromObject = new TestEntry<Holder<Long>, Holder<String>>();
        fromObject.setKey(new Holder<Long>());
        fromObject.getKey().setHeld(Long.valueOf(42L));
        fromObject.setValue(new Holder<String>());
        fromObject.getValue().setHeld("What is the meaning of life?");
        
        factory.registerClassMap(
                factory.classMap(
                        new TypeBuilder<Holder<String>>(){}.build(), 
                        new TypeBuilder<Container<String>>(){}.build())
                        .field("held", "contained").byDefault().toClassMap());
        
        factory.registerClassMap(
                factory.classMap(
                        new TypeBuilder<Holder<Long>>(){}.build(), 
                        new TypeBuilder<Container<String>>(){}.build())
                        .field("held", "contained").byDefault().toClassMap());
        
        MapperFacade mapper = factory.getMapperFacade();
        
        OtherTestEntry<Container<String>, Container<String>> result = mapper.map(fromObject, fromType, toType);
        
        Assert.assertNotNull(result);
        Assert.assertEquals("" + fromObject.getKey().getHeld(), result.getKey().getContained());
        Assert.assertEquals(fromObject.getValue().getHeld(), result.getValue().getContained());
    }
    
    @Test
    public void testMappingParameterizedTypes2() {
       
        Type<Entry<Container<Holder<Long>>, Envelope<Container<String>>>> fromType = 
                new TypeBuilder<Entry<Container<Holder<Long>>, Envelope<Container<String>>>>() {}.build();
        Type<Entry<Holder<String>, Container<String>>> toType = 
                new TypeBuilder<Entry<Holder<String>, Container<String>>>() {}.build();
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        // Construct our elaborate 'fromObject'
        Entry<Container<Holder<Long>>, Envelope<Container<String>>> fromObject = 
                new Entry<Container<Holder<Long>>, Envelope<Container<String>>>();
        Container<Holder<Long>> container = new Container<Holder<Long>>();
        Holder<Long> holder = new Holder<Long>();
        holder.setHeld(Long.valueOf(42L));
        container.setContained(holder);
        fromObject.setKey(container);
        
        Envelope<Container<String>> envelope = new Envelope<Container<String>>();
        Container<String> container2 = new Container<String>();
        container2.setContained("What is the meaning of life?");
        envelope.setContents(container2);
        fromObject.setValue(envelope);
        
        factory.registerClassMap(
                factory.classMap(fromType, toType)
                .field("key.contained.held", "key.held")
                .field("value.contents.contained", "value.contained")
                /*.byDefault()*/.toClassMap());
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Entry<Holder<String>, Container<String>> result = mapper.map(fromObject, fromType, toType);
        
        Assert.assertNotNull(result);
        Assert.assertEquals("" + fromObject.getKey().getContained().getHeld(), "" +result.getKey().getHeld());
        Assert.assertEquals("" + fromObject.getValue().getContents().getContained(), ""+result.getValue().getContained());
    }
    
    /**
     * This test confirms that multiple mappings using different
     * parameterizations of the same raw class type do not overwrite each other
     * or cause conflicts
     */
    @Test
    public void testMultipleMappingsForParameterizedTypes() {
        
        Type<TestEntry<Holder<Long>, Holder<String>>> fromType = new TypeBuilder<TestEntry<Holder<Long>, Holder<String>>>() {}.build();
        Type<OtherTestEntry<Container<String>, Container<String>>> toType_1 = new TypeBuilder<OtherTestEntry<Container<String>, Container<String>>>() {}.build();
        Type<OtherTestEntry<Container<Long>, Container<String>>> toType_2 = new TypeBuilder<OtherTestEntry<Container<Long>, Container<String>>>() {}.build();
        
        Type<Container<Long>> _Container_Long = toType_2.getNestedType(0);
        Type<Holder<String>> _Holder_String = fromType.getNestedType(1);
        Type<Container<String>> _Container_String = toType_1.getNestedType(0);
        Type<Holder<Long>> _Holder_Long = fromType.getNestedType(0);
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        TestEntry<Holder<Long>, Holder<String>> fromObject = new TestEntry<Holder<Long>, Holder<String>>();
        fromObject.setKey(new Holder<Long>());
        fromObject.getKey().setHeld(Long.valueOf(42L));
        fromObject.setValue(new Holder<String>());
        fromObject.getValue().setHeld("What is the meaning of life?");
        
        /*
         * We map the field types explicitly for the separate type mappings
         */
        factory.registerClassMap(factory.classMap(_Holder_String, _Container_String)
                .field("held", "contained")
                .byDefault()
                .toClassMap());
        factory.registerClassMap(factory.classMap(_Holder_Long, _Container_String)
                .field("held", "contained")
                .byDefault()
                .toClassMap());
        factory.registerClassMap(factory.classMap(_Holder_Long, _Container_Long)
                .field("held", "secondaryContained")
                .byDefault()
                .toClassMap());
        
        MapperFacade mapper = factory.getMapperFacade();
        
        /*
         * Map the same source type to 2 different parameterized variations of
         * the destination class; the mappings should not step on each other,
         * since different types are used
         */
        OtherTestEntry<Container<String>, Container<String>> result1 = mapper.map(fromObject, fromType, toType_1);
        OtherTestEntry<Container<Long>, Container<String>> result2 = mapper.map(fromObject, fromType, toType_2);
        
        Assert.assertNotNull(result1);
        Assert.assertEquals("" + fromObject.getKey().getHeld(), result1.getKey().getContained());
        Assert.assertEquals(fromObject.getValue().getHeld(), result1.getValue().getContained());
        Assert.assertNull(result1.getKey().getSecondaryContained());
        
        Assert.assertNotNull(result1);
        Assert.assertNull(result2.getKey().getContained());
        Assert.assertEquals(fromObject.getKey().getHeld(), result2.getKey().getSecondaryContained());
        Assert.assertEquals(fromObject.getValue().getHeld(), result2.getValue().getContained());
    }
    
    /**
     */
    @Test
    public void testParameterizedTypeTokens() {
        
        Type<TestEntry<Holder<Long>, Holder<String>>> typeA = new TypeBuilder<TestEntry<Holder<Long>, Holder<String>>>() {}.build();
        Type<OtherTestEntry<Container<Long>, Container<String>>> typeB = new TypeBuilder<OtherTestEntry<Container<Long>, Container<String>>>() {}.build();
        
        Type<Container<Long>> _Container_Long = typeB.getNestedType(0);
        Type<Holder<String>> _Holder_String = typeA.getNestedType(1);
        Type<Container<String>> _Container_String = typeB.getNestedType(1);
        Type<Holder<Long>> _Holder_Long = typeA.getNestedType(0);
        Type<Long> _Long = typeA.getNestedType(0).getNestedType(0);
        Type<String> _String = typeA.getNestedType(1).getNestedType(0);
        
        // Test type equivalence of nested types
        Assert.assertEquals(new TypeBuilder<Container<Long>>() {}.build(), _Container_Long);
        Assert.assertEquals(new TypeBuilder<Container<String>>() {}.build(), _Container_String);
        Assert.assertEquals(new TypeBuilder<Holder<Long>>() {}.build(), _Holder_Long);
        Assert.assertEquals(new TypeBuilder<Holder<String>>() {}.build(), _Holder_String);
        Assert.assertEquals(new TypeBuilder<Long>() {}.build(), _Long);
        Assert.assertEquals(new TypeBuilder<String>() {}.build(), _String);
        
        abstract class RawParameterizedType<T> {
        }
        
        // Test types constructed through alternate means
        RawParameterizedType<?> rawType = new RawParameterizedType<TestEntry<Holder<Long>, Holder<String>>>() {};
        Type<?> valueOfRawType = TypeFactory.valueOf(rawType.getClass());
        Assert.assertEquals(typeA, valueOfRawType.getNestedType(0));
        
        Type<?> alternateTypeA1 = TypeFactory.valueOf(TestEntry.class, TypeFactory.valueOf(Holder.class, Long.class),
                TypeFactory.valueOf(Holder.class, String.class));
        Assert.assertEquals(typeA, alternateTypeA1);
        
        Type<?> alternateTypeA2 = TypeFactory.valueOf(TestEntry.class, _Holder_Long, _Holder_String);
        Assert.assertEquals(typeA, alternateTypeA2);
        
        Type<? extends Container<? extends Number>> wildType = new TypeBuilder<Container<? extends Number>>(){}.build();
        Assert.assertNotNull(wildType);
        // Note that wildcard types are resolved to the nearest actual type
        Assert.assertEquals(new TypeBuilder<Container<Number>>(){}.build(), wildType);
    }
    
    @Test
    public void testRecursivelyDefinedTypes() {
        
        Type<?> recursive = new TypeBuilder<RecursiveImpl>(){}.build();
        Assert.assertNotNull(recursive);
        Type<?> recursiveSuper = recursive.getSuperType();
        Assert.assertNotNull(recursiveSuper);
        
        recursive = TypeFactory.valueOf(RecursiveType.class);
        Assert.assertNotNull(recursive);
        Assert.assertEquals(recursive.getNestedType(0).getRawType(), RecursiveType.class);
    }
    
    
    public static class Envelope<T> {
        private String id;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public T getContents() {
            return contents;
        }
        
        public void setContents(T contents) {
            this.contents = contents;
        }
        
        private T contents;
    }
    
    public static class Entry<K, V> {
        private K key;
        private V value;
        
        public K getKey() {
            return key;
        }
        
        public void setKey(K key) {
            this.key = key;
        }
        
        public V getValue() {
            return value;
        }
        
        public void setValue(V value) {
            this.value = value;
        }
    }
    
    public static class TestEntry<K, V> {
        
        private K key;
        private V value;
        
        public K getKey() {
            return key;
        }
        
        public void setKey(K key) {
            this.key = key;
        }
        
        public V getValue() {
            return value;
        }
        
        public void setValue(V value) {
            this.value = value;
        }
    }
    
    public static class OtherTestEntry<A, B> {
        
        private A key;
        private B value;
        
        public A getKey() {
            return key;
        }
        
        public void setKey(A key) {
            this.key = key;
        }
        
        public B getValue() {
            return value;
        }
        
        public void setValue(B value) {
            this.value = value;
        }
    }
    
    public static class Holder<H> {
        private H held;
        
        public H getHeld() {
            return held;
        }
        
        public void setHeld(H held) {
            this.held = held;
        }
        
    }
    
    public static class Container<C> {
        private C contained;
        private C secondaryContained;
        
        public C getSecondaryContained() {
            return secondaryContained;
        }
        
        public void setSecondaryContained(C secondaryContained) {
            this.secondaryContained = secondaryContained;
        }
        
        public C getContained() {
            return contained;
        }
        
        public void setContained(C contained) {
            this.contained = contained;
        }
    }
    
    public static interface Entity<T extends Serializable> {
        public T getId();
        
        public void
        
        setId(T id);
    }
    
    public static class EntityLong implements Entity<Long> {
        private Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
    }
    
    public static class EntityString implements Entity<String> {
        private String id;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
    }
    
    public static class NestedKey<K> implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private K key;
        
        public K getKey() {
            return key;
        }
        
        public void setKey(K key) {
            this.key = key;
        }
    }
    
    public static class EntityGeneric<T extends Serializable> implements Entity<T> {
        private T id;
        
        public T getId() {
            return id;
        }
        
        public void setId(T id) {
            this.id = id;
        }
        
    }
    
    public static abstract class RecursiveType<R extends RecursiveType<R>> implements Comparable<R>{
        
        private Type<R> recursiveType;
        
        public RecursiveType() {
            ParameterizedType superType = (ParameterizedType)getClass().getGenericSuperclass();
            recursiveType = TypeFactory.valueOf(superType.getActualTypeArguments()[0]);
        }
        
        public final int compareTo(R o) {
            RecursiveType<R> other = (RecursiveType<R>)o;
            RecursiveType<R> self = this;
            return self.recursiveType.getName().compareTo(other.recursiveType.getName());
        }
        
    }
    
    public static class RecursiveImpl extends RecursiveType<RecursiveImpl> {
        
    }
    
    
}
