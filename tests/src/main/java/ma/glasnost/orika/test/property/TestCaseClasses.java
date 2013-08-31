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

package ma.glasnost.orika.test.property;

import java.util.ArrayList;
import java.util.List;

public interface TestCaseClasses {

    
    public class Student {
        public Book favoriteBook;
    }
    
	public class Book {

		public String title;
		
		private Author author;

		public Author getAuthor() {
		    return author;
		}
        
        public void setAuthor(Author author) {
            this.author = author;
        }
	}
	
	public class Author {

		private String name;
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}
	
	public class Library {
		
		private String title;

		public List<Book> books;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}

	public class BookChild extends Book {
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}
	}
	
	public class AuthorChild extends Author {
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}
	}
	
	public class LibraryChild extends Library {
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
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
	}
	
	public class BookDTO {

		private String title;
		public AuthorDTO author;
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
	}
	
	public class LibraryDTO {
		
		public String title;
		private List<BookDTO> books;
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}

		public List<BookDTO> getBooks() {
			if (books==null) {
				books = new ArrayList<BookDTO>();
			}
			return books;
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
	}
	
	public class BookMyDTO {

		private String title;
		private AuthorMyDTO author;
		public String additionalValue;

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
	}
	
	public class LibraryMyDTO {
		
		public String myTitle;
		private List<BookMyDTO> books;
		private String additionalValue;
		
		public String getMyAdditionalValue() {
			return additionalValue;
		}

		public void setMyAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}

		public List<BookMyDTO> getMyBooks() {
			if (books==null) {
				books = new ArrayList<BookMyDTO>();
			}
			return books;
		}
	}
	
	public static class A {
        private Name name;
        private Address address;
        
        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Name readTheNameForThisBean() {
            return name;
        }

        public void assignTheName(Name name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((address == null) ? 0 : address.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            A other = (A) obj;
            if (address == null) {
                if (other.address != null)
                    return false;
            } else if (!address.equals(other.address))
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        } 
        
        
    }
    
    public static class B {
        public String givenName;
        public String sirName;
        public String street;
        public String city;
        public String postalCode;
        public String country;
    }
    
    public static class Name {
        private String firstName;
        private String lastName;
        
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
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
            result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Name other = (Name) obj;
            if (firstName == null) {
                if (other.firstName != null)
                    return false;
            } else if (!firstName.equals(other.firstName))
                return false;
            if (lastName == null) {
                if (other.lastName != null)
                    return false;
            } else if (!lastName.equals(other.lastName))
                return false;
            return true;
        }
        
    }
    
    public static class Address {
        public String street;
        public String city;
        public String postalCode;
        public String country;
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((city == null) ? 0 : city.hashCode());
            result = prime * result + ((country == null) ? 0 : country.hashCode());
            result = prime * result + ((postalCode == null) ? 0 : postalCode.hashCode());
            result = prime * result + ((street == null) ? 0 : street.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Address other = (Address) obj;
            if (city == null) {
                if (other.city != null)
                    return false;
            } else if (!city.equals(other.city))
                return false;
            if (country == null) {
                if (other.country != null)
                    return false;
            } else if (!country.equals(other.country))
                return false;
            if (postalCode == null) {
                if (other.postalCode != null)
                    return false;
            } else if (!postalCode.equals(other.postalCode))
                return false;
            if (street == null) {
                if (other.street != null)
                    return false;
            } else if (!street.equals(other.street))
                return false;
            return true;
        }
        
    }
}
