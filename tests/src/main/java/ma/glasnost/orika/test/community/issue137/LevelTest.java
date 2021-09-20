package ma.glasnost.orika.test.community.issue137;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.TypeFactory;
import org.junit.Test;

import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: tgruenheit
 * Date: 06.12.13
 * Time: 19:36
 * To change this template use File | Settings | File Templates.
 */
public class LevelTest {

    @Test
    public void orikaTest() {

        MapperFactory factory = new DefaultMapperFactory.Builder().build();

        factory.registerObjectFactory(new CustomFactory<>(), TypeFactory.<LevelOne>valueOf( LevelOne.class ) );
        factory.registerObjectFactory(new CustomFactory<>(), TypeFactory.<LevelTwo>valueOf( LevelTwo.class ) );
        factory.registerObjectFactory(new CustomFactory<>(), TypeFactory.<LevelThree>valueOf( LevelThree.class ) );

        LevelOne levelOne = new LevelOne();

        levelOne.setLevelTwos(new HashSet<>() );

        for( int i=0; i < 2; i++ ){
            LevelTwo two = new LevelTwo();
            two.setLevelThreeValue( new LevelThree() );

            levelOne.getLevelTwos().add( two );
        }

        MapperFacade mapperFacade = factory.getMapperFacade();

        LevelOne mapped = mapperFacade.map(levelOne, LevelOne.class);

        System.out.println( mapped );
    }

}
