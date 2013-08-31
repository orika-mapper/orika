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
package ma.glasnost.orika.test.community.issue25.modelB;

import java.io.Serializable;

public class  AddressDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8573731692897972845L;
	
	private Long idNumber;
	private String street = null;
	private Long postalcode = null;
	private String comment = null;
	private Character land = null;
	public Long getIdNumber() {
		return idNumber;
	}
	public void setIdNumber(Long idNumber) {
		this.idNumber = idNumber;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public Long getPostalcode() {
		return postalcode;
	}
	public void setPostalcode(Long postalcode) {
		this.postalcode = postalcode;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Character getLand() {
		return land;
	}
	public void setLand(Character land) {
		this.land = land;
	}
	
	
	
	
}
