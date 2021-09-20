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

package ma.glasnost.orika.test.converter;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.converter.builtin.DateToStringConverter;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.converter.FieldLevelConverterClasses.A;
import ma.glasnost.orika.test.converter.FieldLevelConverterClasses.B;
import ma.glasnost.orika.test.converter.FieldLevelConverterClasses.C;

public class FieldLevelConverterTestCase {
    
    @Test
    public void testDateToString() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        ConverterFactory converterFactory = factory.getConverterFactory();
        converterFactory.registerConverter("dateConverter1", new DateToStringConverter("dd/MM/yyyy"));
        converterFactory.registerConverter("dateConverter2", new DateToStringConverter("dd-MM-yyyy"));
        
        factory.registerClassMap(factory.classMap(A.class, B.class).fieldMap("date").converter("dateConverter1").add().toClassMap());
        factory.registerClassMap(factory.classMap(A.class, C.class).fieldMap("date").converter("dateConverter2").add().toClassMap());
                
        MapperFacade mapperFacade = factory.getMapperFacade();
        
        C c = new C();
        c.setDate(new Date());
        
        A a = mapperFacade.map(c, A.class);
        
        Assert.assertEquals(new SimpleDateFormat("dd-MM-yyyy").format(c.getDate()), a.getDate());
        
        B b = new B();
        b.setDate(new Date());
        
        a = mapperFacade.map(b, A.class);
        
        Assert.assertEquals(new SimpleDateFormat("dd/MM/yyyy").format(b.getDate()), a.getDate());
        
    }
}
