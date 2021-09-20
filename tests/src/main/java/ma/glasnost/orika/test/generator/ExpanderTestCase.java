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

package ma.glasnost.orika.test.generator;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ScoringClassMapBuilder;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This test case demonstrates two things together:
 * 
 * 1) The usage of the ScoringClassMapBuilder to automagically guess
 * the right mapping of various fields based on their "sameness"
 * 
 * 2) The usage of built-in nested field mapping functionality
 *  to handle mapping these objects, resulting in the mapping of a
 *  flat list structure into an expanded object graph by guessing
 *  how the fields should line up.
 * 
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class ExpanderTestCase {
    
    public static class Year {
        public int yearNumber;
        public String yearAnimal;
        public List<Month> months = new ArrayList<>();
        @Override
		public String toString() {
			return "Year [yearNumber=" + yearNumber + ", yearAnimal=" + yearAnimal + ", months=" + months + "]";
		}
		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof Year)) {
				return false;
			}
			Year castOther = (Year) other;
			return Objects.equals(yearNumber, castOther.yearNumber) && Objects.equals(yearAnimal, castOther.yearAnimal)
					&& Objects.equals(months, castOther.months);
		}
		@Override
		public int hashCode() {
			return Objects.hash(yearNumber, yearAnimal, months);
		}
    }
    
    public static class Month {
        public int monthNumber;
        public String monthName;
        public List<Day> days = new ArrayList<>();
        @Override
		public String toString() {
			return "Month [monthNumber=" + monthNumber + ", monthName=" + monthName + ", days=" + days + "]";
		}
		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof Month)) {
				return false;
			}
			Month castOther = (Month) other;
			return Objects.equals(monthNumber, castOther.monthNumber) && Objects.equals(monthName, castOther.monthName)
					&& Objects.equals(days, castOther.days);
		}
		@Override
		public int hashCode() {
			return Objects.hash(monthNumber, monthName, days);
		}
    }
    
    public static class Day {
        public int dayNumber;
        public String dayOfWeek;
        @Override
		public String toString() {
			return "Day [dayNumber=" + dayNumber + ", dayOfWeek=" + dayOfWeek + "]";
		}
		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof Day)) {
				return false;
			}
			Day castOther = (Day) other;
			return Objects.equals(dayNumber, castOther.dayNumber) && Objects.equals(dayOfWeek, castOther.dayOfWeek);
		}
		@Override
		public int hashCode() {
			return Objects.hash(dayNumber, dayOfWeek);
		}
    }
    
    public static class FlatData {
        public int dayNumber;
        public String dayOfWeek;
        public int yearNumber;
        public String yearAnimal;
        public int monthNumber;
        public String monthName;
        @Override
		public String toString() {
			return "FlatData [dayNumber=" + dayNumber + ", dayOfWeek=" + dayOfWeek + ", yearNumber=" + yearNumber
					+ ", yearAnimal=" + yearAnimal + ", monthNumber=" + monthNumber + ", monthName=" + monthName + "]";
		}
		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof FlatData)) {
				return false;
			}
			FlatData castOther = (FlatData) other;
			return Objects.equals(dayNumber, castOther.dayNumber) && Objects.equals(dayOfWeek, castOther.dayOfWeek)
					&& Objects.equals(yearNumber, castOther.yearNumber)
					&& Objects.equals(yearAnimal, castOther.yearAnimal)
					&& Objects.equals(monthNumber, castOther.monthNumber)
					&& Objects.equals(monthName, castOther.monthName);
		}
		@Override
		public int hashCode() {
			return Objects.hash(dayNumber, dayOfWeek, yearNumber, yearAnimal, monthNumber, monthName);
		}
    }
    
    @Test
    public void testExpand() {
        
        Type<List<FlatData>> typeOf_FlatData = new TypeBuilder<List<FlatData>>(){}.build();
        Type<List<Year>> typeOf_Year = new TypeBuilder<List<Year>>(){}.build();
        
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder()
            .classMapBuilderFactory(new ScoringClassMapBuilder.Factory())
            .build();
        
        mapperFactory.classMap(typeOf_FlatData, typeOf_Year).byDefault().register();
        
        MapperFacade mapper = mapperFactory.getMapperFacade();
        
        
        List<FlatData> flatData = new ArrayList<>();
        FlatData item = new FlatData();
        item.dayNumber = 1;
        item.dayOfWeek = "Monday";
        item.monthNumber = 10;
        item.monthName = "October";
        item.yearNumber = 2011;
        item.yearAnimal = "monkey";
        flatData.add(item);
        
        item = new FlatData();
        item.dayNumber = 2;
        item.dayOfWeek = "Tuesday";
        item.monthNumber = 12;
        item.monthName = "December";
        item.yearNumber = 2011;
        item.yearAnimal = "monkey";
        flatData.add(item);
        
        item = new FlatData();
        item.dayNumber = 2;
        item.dayOfWeek = "Tuesday";
        item.monthNumber = 12;
        item.monthName = "December";
        item.yearNumber = 2012;
        item.yearAnimal = "dragon";
        flatData.add(item);
        
        List<Year> years = mapper.map(flatData, typeOf_FlatData, typeOf_Year);
        
        Assert.assertNotNull(years);
        Assert.assertFalse(years.isEmpty());
        Assert.assertEquals(2, years.size());
        
        Year year = years.get(0);
        Assert.assertEquals(2011, year.yearNumber);
        Assert.assertEquals(2, year.months.size());
        
        Month m1 = year.months.get(0);
        Assert.assertEquals("October", m1.monthName);
        Assert.assertEquals(10,m1.monthNumber);
        
        Day m1d1 = m1.days.get(0); 
        Assert.assertEquals("Monday", m1d1.dayOfWeek);
        Assert.assertEquals(1,m1d1.dayNumber);
        
        Month m2 = year.months.get(1);
        Assert.assertEquals("December", m2.monthName);
        Assert.assertEquals(12, m2.monthNumber);
        
        Day m2d1 = m2.days.get(0); 
        Assert.assertEquals("Tuesday", m2d1.dayOfWeek);
        Assert.assertEquals(2,m2d1.dayNumber);
        
        year = years.get(1);
        Assert.assertEquals(2012, year.yearNumber);
        Assert.assertEquals(1, year.months.size());
        
        m1 = year.months.get(0);
        Assert.assertEquals("December", m1.monthName);
        Assert.assertEquals(12, m1.monthNumber);
        
        m1d1 = m1.days.get(0); 
        Assert.assertEquals("Tuesday", m1d1.dayOfWeek);
        Assert.assertEquals(2,m1d1.dayNumber);
        
        
        List<FlatData> mapBack = mapper.map(years, typeOf_Year, typeOf_FlatData);
        
        Assert.assertEquals(flatData, mapBack);
        
    }
}
