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

import java.util.HashMap;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * There is a bug for JavaBean to Map.
 * <p>
 * I define a class like this: public class JsonObject extends HashMap<String,Object>{...}. Orika can not map JavaBean to JsonObject.
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/80">https://code.google.com/archive/p/orika/</a>
 *
 */
public class Issue80TestCase {
    
    public static class JsonObject extends HashMap<String,Object> {
        
    }
    
    public static class JavaBean {
        public String name;
        public Integer id;
    }
    
    @Test
    public void test() {
        
        // TODO: determine whether the solution is that
        // 1. we should be able to getNestedType(0) on JsonObject and get String
        // or
        // 2. we should be able to resolve the values of the get and put 
        // methods...
        
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.classMap(JsonObject.class, JavaBean.class)
        .field("name", "name")
        .field("id", "id")
        .register();
        
        JsonObject source = new JsonObject();
        source.put("name", "Joe Smit");
        source.put("id", 22);
        
        MapperFacade mapper = factory.getMapperFacade();
        
        JavaBean dest = mapper.map(source, JavaBean.class);
        
        Assert.assertEquals(source.get("name"), dest.name);
        Assert.assertEquals(source.get("id"), dest.id);
        
    }
}
