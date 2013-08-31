package ma.glasnost.orika.test.array;

import java.util.List;

public class ArrayTestCaseClasses {
    
    public static class A {
        private byte[] buffer;

        public byte[] getBuffer() {
            return buffer;
        }

        public void setBuffer(byte[] buffer) {
            this.buffer = buffer;
        }
    }

    public static class B {
        private byte[] buffer;

        public byte[] getBuffer() {
            return buffer;
        }

        public void setBuffer(byte[] buffer) {
            this.buffer = buffer;
        }
    }

    public static class C {
        private Byte[] buffer;

        public Byte[] getBuffer() {
            return buffer;
        }

        public void setBuffer(Byte[] buffer) {
            this.buffer = buffer;
        }
    }
    
    public static class D {
    	private List<Byte> buffer;

		public List<Byte> getBuffer() {
			return buffer;
		}

		public void setBuffer(List<Byte> buffer) {
			this.buffer = buffer;
		}

    	
    }
}
