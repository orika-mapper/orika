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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class Issue69TestCase {
    
    
    @Test
    public void verify() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        Source s = new Source();
        Map<String, String> map = new HashMap<String, String>();
        map.put("Kofi", "Annan");
        map.put("Julius", "Ceasar");
        s.setMap(map);
        
        List<String> list = new ArrayList<String>();
        list.add("apple");
        list.add("banana");
        s.setList(list);
        
        String[] array = new String[2];
        array[0] = "one";
        array[1] = "two";
        s.setArray(array);
        
        Destination d = mapper.map(s, Destination.class);
        
        Source mapBack = mapper.map(d, Source.class);
        
        Assert.assertEquals(s, mapBack);
        
    }
    
    
    
    /*
     *  TODO:
     *  Construct a usage case which contains a class with a list type
     *  which is returned as an unmodifiable list from the getter
     *   
     */
    
    public static class Source {
        private Map<String, String> map;
        private List<String> list;
        private String[] array;
        /**
         * @return the map
         */
        public Map<String, String> getMap() {
            return map != null ? Collections.unmodifiableMap(map) : null;
        }
        /**
         * @param map the map to set
         */
        public void setMap(Map<String, String> map) {
            this.map = map;
        }
        /**
         * @return the list
         */
        public List<String> getList() {
            return list != null ? Collections.unmodifiableList(list) : null;
        }
        /**
         * @param list the list to set
         */
        public void setList(List<String> list) {
            this.list = list;
        }
        /**
         * @return the array
         */
        public String[] getArray() {
            return array!=null ? array.clone() : null;
        }
        /**
         * @param array the array to set
         */
        public void setArray(String[] array) {
            this.array = array;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(array);
            result = prime * result + ((list == null) ? 0 : list.hashCode());
            result = prime * result + ((map == null) ? 0 : map.hashCode());
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
            Source other = (Source) obj;
            if (!Arrays.equals(array, other.array))
                return false;
            if (list == null) {
                if (other.list != null)
                    return false;
            } else if (!list.equals(other.list))
                return false;
            if (map == null) {
                if (other.map != null)
                    return false;
            } else if (!map.equals(other.map))
                return false;
            return true;
        }
    }
    
    public static class Destination {
        private Map<String, String> map;
        private List<String> list;
        private String[] array;
        /**
         * @return the map
         */
        public Map<String, String> getMap() {
            return map != null ? Collections.unmodifiableMap(map) : null;
        }
        /**
         * @param map the map to set
         */
        public void setMap(Map<String, String> map) {
            this.map = map;
        }
        /**
         * @return the list
         */
        public List<String> getList() {
            return list != null ? Collections.unmodifiableList(list) : null;
        }
        /**
         * @param list the list to set
         */
        public void setList(List<String> list) {
            this.list = list;
        }
        /**
         * @return the array
         */
        public String[] getArray() {
            return array!=null ? array.clone() : null;
        }
        /**
         * @param array the array to set
         */
        public void setArray(String[] array) {
            this.array = array;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(array);
            result = prime * result + ((list == null) ? 0 : list.hashCode());
            result = prime * result + ((map == null) ? 0 : map.hashCode());
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
            Destination other = (Destination) obj;
            if (!Arrays.equals(array, other.array))
                return false;
            if (list == null) {
                if (other.list != null)
                    return false;
            } else if (!list.equals(other.list))
                return false;
            if (map == null) {
                if (other.map != null)
                    return false;
            } else if (!map.equals(other.map))
                return false;
            return true;
        }
        
        
        
    }
    
}
