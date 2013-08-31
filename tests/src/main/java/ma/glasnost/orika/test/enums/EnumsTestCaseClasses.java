/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
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

package ma.glasnost.orika.test.enums;


public interface EnumsTestCaseClasses {

	public enum PublicationFormat {
		HARDBACK, SOFTBACK, EBOOK;
	}
	
	public enum PublicationFormatDTO {
		HARDBACK, SOFTBACK, EBOOK;
		
		@Override
        public String toString() {
		    return name()+ordinal();
		}
	}
	
	public enum PublicationFormatDTOAltCase {
		hardBack, softBack, eBook;
	}
	
	public enum PublicationFormatDTOAlternate {
		PUB_HARDBACK, PUB_SOFTBACK, PUB_EBOOK;
	}
	
	public interface Book {
		
		public String getTitle();

		public void setTitle(String title);

		public PublicationFormat getFormat();
		
		public void setFormat(PublicationFormat format);
	}
	
	public class BookImpl implements Book {

		private String title;
		private PublicationFormat format;
		
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public PublicationFormat getFormat() {
			return format;
		}
		
		public void setFormat(PublicationFormat format) {
			this.format = format;
		}
	}


	public class BookDTOWithSameEnum {

		private String title;
		private PublicationFormat format;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
		
		public PublicationFormat getFormat() {
			return format;
		}
		
		public void setFormat(PublicationFormat format) {
			this.format = format;
		}
	}
	
	public class BookDTOWithParallelEnum {

		private String title;
		private PublicationFormatDTO format;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
		
		public PublicationFormatDTO getFormat() {
			return format;
		}
		
		public void setFormat(PublicationFormatDTO format) {
			this.format = format;
		}
	}
	
	public class BookDTOWithAltCaseEnum {

		private String title;
		private PublicationFormatDTOAltCase format;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
		
		public PublicationFormatDTOAltCase getFormat() {
			return format;
		}
		
		public void setFormat(PublicationFormatDTOAltCase format) {
			this.format = format;
		}
	}
	
	public class BookDTOWithAlternateEnum {

		private String title;
		private PublicationFormatDTOAlternate format;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
		
		public PublicationFormatDTOAlternate getFormat() {
			return format;
		}
		
		public void setFormat(PublicationFormatDTOAlternate format) {
			this.format = format;
		}
	}
	
}
