package ma.glasnost.orika.test.community.issue135;

public class Domain {

	private SubA subA;

	private SubB subB;
	
	private int primitive;
	private boolean active; 

	public SubA getSubA() {
		return subA;
	}

	public void setSubA(SubA subA) {
		this.subA = subA;
	}

	public SubB getSubB() {
		return subB;
	}

	public void setSubB(SubB subB) {
		this.subB = subB;
	}

	public int getPrimitive() {
		return primitive;
	}

	public void setPrimitive(int primtive) {
		this.primitive = primtive;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	
}
