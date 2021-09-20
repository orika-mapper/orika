package ma.glasnost.orika.test.packageprivate;

public class SomeParentClass {

	protected static class SomeProtectedClass {
		private String field;

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}
	}

}
