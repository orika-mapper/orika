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
package ma.glasnost.orika.test.community.issue21;

import java.io.Serializable;

/**
 * 
 * @author Dmitriy Khomyakov
 * @author matt.deboer@gmail.com
 */
public abstract class BaseDto implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private Long version;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public boolean isNew() {
		return id == null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o == null || (getClass() != o.getClass()))
			return false;

		BaseDto that = (BaseDto) o;

		if (isNew()) {
			return this == that;
		} else {
			return id.equals(that.id);
		}

	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "BaseDto{" + "id=" + id + ", version=" + version + '}';
	}
}
