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

package ma.glasnost.orika.test.object;

import java.util.HashSet;
import java.util.Set;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class InverseMappingTestCase {
    
    @Test
    public void testInverseOneToOneMapping() {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        
        ClassMapBuilder<PersonDTO, Person> classMapBuilder = ClassMapBuilder.map(PersonDTO.class, Person.class);
        classMapBuilder.fieldMap("address").bInverse("person").add();
        mapperFactory.registerClassMap(classMapBuilder.byDefault().toClassMap());
        
        MapperFacade mapper = mapperFactory.getMapperFacade();
        
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setLine1("5 rue Blida");
        addressDTO.setLine2("20100 Casablanca");
        
        PersonDTO personDTO = new PersonDTO();
        personDTO.setFirstName("Khalil");
        personDTO.setLastName("Gibran");
        personDTO.setAddress(addressDTO);
        
        Person person = mapper.map(personDTO, Person.class);
        
        Assert.assertEquals(personDTO.getFirstName(), person.getFirstName());
        Assert.assertEquals(personDTO.getAddress().getLine1(), person.getAddress().getLine1());
        
        Assert.assertTrue(person == person.getAddress().getPerson());
    }
    
    @Test
    public void testInverseOneToManyMapping() {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        
        ClassMapBuilder<PublisherDTO, Publisher> classMapBuilder = ClassMapBuilder.map(PublisherDTO.class, Publisher.class);
        classMapBuilder.fieldMap("books").bInverse("publisher").add();
        mapperFactory.registerClassMap(classMapBuilder.byDefault().toClassMap());
        
        mapperFactory.build();
        
        MapperFacade mapper = mapperFactory.getMapperFacade();
        
        BookDTO parisNoirDTO = new BookDTO();
        parisNoirDTO.setTitle("Paris Noir");
        
        BookDTO chiensFousDTO = new BookDTO();
        chiensFousDTO.setTitle("Chiens Fous");
        
        PublisherDTO publisherDTO = new PublisherDTO();
        publisherDTO.setName("Asphalte Editions");
        publisherDTO.getBooks().add(parisNoirDTO);
        publisherDTO.getBooks().add(chiensFousDTO);
        
        Publisher publisher = mapper.map(publisherDTO, Publisher.class);
        
        Assert.assertTrue(publisher == publisher.getBooks().iterator().next().getPublisher());
    }
    
    @Test
    public void testInverseManyToOneMapping() {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        
        ClassMapBuilder<BookDTO, Book> classMapBuilder = ClassMapBuilder.map(BookDTO.class, Book.class);
        classMapBuilder.fieldMap("author").bInverse("books").add();
        mapperFactory.registerClassMap(classMapBuilder.byDefault().toClassMap());
        
        mapperFactory.build();
        
        MapperFacade mapper = mapperFactory.getMapperFacade();
        
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setFirstName("Khalil");
        authorDTO.setLastName("Gibran");
        
        BookDTO bookDTO = new BookDTO();
        bookDTO.setTitle("The Prophet");
        bookDTO.setAuthor(authorDTO);
        
        Book book = mapper.map(bookDTO, Book.class);
        
        Assert.assertTrue(book.getAuthor().getBooks().contains(book));
    }
    
    @Test
    public void testInverseManyToManyMapping() {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        
        ClassMapBuilder<ReaderDTO, Reader> classMapBuilder = ClassMapBuilder.map(ReaderDTO.class, Reader.class);
        classMapBuilder.fieldMap("books").bInverse("readers").add();
        mapperFactory.registerClassMap(classMapBuilder.byDefault().toClassMap());
        
        mapperFactory.build();
        
        MapperFacade mapper = mapperFactory.getMapperFacade();
        
        Set<BookDTO> bookDTOs = new HashSet<BookDTO>();
        BookDTO bookDTO = new BookDTO();
        bookDTO.setTitle("The Prophet");
        bookDTOs.add(bookDTO);
        bookDTO = new BookDTO();
        bookDTO.setTitle("More Effective Java");
        bookDTOs.add(bookDTO);
        
        ReaderDTO readerDTO = new ReaderDTO();
        readerDTO.setFirstName("Jennifer");
        readerDTO.setLastName("Lopez");
        readerDTO.setBooks(bookDTOs);
        
        Reader reader = mapper.map(readerDTO, Reader.class);
        
        for (Book book : reader.getBooks()) {
            Assert.assertTrue(book.getReaders().contains(reader));
        }
    }
    
    public static class Person {
        
        private String firstName;
        
        private String lastName;
        
        private Address address;
        
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public Address getAddress() {
            return address;
        }
        
        public void setAddress(Address address) {
            this.address = address;
        }
        
    }
    
    public static class Address {
        
        private String line1;
        
        private String line2;
        
        private Person person;
        
        public String getLine1() {
            return line1;
        }
        
        public void setLine1(String line1) {
            this.line1 = line1;
        }
        
        public String getLine2() {
            return line2;
        }
        
        public void setLine2(String line2) {
            this.line2 = line2;
        }
        
        public Person getPerson() {
            return person;
        }
        
        public void setPerson(Person person) {
            this.person = person;
        }
        
    }
    
    public static class Book {
        
        private String title;
        
        private Author author;
        
        private Publisher publisher;
        
        private Set<Reader> readers;
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public Author getAuthor() {
            return author;
        }
        
        public void setAuthor(Author author) {
            this.author = author;
        }
        
        public Publisher getPublisher() {
            return publisher;
        }
        
        public void setPublisher(Publisher publisher) {
            this.publisher = publisher;
        }
        
        public Set<Reader> getReaders() {
            return readers;
        }
        
        public void setReaders(Set<Reader> readers) {
            this.readers = readers;
        }
        
    }
    
    public static class Author extends Person {
        
        private Set<Book> books;
        
        public Set<Book> getBooks() {
            return books;
        }
        
        public void setBooks(Set<Book> books) {
            this.books = books;
        }
        
    }
    
    public static class Reader extends Person {
        
        private Set<Book> books;
        
        public Set<Book> getBooks() {
            return books;
        }
        
        public void setBooks(Set<Book> books) {
            this.books = books;
        }
        
    }
    
    public static class Publisher {
        
        private String name;
        
        private Set<Book> books;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Set<Book> getBooks() {
            return books;
        }
        
        public void setBooks(Set<Book> books) {
            this.books = books;
        }
        
    }
    
    public static class PersonDTO {
        
        private String firstName;
        
        private String lastName;
        
        private AddressDTO address;
        
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public AddressDTO getAddress() {
            return address;
        }
        
        public void setAddress(AddressDTO address) {
            this.address = address;
        }
        
    }
    
    public static class AddressDTO {
        
        private String line1;
        
        private String line2;
        
        public String getLine1() {
            return line1;
        }
        
        public void setLine1(String line1) {
            this.line1 = line1;
        }
        
        public String getLine2() {
            return line2;
        }
        
        public void setLine2(String line2) {
            this.line2 = line2;
        }
        
    }
    
    public static class BookDTO {
        
        private String title;
        
        private AuthorDTO author;
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public AuthorDTO getAuthor() {
            return author;
        }
        
        public void setAuthor(AuthorDTO author) {
            this.author = author;
        }
        
    }
    
    public static class AuthorDTO extends PersonDTO {
        
    }
    
    public static class ReaderDTO extends PersonDTO {
        
        private Set<BookDTO> books;
        
        public Set<BookDTO> getBooks() {
            return books;
        }
        
        public void setBooks(Set<BookDTO> books) {
            this.books = books;
        }
        
    }
    
    public static class PublisherDTO {
        
        private String name;
        
        private Set<BookDTO> books = new HashSet<BookDTO>();
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Set<BookDTO> getBooks() {
            return books;
        }
        
        public void setBooks(Set<BookDTO> books) {
            this.books = books;
        }
        
    }
    
}
