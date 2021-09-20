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

package ma.glasnost.orika.test.metadata;

import ma.glasnost.orika.impl.UtilityResolver;
import ma.glasnost.orika.metadata.Property;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VariableRefTestCase {
    
    
    public static class Year {
        public int yearNumber;
        public List<Month> months = new ArrayList<>();
    }
    
    public static class Month {
        public int monthNumber;
        public List<Day> days = new ArrayList<>();
    }
    
    public static class Day {
        public int dayNumber;
        public String dayOfWeek;
    }
    
    public static class FlatData {
        public int dayNumber;
        public String dayOfWeek;
        public int yearNumber;
        public int monthNumber;
    }
    
    
    @Test
    public void testGetter() {
     
        Property prop = UtilityResolver.getDefaultPropertyResolverStrategy().getProperty(Year.class, "months{days{dayOfWeek}}");
        Assert.assertNotNull(prop);
        Assert.assertNotNull(prop.getContainer());
        Assert.assertNotNull(prop.getContainer().getContainer());
        
    }
}
