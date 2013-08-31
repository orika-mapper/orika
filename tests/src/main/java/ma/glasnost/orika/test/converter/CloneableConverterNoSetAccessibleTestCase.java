/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
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

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.CloneableConverter;
import ma.glasnost.orika.test.MappingUtil;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.junit.Test;

public class CloneableConverterNoSetAccessibleTestCase {
    
    @Test
    public void cloneableConverterWithoutSetAccessible() throws DatatypeConfigurationException {
        
        final SecurityManager initialSm = System.getSecurityManager();
        try {
            System.setSecurityManager(new SecurityManager() {
                public void checkPermission(java.security.Permission perm) {
                    if ("suppressAccessChecks".equals(perm.getName())) {
                        for (StackTraceElement ste: new Throwable().getStackTrace()) {
                            if (ste.getClassName().equals(CloneableConverter.class.getCanonicalName())) {
                                throw new SecurityException("not permitted");
                            }
                        }
                    } 
                }
            });
        
        
            CloneableConverter cc = new CloneableConverter(SampleCloneable.class);
            
            MapperFactory factory = MappingUtil.getMapperFactory();
            factory.getConverterFactory().registerConverter(cc);
            
            GregorianCalendar cal = new GregorianCalendar();
            cal.add(Calendar.YEAR, 10);
            XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)cal);
            cal.add(Calendar.MONTH, 3);
            
            ClonableHolder source = new ClonableHolder();
            source.value = new SampleCloneable();
            source.value.id = 5L;
            source.date = new Date(System.currentTimeMillis() + 100000);
            source.timestamp = new Timestamp(System.currentTimeMillis() + 50000);
            source.calendar = cal;
            source.xmlCalendar = xmlCal;
            
            ClonableHolder dest = factory.getMapperFacade().map(source, ClonableHolder.class);
            Assert.assertEquals(source.value, dest.value);
            Assert.assertNotSame(source.value, dest.value);
            Assert.assertEquals(source.date, dest.date);
            Assert.assertNotSame(source.date, dest.date);
            Assert.assertEquals(source.timestamp, dest.timestamp);
            Assert.assertNotSame(source.timestamp, dest.timestamp);
            Assert.assertEquals(source.calendar, dest.calendar);
            Assert.assertNotSame(source.calendar, dest.calendar);
            Assert.assertEquals(source.xmlCalendar, dest.xmlCalendar);
            Assert.assertNotSame(source.xmlCalendar, dest.xmlCalendar);
        } finally {
            System.setSecurityManager(initialSm);
        }
    }
    
    public static class ClonableHolder {
        public SampleCloneable value;
        public Date date;
        public java.sql.Timestamp timestamp;
        public Calendar calendar;
        public XMLGregorianCalendar xmlCalendar;
    }
    
    
    public static class SampleCloneable implements Cloneable {
        private Long id;
        
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public Object clone() {
            SampleCloneable clone;
            try {
                clone = (SampleCloneable) super.clone();
                clone.id = id;
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }
            
            return clone;
        }
        
        public boolean equals(Object that) {
        	return EqualsBuilder.reflectionEquals(this, that);
        }
        
        public int hashCode() {
        	return HashCodeBuilder.reflectionHashCode(this);
        }
    }

}
