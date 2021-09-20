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

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

public class JaxbElementTestCase {
    
    /**
     * Fake JAXB element...
     * 
     * @param <T>
     */
    public static class MockJAXBElement<T> {
        private T value;
        
        public T getValue() {
            return value;
        }
        
        public void setValue(T value) {
            this.value = value;
        }
    }
    
    public static class ActorDTO {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class PersonDTO extends ActorDTO {
    }
    
    public static class InstitutionDTO extends ActorDTO {
    }
    
    public static class EventDTO {
        MockJAXBElement<? extends ActorDTO> actor; // actor.getValue() returns
        
        public MockJAXBElement<? extends ActorDTO> getActor() {
            return actor;
        }
        
        public void setActor(MockJAXBElement<? extends ActorDTO> actor) {
            this.actor = actor;
        }
        
    }
    
    public static class Actor {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class ActorHolder {
        public Actor actor;
    }
    
    public static class ActorDTOHolder {
        public JAXBElement<Actor> actor;
    }
    
    /**
     * Encapsulate the configuration in your own reusable mapper
     *
     */
    public static class MyMapper extends ConfigurableMapper {
        
        public static class EventConverter extends CustomConverter<EventDTO, Actor> {
            
            private final MapperFacade mapper;
            private final Type<ActorDTO> typeOf_ActorDTO = new TypeBuilder<ActorDTO>() {
            }.build();
            
            public EventConverter(MapperFacade mapper) {
                this.mapper = mapper;
            }
            
            public Actor convert(EventDTO source, Type<? extends Actor> destinationType, MappingContext context) {
                return mapper.map(source.getActor().getValue(), typeOf_ActorDTO, destinationType, context);
            }
        }
        
        public void configure(MapperFactory factory) {
            factory.getConverterFactory().registerConverter(new EventConverter(this));
        }
        
    }
    
    @Test
    public void testJaxbElement() {
        
        MapperFacade mapper = new MyMapper();
        
        EventDTO event = new EventDTO();
        MockJAXBElement<ActorDTO> element = new MockJAXBElement<>();
        PersonDTO person = new PersonDTO();
        person.setName("Chuck Testa");
        element.setValue(person);
        event.setActor(element);
        
        Actor actor = mapper.map(event, Actor.class);
        
        Assert.assertNotNull(actor);
        Assert.assertEquals(person.getName(), actor.getName());
        
        InstitutionDTO institution = new InstitutionDTO();
        institution.setName("Vermin Supreme");
        element.setValue(institution);
        
        actor = mapper.map(event, Actor.class);
        
        Assert.assertNotNull(actor);
        Assert.assertEquals(institution.getName(), actor.getName());
    }
    
    public static class JaxbTypeFactory implements ObjectFactory<JAXBElement<Actor>> {
        
        public JAXBElement<Actor> create(Object source, MappingContext mappingContext) {
            if (source instanceof Actor) {
                return new JAXBElement<>(new QName("http://example.com/JAXBTest", "Actor"), Actor.class, (Actor) source);
            }
            throw new IllegalArgumentException("source must be an Actor");
        }
    }
    
    @Test
    public void testRealJaxbElement() {
        
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        factory.registerObjectFactory(new JaxbTypeFactory(), new TypeBuilder<JAXBElement<Actor>>() {
        }.build(), TypeFactory.valueOf(Actor.class));
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Actor actor = new Actor();
        actor.setName("Some Body");
        ActorHolder holder = new ActorHolder();
        holder.actor = actor;
        
        ActorDTOHolder dest = mapper.map(holder, ActorDTOHolder.class);
        
        Assert.assertNotNull(dest);
        Assert.assertNotNull(dest.actor);
        Assert.assertEquals(dest.actor.getValue().getName(), actor.getName());
        
    }
    
}
