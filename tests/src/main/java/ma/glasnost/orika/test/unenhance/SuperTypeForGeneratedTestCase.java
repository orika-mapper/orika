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

package ma.glasnost.orika.test.unenhance;

import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Author;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.AuthorParent;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Book;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.BookParent;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Library;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.LibraryDTO;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.LibraryParent;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

public class SuperTypeForGeneratedTestCase {

	
	private Author createAuthor() throws InstantiationException, IllegalAccessException {

		Author author = EasyMock.createNiceMock(AuthorParent.class);
		EasyMock.expect(author.getName()).andReturn("Khalil Gebran").anyTimes();
		EasyMock.replay(author);
		
		return author;
	}
	
	private Book createBook() throws InstantiationException, IllegalAccessException {
		Book book = EasyMock.createNiceMock(BookParent.class);
		EasyMock.expect(book.getTitle()).andReturn("The Prophet").anyTimes();
		Author author = createAuthor();
		EasyMock.expect(book.getAuthor()).andReturn(author).anyTimes();
		EasyMock.replay(book);
		
		return book;
	}
	
	private Library createLibrary() throws InstantiationException, IllegalAccessException {
		
		Library lib = EasyMock.createNiceMock(LibraryParent.class);
		EasyMock.expect(lib.getTitle()).andReturn("Test Library").anyTimes();
		List<Book> books = new ArrayList<Book>();
		Book book = createBook();
		books.add(book);
		EasyMock.expect(lib.getBooks()).andReturn(books).anyTimes();
		
		EasyMock.replay(lib);
		
		return lib;
	}
	

	@Test
	public void testSuperTypeMappingForInaccessibleClasses() throws Exception {
		
		MapperFactory factory = MappingUtil.getMapperFactory();

		Library lib = createLibrary();
		Book book = lib.getBooks().get(0);
		
		LibraryDTO mappedLib = factory.getMapperFacade().map(lib, LibraryDTO.class);
		
		Assert.assertNotNull(mappedLib);

		Assert.assertEquals(lib.getTitle(),mappedLib.getTitle());
		Assert.assertEquals(book.getTitle(),mappedLib.getBooks().get(0).getTitle());
		Assert.assertEquals(book.getAuthor().getName(),mappedLib.getBooks().get(0).getAuthor().getName());
		
	}
	
}
