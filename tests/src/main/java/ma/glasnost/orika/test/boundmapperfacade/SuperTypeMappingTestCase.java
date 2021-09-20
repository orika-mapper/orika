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

package ma.glasnost.orika.test.boundmapperfacade;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.MavenProjectUtil;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Author;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.AuthorChild;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.AuthorMyDTO;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.AuthorParent;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Book;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.BookChild;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.BookMyDTO;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.BookParent;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Library;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.LibraryChild;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.LibraryMyDTO;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.LibraryParent;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.util.Date;

public class SuperTypeMappingTestCase {
    
    private Author createAuthor(Class<? extends Author> type) throws InstantiationException, IllegalAccessException {
        Author author = type.newInstance();
        author.setName("Khalil Gebran");
        
        return author;
    }
    
    private Book createBook(Class<? extends Book> type) throws InstantiationException, IllegalAccessException {
        Book book = type.newInstance();
        book.setTitle("The Prophet");
        
        return book;
    }
    
    private Library createLibrary(Class<? extends Library> type) throws InstantiationException, IllegalAccessException {
        Library lib = type.newInstance();
        lib.setTitle("Test Library");
        
        return lib;
    }
    
    @Test
    public void testMappingInterfaceImplementationNoExistingMapping() throws Exception {
        
        MapperFacade mapper = MappingUtil.getMapperFactory().getMapperFacade();
        
        Book book = createBook(BookChild.class);
        book.setAuthor(createAuthor(AuthorChild.class));
        
        BookMyDTO mappedBook = mapper.map(book, BookMyDTO.class);
        
        Assert.assertNotNull(mappedBook);
        Assert.assertNull(mappedBook.getMyTitle());
        Assert.assertNull(mappedBook.getMyAuthor());
    }
    
    @Test
    public void testMappingInterfaceImplementationWithExistingDirectMapping() throws Exception {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.registerClassMap(factory.classMap(Library.class, LibraryMyDTO.class)
                .field("title", "myTitle")
                .field("books", "myBooks")
                .byDefault()
                .toClassMap());
        
        factory.registerClassMap(factory.classMap(Author.class, AuthorMyDTO.class).field("name", "myName").byDefault().toClassMap());
        factory.registerClassMap(factory.classMap(Book.class, BookMyDTO.class)
                .field("title", "myTitle")
                .field("author", "myAuthor")
                .byDefault()
                .toClassMap());
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Book book = createBook(BookParent.class);
        book.setAuthor(createAuthor(AuthorParent.class));
        Library lib = createLibrary(LibraryParent.class);
        lib.getBooks().add(book);
        
        LibraryMyDTO mappedLib = mapper.map(lib, LibraryMyDTO.class);
        
        Assert.assertEquals(lib.getTitle(), mappedLib.getMyTitle());
        Assert.assertEquals(book.getTitle(), mappedLib.getMyBooks().get(0).getMyTitle());
        Assert.assertEquals(book.getAuthor().getName(), mappedLib.getMyBooks().get(0).getMyAuthor().getMyName());
    }
    
    @Test
    public void testMappingInterfaceImplementationWithExistingInheritedMapping() throws Exception {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.registerClassMap(factory.classMap(Library.class, LibraryMyDTO.class)
                .field("title", "myTitle")
                .field("books", "myBooks")
                .byDefault()
                .toClassMap());
        
        factory.registerClassMap(factory.classMap(Author.class, AuthorMyDTO.class).field("name", "myName").byDefault().toClassMap());
        factory.registerClassMap(factory.classMap(Book.class, BookMyDTO.class)
                .field("title", "myTitle")
                .field("author", "myAuthor")
                .byDefault()
                .toClassMap());
        
        MapperFacade mapper = factory.getMapperFacade();
        
        // BookChild, AuthorChild, LibraryChild don't directly
        // implement Book, Author and Library
        Book book = createBook(BookChild.class);
        book.setAuthor(createAuthor(AuthorChild.class));
        Library lib = createLibrary(LibraryChild.class);
        lib.getBooks().add(book);
        
        LibraryMyDTO mappedLib = mapper.map(lib, LibraryMyDTO.class);
        
        Assert.assertEquals(lib.getTitle(), mappedLib.getMyTitle());
        Assert.assertEquals(book.getTitle(), mappedLib.getMyBooks().get(0).getMyTitle());
        Assert.assertEquals(book.getAuthor().getName(), mappedLib.getMyBooks().get(0).getMyAuthor().getMyName());
    }
    
    @Test
    public void testMappingSubclassImplementationWithoutExistingMapping() throws Exception {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        DefaultFieldMapper myHint =
        /**
         * This sample hint converts "myProperty" to "property", and vis-versa.
         */
                (fromProperty, fromPropertyType) -> {
                    if (fromProperty.startsWith("my")) {
                        return fromProperty.substring(2, 1).toLowerCase() + fromProperty.substring(3);
                    } else {
                        return "my" + fromProperty.substring(0, 1).toUpperCase() + fromProperty.substring(1);
                    }
                };
        factory.registerDefaultFieldMapper(myHint);
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Book book = createBook(BookChild.class);
        book.setAuthor(createAuthor(AuthorChild.class));
        
        BookMyDTO mappedBook = mapper.map(book, BookMyDTO.class);
        
        Assert.assertEquals(book.getTitle(), mappedBook.getMyTitle());
        Assert.assertEquals(book.getAuthor().getName(), mappedBook.getMyAuthor().getMyName());
    }
    
    @Test
    public void testMappingSubclassImplementationWithExistingMapping() throws Exception {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.registerClassMap(factory.classMap(AuthorParent.class, AuthorMyDTO.class)
                .field("name", "myName")
                .byDefault()
                .toClassMap());
        factory.registerClassMap(factory.classMap(BookParent.class, BookMyDTO.class)
                .field("title", "myTitle")
                .field("author", "myAuthor")
                .byDefault()
                .toClassMap());
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Book book = createBook(BookChild.class);
        book.setAuthor(createAuthor(AuthorChild.class));
        
        BookMyDTO mappedBook = mapper.map(book, BookMyDTO.class);
        
        Assert.assertEquals(book.getTitle(), mappedBook.getMyTitle());
        Assert.assertEquals(book.getAuthor().getName(), mappedBook.getMyAuthor().getMyName());
    }
    
    public static class A {
        XMLGregorianCalendar time;
        
        public A() {
        }
        
        public XMLGregorianCalendar getTime() {
            return time;
        }
        
        public void setTime(XMLGregorianCalendar time) {
            this.time = time;
        }
    }
    
    public static class B {
        Date time;
        
        public B() {
        }
        
        public Date getTime() {
            return time;
        }
        
        public void setTime(Date time) {
            this.time = time;
        }
    }

}
