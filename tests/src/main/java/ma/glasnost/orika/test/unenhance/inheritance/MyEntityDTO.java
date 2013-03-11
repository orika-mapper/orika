package ma.glasnost.orika.test.unenhance.inheritance;

public class MyEntityDTO extends AbstractDTO {
	private String myProperty;

	private MyEntityDTO references;

	public MyEntityDTO() {
	}

	public String getMyProperty() {
		return myProperty;
	}

	public void setMyProperty(String myProperty) {
		this.myProperty = myProperty;
	}

	public MyEntityDTO getReferences() {
		return references;
	}

	public void setReferences(MyEntityDTO references) {
		this.references = references;
	}
}
