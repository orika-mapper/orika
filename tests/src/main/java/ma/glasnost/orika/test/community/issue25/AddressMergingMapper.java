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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.test.community.issue25.modelA.Address;
import ma.glasnost.orika.test.community.issue25.modelB.AddressDTO;

public class AddressMergingMapper extends
		CustomMapper<List<Address>, List<AddressDTO>> {

	public void mapAtoB(List<Address> a, List<AddressDTO> b,
			MappingContext context) {
	    merge(a, b, context);
	}

	public void mapBtoA(List<AddressDTO> b, List<Address> a,
			MappingContext context) {
	    mapperFacade.mapAsCollection(b, a, Address.class, context);
	}

	private List<AddressDTO> merge(
			List<Address> srcAddress,
			List<AddressDTO> dstAddressesDTO,
			MappingContext context) {
		Set<Long> savedIdNumbers = new HashSet<Long>(
				srcAddress.size());
		for (Address currentAddress : srcAddress) {
			AddressDTO foundAddressDTO = findEntity(dstAddressesDTO,
					currentAddress.getIdNumber());
			if (foundAddressDTO == null) { 
				dstAddressesDTO.add(mapperFacade.map(currentAddress,
						AddressDTO.class, context));
			} else {
				mapperFacade.map(currentAddress, foundAddressDTO, context);
			}
			savedIdNumbers.add(currentAddress.getIdNumber());
		}
		for (Iterator<AddressDTO> iterator = dstAddressesDTO.iterator(); iterator
				.hasNext();) {
			AddressDTO vCurrentAnschriftDTO = iterator.next();
			if (!savedIdNumbers.contains(vCurrentAnschriftDTO
					.getIdNumber())) {
				iterator.remove();
			}
		}
		return dstAddressesDTO;
	}

	private AddressDTO findEntity(List<AddressDTO> dstAddressDTO,
			Long aIdnumber) {
		for (AddressDTO vCurrentDTO : dstAddressDTO) {
			if (aIdnumber.equals(vCurrentDTO.getIdNumber())) {
				return vCurrentDTO;
			}
		}
		return null;
	}
}
