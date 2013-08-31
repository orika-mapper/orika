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

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.property.TestCaseClasses.Author;
import ma.glasnost.orika.test.property.TestCaseClasses.AuthorChild;
import ma.glasnost.orika.test.property.TestCaseClasses.Book;
import ma.glasnost.orika.test.property.TestCaseClasses.BookChild;
import ma.glasnost.orika.test.property.TestCaseClasses.BookMyDTO;

import org.junit.Assert;
import org.junit.Test;

/**
 * Verifies that objects that include public fields can also be properly mapped
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class PublicFieldMappingTestCase {
    
    private Author createAuthor(Class<? extends Author> type) throws InstantiationException, IllegalAccessException {
        Author author = type.newInstance();
        author.setName("Khalil Gebran");
        
        return author;
    }
    
    private Book createBook(Class<? extends Book> type) throws InstantiationException, IllegalAccessException {
        Book book = type.newInstance();
        book.title = "The Prophet";
        
        return book;
    }
    
    @Test
    public void testMappingInterfaceImplementationNoExistingMapping() throws Exception {
        
      
        DefaultFieldMapper myHint =
                /**
                 * This sample hint converts "myProperty" to "property", and vis-versa.
                 */
                new DefaultFieldMapper() {
                    
                    public String suggestMappedField(String fromProperty, Type<?> fromPropertyType) {
                        if (fromProperty.startsWith("my")) {
                            return fromProperty.substring(2, 3).toLowerCase() + fromProperty.substring(3);
                        } else {
                            return "my" + fromProperty.substring(0, 1).toUpperCase() + fromProperty.substring(1);
                        }
                    }
                    
                };
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.registerDefaultFieldMapper(myHint);
        MapperFacade mapper = factory.getMapperFacade();
        
        Book book = createBook(BookChild.class);
        book.setAuthor(createAuthor(AuthorChild.class));
        
        BookMyDTO mappedBook = mapper.map(book, BookMyDTO.class);
        Book mapBack = mapper.map(mappedBook, Book.class);
        
        Assert.assertNotNull(mappedBook);
        Assert.assertNotNull(mapBack);
        
        Assert.assertEquals(book.getAuthor().getName(), mappedBook.getMyAuthor().getMyName());
        Assert.assertEquals(book.title, mappedBook.getMyTitle());
        Assert.assertEquals(book.getAuthor().getName(), mapBack.getAuthor().getName());
        Assert.assertEquals(book.title, mapBack.title);
    }
 
    
}
