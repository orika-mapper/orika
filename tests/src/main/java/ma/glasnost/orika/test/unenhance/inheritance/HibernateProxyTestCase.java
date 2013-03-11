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

package ma.glasnost.orika.test.unenhance.inheritance;

import java.io.Serializable;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.unenhance.HibernateUnenhanceStrategy;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:HibernateProxyTestCase-context.xml")
@Transactional
@DirtiesContext
public class HibernateProxyTestCase {
	@Autowired
	private SessionFactory sessionFactory;

	private Serializable sub2Id;

	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	@Before
	public void setup() {
		Sub1Entity sub1 = new Sub1Entity();
		sub1.setMyProperty("my property on sub1");
		sub1.setSub1Property(1);
		getSession().save(sub1);

		Sub2Entity sub2 = new Sub2Entity();
		sub2.setMyProperty("my property on sub2");
		sub2.setSub2Property(2);
		sub2.setReferences(sub1);
		sub2Id = getSession().save(sub2);

		getSession().flush();
		getSession().clear();
	}

	private MapperFacade buildMapper() {
		MapperFactory mf = new DefaultMapperFactory.Builder()
				.unenhanceStrategy(new HibernateUnenhanceStrategy()).build();
		mf.registerClassMap(mf
				.classMap(AbstractEntity.class, AbstractDTO.class).byDefault()
				.toClassMap());
		mf.registerClassMap(mf.classMap(Sub1Entity.class, Sub1EntityDTO.class)
				.use(AbstractEntity.class, AbstractDTO.class).byDefault()
				.toClassMap());
		mf.registerClassMap(mf.classMap(Sub2Entity.class, Sub2EntityDTO.class)
				.use(AbstractEntity.class, AbstractDTO.class).byDefault()
				.toClassMap());
		return mf.getMapperFacade();
	}

	@Test
	public void testMappingNonProxyObject() {
		MapperFacade mapper = buildMapper();

		Sub2Entity sub2 = (Sub2Entity) getSession().get(Sub2Entity.class,
				sub2Id);
		sub2.setReferences((MyEntity) ((HibernateProxy) sub2.getReferences())
				.getHibernateLazyInitializer().getImplementation());
		Sub2EntityDTO sub2Dto = mapper.map(sub2, Sub2EntityDTO.class);

		Assert.assertEquals(sub2.getMyProperty(), sub2Dto.getMyProperty());
		Assert.assertEquals(sub2.getSub2Property(), sub2Dto.getSub2Property());
		Assert.assertNotNull(sub2Dto.getReferences());
		Assert.assertEquals(sub2.getReferences().getMyProperty(), sub2Dto
				.getReferences().getMyProperty());
		Assert.assertEquals(Sub1EntityDTO.class, sub2Dto.getReferences()
				.getClass());
		Assert.assertEquals(1,
				((Sub1EntityDTO) sub2Dto.getReferences()).getSub1Property());
	}

	@Test
	public void testMappingProxyObject() {
		MapperFacade mapper = buildMapper();

		Sub2Entity sub2 = (Sub2Entity) getSession().get(Sub2Entity.class,
				sub2Id);
		Sub2EntityDTO sub2Dto = mapper.map(sub2, Sub2EntityDTO.class);

		Assert.assertEquals(sub2.getMyProperty(), sub2Dto.getMyProperty());
		Assert.assertEquals(sub2.getSub2Property(), sub2Dto.getSub2Property());
		Assert.assertNotNull(sub2Dto.getReferences());
		Assert.assertEquals(sub2.getReferences().getMyProperty(), sub2Dto
				.getReferences().getMyProperty());
		Assert.assertEquals(Sub1EntityDTO.class, sub2Dto.getReferences()
				.getClass());
		Assert.assertEquals(1,
				((Sub1EntityDTO) sub2Dto.getReferences()).getSub1Property());
	}
}
