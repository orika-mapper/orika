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

package ma.glasnost.orika.test.unenhance;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.BookDTO;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
@ContextConfiguration(locations="classpath:HibernateProxyTestCase-context.xml")
@Transactional
@DirtiesContext
public class HibernateProxyTestCase {

	private MapperFacade mapper = MappingUtil.getMapperFactory().getMapperFacade();
	
	@Autowired
	private SessionFactory sessionFactory;
	  
	
	protected Session getSession() {
	    return sessionFactory.getCurrentSession();
	}
	
	@Before
	public void setup() {
		
		Author author = new Author();
		author.setName("Khalil Gebran");
		getSession().save(author);

		Book book = new Book();
		book.setTitle("The Prophet");
		book.setAuthor(author);
		
		
		Book book2 = new Book();
		book2.setTitle("The Prophet 2");
		book.setAuthor(author);
		
		getSession().save(book);
		getSession().save(book2);
		
		/*
		 * Set books also to complete cyclic relationship
		 */
		author.getBooks().add(book);
		author.getBooks().add(book2);
		
		
		getSession().flush();
		getSession().clear();
	}
	
	@Test
	public void testMappingProxyObject() {
		
		Book book = (Book) getSession().load(Book.class, 1L);
		for (int i=0; i < 100; ++i) {
			BookDTO bookDto = mapper.map(book, BookDTO.class);
	
			Assert.assertEquals("The Prophet", bookDto.getTitle());
			Assert.assertEquals("Khalil Gebran", bookDto.getAuthor().getName());
		}
	}
}
