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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.test.community.issue20.User;
import ma.glasnost.orika.test.community.issue20.UserDto;
import ma.glasnost.orika.test.community.issue20.UsrGroup;
import ma.glasnost.orika.unenhance.HibernateUnenhanceStrategy;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Dmitriy Khomyakov
 * @author matt.deboer@gmail.com
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:Issue20TestCase-context.xml")
@Transactional
@DirtiesContext
public class Issue20TestCase {

	@Autowired
	private SessionFactory sessionFactory;

	private Serializable user1Id;
	private MapperFacade mapperFacade;

	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	@Before
	public void init() {
		UsrGroup group = new UsrGroup("main");
		getSession().save(group);

		User user1 = new User("User1");
		user1Id = getSession().save(user1);

		User user2 = new User("user2");
		getSession().save(user2);

		group.addUser(user1);
		group.addUser(user2);

		DefaultMapperFactory.Builder builder = new DefaultMapperFactory.Builder();
		builder.unenhanceStrategy(new HibernateUnenhanceStrategy());
		MapperFactory factory = builder.build();
		mapperFacade = factory.getMapperFacade();

		getSession().flush();
		getSession().clear();
	}

	@Test
	public void testMapUser() {
		User user1 = (User) getSession().load(User.class, user1Id);
		assertNotNull(user1);
		for (int i = 0; i < 100; i++) {
			UserDto userDto = mapperFacade.map(user1, UserDto.class);
			assertEquals(userDto.getName(), user1.getName());
		}
	}

}
