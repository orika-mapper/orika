package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Assert;
import org.junit.Test;

/**
 * Verify that we use Enum.name istead of Enum.toString
 * 
 * @author Dmitriy Khomyakov
 */
public class Issue49TestCase {
    
    @Test
    public void testMapOfEnum() {
        DefaultMapperFactory.Builder builder = new DefaultMapperFactory.Builder();
        MapperFactory factory = builder.build();
        MapperFacade mapperFacade = factory.getMapperFacade();
        Entity entity = new Entity();
        entity.setState(State.B);
        final Dto dto = mapperFacade.map(entity, Dto.class);
        Assert.assertEquals(dto.getState(), entity.getState());
    }
    
    public static enum State {
        A {
            @Override
            public String toString() {
                return "first";
            }
        },
        B {
            @Override
            public String toString() {
                return "second";
            }
        }
    }
    
    public static class Entity {
        private State state;
        
        public State getState() {
            return state;
        }
        
        public void setState(State state) {
            this.state = state;
        }
    }
    
    public static class Dto {
        private State state;
        
        public State getState() {
            return state;
        }
        
        public void setState(State state) {
            this.state = state;
        }
    }
    
}