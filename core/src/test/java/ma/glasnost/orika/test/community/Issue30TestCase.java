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
package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * MappingException: cannot determine runtime type of destination collection.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/30">https://code.google.com/archive/p/orika/</a>
 *
 */
public class Issue30TestCase {

	public static abstract class ComputerPart {
		private Long id;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}
	}

	public static class HardDrive extends ComputerPart {
		private String capacity;

		public String getCapacity() {
			return capacity;
		}

		public void setCapacity(String capacity) {
			this.capacity = capacity;
		}
	}

	public static class VideoCard extends ComputerPart {
		public String chip;

		public String getChip() {
			return chip;
		}

		public void setChip(String chip) {
			this.chip = chip;
		}
	}

	public static class Inventory<T extends ComputerPart> {
		private List<T> items;

		public List<T> getItems() {
			return items;
		}

		public void setItems(List<T> items) {
			this.items = items;
		}
	}

	public static abstract class ComputerPartDTO {
		private Long id;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}
	}

	public static class HardDriveDTO extends ComputerPartDTO {
		private String capacity;

		public String getCapacity() {
			return capacity;
		}

		public void setCapacity(String capacity) {
			this.capacity = capacity;
		}
	}

	public static class VideoCardDTO extends ComputerPartDTO {
		public String chip;

		public String getChip() {
			return chip;
		}

		public void setChip(String chip) {
			this.chip = chip;
		}
	}

	public static class InventoryDTO<T extends ComputerPartDTO> {
		private List<T> items;

		public List<T> getItems() {
			return items;
		}

		public void setItems(List<T> items) {
			this.items = items;
		}
	}

	@Test
	public void testOrder() {
		MapperFactory factory = new DefaultMapperFactory.Builder().build();

		Type<Inventory<HardDrive>> ih1 = new TypeBuilder<Inventory<HardDrive>>() {
		}.build();
		Type<InventoryDTO<HardDriveDTO>> ih2 = new TypeBuilder<InventoryDTO<HardDriveDTO>>() {
		}.build();

		Type<Inventory<VideoCard>> iv1 = new TypeBuilder<Inventory<VideoCard>>() {
		}.build();
		Type<InventoryDTO<VideoCardDTO>> iv2 = new TypeBuilder<InventoryDTO<VideoCardDTO>>() {
		}.build();

		factory.registerClassMap(factory.classMap(HardDrive.class,
				HardDriveDTO.class).toClassMap());
		factory.registerClassMap(factory.classMap(VideoCard.class,
				VideoCardDTO.class).toClassMap());
		factory.registerClassMap(factory.classMap(ih1, ih2).byDefault()
				.toClassMap());
		factory.registerClassMap(factory.classMap(iv1, iv2).byDefault()
				.toClassMap());

		List<HardDrive> hardDrives = new ArrayList<>();
		hardDrives.add(new HardDrive());
		hardDrives.add(new HardDrive());

		Inventory<HardDrive> inventory = new Inventory<>();
		inventory.setItems(hardDrives);

		InventoryDTO<?> inventoryDTO = factory.getMapperFacade().map(inventory,
				InventoryDTO.class);
		
		
        assertThat(inventory.getItems(), hasSize(inventoryDTO.getItems().size()));
		
		for (Object o: inventoryDTO.getItems()) {
            assertThat(o, is(instanceOf(ComputerPartDTO.class)));
		}
	}
}
