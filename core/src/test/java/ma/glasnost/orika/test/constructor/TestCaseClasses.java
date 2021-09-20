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

package ma.glasnost.orika.test.constructor;

import ma.glasnost.orika.test.common.types.TestCaseClasses.PrimitiveWrapperHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public interface TestCaseClasses {

	class Person {
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
    
    class PersonVO {
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
    
    class PersonVO2 {
    	
    	private final String firstName;
        private final String lastName;
        
        private final long age;
        private final String dateOfBirth;
        
        public PersonVO2(String firstName, String lastName, Long age, String dateOfBirth) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age!=null ? age : 0;
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
    
    
    class PersonVO3 {
    	
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
            this.age = age!=null ? age : 0;
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

    class PersonVO4 {

        private final String firstName;
        private final String lastName;
        private final long age;

        public PersonVO4(String pFirstName, String pLastName, long pAge) {
            firstName = pFirstName;
            lastName = pLastName;
            age = pAge;
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
    }

    class NestedPrimitiveHolder {
    	
    	private final char charValue;
    	private final boolean booleanValue;
    	private final byte byteValue;
    	private final PrimitiveNumberHolder numbers;
		
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
			 if (that == this) return true;
		        if (!(that instanceof NestedPrimitiveHolder)) {
		            return false;
		        }
		     NestedPrimitiveHolder nph = (NestedPrimitiveHolder) that;
		     return Objects.equals(getCharValue(), nph.getCharValue()) &&
		    		 Objects.equals(isBooleanValue(), nph.isBooleanValue()) &&
		    		 Objects.equals(getByteValue(), nph.getByteValue()) &&
		    		 Objects.equals(getNumbers(), nph.getNumbers());
		}
		
		public int hashCode() {
			return Objects.hash(charValue, booleanValue, byteValue, numbers);
		}
		
		@Override
		public String toString() {
			return "NestedPrimitiveHolder [charValue=" + charValue + ", booleanValue=" + booleanValue + ", byteValue="
					+ byteValue + ", numbers=" + numbers + "]";
		}
    }
    
    class PrimitiveNumberHolder {
    	private final short shortValue;
    	private final int intValue;
    	private final long longValue;
    	private final float floatValue;
    	private final double doubleValue;
		
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

		@Override
		public String toString() {
			return "PrimitiveNumberHolder [shortValue=" + shortValue + ", intValue=" + intValue + ", longValue="
					+ longValue + ", floatValue=" + floatValue + ", doubleValue=" + doubleValue + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof PrimitiveNumberHolder)) {
				return false;
			}
			PrimitiveNumberHolder castOther = (PrimitiveNumberHolder) other;
			return Objects.equals(shortValue, castOther.shortValue) && Objects.equals(intValue, castOther.intValue)
					&& Objects.equals(longValue, castOther.longValue)
					&& Objects.equals(floatValue, castOther.floatValue)
					&& Objects.equals(doubleValue, castOther.doubleValue);
		}

		@Override
		public int hashCode() {
			return Objects.hash(shortValue, intValue, longValue, floatValue, doubleValue);
		}
    }
    
    class Holder {
    	private final NestedPrimitiveHolder nested;

		public Holder(NestedPrimitiveHolder nestedHolder) {
			super();
			this.nested = nestedHolder;
		}

		public NestedPrimitiveHolder getNested() {
			return nested;
		}
    }
    
    class WrapperHolder {
    	private PrimitiveWrapperHolder nested;

		public PrimitiveWrapperHolder getNested() {
			return nested;
		}

		public void setNested(PrimitiveWrapperHolder nested) {
			this.nested = nested;
		}
    }
}
