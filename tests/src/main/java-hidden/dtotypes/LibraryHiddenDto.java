package dtotypes;

import java.util.ArrayList;
import java.util.List;


public class LibraryHiddenDto {
		
	private String title;

	private List<BookHiddenDto> books;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<BookHiddenDto> getBooks() {
		if (books==null) {
			books = new ArrayList<BookHiddenDto>();
		}
		return books;
	}

}