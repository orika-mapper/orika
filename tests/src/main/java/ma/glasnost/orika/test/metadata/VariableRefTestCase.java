package ma.glasnost.orika.test.metadata;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import ma.glasnost.orika.impl.UtilityResolver;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.property.IntrospectorPropertyResolver;

import org.junit.Test;

public class VariableRefTestCase {
    
    
    public static class Year {
        public int yearNumber;
        public List<Month> months = new ArrayList<Month>();
    }
    
    public static class Month {
        public int monthNumber;
        public List<Day> days = new ArrayList<Day>();
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
