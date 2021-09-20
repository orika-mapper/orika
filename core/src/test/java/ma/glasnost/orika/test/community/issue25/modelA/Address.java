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
package ma.glasnost.orika.test.community.issue25.modelA;


import java.util.Objects;

public class Address {

	private Long idNumber = null;
    private String street = null;
	private Long postalcode = null;

    public Address() {
        super();
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

	public Long getIdNumber() {
		return idNumber;
	}

	public void setIdNumber(Long idNumber) {
		this.idNumber = idNumber;
	}

	@Override
    public boolean equals(Object other){
        if (other == this) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;

        Address anschrift = (Address)other;

        return Objects.equals(street, anschrift.street) &&
                Objects.equals(postalcode, anschrift.postalcode) &&
                Objects.equals(idNumber, anschrift.idNumber);
    }

    @Override
    public int hashCode(){
       return (this.street  == null ?     10 : street.hashCode()) ^
              (this.postalcode == null ? 11 : postalcode.hashCode()^
              (this.idNumber == null ? 11 : idNumber.hashCode()));
    }
}
