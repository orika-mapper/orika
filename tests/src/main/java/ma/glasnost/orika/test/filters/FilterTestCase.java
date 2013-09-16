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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.NullFilter;
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

    public static class SecurityFilter extends NullFilter<Object, Object> {
        
        private final String MASK = "*************";
        
        public boolean filtersDestination() {
            return true;
        }
        
        public <S, D> boolean shouldMap(final Type<S> sourceType, final String sourceName, final S source, final Type<D> destType, final String destName,
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
    
    private static class CostFilter extends NullFilter<Number, Number> {
        @Override
        public boolean appliesTo(Property source, Property destination) {
            return super.appliesTo(source, destination) && source.getName().equals("cost");
        }
    
        @Override
        public boolean filtersSource() {
            return true;
        }
    
        @Override
        public <S extends Number> S filterSource(S sourceValue, final Type<S> sourceType, final String sourceName, final Type<?> destType,
                final String destName, final MappingContext mappingContext) {
            return (S) new BigDecimal(((Double) sourceValue) * 2);
        }
    }
    
    @Test
    public void testMultiOccurenceFiltering() {
        // run without filter
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.classMap(Source.class, Destination.class)
                .field("infoMap{key}", "infos{item}")
                .field("infoMap{value}", "infos{info}")
                .byDefault().register();

        MapperFacade mapper = factory.getMapperFacade();
        
        Source source = new Source();
        source.age = 35;
        source.infoMap = new HashMap<String, String>();
        source.infoMap.put("weather", "nice");
        
        Destination dest = mapper.map(source, Destination.class);
        
        Assert.assertEquals(source.age, (int) dest.age);
        Assert.assertEquals(1, dest.infos.size());
        Info info = dest.infos.get(0);
        Assert.assertEquals("weather", info.item);
        Assert.assertEquals("nice", info.info);

        // run with filter
        factory = MappingUtil.getMapperFactory();
        factory.classMap(Source.class, Destination.class)
                .field("infoMap{key}", "infos{item}")
                .field("infoMap{value}", "infos{info}")
                .byDefault().register();
        factory.registerFilter(new InfoFilter());
        mapper = factory.getMapperFacade();
        
        dest = mapper.map(source, Destination.class);
        
        Assert.assertEquals(source.age, (int) dest.age);
        Assert.assertNull(dest.infos);
    }
    
    private static class InfoFilter extends NullFilter<Map<?, ?>, List<?>> {
        @Override
        public <S extends Map<?, ?>, D extends List<?>> boolean shouldMap(final Type<S> sourceType, final String sourceName, final S source, final Type<D> destType, final String destName,
                final MappingContext mappingContext) {
            if (sourceName.equals("infoMap")) {
                return false;
            }
            return true;
        }
    }
    
    public static class Source {
        public SourceName name;
        public Long id;
        public int age;
        public double cost;
        public String creditCardNumber;
        public SourceAddress address;
        public Map<String, String> infoMap;
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
        public List<Info> infos;
    }
    
    public static class DestinationName {
        public String first;
        public String last;
    }
    
    public static class Info {
        public String item;
        public String info;
    }
}
