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

import org.junit.Assert;
import org.junit.Test;

/**
 * MappingException when enum toString() is overridden.
 * <p>
 * Verify that we use Enum.name instead of Enum.toString
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/49">https://code.google.com/archive/p/orika/</a>
 * @author Dmitriy Khomyakov
 */
public class Issue49TestCase {
    
    @Test
    public void testMapOfEnum() {
        DefaultMapperFactory.Builder builder = new DefaultMapperFactory.Builder();
        MapperFactory factory = builder.build();
        MapperFacade mapperFacade = factory.getMapperFacade();
        Entity entity = new Entity();
        entity.setState(State.B);
        final Dto dto = mapperFacade.map(entity, Dto.class);
        Assert.assertEquals(dto.getState(), entity.getState());
    }
    
    public static enum State {
        A {
            @Override
            public String toString() {
                return "first";
            }
        },
        B {
            @Override
            public String toString() {
                return "second";
            }
        }
    }
    
    public static class Entity {
        private State state;
        
        public State getState() {
            return state;
        }
        
        public void setState(State state) {
            this.state = state;
        }
    }
    
    public static class Dto {
        private State state;
        
        public State getState() {
            return state;
        }
        
        public void setState(State state) {
            this.state = state;
        }
    }
    
}