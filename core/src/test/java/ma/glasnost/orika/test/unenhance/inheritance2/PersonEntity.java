package ma.glasnost.orika.test.unenhance.inheritance2;

import javax.persistence.Entity;

/**
 * @author Sergey Vasilyev
 */
@Entity
public class PersonEntity extends AbstractEntity {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


}
