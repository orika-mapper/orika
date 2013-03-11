package ma.glasnost.orika.test.unenhance.inheritance;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Sub1Entity extends MyEntity {
	@Column
	private int sub1Property;

	public Sub1Entity() {
	}

	public int getSub1Property() {
		return sub1Property;
	}

	public void setSub1Property(int sub1Property) {
		this.sub1Property = sub1Property;
	}
}
