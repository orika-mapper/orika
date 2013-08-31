package types;


import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.common.types.TestCaseClasses.BookDTO;

import org.junit.Test;

public class Runner {

	public static void run(MapperFactory mapperFactory) {
		
		
		mapperFactory.registerClassMap(
				ClassMapBuilder.map(BookHidden.class, BookDTO.class).byDefault().toClassMap());
		
		AuthorHidden author = new AuthorHidden();
		author.setName("Chuck Testa");
		
		BookHidden book = new BookHidden();
		book.setAuthor(author);
		
		mapperFactory.getMapperFacade().map(book, BookDTO.class);
		
	}
	
	
	@Test
	public void test() {
		run(MappingUtil.getMapperFactory());
	}
	
}
