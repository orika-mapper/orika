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

import java.util.Set;

/**
 * 
 * @author Dmitriy Khomyakov
 * @author matt.deboer@gmail.com
 */
public class UserGroupDto extends BaseDto {
	private String name;
	private Set<UserDto> users;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<UserDto> getUsers() {
		return users;
	}

	public void setUsers(Set<UserDto> users) {
		this.users = users;
	}

	public void addUser(UserDto user) {
		getUsers().add(user);
		user.setGroup(this);
	}

	public void removeUser(UserDto user) {
		getUsers().remove(user);
		user.setGroup(this);
	}

	@Override
	public String toString() {
		return "UserGroupDto{" + "name='" + name + '\'' + ", users=" + users
				+ "} " + super.toString();
	}
}
