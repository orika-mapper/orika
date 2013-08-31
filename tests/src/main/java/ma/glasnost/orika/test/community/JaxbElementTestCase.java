package ma.glasnost.orika.test.community;

import junit.framework.Assert;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.community.JaxbElementTestCase.MyMapper.EventConverter;

import org.junit.Test;

public class JaxbElementTestCase {

	/**
	 * Fake JAXB element...
	 * 
	 * @param <T>
	 */
	public static class JAXBElement<T> {
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
		JAXBElement<? extends ActorDTO> actor; // actor.getValue() returns

		public JAXBElement<? extends ActorDTO> getActor() {
			return actor;
		}

		public void setActor(JAXBElement<? extends ActorDTO> actor) {
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

	
	/**
	 * Encapsulate the configuration in your own reusable mapper
	 *
	 */
	public static class MyMapper extends ConfigurableMapper {
		
		public static class EventConverter extends CustomConverter<EventDTO, Actor> {

			private MapperFacade mapper;
			private Type<ActorDTO> typeOf_ActorDTO = new TypeBuilder<ActorDTO>() {}.build();

			public EventConverter(MapperFacade mapper) {
				this.mapper = mapper;
			}

			public Actor convert(EventDTO source, Type<? extends Actor> destinationType) {
				return mapper.map(source.getActor().getValue(),
						typeOf_ActorDTO, destinationType);
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
		JAXBElement<ActorDTO> element = new JAXBElement<ActorDTO>();
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
	
	

}
