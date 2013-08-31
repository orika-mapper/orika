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

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * </p>
 * 
 * @author Dmitriy Khomyakov
 */
public class Issue61TestCase {
    @Test
    public void testAuthorityMap() {
        DefaultMapperFactory.Builder builder = new DefaultMapperFactory.Builder();
        MapperFactory factory = builder.build();
        MapperFacade mapperFacade = factory.getMapperFacade();
        
        MyEntity root = new MyEntity("root");
        MyEntity child = new MyEntity("child");
        root.getChildren().add(child);
        root.getChildren().add(root);
        child.addChild(root);
        
        MyDto myDto = mapperFacade.map(root, MyDto.class);
        Assert.assertEquals(myDto.getCaption(), root.getCaption());
        
        // System.out.println("myDto = " + myDto);
        
    }
    
    public static class MyEntity {
        private String name;
        private String caption;
        private Set<MyEntity> children;
        
        public MyEntity() {
        }
        
        public MyEntity(String name) {
            this.name = name;
            children = new HashSet<MyEntity>();
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getCaption() {
            return caption;
        }
        
        public void setCaption(String caption) {
            this.caption = caption;
        }
        
        public Set<MyEntity> getChildren() {
            return children;
        }
        
        public void addChild(MyEntity myEntity) {
            children.add(myEntity);
        }
        
        public void setChildren(Set<MyEntity> children) {
            this.children = children;
        }
        
    }
    
    public static class MyDto {
        private String caption;
        
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        private Set<MyDto> children;
        
        public String getCaption() {
            return caption;
        }
        
        public void setCaption(String caption) {
            this.caption = caption;
        }
        
        public Set<MyDto> getChildren() {
            return children;
        }
        
        public void setChildren(Set<MyDto> children) {
            this.children = children;
        }
        
    }
}
