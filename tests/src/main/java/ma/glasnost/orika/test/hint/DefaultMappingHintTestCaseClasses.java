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

package ma.glasnost.orika.test.hint;

import java.util.ArrayList;
import java.util.List;

public interface DefaultMappingHintTestCaseClasses {

	public interface Book {
		
		public String getTitle();

		public void setTitle(String title);

		public Author getAuthor();

		public void setAuthor(Author author);

	}
	
	public interface Author {

		public String getName();
		
		public void setName(String name);
		
	}
	
	public interface Library {
		
		public String getTitle();
		
		public List<Book> getBooks();
	}
	
	public class BookParent implements Book {

		private String title;
		private Author author;
		
		
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public Author getAuthor() {
			return author;
		}

		public void setAuthor(Author author) {
			this.author = author;
		}

	}
	
	public class AuthorParent implements Author {

		private String name;
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}
	
	public class LibraryParent implements Library {
		
		private String title;

		private List<Book> books;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public List<Book> getBooks() {
			if (books==null) {
				books = new ArrayList<Book>();
			}
			return books;
		}

	}

	public class BookChild extends BookParent {
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}
	}
	
	public class AuthorChild extends AuthorParent {
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}
	}
	
	public class LibraryChild extends LibraryParent {
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
	
	public class BookDTO {

		private String title;
		private AuthorDTO author;
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

		public AuthorDTO getMyAuthor() {
			return author;
		}

		public void setMyAuthor(AuthorDTO author) {
			this.author = author;
		}
	}
	
	public class LibraryDTO {
		
		private String title;
		private List<BookDTO> books;
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

		public List<BookDTO> getMyBooks() {
			if (books==null) {
				books = new ArrayList<BookDTO>();
			}
			return books;
		}
	}
}
