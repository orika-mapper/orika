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

import java.util.Calendar;
import java.util.Date;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * Calendar as destination throwing exception.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/14">https://code.google.com/archive/p/orika/</a>
 */
public class Issue14TestCase {
    
    public static class Product {
        
        private Date tempCal;
        
        public Date getTempCal() {
            return tempCal;
        }
        
        public void setTempCal(Date tempCal) {
            this.tempCal = tempCal;
        }
        
    }
    
    public static class ProductDTO {
        
        private Calendar tempCal;
        
        public Calendar getTempCal() {
            return tempCal;
        }
        
        public void setTempCal(Calendar tempCal) {
            this.tempCal = tempCal;
        }
        
    }
    
    @Test
    public void testMapDateToCalendar() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new BidirectionalConverter<Date, Calendar>() {
            
            @Override
            public Calendar convertTo(Date source, Type<Calendar> destinationType, MappingContext context) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(source);
                return cal;
            }
            
            @Override
            public Date convertFrom(Calendar source, Type<Date> destinationType, MappingContext context) {
                return source.getTime();
            }
            
        });
        MapperFacade mapper = factory.getMapperFacade();
        
        Product p = new Product();
        p.setTempCal(new Date());
        
        ProductDTO result = mapper.map(p, ProductDTO.class);
        
        Assert.assertEquals(p.getTempCal(), result.getTempCal().getTime());
    }
    
    @Test
    public void testMapDateToCalendar_usingBuiltinConverters() {
        MapperFactory factory = new DefaultMapperFactory.Builder().useBuiltinConverters(true).build();
        MapperFacade mapper = factory.getMapperFacade();
        
        Product p = new Product();
        p.setTempCal(new Date());
        
        ProductDTO result = mapper.map(p, ProductDTO.class);
        
        Assert.assertEquals(p.getTempCal(), result.getTempCal().getTime());
    }
    
}
