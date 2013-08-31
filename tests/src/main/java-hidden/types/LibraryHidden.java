package types;

import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Book;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Library;

public class LibraryHidden implements Library {
		
	private String title;

	private List<Book> books;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Book> getBooks() {
		if (books==null) {
			books = new ArrayList<Book>();
		}
		return books;
	}

}