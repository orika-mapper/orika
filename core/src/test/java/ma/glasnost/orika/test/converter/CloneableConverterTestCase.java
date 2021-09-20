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

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.CloneableConverter;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

public class CloneableConverterTestCase {
    
    @Test
    public void cloneableConverter() throws DatatypeConfigurationException {
        
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
    }

    @Test
    public void cloneableConverterNullSource() throws DatatypeConfigurationException {    	
    	MapperFactory factory = new DefaultMapperFactory.Builder().build();
        factory.classMap(ComplexSource.class, ComplexDest.class)
        	.field("someDate.dateHolder{date}", "meaningfulDate{date}")
        	.byDefault().register();
        MapperFacade facade = factory.getMapperFacade();
        
        Date date1 = new Date(System.currentTimeMillis() + 100001);        
        Date date2 = new Date(System.currentTimeMillis() + 100002);        
        ArrayOfDates arrayOfDate = new ArrayOfDates();
        arrayOfDate.getDateHolder().add(new SourceDateHolder(date1));
        arrayOfDate.getDateHolder().add(new SourceDateHolder(date2));
                
        ComplexSource complexSource = new ComplexSource(arrayOfDate);
        ComplexDest complexDest = facade.map(complexSource, ComplexDest.class);
                
        Assert.assertEquals(complexSource.getSomeDate().getDateHolder().size(), complexDest.getMeaningfulDate().size());
        
        Assert.assertEquals(date1, complexDest.getMeaningfulDate().get(0).getDate());        
        Assert.assertNotSame(date1, complexDest.getMeaningfulDate().get(0).getDate());
        
        Assert.assertEquals(date2, complexDest.getMeaningfulDate().get(1).getDate());        
        Assert.assertNotSame(date2, complexDest.getMeaningfulDate().get(1).getDate());
        
        complexSource.getSomeDate().getDateHolder().add(new SourceDateHolder(null));
        
        // use cached clone method here without checking if source is null causes a NullPointerException 
        complexDest = facade.map(complexSource, ComplexDest.class);
        Assert.assertEquals(complexSource.getSomeDate().getDateHolder().size(), complexDest.getMeaningfulDate().size());
        
        Assert.assertNull(complexDest.getMeaningfulDate().get(2).getDate());
    }
    
    
    /**
     * This test method demonstrates that you can decide to treat one of the default cloneable types
     * as immutable if desired by registering your own PassThroughConverter for that type
     * 
     * @throws DatatypeConfigurationException
     */
    @Test
    public void overrideDefaultCloneableToImmutable() throws DatatypeConfigurationException {
        
        PassThroughConverter cc = new PassThroughConverter(Date.class, Calendar.class);
        
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
        source.calendar = cal;
        source.xmlCalendar = xmlCal;
        
        ClonableHolder dest = factory.getMapperFacade().map(source, ClonableHolder.class);
        Assert.assertEquals(source.value, dest.value);
        Assert.assertNotSame(source.value, dest.value);
        Assert.assertEquals(source.date, dest.date);
        Assert.assertSame(source.date, dest.date);
        Assert.assertEquals(source.calendar, dest.calendar);
        Assert.assertSame(source.calendar, dest.calendar);
        Assert.assertEquals(source.xmlCalendar, dest.xmlCalendar);
        Assert.assertNotSame(source.xmlCalendar, dest.xmlCalendar);
        
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
        	return Objects.equals(id, ((SampleCloneable) that).id);
        }
        
        public int hashCode() {
        	return Objects.hash(id);
        }
        
    }
    
    public static class ComplexSource {
    	private ArrayOfDates someDate;
    	
    	public ComplexSource() {}
    	
    	public ComplexSource(ArrayOfDates someDate) {
    		this.someDate = someDate;
    	}

		public ArrayOfDates getSomeDate() {
			return someDate;
		}

		public void setSomeDate(ArrayOfDates someDate) {
			this.someDate = someDate;
		}
    }
    
    public static class ArrayOfDates {
    	protected List<SourceDateHolder> dateHolder;
    	
    	public List<SourceDateHolder> getDateHolder() {
            if (dateHolder == null) {
            	dateHolder = new ArrayList<>();
            }
            return this.dateHolder;
        }    	
    }
    
    public static class SourceDateHolder {
    	private Date date;
    	
    	public SourceDateHolder() {}
    	
    	public SourceDateHolder(Date date) {
    		this.date = date;    		
    	}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}
    }
    
    public static class ComplexDest {
    	private List<DestDateHolder> meaningfulDate = new ArrayList<>();

		public List<DestDateHolder> getMeaningfulDate() {
			return meaningfulDate;
		}

		public void setMeaningfulDate(List<DestDateHolder> meaningfulDate) {
			this.meaningfulDate = meaningfulDate;
		}
    }
    
    public static class DestDateHolder {
    	private Date date;
    	
    	public DestDateHolder() {}
    	
    	public DestDateHolder(Date date) {
    		this.date = date;    		
    	}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}
    }

}
