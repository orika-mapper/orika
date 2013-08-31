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
package ma.glasnost.orika.test.community;

import static org.junit.Assert.assertEquals;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.unenhance.HibernateUnenhanceStrategy;

import org.junit.Test;

/**
 * <p>
 * </p>
 * 
 * @author Dmitriy Khomyakov
 */
public class Issue34TestCase {
    
    @Test
    public void testDefaultMapping() {
        DefaultMapperFactory.Builder builder = new DefaultMapperFactory.Builder();
        builder.unenhanceStrategy(new HibernateUnenhanceStrategy());
        MapperFactory mapperFactory = builder.build();
        
        MapperFacade mapperFacade = mapperFactory.getMapperFacade();
        
        Entity entity = new Entity("small data", "big data");
        
        Child child = mapperFacade.map(entity, Child.class);
        assertEquals(child.getClass(), Child.class);
        
        Parent parent = mapperFacade.map(entity, Parent.class);// orika returns
                                                               // expensive
                                                               // object
        assertEquals(Parent.class, parent.getClass());
        
    }
    
    public static class Entity {
        private String light;
        private String expensive;
        
        public Entity(String light, String expensive) {
            this.light = light;
            this.expensive = expensive;
        }
        
        public Entity() {
        }
        
        public String getLight() {
            return light;
        }
        
        public void setLight(String light) {
            this.light = light;
        }
        
        public String getExpensive() {
            return expensive;
        }
        
        public void setExpensive(String expensive) {
            this.expensive = expensive;
        }
    }
    
    public static class Parent {
        private String light;
        
        public String getLight() {
            return light;
        }
        
        public void setLight(String light) {
            this.light = light;
        }
    }
    
    public static class Child extends Parent {
        private String expensive;
        
        public String getExpensive() {
            return expensive;
        }
        
        public void setExpensive(String expensive) {
            this.expensive = expensive;
        }
    }
    
}
