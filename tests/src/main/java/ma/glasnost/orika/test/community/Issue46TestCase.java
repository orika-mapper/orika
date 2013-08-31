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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class Issue46TestCase {
    public static class One {
        public List<Two> getTwos() {
            return twos;
        }
        
        public void setTwos(List<Two> twos) {
            this.twos = twos;
        }
        
        List<Two> twos = new ArrayList<Two>();
        
        public One(String name) {
            this.name = name;
            twos.add(new Two(name));
        }
        
        public One() {
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        String name;
        
    }
    
    public static class Two {
        public Two() {
        }
        
        public Two(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        String name;
    }
    
    public static class Parent {
        public List<One> getOneList() {
            return oneList;
        }
        
        public void setOneList(List<One> oneList) {
            this.oneList = oneList;
        }
        
        List<One> oneList = new ArrayList<One>();
        
        public List<Two> getTwoList() {
            return twoList;
        }
        
        public void setTwoList(List<Two> twoList) {
            this.twoList = twoList;
        }
        
        List<Two> twoList = new ArrayList<Two>();
    }
    
    @Test
    public void test() {
        System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES, "true");
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade facade = factory.getMapperFacade();
        
        List<Parent> parents = new ArrayList<Parent>();
        for (int i = 0; i < 100; i++) {
            Parent parent = new Parent();
            List<One> ones = new ArrayList<One>();
            List<Two> twos = new ArrayList<Two>();
            for (int j = 0; j < 1000; j++) {
                ones.add(new One(Integer.toString(j)));
                twos.add(new Two(Integer.toString(j)));
            }
            
            parent.oneList = ones;
            parent.twoList = twos;
            parents.add(parent);
        }
        
        int transforms = 0;
        try {
            for (Parent parent : parents) {
                transforms++;
                
                Parent result = facade.map(parent, Parent.class);
                
                for (One one : result.oneList)
                    for (Two two : one.twos)
                        Assert.assertNotNull(two.getName());
                
            }
        } catch (Exception e) {
            throw new AssertionError("Failed to process graph. failed after " + transforms + " transforms, exception = " + e);
        }
    }
    
}
