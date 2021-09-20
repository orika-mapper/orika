package ma.glasnost.orika.test.community.issue135;

public class RepA {

	private String id;
	private int primitive;
	private Boolean active;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	private RepB repB;

	public RepB getRepB() {
		return repB;
	}

	public void setRepB(RepB repB) {
		this.repB = repB;
	}

	public int getPrimitive() {
		return primitive;
	}
	
	public void setPrimitive(int primtive) {
		this.primitive = primtive;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
	
	
	
}
