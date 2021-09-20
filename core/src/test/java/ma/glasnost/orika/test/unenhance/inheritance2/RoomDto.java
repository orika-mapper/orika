package ma.glasnost.orika.test.unenhance.inheritance2;

import javax.persistence.Entity;
import java.util.Set;

/**
 * @author Sergey Vasilyev
 */
@Entity
public class RoomDto extends AbstractDto {
	private Set<PersonDto> tenants;

	public Set<PersonDto> getTenants() {
		return tenants;
	}

	public void setTenants(Set<PersonDto> tenants) {
		this.tenants = tenants;
	}
}
