package ma.glasnost.orika.test.unenhance.inheritance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class MyEntity extends AbstractEntity {
	@Column
	private String myProperty;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "references")
	private MyEntity references;

	public MyEntity() {
	}

	public String getMyProperty() {
		return myProperty;
	}

	public void setMyProperty(String myProperty) {
		this.myProperty = myProperty;
	}

	public MyEntity getReferences() {
		return references;
	}

	public void setReferences(MyEntity references) {
		this.references = references;
	}
}
