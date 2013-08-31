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

import ma.glasnost.orika.test.community.issue21.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Dmitriy Khomyakov
 * @author matt.deboer@gmail.com
 */
@Entity
public class Authority extends BaseEntity {
  private String name;
  private String caption;
  private Set<Authority> children;

  public Authority() {
  }

  public Authority(String name) {
    this.name = name;
    children = new HashSet<Authority>();
  }

  @Column(unique = true)
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




  @ManyToMany
  public Set<Authority> getChildren() {
    return children;
  }

  public void addChild(Authority authority){
    children.add(authority);
  }

  public void setChildren(Set<Authority> children) {
    this.children = children;
  }

  @Override
  public String toString() {
    return "Authority{" +
      "name='" + name + '\'' +
      ", caption='" + caption + '\'' +
      "} " + super.toString();
  }
}


