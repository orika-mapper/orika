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

package ma.glasnost.orika.test.unenhance;

import java.util.ArrayList;
import java.util.List;

public interface SuperTypeTestCaseClasses {

	interface Book {
		
		String getTitle();

		void setTitle(String title);

		Author getAuthor();

		void setAuthor(Author author);

	}
	
	interface Author {

		String getName();
		
		void setName(String name);
		
	}
	
	interface Library {
		
		String getTitle();
		
		void setTitle(String title);
		
		List<Book> getBooks();
	}
	
	class BookParent implements Book {

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
	
	class AuthorParent implements Author {

		private String name;
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}
	
	class LibraryParent implements Library {
		
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
				books = new ArrayList<>();
			}
			return books;
		}

	}

	class BookChild extends BookParent {
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}
	}
	
	class AuthorChild extends AuthorParent {
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}
	}
	
	class LibraryChild extends LibraryParent {
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
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
	}
}
