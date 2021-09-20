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
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * NullPointerException in MultiOccurrenceToMultiOccurrence.mapFields.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/85">https://code.google.com/archive/p/orika/</a>
 * @author conleym
 * 
 */
public final class Issue85TestCase {
    
    @Test
    public void test() {
        MapperFactory f = MappingUtil.getMapperFactory();
        f.classMap(MapContainer.class, Map.class).field("map{value}", "{key}").register();
        
        final MapperFacade facade = f.getMapperFacade();
        final Map<Object, Object> dest = new HashMap<>();
        final Map<Object, Object> src = new HashMap<>();
        src.put("xyz", "123456");
        facade.map(new MapContainer<>(src), dest);
        System.out.println(dest);
    }
    
    
    public final static class MapContainer<X, Y> {
        public final Map<X, Y> map;
        
        public MapContainer(final Map<X, Y> map) {
            this.map = map;
        }
    }
    
}
