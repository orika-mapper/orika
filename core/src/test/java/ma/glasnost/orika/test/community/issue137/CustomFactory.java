package ma.glasnost.orika.test.community.issue137;

import java.lang.reflect.Constructor;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.ObjectFactory;

/**
 * Created with IntelliJ IDEA.
 * User: tgruenheit
 * Date: 06.12.13
 * Time: 20:13
 * To change this template use File | Settings | File Templates.
 */
public class CustomFactory<T> implements ObjectFactory<T> {
    
	public T create( Object o, MappingContext mappingContext ){

        //FIXME: While converting second LevelTwo object, resolvedDestinationType is LevelThree
        @SuppressWarnings("unchecked")
		Class<T> rawType = (Class<T>) mappingContext.getResolvedDestinationType().getRawType();

        try {

            Constructor<T> declaredConstructor = rawType.getDeclaredConstructor();
            declaredConstructor.setAccessible( true );

            return declaredConstructor.newInstance();

        } catch (Exception e) {
            throw new RuntimeException( e );
        }

    }
}
