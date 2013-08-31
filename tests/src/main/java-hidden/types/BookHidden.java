package types;

import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Author;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Book;

public class BookHidden implements Book {

	private String title;
	private Author author;
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

}