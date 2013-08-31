package ma.glasnost.orika.test.community;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Issue92TestCase {

        private MapperFactory factory;
        
        @BeforeClass
        public static void setUpBeforeClass() throws Exception {
        }

        @AfterClass
        public static void tearDownAfterClass() throws Exception {
        }

        @Before
        public void setUp() throws Exception {
                factory = new DefaultMapperFactory.Builder().build();
        }

        @After
        public void tearDown() throws Exception {
        }

        @Test
        public void test() {
                factory.classMap(CustomMap.class, CustomClass.class).field("id", "id").register();
                CustomMap customMap = new CustomMap();
                customMap.setId("test");
                CustomClass customClass = factory.getMapperFacade().map(customMap, CustomClass.class);
                assertThat(customClass.getId(),is(equalTo(customMap.getId())));
        }

        static public class CustomMap extends HashMap<String,String> {
                private String id;

                /**
                 * @return the id
                 */
                public final String getId() {
                        return id;
                }

                /**
                 * @param id the id to set
                 */
                public final void setId(String id) {
                        this.id = id;
                }
        }
        
        static public class CustomClass {
                private String id;

                /**
                 * @return the id
                 */
                public final String getId() {
                        return id;
                }

                /**
                 * @param id the id to set
                 */
                public final void setId(String id) {
                        this.id = id;
                }
        }
}
