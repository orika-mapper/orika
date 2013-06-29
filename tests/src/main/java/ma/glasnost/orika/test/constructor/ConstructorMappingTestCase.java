package ma.glasnost.orika.test.constructor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.net.URLStreamHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.DateToStringConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.common.types.TestCaseClasses.Author;
import ma.glasnost.orika.test.common.types.TestCaseClasses.AuthorDTO;
import ma.glasnost.orika.test.common.types.TestCaseClasses.AuthorImpl;
import ma.glasnost.orika.test.common.types.TestCaseClasses.AuthorNested;
import ma.glasnost.orika.test.common.types.TestCaseClasses.Book;
import ma.glasnost.orika.test.common.types.TestCaseClasses.BookDTO;
import ma.glasnost.orika.test.common.types.TestCaseClasses.BookImpl;
import ma.glasnost.orika.test.common.types.TestCaseClasses.BookNested;
import ma.glasnost.orika.test.common.types.TestCaseClasses.Library;
import ma.glasnost.orika.test.common.types.TestCaseClasses.LibraryDTO;
import ma.glasnost.orika.test.common.types.TestCaseClasses.LibraryImpl;
import ma.glasnost.orika.test.common.types.TestCaseClasses.LibraryNested;
import ma.glasnost.orika.test.common.types.TestCaseClasses.Name;
import ma.glasnost.orika.test.common.types.TestCaseClasses.PrimitiveHolder;
import ma.glasnost.orika.test.common.types.TestCaseClasses.PrimitiveHolderDTO;
import ma.glasnost.orika.test.common.types.TestCaseClasses.PrimitiveWrapperHolder;
import ma.glasnost.orika.test.common.types.TestCaseClasses.PrimitiveWrapperHolderDTO;
import ma.glasnost.orika.test.constructor.TestCaseClasses.Holder;
import ma.glasnost.orika.test.constructor.TestCaseClasses.NestedPrimitiveHolder;
import ma.glasnost.orika.test.constructor.TestCaseClasses.Person;
import ma.glasnost.orika.test.constructor.TestCaseClasses.PersonVO;
import ma.glasnost.orika.test.constructor.TestCaseClasses.PersonVO2;
import ma.glasnost.orika.test.constructor.TestCaseClasses.PersonVO3;
import ma.glasnost.orika.test.constructor.TestCaseClasses.PrimitiveNumberHolder;
import ma.glasnost.orika.test.constructor.TestCaseClasses.WrapperHolder;

import org.apache.commons.collections.list.TreeList;
import org.junit.Test;

public class ConstructorMappingTestCase {
    
    private static final String DATE_CONVERTER = "dateConverter";
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    
    @Test
    public void testSimpleCase() throws Throwable {
    	
        final SimpleDateFormat df = new SimpleDateFormat(DATE_PATTERN);
        MapperFactory factory = MappingUtil.getMapperFactory(true);
        
        factory.registerClassMap(factory.classMap(PersonVO.class, Person.class)
                //.constructorA()
                .fieldMap("dateOfBirth", "date")
                .converter(DATE_CONVERTER)
                .add()
                .byDefault()
                .toClassMap());
        factory.getConverterFactory().registerConverter(DATE_CONVERTER, new DateToStringConverter(DATE_PATTERN));
        
        Person person = new Person();
        person.setFirstName("Abdelkrim");
        person.setLastName("EL KHETTABI");
        person.setDate(df.parse("01/01/1980"));
        person.setAge(31L);
        
        PersonVO vo = factory.getMapperFacade().map(person, PersonVO.class);
        
        Assert.assertEquals(person.getFirstName(), vo.getFirstName());
        Assert.assertEquals(person.getLastName(), vo.getLastName());
        Assert.assertTrue(person.getAge() == vo.getAge());
        Assert.assertEquals("01/01/1980", vo.getDateOfBirth());
    }
    
    
    @Test
    public void testFindConstructor() throws Throwable {
    	final SimpleDateFormat df = new SimpleDateFormat(DATE_PATTERN);
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.registerClassMap(factory.classMap(PersonVO3.class, Person.class)
                .fieldMap("dateOfBirth", "date").converter(DATE_CONVERTER).add()
                .byDefault()
                .toClassMap());
        factory.getConverterFactory().registerConverter(DATE_CONVERTER, new DateToStringConverter(DATE_PATTERN));
        
      
        Person person = new Person();
        person.setFirstName("Abdelkrim");
        person.setLastName("EL KHETTABI");
        person.setDate(df.parse("01/01/1980"));
        person.setAge(31L);
        
        PersonVO3 vo = factory.getMapperFacade().map(person, PersonVO3.class);
        
        Assert.assertEquals(person.getFirstName(), vo.getFirstName());
        Assert.assertEquals(person.getLastName(), vo.getLastName());
        Assert.assertTrue(person.getAge() == vo.getAge());
        Assert.assertEquals("01/01/1980", vo.getDateOfBirth());
    }
    
    public static long yearsDifference(final Date start, final Date end) {
		long diff = end.getTime() - start.getTime();
		return diff / TimeUnit.SECONDS.toMillis(60*60*24*365);
	}
    
    @Test
    public void testFindConstructor2() throws Throwable {
    	final SimpleDateFormat df = new SimpleDateFormat(DATE_PATTERN);
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.registerClassMap(factory.classMap(PersonVO3.class, Person.class)
                .field("firstName", "firstName")
                .field("lastName", "lastName")
        		.field("dateOfBirth", "date"));
        factory.getConverterFactory().registerConverter(DATE_CONVERTER, new DateToStringConverter(DATE_PATTERN));
        
        Person person = new Person();
        person.setFirstName("Abdelkrim");
        person.setLastName("EL KHETTABI");
        person.setDate(df.parse("01/01/1980"));
        
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 1980);
        c.set(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);
        
        person.setAge(yearsDifference(c.getTime(), new Date()));
        
        PersonVO3 vo = factory.getMapperFacade().map(person, PersonVO3.class);
        
        Assert.assertEquals(person.getFirstName(), vo.getFirstName());
        Assert.assertEquals(person.getLastName(), vo.getLastName());
        Assert.assertTrue(person.getAge() == vo.getAge());
        Assert.assertEquals("01/01/1980", vo.getDateOfBirth());
    }
    
    @Test
    public void testAutomaticCaseWithHint() throws Throwable {
        
        final SimpleDateFormat df = new SimpleDateFormat(DATE_PATTERN);
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.registerDefaultFieldMapper(new DefaultFieldMapper() {

        	public String suggestMappedField(String fromProperty,
					Type<?> fromPropertyType) {
					if ("dateOfBirth".equals(fromProperty)) {
						return "date";
					} else if("date".equals(fromProperty)) {
						return "dateOfBirth";
					}
					return null;
				}
	        });

        factory.getConverterFactory().registerConverter(new DateToStringConverter(DATE_PATTERN));
        
        
        Person person = new Person();
        person.setFirstName("Abdelkrim");
        person.setLastName("EL KHETTABI");
        person.setDate(df.parse("01/01/1980"));
        person.setAge(31L);
        
        PersonVO vo = factory.getMapperFacade().map(person, PersonVO.class);
        
        Assert.assertEquals(person.getFirstName(), vo.getFirstName());
        Assert.assertEquals(person.getLastName(), vo.getLastName());
        Assert.assertTrue(person.getAge() == vo.getAge());
        Assert.assertEquals("01/01/1980", vo.getDateOfBirth());
    }
    
    @Test
    public void testPrimitiveToPrimitiveTypes() {
    	
    	PrimitiveHolder primitiveHolder = 
    			new PrimitiveHolder(
    					Short.MAX_VALUE, 
    					Integer.MAX_VALUE, 
    					Long.MAX_VALUE, 
    					Float.MAX_VALUE, 
    					Double.MAX_VALUE,
    					Character.MAX_VALUE, 
    					true,
    					Byte.MAX_VALUE);
    	
    	MapperFactory factory = MappingUtil.getMapperFactory();
    	
    	PrimitiveHolderDTO dto = factory.getMapperFacade().map(primitiveHolder, PrimitiveHolderDTO.class);
    	
    	assertValidMapping(primitiveHolder,dto);
    	
    	PrimitiveHolder mapBack = factory.getMapperFacade().map(dto, PrimitiveHolder.class);
    	
    	assertValidMapping(mapBack, dto);
    }
    
    @Test
    public void testPrimitiveToWrapperTypes() {
    	
    	PrimitiveHolder primitiveHolder = 
    			new PrimitiveHolder(
    					Short.MAX_VALUE, 
    					Integer.MAX_VALUE, 
    					Long.MAX_VALUE, 
    					Float.MAX_VALUE, 
    					Double.MAX_VALUE,
    					Character.MAX_VALUE, 
    					true,
    					Byte.MAX_VALUE);
    	
    	MapperFactory factory = MappingUtil.getMapperFactory();
    	
    	PrimitiveWrapperHolder wrapper = factory.getMapperFacade().map(primitiveHolder, PrimitiveWrapperHolder.class);
    	
    	assertValidMapping(wrapper, primitiveHolder);
    	
    	PrimitiveHolder mapBack = factory.getMapperFacade().map(wrapper, PrimitiveHolder.class);
    	
    	assertValidMapping(wrapper, mapBack);
    }
    
    @Test
    public void testWrapperToWrapperTypes() {
    	
    	PrimitiveWrapperHolder primitiveHolder = 
    			new PrimitiveWrapperHolder(
    					Short.MAX_VALUE, 
    					Integer.MAX_VALUE, 
    					Long.MAX_VALUE, 
    					Float.MAX_VALUE, 
    					Double.MAX_VALUE,
    					Character.MAX_VALUE, 
    					true,
    					Byte.MAX_VALUE);
    	
    	MapperFactory factory = MappingUtil.getMapperFactory();
    	
    	PrimitiveWrapperHolderDTO dto = factory.getMapperFacade().map(primitiveHolder, PrimitiveWrapperHolderDTO.class);
    	
    	assertValidMapping(primitiveHolder, dto);
    	
    	PrimitiveWrapperHolder mapBack = factory.getMapperFacade().map(dto, PrimitiveWrapperHolder.class);
    	
    	assertValidMapping(mapBack, dto);
    }
    
    @Test
    public void testPrimitivePropertiesWithWrapperConstructor() throws Throwable {
    	
    	final SimpleDateFormat df = new SimpleDateFormat(DATE_PATTERN);
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.registerDefaultFieldMapper(new DefaultFieldMapper() {

        	public String suggestMappedField(String fromProperty,
					Type<?> fromPropertyType) {
					if ("dateOfBirth".equals(fromProperty)) {
						return "date";
					} else if("date".equals(fromProperty)) {
						return "dateOfBirth";
					}
					return null;
				}
	        });

        factory.getConverterFactory().registerConverter(new DateToStringConverter(DATE_PATTERN));
        
        Person person = new Person();
        person.setFirstName("Abdelkrim");
        person.setLastName("EL KHETTABI");
        person.setDate(df.parse("01/01/1980"));
        person.setAge(31L);
        
        PersonVO2 vo = factory.getMapperFacade().map(person, PersonVO2.class);
        
        Assert.assertEquals(person.getFirstName(), vo.getFirstName());
        Assert.assertEquals(person.getLastName(), vo.getLastName());
        Assert.assertTrue(person.getAge() == vo.getAge());
        Assert.assertEquals("01/01/1980", vo.getDateOfBirth());
    	
    }
    
    @Test
    public void testBaseCaseWithCollectionTypes() {
    	
    	List<Book> books = new ArrayList<Book>(4);
    	
    	Author author1 = new AuthorImpl("Author #1");
    	Author author2 = new AuthorImpl("Author #2");
    	
    	books.add(new BookImpl("Book #1", author1));
    	books.add(new BookImpl("Book #2", author1));
    	books.add(new BookImpl("Book #3", author2));
    	books.add(new BookImpl("Book #4", author2));
    	
    	Library library = new LibraryImpl("Library #1", books);
    	
    	MapperFactory factory = MappingUtil.getMapperFactory();
    	MapperFacade mapper = factory.getMapperFacade();
    	
    	LibraryDTO mapped = mapper.map(library, LibraryDTO.class);
    	
    	assertValidMapping(library,mapped);
    	
    	Library libraryMapBack = mapper.map(mapped, LibraryImpl.class);
    	
    	assertValidMapping(libraryMapBack,mapped);
    	
    }
    
    @Test
    public void testMappingNestedTypes() {
    	
    	List<BookNested> books = new ArrayList<BookNested>(4);
    	
    	AuthorNested author1 = new AuthorNested(new Name("Abdelkrim","EL KHETTABI"));
    	AuthorNested author2 = new AuthorNested(new Name("Bill","Shakespeare"));
    	
    	books.add(new BookNested("Book #1", author1));
    	books.add(new BookNested("Book #2", author1));
    	books.add(new BookNested("Book #3", author2));
    	books.add(new BookNested("Book #4", author2));
    	
    	LibraryNested library = new LibraryNested("Library #1", books);
    	
    	MapperFactory factory = MappingUtil.getMapperFactory();
    	factory.registerClassMap(
    			factory.classMap(AuthorNested.class, AuthorDTO.class)
    				.field("name.fullName", "name").byDefault().toClassMap());
    
    	MapperFacade mapper = factory.getMapperFacade();
    	
    	LibraryDTO mapped = mapper.map(library, LibraryDTO.class);
    	
    	assertValidMapping(library,mapped);
    	
    	/*
    	// this situation is a bit too complicated to handle normally; 
    	// how would Orika even know how to create a Name object which takes
    	// in multiple parameters it cannot find on the source object?
    	LibraryNested libraryMapBack = mapper.map(mapped, LibraryNested.class);
    	
    	assertValidMapping(libraryMapBack,mapped);
    	
    	*/
    }
    
    
    @Test
    public void testComplexMappingNestedTypes() {
    	
    	
    	PrimitiveNumberHolder numbers = 
    			new PrimitiveNumberHolder(
    					Short.MAX_VALUE, 
    					Integer.MAX_VALUE, 
    					Long.MAX_VALUE, 
    					Float.MAX_VALUE, 
    					Double.MAX_VALUE);
    	
    	NestedPrimitiveHolder primitiveHolder = new NestedPrimitiveHolder(numbers, Character.MAX_VALUE, Boolean.TRUE, Byte.MAX_VALUE);
    	
    	Holder holder = new Holder(primitiveHolder);
    	
    	MapperFactory factory = MappingUtil.getMapperFactory();
    	factory.registerClassMap(
    			factory.classMap(NestedPrimitiveHolder.class, PrimitiveWrapperHolder.class)
    				.field("numbers.shortValue", "shortValue")
    				.field("numbers.intValue", "intValue")
    				.field("numbers.longValue", "longValue")
    				.field("numbers.floatValue", "floatValue")
    				.field("numbers.doubleValue", "doubleValue")
    				.byDefault().toClassMap());
    	
    	WrapperHolder wrapper = factory.getMapperFacade().map(holder, WrapperHolder.class);
    	
    	assertValidMapping(holder, wrapper);
	
    } 
    
    
    public static class URLDto1 {
    	public String protocolX;
    	public String hostX; 
    	public int portX;
    	public String fileX;
    }
    
    public static class URLDto2 {
    	public String protocol;
    	public String host; 
    	public String file;
    }
    
    public static class URLDto3 {
    	public String protocol;
    	public String host; 
    	public int port;
    	public String file;
    	public URLStreamHandler handler;
    }
    
    public static class URLDto4 {
    	public URL context;
    	public String spec;
    }
    
    @Test
    public void testConstructorsWithoutDebugInfo() {
    	MapperFactory factory = MappingUtil.getMapperFactory();
    	factory.registerClassMap(
    			factory.classMap(URLDto1.class, URL.class)
		    		.field("protocolX", "protocol")
		    		.field("hostX", "host")
		    		.field("portX", "port")
		    		.field("fileX", "file"));
    	MapperFacade mapper = factory.getMapperFacade();
    	
    	URLDto1 dto1 = new URLDto1();
    	dto1.protocolX = "http";
    	dto1.hostX = "somewhere.com";
    	dto1.portX = 8080;
    	dto1.fileX = "index.html";
    	
    	URL url = mapper.map(dto1, URL.class);
    	Assert.assertNotNull(url);
    	Assert.assertEquals(dto1.protocolX, url.getProtocol());
    	Assert.assertEquals(dto1.hostX, url.getHost());
    	Assert.assertEquals(dto1.portX, url.getPort());
    	
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Common mapping validations
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private void assertValidMapping(Holder holder, WrapperHolder dto) {
    	assertValidMapping(holder.getNested(), dto.getNested());
    }
    
    private void assertValidMapping(NestedPrimitiveHolder nested, PrimitiveWrapperHolder wrapper) {
    	assertEquals(nested.getNumbers().getShortValue(), wrapper.getShortValue().shortValue());
    	assertEquals(nested.getNumbers().getIntValue(), wrapper.getIntValue().intValue());
    	assertEquals(nested.getNumbers().getLongValue(), wrapper.getLongValue().longValue());
    	assertEquals(nested.getNumbers().getFloatValue(), wrapper.getFloatValue(), 1.0f);
    	assertEquals(nested.getNumbers().getDoubleValue(), wrapper.getDoubleValue(), 1.0d);
    	assertEquals(nested.getCharValue(), wrapper.getCharValue().charValue());
    	assertEquals(nested.isBooleanValue(), wrapper.getBooleanValue().booleanValue());
    	assertEquals(nested.getByteValue(), wrapper.getByteValue().byteValue());
    }
    
    private void assertValidMapping(PrimitiveHolder primitiveHolder, PrimitiveHolderDTO dto) {
    	assertEquals(primitiveHolder.getShortValue(), dto.getShortValue());
    	assertEquals(primitiveHolder.getIntValue(), dto.getIntValue());
    	assertEquals(primitiveHolder.getLongValue(), dto.getLongValue());
    	assertEquals(primitiveHolder.getFloatValue(), dto.getFloatValue(), 1.0f);
    	assertEquals(primitiveHolder.getDoubleValue(), dto.getDoubleValue(), 1.0d);
    	assertEquals(primitiveHolder.getCharValue(), dto.getCharValue());
    	assertEquals(primitiveHolder.isBooleanValue(), dto.isBooleanValue());
    	assertEquals(primitiveHolder.getByteValue(), dto.getByteValue());
    }
    
    private void assertValidMapping(PrimitiveWrapperHolder primitiveHolder, PrimitiveWrapperHolderDTO dto) {
    	assertEquals(primitiveHolder.getShortValue(), dto.getShortValue());
    	assertEquals(primitiveHolder.getIntValue(), dto.getIntValue());
    	assertEquals(primitiveHolder.getLongValue(), dto.getLongValue());
    	assertEquals(primitiveHolder.getFloatValue(), dto.getFloatValue(), 1.0f);
    	assertEquals(primitiveHolder.getDoubleValue(), dto.getDoubleValue(), 1.0d);
    	assertEquals(primitiveHolder.getCharValue(), dto.getCharValue());
    	assertEquals(primitiveHolder.getBooleanValue(), dto.getBooleanValue());
    	assertEquals(primitiveHolder.getByteValue(), dto.getByteValue());
    }
    
    private void assertValidMapping(PrimitiveWrapperHolder wrappers, PrimitiveHolder primitives) {
    	assertEquals(wrappers.getShortValue().shortValue(), primitives.getShortValue());
    	assertEquals(wrappers.getIntValue().intValue(), primitives.getIntValue());
    	assertEquals(wrappers.getLongValue().longValue(), primitives.getLongValue());
    	assertEquals(wrappers.getFloatValue().floatValue(), primitives.getFloatValue(), 1.0f);
    	assertEquals(wrappers.getDoubleValue().doubleValue(), primitives.getDoubleValue(), 1.0d);
    	assertEquals(wrappers.getCharValue().charValue(), primitives.getCharValue());
    	assertEquals(wrappers.getBooleanValue().booleanValue(), primitives.isBooleanValue());
    	assertEquals(wrappers.getByteValue().byteValue(), primitives.getByteValue());
    }
    
    
    private void assertValidMapping(Library library, LibraryDTO dto) {
    	
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
    
    private void assertValidMapping(LibraryNested library, LibraryDTO dto) {
    	
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
    
    private void assertValidMapping(Book book, BookDTO dto) {
    	assertNotNull(book);
    	assertNotNull(dto);
    	assertEquals(book.getTitle(), dto.getTitle());
    	assertValidMapping(book.getAuthor(), dto.getAuthor());
    }
    
    private void assertValidMapping(BookNested book, BookDTO dto) {
    	assertNotNull(book);
    	assertNotNull(dto);
    	assertEquals(book.getTitle(), dto.getTitle());
    	assertValidMapping(book.getAuthor(), dto.getAuthor());
    }
    
    private void assertValidMapping(Author author, AuthorDTO authorDTO) {
    	assertNotNull(author);
    	assertNotNull(authorDTO);
    	assertEquals(author.getName(),authorDTO.getName());
    }
    
    private void assertValidMapping(AuthorNested author, AuthorDTO authorDTO) {
    	assertNotNull(author);
    	assertNotNull(authorDTO);
    	assertEquals(author.getName().getFullName(),authorDTO.getName());
    }
    
    
    
    
}
