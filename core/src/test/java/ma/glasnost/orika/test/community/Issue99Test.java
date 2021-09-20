package ma.glasnost.orika.test.community;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.DateToStringConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Registering map with multiple converters does not work.
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/issues/99">https://github.com/orika-mapper/orika/issues</a>
 */
public class Issue99Test {
    @Test
    public void testDateMapping() throws Exception {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().mapNulls(false).useAutoMapping(false).build();

        DateToStringConverter dateConverter1 = new DateToStringConverter("yyyy-MM-dd");
        DateToStringConverter dateConverter2 = new DateToStringConverter("yyyy-MM-dd kk:mm:ss");
        mapperFactory.getConverterFactory().registerConverter("yyyy-MM-dd", dateConverter1);
        mapperFactory.getConverterFactory().registerConverter("yyyy-MM-dd kk:mm:ss", dateConverter2);

        mapperFactory.classMap(A.class, B.class)
                .fieldMap("dateA1", "dateB1").converter("yyyy-MM-dd").add()
                .fieldMap("dateA2", "dateB2").converter("yyyy-MM-dd kk:mm:ss").add()
                .register();

        A a = new A();
        a.setDateA1(Calendar.getInstance().getTime());
        a.setDateA2("2015-12-30 10:26:33");
        B b = new B();

        mapperFactory.getMapperFacade().map(a, b);

        assertEquals(b.getDateB1(), new SimpleDateFormat("yyyy-MM-dd").format(a.getDateA1()));
        assertEquals(b.getDateB2(), new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").parse(a.getDateA2()));
    }

    public class A {
        private Date dateA1; // map to string
        private String dateA2; // map from string to date

        public Date getDateA1() {
            return dateA1;
        }
        public void setDateA1(Date dateA1) {
            this.dateA1 = dateA1;
        }
        public String getDateA2() {
            return dateA2;
        }
        public void setDateA2(String dateA2) {
            this.dateA2 = dateA2;
        }
    }

    public class B {
        private String dateB1;
        private Date dateB2;

        public String getDateB1() {
            return dateB1;
        }
        public void setDateB1(String dateB1) {
            this.dateB1 = dateB1;
        }
        public Date getDateB2() {
            return dateB2;
        }
        public void setDateB2(Date dateB2) {
            this.dateB2 = dateB2;
        }
    }
}
