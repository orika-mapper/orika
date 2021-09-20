package ma.glasnost.orika.test.unenhance.inheritance2;

import javax.persistence.Entity;

/**
 * @author Sergey Vasilyev
 */
@Entity
public class OwnerDto extends PersonDto {
	private String specialInformation;

	public String getSpecialInformation() {
		return specialInformation;
	}

	public void setSpecialInformation(String specialInformation) {
		this.specialInformation = specialInformation;
	}
}
