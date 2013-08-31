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

package ma.glasnost.orika.test.converter;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.junit.Assert;
import org.junit.Test;

public class PassThroughConverterTestCase {
    
    @Test
    public void testPassThroughConverter() {
        
        PassThroughConverter ptc = new PassThroughConverter(A.class);
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.getConverterFactory().registerConverter(ptc);
        
        A a = new A();
        a.setId(42L);
        B b = new B();
        b.setString("Hello");
        C c = new C();
        c.setA(a);
        c.setB(b);
        
        D d = factory.getMapperFacade().map(c, D.class);
        
        Assert.assertEquals(c.getA(), d.getA());
        Assert.assertEquals(c.getB(), d.getB());
        Assert.assertSame(c.getA(), d.getA());
        Assert.assertNotSame(c.getB(), d.getB());
        
    }
    
    @Test
    public void testPassThroughConverterGenerics() {
        
        /*
         * Note: we register the generic Holder<?> and pass it a
         * Holder<Wrapper<B>> we expect that it should be passed-through
         */
        PassThroughConverter ptc = new PassThroughConverter(new TypeBuilder<Holder<?>>() {
        }.build());
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.getConverterFactory().registerConverter(ptc);
        
        B b = new B();
        b.setString("Hello");
        Holder<B> holder = new Holder<B>();
        holder.setHeld(b);
        Wrapper<Holder<B>> wrapper = new Wrapper<Holder<B>>();
        wrapper.setHeld(holder);
        
        Type<Wrapper<Holder<B>>> fromType = new TypeBuilder<Wrapper<Holder<B>>>() {
        }.build();
        Type<Decorator<Holder<B>>> toType = new TypeBuilder<Decorator<Holder<B>>>() {
        }.build();
        
        Decorator<Holder<B>> d = factory.getMapperFacade().map(wrapper, fromType, toType);
        
        Assert.assertEquals(wrapper.getHeld(), d.getHeld());
        Assert.assertSame(wrapper.getHeld(), d.getHeld());
    }
    
    @Test
    public void testPassThroughConverterGenerics2() {
        
        /*
         * Note: we register the specific Holder<Wrapper<A>> and pass it a
         * Holder<Wrapper<B>>; we expect that it should not be passed through
         */
        PassThroughConverter ptc = new PassThroughConverter(new TypeBuilder<Holder<Wrapper<A>>>() {
        }.build());
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.getConverterFactory().registerConverter(ptc);
        
        B b = new B();
        b.setString("Hello");
        Holder<B> holder = new Holder<B>();
        holder.setHeld(b);
        Wrapper<Holder<B>> wrapper = new Wrapper<Holder<B>>();
        wrapper.setHeld(holder);
        
        Type<Wrapper<Holder<B>>> fromType = new TypeBuilder<Wrapper<Holder<B>>>() {
        }.build();
        Type<Decorator<Holder<B>>> toType = new TypeBuilder<Decorator<Holder<B>>>() {
        }.build();
        
        Decorator<Holder<B>> d = factory.getMapperFacade().map(wrapper, fromType, toType);
        
        Assert.assertEquals(wrapper.getHeld(), d.getHeld());
        Assert.assertNotSame(wrapper.getHeld(), d.getHeld());
    }
    
    public static class A {
        private Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(final Long id) {
            this.id = id;
        }
        
        @Override
        public boolean equals(final Object that) {
            return EqualsBuilder.reflectionEquals(this, that);
        }
        
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
        
    }
    
    public static class B {
        private String string;
        
        public String getString() {
            return string;
        }
        
        public void setString(final String string) {
            this.string = string;
        }
        
        @Override
        public boolean equals(final Object that) {
            return EqualsBuilder.reflectionEquals(this, that);
        }
        
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
        
    }
    
    public static class C {
        
        private A a;
        private B b;
        
        public A getA() {
            return a;
        }
        
        public void setA(final A a) {
            this.a = a;
        }
        
        public B getB() {
            return b;
        }
        
        public void setB(final B b) {
            this.b = b;
        }
        
        @Override
        public boolean equals(final Object that) {
            return EqualsBuilder.reflectionEquals(this, that);
        }
        
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }
    
    public static class D {
        
        private A a;
        private B b;
        
        public A getA() {
            return a;
        }
        
        public void setA(final A a) {
            this.a = a;
        }
        
        public B getB() {
            return b;
        }
        
        public void setB(final B b) {
            this.b = b;
        }
        
        @Override
        public boolean equals(final Object that) {
            return EqualsBuilder.reflectionEquals(this, that);
        }
        
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }
    
    public static class Holder<T> {
        
        private T held;
        
        public T getHeld() {
            return held;
        }
        
        public void setHeld(final T held) {
            this.held = held;
        }
        
        @Override
        public boolean equals(final Object that) {
            return EqualsBuilder.reflectionEquals(this, that);
        }
        
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }
    
    public static class Container<T> {
        
        private T held;
        
        public T getHeld() {
            return held;
        }
        
        public void setHeld(final T held) {
            this.held = held;
        }
        
        @Override
        public boolean equals(final Object that) {
            return EqualsBuilder.reflectionEquals(this, that);
        }
        
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }
    
    public static class Wrapper<T> {
        
        private T held;
        
        public T getHeld() {
            return held;
        }
        
        public void setHeld(final T held) {
            this.held = held;
        }
        
        @Override
        public boolean equals(final Object that) {
            return EqualsBuilder.reflectionEquals(this, that);
        }
        
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }
    
    public static class Decorator<T> {
        
        private T held;
        
        public T getHeld() {
            return held;
        }
        
        public void setHeld(final T held) {
            this.held = held;
        }
        
        @Override
        public boolean equals(final Object that) {
            return EqualsBuilder.reflectionEquals(this, that);
        }
        
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }
    
}
