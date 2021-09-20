package ma.glasnost.orika.test.unenhance.inheritance2;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.Set;

/**
 * @author Sergey Vasilyev
 */
@Entity
public class RoomEntity extends AbstractEntity {
	private Set<PersonEntity> tenants;

	@OneToMany(fetch = FetchType.LAZY)
	@OrderBy
	public Set<PersonEntity> getTenants() {
		return tenants;
	}

	public void setTenants(Set<PersonEntity> tenants) {
		this.tenants = tenants;
	}
}
