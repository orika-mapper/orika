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
package ma.glasnost.orika.test.community;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.UtilityResolver;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.community.issue25.BaseManufacturingFacilityTest;
import ma.glasnost.orika.test.community.issue25.CustomOrikaMapper;
import ma.glasnost.orika.test.community.issue25.modelA.Address;
import ma.glasnost.orika.test.community.issue25.modelA.ManufacturingFacility;
import ma.glasnost.orika.test.community.issue25.modelB.AddressDTO;
import ma.glasnost.orika.test.community.issue25.modelB.ManufacturingFacilityDTS;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class Issue25TestCase extends BaseManufacturingFacilityTest{

	private MapperFacade mapper = null; 
	
    public Issue25TestCase() {
    }

    @Before
    public void setUp() throws Exception {
    	System.setProperty(OrikaSystemProperties.COMPILER_STRATEGY,EclipseJdtCompilerStrategy.class.getName());
        
        mapper = new CustomOrikaMapper();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testMapManufacuringFacilityToDTS() {
        Map<String, Object> betriebsstaetteMap =
            getManufacturingFacility(1L,"First");
        
        ManufacturingFacility manufacturingFacility = (ManufacturingFacility)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITY_KEY);
        ManufacturingFacilityDTS manufacturingFacilityDTS = (ManufacturingFacilityDTS)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITYDTS_KEY);
        
        addAddressToManufacturingFacility(betriebsstaetteMap, 1L, "Beispielstra�e 2", 815L, "This is a comment.", 'D');

        ManufacturingFacilityDTS betriebsstaetteDTSMapped = mapper.map(manufacturingFacility, ManufacturingFacilityDTS.class);
        ManufacturingFacility betriebsstaetteMappedBack = mapper.map(betriebsstaetteDTSMapped, ManufacturingFacility.class);
        assertTrue(manufacturingFacility.equals(betriebsstaetteMappedBack));
    }
    
    @Test
    public void testMapManufacuringFacilityToDTSNullValues() {
        Map<String, Object> betriebsstaetteMap =
                getManufacturingFacility(2L,"Second");
        
        ManufacturingFacility manufacturingFacility = (ManufacturingFacility)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITY_KEY);
        ManufacturingFacilityDTS manufacturingFacilityDTS = (ManufacturingFacilityDTS)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITYDTS_KEY);
        
        addAddressToManufacturingFacility(betriebsstaetteMap, 1L, null, 815L, "This is a comment.", 'D');

        ManufacturingFacilityDTS betriebsstaetteDTSMapped = mapper.map(manufacturingFacility, ManufacturingFacilityDTS.class);
        ManufacturingFacility betriebsstaetteMappedBack = mapper.map(betriebsstaetteDTSMapped, ManufacturingFacility.class);
        assertTrue(manufacturingFacility.equals(betriebsstaetteMappedBack));
    }
    
    @Test
    public void testMapManufacuringFacilityToDTSMultipleAdr() {
        Map<String, Object> betriebsstaetteMap =
                getManufacturingFacility(3L,"First");
        
        ManufacturingFacility manufacturingFacility = (ManufacturingFacility)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITY_KEY);
        ManufacturingFacilityDTS manufacturingFacilityDTS = (ManufacturingFacilityDTS)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITYDTS_KEY);
        
        addAddressToManufacturingFacility(betriebsstaetteMap, 10L, "StreetA", 815L, "This is a comment. 10", 'D');
        addAddressToManufacturingFacility(betriebsstaetteMap, 11L, "StreetB", 816L, "This is a comment. 11", 'E');
        addAddressToManufacturingFacility(betriebsstaetteMap, 12L, "StreetC", 817L, "This is a comment. 12", 'F');

        ManufacturingFacilityDTS betriebsstaetteDTSMapped = mapper.map(manufacturingFacility, ManufacturingFacilityDTS.class);
        ManufacturingFacility betriebsstaetteMappedBack = mapper.map(betriebsstaetteDTSMapped, ManufacturingFacility.class);
        assertTrue(manufacturingFacility.equals(betriebsstaetteMappedBack));
    }
    
    @Test
    public void testMapManufacuringFacilityToDTSMerge() {
        Map<String, Object> betriebsstaetteMap =
            getManufacturingFacility(4L,"Manufacturing Facility Description.");
        
        ManufacturingFacility manufacturingFacility = (ManufacturingFacility)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITY_KEY);
        ManufacturingFacilityDTS manufacturingFacilityDTS = (ManufacturingFacilityDTS)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITYDTS_KEY);
        
        addAddressToManufacturingFacility(betriebsstaetteMap, 10L, "StreetA", 815L, "This is a comment. 10", 'D');
        addAddressToManufacturingFacility(betriebsstaetteMap, 11L, "StreetB", 816L, "This is a comment. 11", 'E');
        addAddressToManufacturingFacility(betriebsstaetteMap, 12L, "StreetC", 817L, "This is a comment. 12", 'F');

        // Edit some values on "orginal" bean. 
        // This is because we want to see if values are kept which are not mapped.
        
        // With the AddressMergingMapper this variable does not contain any addresses after merge. 
        // The entity mapped from contains 3 addresses.
        // Without the AddressMergingMapper there are contained 3 adresses.
        // But then the check for the land value fails.
        ManufacturingFacilityDTS manufacturingFacilityDTSServer = mapper.map(manufacturingFacility, ManufacturingFacilityDTS.class);
        manufacturingFacilityDTSServer.setIdNumber(4L);
        Character vLand = 'D';
        for(AddressDTO currAnschrift: manufacturingFacilityDTSServer.getAddressL()){
            currAnschrift.setLand(vLand);
        }

        ManufacturingFacility manufacturingFacilityToEdit = mapper.map(manufacturingFacilityDTSServer,ManufacturingFacility.class);
        manufacturingFacilityToEdit.putPrototype(manufacturingFacilityDTSServer);
        
        // Now the bean will be edited to see if the mapped values are correctly merged.
        List<Address> anschriftenGUI = manufacturingFacilityToEdit.getAddresses();
        anschriftenGUI.remove(1);

        manufacturingFacilityToEdit.setDescription("Description after merge.");
        
        Address firstAdr = anschriftenGUI.get(0);
        firstAdr.setStreet("Street new");
        
        // merge
        ManufacturingFacilityDTS prototype = manufacturingFacilityToEdit.returnPrototype();
        mapper.map(manufacturingFacilityToEdit, prototype);
        
        // Do some checks.
        assertTrue("IdNumber was not kept after merge.", 
        			4L == prototype.getIdNumber());
        
        // Check name of street
        assertTrue("Street new".equals(prototype.getAddressL().get(0).getStreet()));
        
        // Amount of addresses
        List<AddressDTO> addressesFromPrototype = prototype.getAddressL();
        assertTrue("An address was removed. In the merged DS this address does still exist.", 
                   addressesFromPrototype.size() == 2);
        
        // land check
        List<AddressDTO> addressesAfterMerge = prototype.getAddressL();
        AddressDTO addressOne = addressesAfterMerge.get(0);
        AddressDTO addressTwo = addressesAfterMerge.get(1);

        assertTrue("Land after merge is wrong.", Character.valueOf('D').equals(addressOne.getLand()));
        assertTrue("Land after merge is wrong.", Character.valueOf('D').equals(addressTwo.getLand()));
    }
    
    
    @Test
    public void testMergingWithCustomMapper() {
        MapperFacade mapper = createMapperFacade();
        
        List<Dto> dtos = new ArrayList<Dto>();
        
        Dto dto = new Dto();
        dto.setId(1L);
        dto.setName("A");
        dtos.add(dto);
        
        Dto dto2 = new Dto();
        dto2.setId(2L);
        dto2.setName("B");
        dtos.add(dto2);
        
        dto = new Dto();
        dto.setId(3L);
        dto.setName("C");
        dtos.add(dto);
        
        DtoHolder source = new DtoHolder();
        source.setEntities(dtos);
        
        Type<?> typeOf_DtoHolder = TypeFactory.valueOf(DtoHolder.class);
        UtilityResolver.getDefaultPropertyResolverStrategy().getProperties(typeOf_DtoHolder);
        
        final EntityHolder entities = mapper.map(source, EntityHolder.class);
        
        Assert.assertNotNull(entities);
        Assert.assertEquals(3, entities.getEntities().size());
        
        final EntityHolder originalEntities = entities;
        source.getEntities().remove(dto2);
        dto2.setName("B-Changed");
        source.getEntities().add(dto2);
        
        mapper.map(source, entities);
        
        Assert.assertEquals(entities.getEntities().size(), originalEntities.getEntities().size());
        
        Iterator<Entity> entitiesIter = entities.getEntities().iterator();
        Iterator<Entity> originalIter = originalEntities.getEntities().iterator();
        while (entitiesIter.hasNext()) {
            Entity e = entitiesIter.next();
            Entity o = originalIter.next();
            Assert.assertSame(e, o);
        }
    }
    
    private static MapperFacade createMapperFacade() {
        DefaultMapperFactory.Builder builder = new DefaultMapperFactory.Builder();
        MapperFactory factory = builder.build();
        
        factory.registerMapper(new MergingMapper());
        return factory.getMapperFacade();
    }
    
    @Test
    public void testMergingWithCustomMapperForChildren() {
        MapperFacade mapper = createMapperFacade();
        
        List<ChildDto> dtos = new ArrayList<ChildDto>();
        
        ChildDto dto = new ChildDto();
        dto.setId(1L);
        dto.setName("A");
        dtos.add(dto);
        
        ChildDto dto2 = new ChildDto();
        dto2.setId(2L);
        dto2.setName("B");
        dtos.add(dto2);
        
        dto = new ChildDto();
        dto.setId(3L);
        dto.setName("C");
        dtos.add(dto);
        
        AnotherDtoHolder source = new AnotherDtoHolder();
        source.setEntities(dtos);
        
        Type<?> typeOf_DtoHolder = TypeFactory.valueOf(DtoHolder.class);
        UtilityResolver.getDefaultPropertyResolverStrategy().getProperties(typeOf_DtoHolder);
        
        final AnotherEntityHolder entities = mapper.map(source, AnotherEntityHolder.class);
        
        Assert.assertNotNull(entities);
        Assert.assertEquals(3, entities.getEntities().size());
        
        final AnotherEntityHolder originalEntities = entities;
        source.getEntities().remove(dto2);
        dto2.setName("B-Changed");
        source.getEntities().add(dto2);
        
        mapper.map(source, entities);
        
        Assert.assertEquals(entities.getEntities().size(), originalEntities.getEntities().size());
        
        Iterator<ChildEntity> entitiesIter = entities.getEntities().iterator();
        Iterator<ChildEntity> originalIter = originalEntities.getEntities().iterator();
        while (entitiesIter.hasNext()) {
            Entity e = entitiesIter.next();
            Entity o = originalIter.next();
            Assert.assertSame(e, o);
        }
    }
    
    public static class MergingMapper extends CustomMapper<Collection<Dto>, Collection<Entity>> {
        
        public void mapAtoB(Collection<Dto> a, Collection<Entity> b, MappingContext context) {
            merge(a, b);
        }
        
        private Collection<Entity> merge(Collection<Dto> srcDtos, Collection<Entity> dstEntities) {
            
            Set<Long> ids = new HashSet<Long>(srcDtos.size());
            for (Dto memberDto : srcDtos) {
                Entity memberEntity = findEntity(dstEntities, memberDto.getId());
                if (memberEntity == null) {
                    dstEntities.add(mapperFacade.map(memberDto, Entity.class));
                } else {
                    mapperFacade.map(memberEntity, memberDto);
                }
                ids.add(memberDto.getId());
            }
            
            for (Iterator<Entity> iterator = dstEntities.iterator(); iterator.hasNext();) {
                Entity dstEntity = iterator.next();
                if (!dstEntity.isNew() && !ids.contains(dstEntity.getId())) {
                    iterator.remove();
                }
            }
            
            return dstEntities;
            
        }
        
        private Entity findEntity(Collection<Entity> dstEntities, Long id) {
            for (Entity dstEntity : dstEntities) {
                if (id.equals(dstEntity.getId())) {
                    return dstEntity;
                }
            }
            return null;
        }
        
    }
    
    public static class EntityHolder {
        
        private Collection<Entity> entityList;
        
        public Collection<Entity> getEntities() {
            return entityList;
        }
        
        public void setEntities(Collection<Entity> entityList) {
            this.entityList = entityList;
        }
        
    }
    
    public static class AnotherEntityHolder {
        private List<ChildEntity> entityList;
        
        public List<ChildEntity> getEntities() {
            return entityList;
        }
        
        public void setEntities(List<ChildEntity> entityList) {
            this.entityList = entityList;
        }
        
    }
    
    public static class DtoHolder {
        
        private Collection<Dto> dtoList;
        
        public Collection<Dto> getEntities() {
            return dtoList;
        }
        
        public void setEntities(Collection<Dto> dtoList) {
            this.dtoList = dtoList;
        }
        
    }
    
    public static class AnotherDtoHolder {
        private List<ChildDto> dtoList;
        
        public List<ChildDto> getEntities() {
            return dtoList;
        }
        
        public void setEntities(List<ChildDto> dtoList) {
            this.dtoList = dtoList;
        }
        
    }
    
    public static class Entity {
        
        private Long id;
        private String name;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public boolean isNew() {
            return id == null;
        }
    }
    
    public static class ChildEntity extends Entity {
        
    }
    
    public static class Dto {
        private Long id;
        private String name;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class ChildDto extends Dto {
        
    }
    
}

