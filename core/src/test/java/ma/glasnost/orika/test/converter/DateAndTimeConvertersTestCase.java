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

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.DateAndTimeConverters.DateToCalendarConverter;
import ma.glasnost.orika.converter.builtin.DateAndTimeConverters.DateToXmlGregorianCalendarConverter;
import ma.glasnost.orika.test.MappingUtil;

/**
 * DateAndTimeConverters provides a set of individual converters
 * for conversion between the below listed enumeration of commonly used data/time
 * representations:
 * <ul>
 * <li>java.util.Date
 * <li>java.util.Calendar
 * <li>java.lang.Long or long
 * <li>javax.xml.datatype.XMLGregorianCalendar
 * </ul>
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class DateAndTimeConvertersTestCase {

	
	@Test
	public void testDateToCalendarConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		MapperFacade mapper = factory.getMapperFacade();
		
		Date now = new Date();
		Calendar cal = mapper.map(now, Calendar.class);
		Assert.assertEquals(now, cal.getTime());
		
		Date reverse = mapper.map(cal, Date.class);
		Assert.assertEquals(now, reverse);
	}
	
	@Test
    public void testSqlDateToCalendarConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        java.sql.Date now = new java.sql.Date(System.currentTimeMillis());
        Calendar cal = mapper.map(now, Calendar.class);
        Assert.assertEquals(now.getTime(), cal.getTime().getTime());
        
        Date reverse = mapper.map(cal, Date.class);
        Assert.assertEquals(now, reverse);
    }
	
	@Test
    public void testDateToTimeConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        Date now = new Date();
        Time time = mapper.map(now, Time.class);
        Assert.assertEquals(now.getTime(), time.getTime());
        
        Date reverse = mapper.map(time, Date.class);
        Assert.assertEquals(now, reverse);
    }
	
	@Test
    public void testSqlDateToTimeConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        java.sql.Date now = new java.sql.Date(System.currentTimeMillis());
        Time time = mapper.map(now, Time.class);
        Assert.assertEquals(now.getTime(), time.getTime());
        
        java.sql.Date reverse = mapper.map(time, java.sql.Date.class);
        Assert.assertEquals(now, reverse);
    }
	
	@Test
    public void testSqlDateToDateConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        java.sql.Date now = new java.sql.Date(System.currentTimeMillis());
        Date date = mapper.map(now, Date.class);
        Assert.assertEquals(now.getTime(), date.getTime());
        
        java.sql.Date reverse = mapper.map(date, java.sql.Date.class);
        Assert.assertEquals(now, reverse);
    }
	
	@Test
	public void testDateToXmlGregorianCalendarConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new DateToXmlGregorianCalendarConverter());
		MapperFacade mapper = factory.getMapperFacade();
		
		Date now = new Date();
		XMLGregorianCalendar xml = mapper.map(now, XMLGregorianCalendar.class);
		Assert.assertEquals(now, xml.toGregorianCalendar().getTime());
		
		Date reverse = mapper.map(xml, Date.class);
		Assert.assertEquals(now, reverse);
	}
	
	@Test
    public void testTimeToXmlGregorianCalendarConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new DateToXmlGregorianCalendarConverter());
        MapperFacade mapper = factory.getMapperFacade();
        
        Time now = new Time(System.currentTimeMillis());
        XMLGregorianCalendar xml = mapper.map(now, XMLGregorianCalendar.class);
        Assert.assertEquals(now.getTime(), xml.toGregorianCalendar().getTime().getTime());
        
        Date reverse = mapper.map(xml, Date.class);
        Assert.assertEquals(now, reverse);
    }
	
	@Test
    public void testSqlDateToXmlGregorianCalendarConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new DateToXmlGregorianCalendarConverter());
        MapperFacade mapper = factory.getMapperFacade();
        
        java.sql.Date now = new java.sql.Date(System.currentTimeMillis());
        XMLGregorianCalendar xml = mapper.map(now, XMLGregorianCalendar.class);
        Assert.assertEquals(now.getTime(), xml.toGregorianCalendar().getTime().getTime());
        
        java.sql.Date reverse = mapper.map(xml, java.sql.Date.class);
        Assert.assertEquals(now, reverse);
    }
	
	@Test
    public void testTimeToCalendarConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new DateToCalendarConverter());
        MapperFacade mapper = factory.getMapperFacade();
        
        Time now = new Time(System.currentTimeMillis());
        Calendar xml = mapper.map(now, Calendar.class);
        Assert.assertEquals(now.getTime(), xml.getTime().getTime());
        
        Date reverse = mapper.map(xml, Date.class);
        Assert.assertEquals(now, reverse);
    }
    
	
	@Test
	public void testCalendarToXmlGregorianCalendarConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		MapperFacade mapper = factory.getMapperFacade();
		
		Calendar cal = Calendar.getInstance();
		XMLGregorianCalendar xml = mapper.map(cal, XMLGregorianCalendar.class);
		Assert.assertEquals(cal.getTime(), xml.toGregorianCalendar().getTime());
		
		Calendar reverse = mapper.map(xml, Calendar.class);
		Assert.assertEquals(cal, reverse);
	}

	@Test
	public void testLongToXmlGregorianCalendarConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		MapperFacade mapper = factory.getMapperFacade();
		
		long now = System.currentTimeMillis();
		XMLGregorianCalendar xml = mapper.map(now, XMLGregorianCalendar.class);
		
		Assert.assertEquals(now, xml.toGregorianCalendar().getTimeInMillis());
		
		long reverse = mapper.map(xml, Long.class);
		Assert.assertEquals(now, reverse);
	}
	
	@Test
	public void testLongToDateConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		MapperFacade mapper = factory.getMapperFacade();
		
		long now = System.currentTimeMillis();
		Date date = mapper.map(now, Date.class);
		Assert.assertEquals(now, date.getTime());
		
		long reverse = mapper.map(date, Long.class);
		Assert.assertEquals(now, reverse);
	}

	@Test
    public void testLongToTimeConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        long now = System.currentTimeMillis();
        Time date = mapper.map(now, Time.class);
        Assert.assertEquals(now, date.getTime());
        
        long reverse = mapper.map(date, Long.class);
        Assert.assertEquals(now, reverse);
    }
	
	@Test
    public void testLongToSqlDateConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        long now = System.currentTimeMillis();
        java.sql.Date date = mapper.map(now, java.sql.Date.class);
        Assert.assertEquals(now, date.getTime());
        
        long reverse = mapper.map(date, Long.class);
        Assert.assertEquals(now, reverse);
    }
	
	@Test
	public void testLongToCalendarConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		MapperFacade mapper = factory.getMapperFacade();
		
		long now = System.currentTimeMillis();
		Calendar cal = mapper.map(now, Calendar.class);
		Assert.assertEquals(now, cal.getTimeInMillis());
		
		long reverse = mapper.map(cal, Long.class);
		Assert.assertEquals(now, reverse);
	}
	
	@Test
    public void testLongToTimestampConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        long now = System.currentTimeMillis();
        Timestamp tstamp = mapper.map(now, Timestamp.class);
        Assert.assertEquals(now, tstamp.getTime());
        
        long reverse = mapper.map(tstamp, Long.class);
        Assert.assertEquals(now, reverse);
    }

	
	@Test
    public void testTimestampToXmlGregorianCalendarConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        XMLGregorianCalendar xml = mapper.map(now, XMLGregorianCalendar.class);
        
        Assert.assertEquals(now.getTime(), xml.toGregorianCalendar().getTimeInMillis());
        
        Timestamp reverse = mapper.map(xml, Timestamp.class);
        Assert.assertEquals(now, reverse);
    }
    
    @Test
    public void testTimestampToDateConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Date date = mapper.map(now, Date.class);
        Assert.assertEquals(now.getTime(), date.getTime());
        
        Timestamp reverse = mapper.map(date, Timestamp.class);
        Assert.assertEquals(now, reverse);
    }

    @Test
    public void testTimestampToTimeConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Time date = mapper.map(now, Time.class);
        Assert.assertEquals(now.getTime(), date.getTime());
        
        Timestamp reverse = mapper.map(date, Timestamp.class);
        Assert.assertEquals(now, reverse);
    }
    
    @Test
    public void testTimestampToSqlDateConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        java.sql.Date date = mapper.map(now, java.sql.Date.class);
        Assert.assertEquals(now.getTime(), date.getTime());
        
        Timestamp reverse = mapper.map(date, Timestamp.class);
        Assert.assertEquals(now, reverse);
    }
    
    @Test
    public void testTimestampToCalendarConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Calendar cal = mapper.map(now, Calendar.class);
        Assert.assertEquals(now.getTime(), cal.getTimeInMillis());
        
        Timestamp reverse = mapper.map(cal, Timestamp.class);
        Assert.assertEquals(now, reverse);
    }
	
	
}
