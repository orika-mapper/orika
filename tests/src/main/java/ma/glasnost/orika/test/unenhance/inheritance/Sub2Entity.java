package ma.glasnost.orika.test.unenhance.inheritance;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Sub2Entity extends MyEntity {
	@Column
	private int sub2Property;

	public Sub2Entity() {
	}

	public int getSub2Property() {
		return sub2Property;
	}

	public void setSub2Property(int sub2Property) {
		this.sub2Property = sub2Property;
	}
}
