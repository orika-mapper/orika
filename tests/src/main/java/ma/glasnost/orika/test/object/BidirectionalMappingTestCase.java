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

package ma.glasnost.orika.test.object;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class BidirectionalMappingTestCase {

	@Test
	public void testBidirectionalMapping() {
		MapperFacade mapper = MappingUtil.getMapperFactory().getMapperFacade();

		Author author = new Author();
		author.setFirstName("Khalil");
		author.setLastName("Gibran");

		Book book = new Book();
		book.setTitle("The Prophet");
		book.setAuthor(author);
		author.setBook(book);

		BookDTO dto = mapper.map(book, BookDTO.class);

		Assert.assertEquals(book.getTitle(), dto.getTitle());
		Assert.assertEquals(book.getAuthor().getFirstName(), dto.getAuthor().getFirstName());

		Assert.assertEquals(book.getAuthor().getLastName(), dto.getAuthor().getLastName());

		Assert.assertTrue(dto == dto.getAuthor().getBook());
	}

	public static class Author {
		private String firstName;
		private String lastName;
		private Book book;

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

		public Book getBook() {
			return book;
		}

		public void setBook(Book book) {
			this.book = book;
		}

	}

	public static class Book {

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

	public static class AuthorDTO {
		private String firstName;
		private String lastName;
		private BookDTO book;

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

		public BookDTO getBook() {
			return book;
		}

		public void setBook(BookDTO book) {
			this.book = book;
		}
	}

	public static class BookDTO {

		private String title;

		private AuthorDTO author;

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

}
