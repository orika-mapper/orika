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

package ma.glasnost.orika.test.primitives;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class BigDecimalPrimitiveTestCase {
    
    @Test
    public void shouldMapBigDecimalToPrimtiveDouble() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new CustomConverter<BigDecimal, Double>() {
            
            public Double convert(BigDecimal source, Type<? extends Double> destinationType, MappingContext context) {
                return source.doubleValue();
            }
            
        });
        
        A source = new A();
        source.setValue(BigDecimal.TEN);
        B dest = factory.getMapperFacade().map(source, B.class);
        
        Assert.assertEquals(new Double(10), (Double) dest.getValue());
        
    }
    
    public static class A {
        private BigDecimal value;
        
        public BigDecimal getValue() {
            return value;
        }
        
        public void setValue(BigDecimal value) {
            this.value = value;
        }
        
    }
    
    public static class B {
        private double value;
        
        public double getValue() {
            return value;
        }
        
        public void setValue(double value) {
            this.value = value;
        }
        
    }
}
