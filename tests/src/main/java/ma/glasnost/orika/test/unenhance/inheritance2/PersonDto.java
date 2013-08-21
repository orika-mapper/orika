package ma.glasnost.orika.test.unenhance.inheritance2;

/**
 * @author Sergey Vasilyev
 */
public class PersonDto extends AbstractDto {
	private String name;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
