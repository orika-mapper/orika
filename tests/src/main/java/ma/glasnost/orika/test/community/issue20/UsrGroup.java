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
package ma.glasnost.orika.test.community.issue20;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

/**
 * @author Dmitriy Khomyakov
 * @author matt.deboer@gmail.com
 */
@Entity
public class UsrGroup extends BaseEntity {
  private String name;
  private String caption;
  private Set<User> users;

  public UsrGroup() {
  }


  public UsrGroup(String name) {
    this.name = name;
    users = new HashSet<User>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  @OneToMany
  @JoinColumn
  public Set<User> getUsers() {
    return users;
  }

  public void setUsers(Set<User> users) {
    this.users = users;
  }

  public void addUser(User user){
    getUsers().add(user);
    user.setGroup(this);
  }

  @Override
  public String toString() {
    return "UserGroup{" +
      "name='" + name + '\'' +
      ", caption='" + caption + '\'' +
      "} " + super.toString();
  }
}
