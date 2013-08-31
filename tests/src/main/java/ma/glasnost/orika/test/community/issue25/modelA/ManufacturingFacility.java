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
package ma.glasnost.orika.test.community.issue25.modelA;

import java.util.List;

import ma.glasnost.orika.test.community.issue25.modelB.ManufacturingFacilityDTS;


public class ManufacturingFacility {

    // If an entity is read from database this property is set for merging.
    private ManufacturingFacilityDTS prototype;
    private List<Address> addresses;
    private String description;
    
    // START read instance
    public ManufacturingFacilityDTS returnPrototype(){
        return this.prototype;
    }
    
    public void putPrototype(ManufacturingFacilityDTS prototype){
        this.prototype = prototype;
    }
    // END read instance

    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
    
    public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	@Override
    public boolean equals(Object other){
        
        if (other == this) return true;
           if (other == null) return false;
           if (getClass() != other.getClass()) return false;
           ManufacturingFacility betriebsstaette = (ManufacturingFacility)other;
           
           boolean areAttributesEqual = (description == betriebsstaette.description || 
               (description != null && description.equals(betriebsstaette.description)));
           areAttributesEqual &= (addresses == betriebsstaette.addresses) || 
               (addresses != null && addresses.equals(betriebsstaette.addresses));
           return areAttributesEqual;
    }

	@Override    
    public int hashCode() {       
       return (this.description  == null ? 17 : description.hashCode());
    }

}
