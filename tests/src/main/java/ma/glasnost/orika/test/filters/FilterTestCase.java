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

package ma.glasnost.orika.test.filters;

import java.math.BigDecimal;

import ma.glasnost.orika.CustomFilter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class FilterTestCase {
    
    @Test
    public void testFiltering() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.classMap(Source.class, Destination.class)
               .field("address.street", "street")
               .field("address.city", "city")
               .byDefault().register();
        factory.registerFilter(new SecurityFilter());
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Source source = new Source();
        source.name = new SourceName();
        source.name.first = "Joe";
        source.name.last = "Smith";
        source.id = 2L;
        source.age = 35;
        source.cost = 12.34d;
        source.creditCardNumber = "5432109876543210";
        source.address = new SourceAddress();
        source.address.street = "ashbury";
        source.address.city = "SF";
        
        Destination dest = mapper.map(source, Destination.class);
        
        Assert.assertEquals(source.name.first, dest.name.first);
        Assert.assertEquals(source.name.last, dest.name.last);
        Assert.assertNull(dest.age);
        Assert.assertEquals(source.cost, dest.cost.doubleValue(), 0.01d);
        Assert.assertEquals("************3210", dest.creditCardNumber);
        Assert.assertNull(dest.street);
        Assert.assertEquals(source.address.city, dest.city);
        
    }

    public static class SecurityFilter extends CustomFilter<Object, Object> {
        
        private final String MASK = "*************";
        
        public boolean filtersSource() {
            return false;
        }
        
        public boolean filtersDestination() {
            return true;
        }
        
        public boolean shouldMap(final Type<?> sourceType, final String sourceName, final Object source, final Type<?> destType, final String destName,
                final MappingContext mappingContext) {
            if ("age".equals(sourceName) || "address.street".equals(sourceName)) {
                return false;
            }
            return true;
        }
        
        public <D> D filterDestination(D destinationValue, final Type<?> sourceType, final String sourceName, final Type<D> destType,
                final String destName, final MappingContext mappingContext) {
            if ("creditCardNumber".equals(sourceName)) {
                String cardMask = (String) destinationValue;
                destinationValue = (D) (MASK.substring(0, cardMask.length() - 4) + cardMask.substring(cardMask.length() - 4));
            }
            return destinationValue;
            
        }
        
        public <S> S filterSource(final S sourceValue, final Type<S> sourceType, final String sourceName, final Type<?> destType,
                final String destName, final MappingContext mappingContext) {
            return sourceValue;
        }
    }
    
    @Test
    public void testFilterAppliesTo() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.classMap(Source.class, Destination.class).byDefault().register();
        factory.registerFilter(new CostFilter());

        MapperFacade mapper = factory.getMapperFacade();
        
        Source source = new Source();
        source.age = 35;
        source.cost = 12.34d;
        source.creditCardNumber = "cc";
        
        Destination dest = mapper.map(source, Destination.class);
        
        Assert.assertEquals(source.age, (int) dest.age);
        Assert.assertEquals(source.cost * 2, dest.cost.doubleValue(), 0.01d);
        Assert.assertEquals(source.creditCardNumber, dest.creditCardNumber);
    }
    
    private static class CostFilter extends CustomFilter<Number, Number> {
        @Override
        public boolean appliesTo(Property source, Property destination) {
            return super.appliesTo(source, destination) && source.getName().equals("cost");
        }

        @Override
        public boolean filtersSource() {
            return false;
        }
    
        @Override
        public boolean filtersDestination() {
            return true;
        }
    
        @Override
        public boolean shouldMap(final Type<?> sourceType, final String sourceName, final Number source, final Type<?> destType, final String destName,
                final MappingContext mappingContext) {
            return true;
        }
    
        @Override
        public <D> D filterDestination(D destinationValue, final Type<?> sourceType, final String sourceName, final Type<D> destType,
                final String destName, final MappingContext mappingContext) {
            return (D) ((BigDecimal) destinationValue).multiply(BigDecimal.valueOf(2));
        }
    
        @Override
        public <S> S filterSource(final S sourceValue, final Type<S> sourceType, final String sourceName, final Type<?> destType,
                final String destName, final MappingContext mappingContext) {
            return sourceValue;
        }
    }
    
    public static class Source {
        public SourceName name;
        public Long id;
        public int age;
        public double cost;
        public String creditCardNumber;
        public SourceAddress address;
    }
    
    public static class SourceName {
        public String first;
        public String last;
    }
    
    public static class SourceAddress {
        public String street;
        public String city;
    }
    
    public static class Destination {
        public DestinationName name;
        public Long id;
        public Integer age;
        public BigDecimal cost;
        public String creditCardNumber;
        public String street;
        public String city;
    }
    
    public static class DestinationName {
        public String first;
        public String last;
    }
}
