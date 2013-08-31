package dtotypes;

public class BookHiddenDto {

	private String title;
	private AuthorHiddenDto author;
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public AuthorHiddenDto getAuthor() {
		return author;
	}

	public void setAuthor(AuthorHiddenDto author) {
		this.author = author;
	}

}