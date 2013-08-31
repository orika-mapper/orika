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

package ma.glasnost.orika.test.constructor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ma.glasnost.orika.test.common.types.TestCaseClasses.PrimitiveWrapperHolder;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public interface TestCaseClasses {

	public static class Person {
        private String firstName;
        private String lastName;
        
        private Long age;
        private Date date;
        
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public Long getAge() {
            return age;
        }
        
        public void setAge(Long age) {
            this.age = age;
        }
        
        public Date getDate() {
            return date;
        }
        
        public void setDate(Date date) {
            this.date = date;
        }
        
    }
    
    public static class PersonVO {
        private final String firstName;
        private final String lastName;
        
        private final long age;
        private final String dateOfBirth;
        
        public PersonVO(String firstName, String lastName, long age, String dateOfBirth) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.dateOfBirth = dateOfBirth;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public long getAge() {
            return age;
        }
        
        public String getDateOfBirth() {
            return dateOfBirth;
        }
    }
    
    public static class PersonVO2 {
    	
    	private final String firstName;
        private final String lastName;
        
        private final long age;
        private final String dateOfBirth;
        
        public PersonVO2(String firstName, String lastName, Long age, String dateOfBirth) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age!=null ? age.longValue() : 0;
            this.dateOfBirth = dateOfBirth;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public long getAge() {
            return age;
        }
        
        public String getDateOfBirth() {
            return dateOfBirth;
        }
    }
    
    
    public static class PersonVO3 {
    	
    	private final String firstName;
        private final String lastName;
        
        private final long age;
        private final String dateOfBirth;
        
        public PersonVO3(String dateOfBirth, Long age, String lastName, String firstName) {
        	this(firstName, lastName, age, dateOfBirth);
        }
        
        public PersonVO3(String firstName, String lastName, Long age, String dateOfBirth) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age!=null ? age.longValue() : 0;
            this.dateOfBirth = dateOfBirth;
        }
        
        public PersonVO3(String firstName, String lastName, Date dateOfBirth) {
        	this(firstName, lastName, yearsDifference(dateOfBirth, new Date()), 
        			new SimpleDateFormat("dd/MM/yyyy").format(dateOfBirth));
        }
        
		public static long yearsDifference(final Date start, final Date end) {
			long diff = end.getTime() - start.getTime();
			return diff / TimeUnit.SECONDS.toMillis(60*60*24*365);
		}
        
        public String getFirstName() {
            return firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public long getAge() {
            return age;
        }
        
        public String getDateOfBirth() {
            return dateOfBirth;
        }
    }
    
    public class NestedPrimitiveHolder {
    	
    	private char charValue;
    	private boolean booleanValue;
    	private byte byteValue;
    	private PrimitiveNumberHolder numbers;
		
    	public NestedPrimitiveHolder(PrimitiveNumberHolder numbers, char charValue, boolean booleanValue, byte byteValue) {
			super();
			this.charValue = charValue;
			this.booleanValue = booleanValue;
			this.byteValue = byteValue;
			this.numbers = numbers;
		}

		public PrimitiveNumberHolder getNumbers() {
			return numbers;
		}

		public char getCharValue() {
			return charValue;
		}

		public boolean isBooleanValue() {
			return booleanValue;
		}
		
		public byte getByteValue() {
			return byteValue;
		}

		public boolean equals(Object that) {
			return EqualsBuilder.reflectionEquals(this,that);
		}
		
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
		
		public String toString() {
			return ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE);
		}
    }
    
    public class PrimitiveNumberHolder {
    	private short shortValue;
    	private int intValue;
    	private long longValue;
    	private float floatValue;
    	private double doubleValue;
		
    	public PrimitiveNumberHolder(short shortValue, int intValue, long longValue,
				float floatValue, double doubleValue) {
			super();
			this.shortValue = shortValue;
			this.intValue = intValue;
			this.longValue = longValue;
			this.floatValue = floatValue;
			this.doubleValue = doubleValue;
		}

		public short getShortValue() {
			return shortValue;
		}

		public int getIntValue() {
			return intValue;
		}

		public long getLongValue() {
			return longValue;
		}

		public float getFloatValue() {
			return floatValue;
		}

		public double getDoubleValue() {
			return doubleValue;
		}

		public boolean equals(Object that) {
			return EqualsBuilder.reflectionEquals(this,that);
		}
		
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
		
		public String toString() {
			return ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE);
		}
    }
    
    public class Holder {
    	private NestedPrimitiveHolder nested;

		public Holder(NestedPrimitiveHolder nestedHolder) {
			super();
			this.nested = nestedHolder;
		}

		public NestedPrimitiveHolder getNested() {
			return nested;
		}
    }
    
    public class WrapperHolder {
    	private PrimitiveWrapperHolder nested;

		public PrimitiveWrapperHolder getNested() {
			return nested;
		}

		public void setNested(PrimitiveWrapperHolder nested) {
			this.nested = nested;
		}
    }
}
