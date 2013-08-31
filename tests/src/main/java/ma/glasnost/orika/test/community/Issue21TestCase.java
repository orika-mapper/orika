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

import static org.junit.Assert.*;

import java.io.Serializable;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.community.issue21.Authority;
import ma.glasnost.orika.test.community.issue21.AuthorityDto;
import ma.glasnost.orika.test.community.issue21.User;
import ma.glasnost.orika.test.community.issue21.UserDto;
import ma.glasnost.orika.test.community.issue21.UserGroup;
import ma.glasnost.orika.test.community.issue21.UserGroupDto;
import ma.glasnost.orika.unenhance.HibernateUnenhanceStrategy;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * </p>
 * 
 * @author Dmitriy Khomyakov
 * @author matt.deboer@gmail.com
 * 
 * TODO: this particular test case is imported AS-IS, but should probably
 * be trimmed down to the specific issue of concern here which is:
 * 
 * Reuse of the same MappingStrategy over map(source) and map(source, dest)
 * is not valid, since the mapping strategy implementations are distinguished
 * by whether or not they instantiate or map in place (among other things).
 * This means that the MappingStrategyKey needs to reflect whether or not
 * the destination object is provided, since a different strategy must be
 * used...
 * 
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:Issue21TestCase-context.xml")
@Transactional
@DirtiesContext
public class Issue21TestCase {

	@Autowired
	private SessionFactory sessionFactory;
	private Serializable user1Id;
	private Serializable user2Id;
	private MapperFacade mapperFacade;
	private Serializable groupId;
	private Serializable adminAuthotityId;

	private final Logger log = LoggerFactory.getLogger(Issue21TestCase.class);

	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	@Before
	public void init() {
		UserGroup group = new UserGroup("main");
		groupId = getSession().save(group);

		User user1 = new User("User1");
		user1Id = getSession().save(user1);

		User user2 = new User("user2");
		user2Id = getSession().save(user2);

		group.addUser(user1);
		group.addUser(user2);

		Authority adminAuthority = new Authority("admin");
		adminAuthotityId = getSession().save(adminAuthority);

		DefaultMapperFactory.Builder builder = new DefaultMapperFactory.Builder();
		builder.unenhanceStrategy(new HibernateUnenhanceStrategy());
		builder.compilerStrategy(new EclipseJdtCompilerStrategy());
		MapperFactory factory = builder.build();
		mapperFacade = factory.getMapperFacade();

		getSession().flush();
		getSession().clear();
	}

	@Test
	public void testMapUser() {
		User user1 = (User) getSession().load(User.class, user1Id);
		User user2 = (User) getSession().load(User.class, user2Id);
		assertNotNull(user1);
		for (int i = 0; i < 100; i++) {
			UserDto userDto1 = mapperFacade.map(user1, UserDto.class);
			assertEquals(userDto1.getName(), user1.getName());

			UserDto userDto2 = mapperFacade.map(user2, UserDto.class);
			assertEquals(userDto2.getName(), user2.getName());

			assertTrue(user1.getGroup().getUsers().contains(user2));
		}
	}

	@Test
	public void testChangeAuthoritiesOfUser() {
		addAuthority();
		getSession().flush();
		getSession().clear();
		removeAuthority();
	}

	private void addAuthority() {
		User user1 = (User) getSession().load(User.class, user1Id);
		UserDto userDto1 = mapperFacade.map(user1, UserDto.class);

		log.debug("\n\n old userTO = " + userDto1);

		Authority authority = (Authority) getSession().load(Authority.class,
				adminAuthotityId);
		AuthorityDto authorityDto = mapperFacade.map(authority,
				AuthorityDto.class);
		assertFalse(userDto1.getAuthorities().contains(authorityDto));

		userDto1.getAuthorities().add(authorityDto);
		Assert.assertNotNull(user1);
		mapperFacade.map(userDto1, user1, TypeFactory.valueOf(UserDto.class),
				TypeFactory.valueOf(User.class));

		assertTrue(user1.getAuthorities().contains(authority));
	}

	private void removeAuthority() {
		User user1 = (User) getSession().load(User.class, user1Id);
		UserDto userDto1 = mapperFacade.map(user1, UserDto.class);

		log.debug("\n\n old userTO = " + userDto1);

		Authority authority = (Authority) getSession().load(Authority.class,
				adminAuthotityId);
		AuthorityDto authorityDto = mapperFacade.map(authority,
				AuthorityDto.class);
		assertTrue(userDto1.getAuthorities().contains(authorityDto));

		userDto1.getAuthorities().remove(authorityDto);
		Assert.assertNotNull(user1);
		mapperFacade.map(userDto1, user1, TypeFactory.valueOf(UserDto.class),
				TypeFactory.valueOf(User.class));

		assertFalse(user1.getAuthorities().contains(authority));
	}

	@Test
	public void testChangeGroup() {
		removeGroup();

		getSession().flush();
		getSession().clear();

		revertGroup();

		getSession().flush();
		getSession().clear();

	}

	private void removeGroup() {
		UserGroup group = (UserGroup) getSession().load(UserGroup.class,
				groupId);

		User user1 = (User) getSession().load(User.class, user1Id);

		assertTrue(group.getUsers().contains(user1));

		UserDto userDto1 = mapperFacade.map(user1, UserDto.class);

		UserGroupDto groupDto = mapperFacade.map(group, UserGroupDto.class);
		groupDto.removeUser(userDto1);

		mapperFacade.map(groupDto, group,
				TypeFactory.valueOf(UserGroupDto.class),
				TypeFactory.valueOf(UserGroup.class));

		assertFalse(group.getUsers().contains(user1));
	}

	private void revertGroup() {
		UserGroup group = (UserGroup) getSession().load(UserGroup.class,
				groupId);

		User user1 = (User) getSession().load(User.class, user1Id);

		assertFalse(group.getUsers().contains(user1));

		UserDto userDto1 = mapperFacade.map(user1, UserDto.class);

		UserGroupDto groupDto = mapperFacade.map(group, UserGroupDto.class);
		groupDto.addUser(userDto1);

		mapperFacade.map(groupDto, group,
				TypeFactory.valueOf(UserGroupDto.class),
				TypeFactory.valueOf(UserGroup.class));

		assertTrue(group.getUsers().contains(user1));
	}

}
