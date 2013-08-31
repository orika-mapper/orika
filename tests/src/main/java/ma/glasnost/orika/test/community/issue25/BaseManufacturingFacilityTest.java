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
package ma.glasnost.orika.test.community.issue25;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ma.glasnost.orika.test.community.issue25.modelA.Address;
import ma.glasnost.orika.test.community.issue25.modelA.ManufacturingFacility;
import ma.glasnost.orika.test.community.issue25.modelB.AddressDTO;
import ma.glasnost.orika.test.community.issue25.modelB.ManufacturingFacilityDTS;

import org.junit.After;
import org.junit.Before;

public abstract class BaseManufacturingFacilityTest {
    
    protected static final String MANUFACTURINGFACILITY_KEY = "ManufacturingFacility";
    protected static final String MANUFACTURINGFACILITYDTS_KEY = "ManufacturingFacilityDTS";
    
    public BaseManufacturingFacilityTest() {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
    public Map<String, Object> getManufacturingFacility(Long aIdNumber, String aDescription){
        Map<String, Object> vReturnMap = new HashMap<String, Object>();
        
        ManufacturingFacility vManufacturingFacility = new ManufacturingFacility();
        vManufacturingFacility.setDescription(aDescription);
        vReturnMap.put(MANUFACTURINGFACILITY_KEY, vManufacturingFacility);
        
        ManufacturingFacilityDTS vBetriebsstaetteDTS = new ManufacturingFacilityDTS();
        vBetriebsstaetteDTS.setDescription(aDescription);
        vBetriebsstaetteDTS.setIdNumber(aIdNumber);
        vReturnMap.put(MANUFACTURINGFACILITYDTS_KEY, vBetriebsstaetteDTS);
        
        return vReturnMap;
    }
    
    
    public void addAddressToManufacturingFacility(Map<String, Object> betriebsstaetteMap, 
									    		  Long idNumber,
									    		  String street,
									    		  Long postalcode,
									    		  String comment,
									    		  Character land){
    	ManufacturingFacility manufacturingFacility = (ManufacturingFacility)betriebsstaetteMap.get(MANUFACTURINGFACILITY_KEY);
    	ManufacturingFacilityDTS manufacturingFacilityDTS = (ManufacturingFacilityDTS)betriebsstaetteMap.get(MANUFACTURINGFACILITYDTS_KEY);
         
        Address vAnschrift = new Address();
        vAnschrift.setIdNumber(idNumber);
        vAnschrift.setStreet(street);
        vAnschrift.setPostalcode(postalcode);

        if(manufacturingFacility.getAddresses()==null)
            manufacturingFacility.setAddresses(new ArrayList<Address>());
        List<Address> anschriften = manufacturingFacility.getAddresses();
        anschriften.add(vAnschrift);


        AddressDTO vAnschriftDTO = new AddressDTO();
        vAnschriftDTO.setComment(comment);
        vAnschriftDTO.setIdNumber(idNumber);
        vAnschriftDTO.setLand(land);
        vAnschriftDTO.setPostalcode(postalcode);
        vAnschriftDTO.setStreet(street);
        
        if(manufacturingFacilityDTS.getAddressL()==null){
            manufacturingFacilityDTS.setAddressL(new ArrayList<AddressDTO>());
        }
        List<AddressDTO> anschriftenDTO = manufacturingFacilityDTS.getAddressL();
        anschriftenDTO.add(vAnschriftDTO);
    }
}

