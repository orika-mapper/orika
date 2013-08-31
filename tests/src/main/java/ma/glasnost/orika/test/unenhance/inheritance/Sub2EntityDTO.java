package ma.glasnost.orika.test.unenhance.inheritance;

public class Sub2EntityDTO extends MyEntityDTO {
	private int sub2Property;

	public Sub2EntityDTO() {
	}

	public int getSub2Property() {
		return sub2Property;
	}

	public void setSub2Property(int sub2Property) {
		this.sub2Property = sub2Property;
	}
}
