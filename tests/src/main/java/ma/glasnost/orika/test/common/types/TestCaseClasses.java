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

package ma.glasnost.orika.test.common.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public interface TestCaseClasses {

    public class PrimitiveHolder {
    	private short shortValue;
    	private int intValue;
    	private long longValue;
    	private float floatValue;
    	private double doubleValue;
    	private char charValue;
    	private boolean booleanValue;
    	private byte byteValue;
		
    	public PrimitiveHolder(short shortValue, int intValue, long longValue,
				float floatValue, double doubleValue, char charValue, boolean booleanValue, byte byteValue) {
			super();
			this.shortValue = shortValue;
			this.intValue = intValue;
			this.longValue = longValue;
			this.floatValue = floatValue;
			this.doubleValue = doubleValue;
			this.charValue = charValue;
			this.booleanValue = booleanValue;
			this.byteValue = byteValue;
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
    
    public class PrimitiveHolderDTO {
    	private short shortValue;
    	private int intValue;
    	private long longValue;
    	private float floatValue;
    	private double doubleValue;
    	private char charValue;
		private boolean booleanValue;
		private byte byteValue;
    	
    	public short getShortValue() {
			return shortValue;
		}
		public void setShortValue(short shortValue) {
			this.shortValue = shortValue;
		}
		public int getIntValue() {
			return intValue;
		}
		public void setIntValue(int intValue) {
			this.intValue = intValue;
		}
		public long getLongValue() {
			return longValue;
		}
		public void setLongValue(long longValue) {
			this.longValue = longValue;
		}
		public float getFloatValue() {
			return floatValue;
		}
		public void setFloatValue(float floatValue) {
			this.floatValue = floatValue;
		}
		public double getDoubleValue() {
			return doubleValue;
		}
		public void setDoubleValue(double doubleValue) {
			this.doubleValue = doubleValue;
		}
		public char getCharValue() {
			return charValue;
		}
		public void setCharValue(char charValue) {
			this.charValue = charValue;
		}
		public boolean isBooleanValue() {
			return booleanValue;
		}
		public void setBooleanValue(boolean booleanValue) {
			this.booleanValue = booleanValue;
		}
		public byte getByteValue() {
			return byteValue;
		}
		public void setByteValue(byte byteValue) {
			this.byteValue = byteValue;
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
    
    public class PrimitiveWrapperHolder {
    	private Short shortValue;
    	private Integer intValue;
    	private Long longValue;
    	private Float floatValue;
    	private Double doubleValue;
    	private Character charValue;
    	private Boolean booleanValue;
    	private Byte byteValue;
		
    	public PrimitiveWrapperHolder(Short shortValue, Integer intValue,
				Long longValue, Float floatValue, Double doubleValue,
				Character charValue, Boolean booleanValue, Byte byteValue) {
			super();
			this.shortValue = shortValue;
			this.intValue = intValue;
			this.longValue = longValue;
			this.floatValue = floatValue;
			this.doubleValue = doubleValue;
			this.charValue = charValue;
			this.booleanValue = booleanValue;
			this.byteValue = byteValue;
		}

		public Short getShortValue() {
			return shortValue;
		}

		public Integer getIntValue() {
			return intValue;
		}

		public Long getLongValue() {
			return longValue;
		}

		public Float getFloatValue() {
			return floatValue;
		}

		public Double getDoubleValue() {
			return doubleValue;
		}

		public Character getCharValue() {
			return charValue;
		}

		public Boolean getBooleanValue() {
			return booleanValue;
		}
		
		public Byte getByteValue() {
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
    
    public class PrimitiveWrapperHolderDTO {
    	private Short shortValue;
    	private Integer intValue;
    	private Long longValue;
    	private Float floatValue;
    	private Double doubleValue;
    	private Character charValue;
    	private Boolean booleanValue;
    	private Byte byteValue;
		
    	public Short getShortValue() {
			return shortValue;
		}
		public void setShortValue(Short shortValue) {
			this.shortValue = shortValue;
		}
		public Integer getIntValue() {
			return intValue;
		}
		public void setIntValue(Integer intValue) {
			this.intValue = intValue;
		}
		public Long getLongValue() {
			return longValue;
		}
		public void setLongValue(Long longValue) {
			this.longValue = longValue;
		}
		public Float getFloatValue() {
			return floatValue;
		}
		public void setFloatValue(Float floatValue) {
			this.floatValue = floatValue;
		}
		public Double getDoubleValue() {
			return doubleValue;
		}
		public void setDoubleValue(Double doubleValue) {
			this.doubleValue = doubleValue;
		}
		public Character getCharValue() {
			return charValue;
		}
		public void setCharValue(Character charValue) {
			this.charValue = charValue;
		}
		public Boolean getBooleanValue() {
			return booleanValue;
		}
		public void setBooleanValue(Boolean booleanValue) {
			this.booleanValue = booleanValue;
		}
		
		public Byte getByteValue() {
			return byteValue;
		}
		public void setByteValue(Byte byteValue) {
			this.byteValue = byteValue;
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
 
	public interface Book {
		
		public String getTitle();
		public Author getAuthor();

	}
	
	public interface Author {

		public String getName();
		
	}
	
	public interface Library {
		
		public String getTitle();
		public List<Book> getBooks();
	}
	
	public class BookImpl implements Book {

		private final String title;
		private final Author author;
		
		public BookImpl(String title, Author author) {
			this.title = title;
			this.author = author;
		}
		
		public String getTitle() {
			return title;
		}

		public Author getAuthor() {
			return author;
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
	
	public class AuthorImpl implements Author {

		private final String name;

		public AuthorImpl(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
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
	
	public class AuthorNested {
		
		private final Name name;
		
		public AuthorNested(Name name) {
			this.name = name;
		}

		public Name getName() {
			
			return name;
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
	
	public class Name {
		private final String firstName;
		private final String lastName;
		
		public Name(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}
		
		public String getFirstName() {
			return firstName;
		}
		
		public String getLastName() {
			return lastName;
		}
		
		public String getFullName() {
			return firstName + " " + lastName;
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
	
	public class LibraryNested {
		
		private String title;
		private List<BookNested> books;
		
		
		public LibraryNested(String title, List<BookNested> books) {
			super();
			this.title = title;
			this.books = books;
		}
		
		public String getTitle() {
			return title;
		}
		
		public List<BookNested> getBooks() {
			return books;
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
	
	public class BookNested {
		private String title;
		private AuthorNested author;
		
		public BookNested(String title, AuthorNested author) {
			super();
			this.title = title;
			this.author = author;
		}

		public String getTitle() {
			return title;
		}

		public AuthorNested getAuthor() {
			return author;
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
	
	public class LibraryImpl implements Library {
		
		private final String title;
		private List<Book> books;

		public LibraryImpl(String title, List<Book> books) {
			super();
			this.title = title;
			this.books = books;
		}

		public String getTitle() {
			return title;
		}

		public List<Book> getBooks() {
			if (books==null) {
				books = new ArrayList<Book>();
			}
			return books;
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

	
	
	public class AuthorDTO {
		
		private String name;
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
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
	
	public class BookDTO {

		private String title;
		private AuthorDTO author;
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public AuthorDTO getAuthor() {
			return author;
		}

		public void setAuthor(AuthorDTO author) {
			this.author = author;
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
	
	public class LibraryDTO {
		
		private String title;
		private List<BookDTO> books;
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}
		
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public List<BookDTO> getBooks() {
			if (books==null) {
				books = new ArrayList<BookDTO>();
			}
			return books;
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

	
	public class AuthorMyDTO {
	
		private String name;
		private String additionalValue;
		
		public String getMyAdditionalValue() {
			return additionalValue;
		}

		public void setMyAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}

		public String getMyName() {
			return name;
		}

		public void setMyName(String name) {
			this.name = name;
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
	
	public class BookMyDTO {

		private String title;
		private AuthorMyDTO author;
		private String additionalValue;
		
		public String getMyAdditionalValue() {
			return additionalValue;
		}

		public void setMyAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}

		public String getMyTitle() {
			return title;
		}

		public void setMyTitle(String title) {
			this.title = title;
		}

		public AuthorMyDTO getMyAuthor() {
			return author;
		}

		public void setMyAuthor(AuthorMyDTO author) {
			this.author = author;
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
	
	public class LibraryMyDTO {
		
		private String title;
		private List<BookMyDTO> books;
		private String additionalValue;
		
		public String getMyAdditionalValue() {
			return additionalValue;
		}

		public void setMyAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}
		
		public String getMyTitle() {
			return title;
		}

		public void setMyTitle(String title) {
			this.title = title;
		}

		public List<BookMyDTO> getMyBooks() {
			if (books==null) {
				books = new ArrayList<BookMyDTO>();
			}
			return books;
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
}
