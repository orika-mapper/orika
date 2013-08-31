package dtotypes;


import types.BookHidden;
import types.AuthorHidden;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class Runner {

	public static void run(MapperFactory mapperFactory) {
		
		mapperFactory.classMap(BookHidden.class, BookHiddenDto.class).byDefault().register();
		
		AuthorHidden author = new AuthorHidden();
		author.setName("Chuck Testa");
		
		BookHidden book = new BookHidden();
		book.setAuthor(author);
		
		mapperFactory.getMapperFacade().map(book, BookHiddenDto.class);
		
	}
	
	
	@Test
	public void test() {
		run(MappingUtil.getMapperFactory());
	}
	
}
