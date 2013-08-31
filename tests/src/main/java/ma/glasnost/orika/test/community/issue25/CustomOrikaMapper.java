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
import java.util.List;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.test.community.issue25.modelA.Address;
import ma.glasnost.orika.test.community.issue25.modelA.ManufacturingFacility;
import ma.glasnost.orika.test.community.issue25.modelB.AddressDTO;
import ma.glasnost.orika.test.community.issue25.modelB.ManufacturingFacilityDTS;

public class CustomOrikaMapper extends ConfigurableMapper {
    
    @Override
    public void configure(MapperFactory mapperFactory) {

        mapperFactory.registerMapper(new AddressMergingMapper());
        mapperFactory.registerConcreteType(new TypeBuilder<List<Address>>(){}.build(), 
                                           new TypeBuilder<ArrayList<Address>>(){}.build()); 
        mapperFactory.registerConcreteType(new TypeBuilder<List<AddressDTO>>(){}.build(), 
                                           new TypeBuilder<ArrayList<AddressDTO>>(){}.build()); 

        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        //Add converters...
        
        mapperFactory.registerClassMap(mapperFactory.classMap(ManufacturingFacility.class, ManufacturingFacilityDTS.class )
                                .fieldMap("description" , "manufacturingfacility.description").add()
                                .fieldMap("addresses","addressL").add()
                                .toClassMap()
                                );
        
        mapperFactory.registerClassMap(mapperFactory.classMap(Address.class, AddressDTO.class )
                                .fieldMap("idNumber",   "idNumber").add()
                                .fieldMap("street",     "street").add()
                                .fieldMap("postalcode", "postalcode").add()
                                .toClassMap()
                                );
    }
}
