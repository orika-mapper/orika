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



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author Dmitriy Khomyakov
 * @author matt.deboer@gmail.com
 */
@Entity
public class User extends BaseEntity {
  private UsrGroup group;
  private String name;
  private String password;

  public User(String name) {
    this(name, name);
  }

  public User(String name, String password) {
    this.name = name;
    this.password = password;
  }

  public User() {
  }


  public void setName(String name) {
    this.name = name;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  public UsrGroup getGroup() {
    return group;
  }

  public void setGroup(UsrGroup group) {
    this.group = group;
  }

  @Column(unique = true)
  public String getName() {
    return name;
  }



  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return "User{" +
      "name='" + name + '\'' +
      "} " + super.toString();
  }
}
