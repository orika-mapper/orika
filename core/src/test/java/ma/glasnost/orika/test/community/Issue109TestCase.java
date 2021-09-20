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
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapping for multi-occurrence elements doesn't trigger auto-generated mappings.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/109">https://code.google.com/archive/p/orika/</a>
 */
public class Issue109TestCase {
 
    public static class Element {
        public int id;
        public String name;
    }
    
    public static class ElementDto {
        public int id;
        public String name;
    }
    
    public static class Base {
        public List<Element> elements;
        public String name;
    }
    
    public static class BaseDto {
        public ElementDto element;
        public String name;
    }
    
    @Test
    public void testOrderingOfClassMaps() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.classMap(Base.class, BaseDto.class)
            .fieldAToB("elements[0]", "element")
            .byDefault().register();

        
        MapperFacade mapper = factory.getMapperFacade();
        
        Base source = new Base();
        source.name = "source1";
        source.elements = new ArrayList<>();
        Element el = new Element();
        el.id = 1;
        el.name = "element1";
        source.elements.add(el);
        
        BaseDto dest = mapper.map(source, BaseDto.class);
        
        Assert.assertEquals(source.elements.get(0).id, dest.element.id);
    }
}
