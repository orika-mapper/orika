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

import java.util.Date;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.converter.builtin.ToStringConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;


public class ToStringConverterTestCase {

	@Test(expected=MappingException.class)
	public void testToString_withoutConverter() {
		MapperFactory factory = new DefaultMapperFactory.Builder().useBuiltinConverters(false).build();
		MapperFacade mapper = factory.getMapperFacade();
		
		Date now = new Date();
		String string = mapper.map(now, String.class);
		Assert.assertFalse(now.toString().equals(string));
	}
	
	@Test
	public void testToString() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new ToStringConverter());
		MapperFacade mapper = factory.getMapperFacade();
		
		Date now = new Date();
		String string = mapper.map(now, String.class);
		Assert.assertEquals(now.toString(), string);
	}
}
