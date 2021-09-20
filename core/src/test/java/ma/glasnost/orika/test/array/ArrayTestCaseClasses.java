/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
