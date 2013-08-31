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
package ma.glasnost.orika.test.converter;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.FromStringConverter;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;


/**
 *
 */
public class FromStringConverterTestCase  {

	@Test
	public void testConvertToEnum() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new FromStringConverter());
		MapperFacade mapper = factory.getMapperFacade();
		
		Fruit fruit = mapper.map("TOMATO", Fruit.class);
		Assert.assertEquals(Fruit.TOMATO, fruit);
		
	}
	
	@Test
	public void testConvertToPrimitive() {
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new FromStringConverter());
		MapperFacade mapper = factory.getMapperFacade();
		
		int age = mapper.map("21", int.class);
		Assert.assertEquals(21, age);
	}
	
	@Test
	public void testConvertToWrapper() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new FromStringConverter());
		MapperFacade mapper = factory.getMapperFacade();
		
		Integer age = mapper.map("21", Integer.class);
		Assert.assertEquals(Integer.valueOf(21), age);
	}
	
	enum Fruit {
		APPLE,
		ORANGE,
		BANANA,
		TOMATO
	}
	
}
