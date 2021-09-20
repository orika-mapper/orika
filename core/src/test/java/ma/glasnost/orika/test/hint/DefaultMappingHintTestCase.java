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

package ma.glasnost.orika.test.hint;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Author;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.AuthorMyDTO;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.AuthorParent;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Book;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.BookMyDTO;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.BookParent;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Library;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.LibraryMyDTO;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.LibraryParent;
import org.junit.Assert;
import org.junit.Test;

public class DefaultMappingHintTestCase {
    
    private Author createAuthor(Class<? extends AuthorParent> type) throws InstantiationException, IllegalAccessException {
        AuthorParent author = type.newInstance();
        author.setName("Khalil Gebran");
        
        return author;
    }
    
    private Book createBook(Class<? extends BookParent> type) throws InstantiationException, IllegalAccessException {
        BookParent book = type.newInstance();
        book.setTitle("The Prophet");
        
        return book;
    }
    
    private Library createLibrary(Class<? extends LibraryParent> type) throws InstantiationException, IllegalAccessException {
        LibraryParent lib = type.newInstance();
        lib.setTitle("Test Library");
        
        return lib;
    }
    
    @Test
    public void testMappingByDefaultWithNoHint() throws Exception {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.registerClassMap(factory.classMap(Library.class, LibraryMyDTO.class).byDefault().toClassMap());
        
        factory.registerClassMap(factory.classMap(Author.class, AuthorMyDTO.class).byDefault().toClassMap());
        
        factory.registerClassMap(factory.classMap(Book.class, BookMyDTO.class).byDefault().toClassMap());
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Book book = createBook(BookParent.class);
        book.setAuthor(createAuthor(AuthorParent.class));
        Library lib = createLibrary(LibraryParent.class);
        lib.getBooks().add(book);
        
        LibraryMyDTO mappedLib = mapper.map(lib, LibraryMyDTO.class);
        
        Assert.assertNotNull(mappedLib);
        Assert.assertTrue(mappedLib.getMyBooks().isEmpty());
    }
    
    /**
     * @throws Exception
     */
    @Test
    public void testMappingByDefaultWithHint() throws Exception {

    	DefaultFieldMapper myHint =
        /**
         * This sample hint converts "myProperty" to "property", and vis-versa.
         */
                (fromProperty, fromPropertyType) -> {
                    if (fromProperty.startsWith("my")) {
                        return fromProperty.substring(2, 3).toLowerCase() + fromProperty.substring(3);
                    } else {
                        return "my" + fromProperty.substring(0, 1).toUpperCase() + fromProperty.substring(1);
                    }
                };
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.registerClassMap(factory.classMap(Library.class, LibraryMyDTO.class).byDefault(myHint).toClassMap());
        
        factory.registerClassMap(factory.classMap(Author.class, AuthorMyDTO.class).byDefault(myHint).toClassMap());
        
        factory.registerClassMap(factory.classMap(Book.class, BookMyDTO.class).byDefault(myHint).toClassMap());
        
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
    
    /**
     * @throws Exception
     */
    @Test
    public void testMappingWithRegisteredHintAndNoClassMap() throws Exception {
        
    	DefaultFieldMapper myHint =
        /**
         * This sample hint converts "myProperty" to "property", and vis-versa.
         */
                (fromProperty, fromPropertyType) -> {
                    if (fromProperty.startsWith("my")) {
                        return fromProperty.substring(2, 3).toLowerCase() + fromProperty.substring(3);
                    } else {
                        return "my" + fromProperty.substring(0, 1).toUpperCase() + fromProperty.substring(1);
                    }
                };
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.registerDefaultFieldMapper(myHint);
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Book book = createBook(BookParent.class);
        book.setAuthor(createAuthor(AuthorParent.class));
        Library lib = createLibrary(LibraryParent.class);
        lib.getBooks().add(book);
        
        LibraryMyDTO mappedLib = mapper.map(lib, LibraryMyDTO.class);
        
        Assert.assertEquals(lib.getTitle(), mappedLib.getMyTitle());
        Assert.assertEquals(book.getTitle(), mappedLib.getMyBooks().get(0).getMyTitle());
        Assert.assertEquals(book.getAuthor().getName(), mappedLib.getMyBooks().get(0).getMyAuthor().getMyName());
        
        // Now, map it back to the original...
        
        Library lib2 = mapper.map(mappedLib, Library.class);
        Assert.assertEquals(lib.getTitle(), lib2.getTitle());
        Assert.assertEquals(book.getTitle(), lib2.getBooks().get(0).getTitle());
        Assert.assertEquals(book.getAuthor().getName(), lib2.getBooks().get(0).getAuthor().getName());
        
    }
}
