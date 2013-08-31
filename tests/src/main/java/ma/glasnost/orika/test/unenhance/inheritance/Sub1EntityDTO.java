package ma.glasnost.orika.test.unenhance.inheritance;

public class Sub1EntityDTO extends MyEntityDTO {
	private int sub1Property;

	public Sub1EntityDTO() {
	}

	public int getSub1Property() {
		return sub1Property;
	}

	public void setSub1Property(int sub1Property) {
		this.sub1Property = sub1Property;
	}
}
