package ma.glasnost.orika.test.common.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.commons.collections.list.TreeList;

import ma.glasnost.orika.test.common.types.TestCaseClasses.Author;
import ma.glasnost.orika.test.common.types.TestCaseClasses.AuthorDTO;
import ma.glasnost.orika.test.common.types.TestCaseClasses.AuthorNested;
import ma.glasnost.orika.test.common.types.TestCaseClasses.Book;
import ma.glasnost.orika.test.common.types.TestCaseClasses.BookDTO;
import ma.glasnost.orika.test.common.types.TestCaseClasses.BookNested;
import ma.glasnost.orika.test.common.types.TestCaseClasses.Library;
import ma.glasnost.orika.test.common.types.TestCaseClasses.LibraryDTO;
import ma.glasnost.orika.test.common.types.TestCaseClasses.LibraryNested;
import ma.glasnost.orika.test.common.types.TestCaseClasses.PrimitiveHolder;
import ma.glasnost.orika.test.common.types.TestCaseClasses.PrimitiveHolderDTO;
import ma.glasnost.orika.test.common.types.TestCaseClasses.PrimitiveWrapperHolder;
import ma.glasnost.orika.test.common.types.TestCaseClasses.PrimitiveWrapperHolderDTO;

public class Validations {
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Common mapping validations
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public static void assertValidMapping(PrimitiveHolder primitiveHolder, PrimitiveHolderDTO dto) {
    	assertEquals(primitiveHolder.getShortValue(), dto.getShortValue());
    	assertEquals(primitiveHolder.getIntValue(), dto.getIntValue());
    	assertEquals(primitiveHolder.getLongValue(), dto.getLongValue());
    	assertEquals(primitiveHolder.getFloatValue(), dto.getFloatValue(), 1.0f);
    	assertEquals(primitiveHolder.getDoubleValue(), dto.getDoubleValue(), 1.0d);
    	assertEquals(primitiveHolder.getCharValue(), dto.getCharValue());
    	assertEquals(primitiveHolder.isBooleanValue(), dto.isBooleanValue());
    }
    
    public static void assertValidMapping(PrimitiveWrapperHolder primitiveHolder, PrimitiveWrapperHolderDTO dto) {
    	assertEquals(primitiveHolder.getShortValue(), dto.getShortValue());
    	assertEquals(primitiveHolder.getIntValue(), dto.getIntValue());
    	assertEquals(primitiveHolder.getLongValue(), dto.getLongValue());
    	assertEquals(primitiveHolder.getFloatValue(), dto.getFloatValue(), 1.0f);
    	assertEquals(primitiveHolder.getDoubleValue(), dto.getDoubleValue(), 1.0d);
    	assertEquals(primitiveHolder.getCharValue(), dto.getCharValue());
    	assertEquals(primitiveHolder.getBooleanValue(), dto.getBooleanValue());
    }
    
    public static void assertValidMapping(PrimitiveWrapperHolder wrappers, PrimitiveHolder primitives) {
    	assertEquals(wrappers.getShortValue().shortValue(), primitives.getShortValue());
    	assertEquals(wrappers.getIntValue().intValue(), primitives.getIntValue());
    	assertEquals(wrappers.getLongValue().longValue(), primitives.getLongValue());
    	assertEquals(wrappers.getFloatValue().floatValue(), primitives.getFloatValue(), 1.0f);
    	assertEquals(wrappers.getDoubleValue().doubleValue(), primitives.getDoubleValue(), 1.0d);
    	assertEquals(wrappers.getCharValue().charValue(), primitives.getCharValue());
    	assertEquals(wrappers.getBooleanValue().booleanValue(), primitives.isBooleanValue());
    }
    
    
    public static void assertValidMapping(Library library, LibraryDTO dto) {
    	
    	assertNotNull(library);
    	assertNotNull(dto);
    	
    	assertNotNull(library.getBooks());
    	assertNotNull(dto.getBooks());
    	
    	@SuppressWarnings("unchecked")
		List<Book> sortedBooks = new TreeList(library.getBooks()); 
    	
    	@SuppressWarnings("unchecked")
		List<BookDTO> sortedDTOs = new TreeList(dto.getBooks());
    	
    	assertEquals(sortedBooks.size(), sortedDTOs.size());
    	
    	for (int i = 0, count=sortedBooks.size(); i < count; ++i) {
    		Book book = sortedBooks.get(i);
    		BookDTO bookDto = sortedDTOs.get(i);
    		assertValidMapping(book,bookDto);
    	}
    }
    
    public static void assertValidMapping(LibraryNested library, LibraryDTO dto) {
    	
    	assertNotNull(library);
    	assertNotNull(dto);
    	
    	assertNotNull(library.getBooks());
    	assertNotNull(dto.getBooks());
    	
    	@SuppressWarnings("unchecked")
		List<BookNested> sortedBooks = new TreeList(library.getBooks()); 
    	
    	@SuppressWarnings("unchecked")
		List<BookDTO> sortedDTOs = new TreeList(dto.getBooks());
    	
    	assertEquals(sortedBooks.size(), sortedDTOs.size());
    	
    	for (int i = 0, count=sortedBooks.size(); i < count; ++i) {
    		BookNested book = sortedBooks.get(i);
    		BookDTO bookDto = sortedDTOs.get(i);
    		assertValidMapping(book,bookDto);
    	}
    }
    
    public static void assertValidMapping(Book book, BookDTO dto) {
    	assertNotNull(book);
    	assertNotNull(dto);
    	assertEquals(book.getTitle(), dto.getTitle());
    	assertValidMapping(book.getAuthor(), dto.getAuthor());
    }
    
    public static void assertValidMapping(BookNested book, BookDTO dto) {
    	assertNotNull(book);
    	assertNotNull(dto);
    	assertEquals(book.getTitle(), dto.getTitle());
    	assertValidMapping(book.getAuthor(), dto.getAuthor());
    }
    
    public static void assertValidMapping(Author author, AuthorDTO authorDTO) {
    	assertNotNull(author);
    	assertNotNull(authorDTO);
    	assertEquals(author.getName(),authorDTO.getName());
    }
    
    public static void assertValidMapping(AuthorNested author, AuthorDTO authorDTO) {
    	assertNotNull(author);
    	assertNotNull(authorDTO);
    	assertEquals(author.getName().getFullName(),authorDTO.getName());
    }
}
