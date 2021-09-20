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

package ma.glasnost.orika.test.common.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface TestCaseClasses {

    class PrimitiveHolder {
    	private final short shortValue;
    	private final int intValue;
    	private final long longValue;
    	private final float floatValue;
    	private final double doubleValue;
    	private final char charValue;
    	private final boolean booleanValue;
    	private final byte byteValue;
		
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

		@Override
		public String toString() {
			return "PrimitiveHolder [shortValue=" + shortValue + ", intValue=" + intValue + ", longValue=" + longValue
					+ ", floatValue=" + floatValue + ", doubleValue=" + doubleValue + ", charValue=" + charValue
					+ ", booleanValue=" + booleanValue + ", byteValue=" + byteValue + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof PrimitiveHolder)) {
				return false;
			}
			PrimitiveHolder castOther = (PrimitiveHolder) other;
			return Objects.equals(shortValue, castOther.shortValue) && Objects.equals(intValue, castOther.intValue)
					&& Objects.equals(longValue, castOther.longValue)
					&& Objects.equals(floatValue, castOther.floatValue)
					&& Objects.equals(doubleValue, castOther.doubleValue)
					&& Objects.equals(charValue, castOther.charValue)
					&& Objects.equals(booleanValue, castOther.booleanValue)
					&& Objects.equals(byteValue, castOther.byteValue);
		}

		@Override
		public int hashCode() {
			return Objects.hash(shortValue, intValue, longValue, floatValue, doubleValue, charValue, booleanValue,
					byteValue);
		}
    }	
    
    class PrimitiveHolderDTO {
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
		@Override
		public String toString() {
			return "PrimitiveHolderDTO [shortValue=" + shortValue + ", intValue=" + intValue + ", longValue="
					+ longValue + ", floatValue=" + floatValue + ", doubleValue=" + doubleValue + ", charValue="
					+ charValue + ", booleanValue=" + booleanValue + ", byteValue=" + byteValue + "]";
		}
		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof PrimitiveHolderDTO)) {
				return false;
			}
			PrimitiveHolderDTO castOther = (PrimitiveHolderDTO) other;
			return Objects.equals(shortValue, castOther.shortValue) && Objects.equals(intValue, castOther.intValue)
					&& Objects.equals(longValue, castOther.longValue)
					&& Objects.equals(floatValue, castOther.floatValue)
					&& Objects.equals(doubleValue, castOther.doubleValue)
					&& Objects.equals(charValue, castOther.charValue)
					&& Objects.equals(booleanValue, castOther.booleanValue)
					&& Objects.equals(byteValue, castOther.byteValue);
		}
		@Override
		public int hashCode() {
			return Objects.hash(shortValue, intValue, longValue, floatValue, doubleValue, charValue, booleanValue,
					byteValue);
		}
    }
    
    class PrimitiveWrapperHolder {
    	private final Short shortValue;
    	private final Integer intValue;
    	private final Long longValue;
    	private final Float floatValue;
    	private final Double doubleValue;
    	private final Character charValue;
    	private final Boolean booleanValue;
    	private final Byte byteValue;
		
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

		@Override
		public String toString() {
			return "PrimitiveWrapperHolder [shortValue=" + shortValue + ", intValue=" + intValue + ", longValue="
					+ longValue + ", floatValue=" + floatValue + ", doubleValue=" + doubleValue + ", charValue="
					+ charValue + ", booleanValue=" + booleanValue + ", byteValue=" + byteValue + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof PrimitiveWrapperHolder)) {
				return false;
			}
			PrimitiveWrapperHolder castOther = (PrimitiveWrapperHolder) other;
			return Objects.equals(shortValue, castOther.shortValue) && Objects.equals(intValue, castOther.intValue)
					&& Objects.equals(longValue, castOther.longValue)
					&& Objects.equals(floatValue, castOther.floatValue)
					&& Objects.equals(doubleValue, castOther.doubleValue)
					&& Objects.equals(charValue, castOther.charValue)
					&& Objects.equals(booleanValue, castOther.booleanValue)
					&& Objects.equals(byteValue, castOther.byteValue);
		}

		@Override
		public int hashCode() {
			return Objects.hash(shortValue, intValue, longValue, floatValue, doubleValue, charValue, booleanValue,
					byteValue);
		}
    }
    
    class PrimitiveWrapperHolderDTO {
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
		@Override
		public String toString() {
			return "PrimitiveWrapperHolderDTO [shortValue=" + shortValue + ", intValue=" + intValue + ", longValue="
					+ longValue + ", floatValue=" + floatValue + ", doubleValue=" + doubleValue + ", charValue="
					+ charValue + ", booleanValue=" + booleanValue + ", byteValue=" + byteValue + "]";
		}
		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof PrimitiveWrapperHolderDTO)) {
				return false;
			}
			PrimitiveWrapperHolderDTO castOther = (PrimitiveWrapperHolderDTO) other;
			return Objects.equals(shortValue, castOther.shortValue) && Objects.equals(intValue, castOther.intValue)
					&& Objects.equals(longValue, castOther.longValue)
					&& Objects.equals(floatValue, castOther.floatValue)
					&& Objects.equals(doubleValue, castOther.doubleValue)
					&& Objects.equals(charValue, castOther.charValue)
					&& Objects.equals(booleanValue, castOther.booleanValue)
					&& Objects.equals(byteValue, castOther.byteValue);
		}
		@Override
		public int hashCode() {
			return Objects.hash(shortValue, intValue, longValue, floatValue, doubleValue, charValue, booleanValue,
					byteValue);
		}
    }
 
	interface Book {
		
		String getTitle();
		Author getAuthor();

	}
	
	interface Author {

		String getName();
		
	}
	
	interface Library {
		
		String getTitle();
		List<Book> getBooks();
	}
	
	class BookImpl implements Book {

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

		@Override
		public String toString() {
			return "BookImpl [title=" + title + ", author=" + author + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof BookImpl)) {
				return false;
			}
			BookImpl castOther = (BookImpl) other;
			return Objects.equals(title, castOther.title) && Objects.equals(author, castOther.author);
		}

		@Override
		public int hashCode() {
			return Objects.hash(title, author);
		}
	}
	
	class AuthorImpl implements Author {

		private final String name;

		public AuthorImpl(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return "AuthorImpl [name=" + name + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof AuthorImpl)) {
				return false;
			}
			AuthorImpl castOther = (AuthorImpl) other;
			return Objects.equals(name, castOther.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name);
		}
	}
	
	class AuthorNested {
		
		private final Name name;
		
		public AuthorNested(Name name) {
			this.name = name;
		}

		public Name getName() {
			
			return name;
		}
		@Override
		public String toString() {
			return "AuthorNested [name=" + name + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof AuthorNested)) {
				return false;
			}
			AuthorNested castOther = (AuthorNested) other;
			return Objects.equals(name, castOther.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name);
		}
	}
	
	class Name {
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
		@Override
		public String toString() {
			return "Name [firstName=" + firstName + ", lastName=" + lastName + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof Name)) {
				return false;
			}
			Name castOther = (Name) other;
			return Objects.equals(firstName, castOther.firstName) && Objects.equals(lastName, castOther.lastName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(firstName, lastName);
		}
	}
	
	class LibraryNested {
		
		private final String title;
		private final List<BookNested> books;
		
		
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
		@Override
		public String toString() {
			return "LibraryNested [title=" + title + ", books=" + books + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof LibraryNested)) {
				return false;
			}
			LibraryNested castOther = (LibraryNested) other;
			return Objects.equals(title, castOther.title) && Objects.equals(books, castOther.books);
		}

		@Override
		public int hashCode() {
			return Objects.hash(title, books);
		}
	}
	
	class BookNested {
		private final String title;
		private final AuthorNested author;
		
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
		@Override
		public String toString() {
			return "BookNested [title=" + title + ", author=" + author + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof BookNested)) {
				return false;
			}
			BookNested castOther = (BookNested) other;
			return Objects.equals(title, castOther.title) && Objects.equals(author, castOther.author);
		}

		@Override
		public int hashCode() {
			return Objects.hash(title, author);
		}
		
	}
	
	class LibraryImpl implements Library {
		
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
				books = new ArrayList<>();
			}
			return books;
		}

		@Override
		public String toString() {
			return "LibraryImpl [title=" + title + ", books=" + books + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof LibraryImpl)) {
				return false;
			}
			LibraryImpl castOther = (LibraryImpl) other;
			return Objects.equals(title, castOther.title) && Objects.equals(books, castOther.books);
		}

		@Override
		public int hashCode() {
			return Objects.hash(title, books);
		}
		
	}

	
	
	class AuthorDTO {
		
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
		
		@Override
		public String toString() {
			return "AuthorDTO [name=" + name + ", additionalValue=" + additionalValue + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof AuthorDTO)) {
				return false;
			}
			AuthorDTO castOther = (AuthorDTO) other;
			return Objects.equals(name, castOther.name) && Objects.equals(additionalValue, castOther.additionalValue);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, additionalValue);
		}
	}
	
	class BookDTO {

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
		
		@Override
		public String toString() {
			return "BookDTO [title=" + title + ", author=" + author + ", additionalValue=" + additionalValue + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof BookDTO)) {
				return false;
			}
			BookDTO castOther = (BookDTO) other;
			return Objects.equals(title, castOther.title) && Objects.equals(author, castOther.author)
					&& Objects.equals(additionalValue, castOther.additionalValue);
		}

		@Override
		public int hashCode() {
			return Objects.hash(title, author, additionalValue);
		}
	}
	
	class LibraryDTO {
		
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
				books = new ArrayList<>();
			}
			return books;
		}
		
		@Override
		public String toString() {
			return "LibraryDTO [title=" + title + ", books=" + books + ", additionalValue=" + additionalValue + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof LibraryDTO)) {
				return false;
			}
			LibraryDTO castOther = (LibraryDTO) other;
			return Objects.equals(title, castOther.title) && Objects.equals(books, castOther.books)
					&& Objects.equals(additionalValue, castOther.additionalValue);
		}

		@Override
		public int hashCode() {
			return Objects.hash(title, books, additionalValue);
		}
	}

	
	class AuthorMyDTO {
	
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
		
		@Override
		public String toString() {
			return "AuthorMyDTO [name=" + name + ", additionalValue=" + additionalValue + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof AuthorMyDTO)) {
				return false;
			}
			AuthorMyDTO castOther = (AuthorMyDTO) other;
			return Objects.equals(name, castOther.name) && Objects.equals(additionalValue, castOther.additionalValue);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, additionalValue);
		}
	}
	
	class BookMyDTO {

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
		
		@Override
		public String toString() {
			return "BookMyDTO [title=" + title + ", author=" + author + ", additionalValue=" + additionalValue + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof BookMyDTO)) {
				return false;
			}
			BookMyDTO castOther = (BookMyDTO) other;
			return Objects.equals(title, castOther.title) && Objects.equals(author, castOther.author)
					&& Objects.equals(additionalValue, castOther.additionalValue);
		}

		@Override
		public int hashCode() {
			return Objects.hash(title, author, additionalValue);
		}
	}
	
	class LibraryMyDTO {
		
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
				books = new ArrayList<>();
			}
			return books;
		}
		
		@Override
		public String toString() {
			return "LibraryMyDTO [title=" + title + ", books=" + books + ", additionalValue=" + additionalValue + "]";
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof LibraryMyDTO)) {
				return false;
			}
			LibraryMyDTO castOther = (LibraryMyDTO) other;
			return Objects.equals(title, castOther.title) && Objects.equals(books, castOther.books)
					&& Objects.equals(additionalValue, castOther.additionalValue);
		}

		@Override
		public int hashCode() {
			return Objects.hash(title, books, additionalValue);
		}
	}
}
