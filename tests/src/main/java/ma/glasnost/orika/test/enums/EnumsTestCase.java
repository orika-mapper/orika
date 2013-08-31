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

package ma.glasnost.orika.test.enums;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.enums.EnumsTestCaseClasses.Book;
import ma.glasnost.orika.test.enums.EnumsTestCaseClasses.BookDTOWithAltCaseEnum;
import ma.glasnost.orika.test.enums.EnumsTestCaseClasses.BookDTOWithAlternateEnum;
import ma.glasnost.orika.test.enums.EnumsTestCaseClasses.BookDTOWithParallelEnum;
import ma.glasnost.orika.test.enums.EnumsTestCaseClasses.BookDTOWithSameEnum;
import ma.glasnost.orika.test.enums.EnumsTestCaseClasses.BookImpl;
import ma.glasnost.orika.test.enums.EnumsTestCaseClasses.PublicationFormat;
import ma.glasnost.orika.test.enums.EnumsTestCaseClasses.PublicationFormatDTOAltCase;
import ma.glasnost.orika.test.enums.EnumsTestCaseClasses.PublicationFormatDTOAlternate;

import org.junit.Assert;
import org.junit.Test;

public class EnumsTestCase {
    
    private Book createBook() {
        Book book = new BookImpl();
        book.setTitle("The Prophet");
        book.setFormat(PublicationFormat.EBOOK);
        return book;
    }
    
    @Test
    public void testMapSharedEnum() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        Book book = createBook();
        BookDTOWithSameEnum mappedBook = mapper.map(book, BookDTOWithSameEnum.class);
        
        Assert.assertEquals(book.getTitle(), mappedBook.getTitle());
        Assert.assertEquals(book.getFormat(), mappedBook.getFormat());
    }
    
    @Test
    public void testMapParallelEnum() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        Book book = createBook();
        BookDTOWithParallelEnum mappedBook = mapper.map(book, BookDTOWithParallelEnum.class);
        
        Assert.assertEquals(book.getTitle(), mappedBook.getTitle());
        Assert.assertEquals(book.getFormat().name(), mappedBook.getFormat().name());
    }
    
    @Test
    public void testMapAltCaseEnumWithConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.getConverterFactory().registerConverter(new CustomConverter<PublicationFormat, PublicationFormatDTOAltCase>() {

            public PublicationFormatDTOAltCase convert(PublicationFormat source, Type<? extends PublicationFormatDTOAltCase> destinationType) {
                switch (source) {
                case HARDBACK:
                    return PublicationFormatDTOAltCase.hardBack;
                case SOFTBACK:
                    return PublicationFormatDTOAltCase.softBack;
                case EBOOK:
                    return PublicationFormatDTOAltCase.eBook;
                default:
                    return null;
                }
            }
        });
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Book book = createBook();
        BookDTOWithAltCaseEnum mappedBook = mapper.map(book, BookDTOWithAltCaseEnum.class);
        
        Assert.assertEquals(book.getTitle(), mappedBook.getTitle());
        Assert.assertEquals(book.getFormat().toString().toUpperCase(), mappedBook.getFormat().toString().toUpperCase());
    }
    
    @Test
    public void testMapAlternateEnumWithConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new CustomConverter<PublicationFormat, PublicationFormatDTOAlternate>() {

            public PublicationFormatDTOAlternate convert(PublicationFormat source,
                    Type<? extends PublicationFormatDTOAlternate> destinationType) {
                switch (source) {
                case HARDBACK:
                    return PublicationFormatDTOAlternate.PUB_HARDBACK;
                case SOFTBACK:
                    return PublicationFormatDTOAlternate.PUB_SOFTBACK;
                case EBOOK:
                    return PublicationFormatDTOAlternate.PUB_EBOOK;
                default:
                    return null;
                }
            }
        });
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Book book = createBook();
        BookDTOWithAlternateEnum mappedBook = mapper.map(book, BookDTOWithAlternateEnum.class);
        
        Assert.assertEquals(book.getTitle(), mappedBook.getTitle());
        Assert.assertEquals("PUB_" + book.getFormat().toString(), mappedBook.getFormat().toString());
    }
    
}
