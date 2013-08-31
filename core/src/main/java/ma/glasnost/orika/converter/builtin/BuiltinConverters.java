package ma.glasnost.orika.converter.builtin;

import java.io.File;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import ma.glasnost.orika.converter.ConverterFactory;

/**
 * BuiltinConverters is a utility class used to register common built-in converters.
 * 
 * @author mattdeboer
 *
 */
public abstract class BuiltinConverters {
    
    /**
     * Registers a common set of built-in converters which can handle many common conversion situations.<br>
     * Specifically, this includes:
     * <ul>
     * <li>ConstructorConverter: converts from the source type to destination type if there is
     * a constructor available on the destination type which takes the source as a single argument.
     * <li>FromStringConverter: able to convert from a String to enum, primitive, or primitive wrapper.
     * <li>ToStringconverter: able to convert any type to String
     * <li>DateAndTimeConverters: convert between common date/time representations<ul>
     * <li>java.util.Calendar
     * <li>java.util.Date
     * <li>long / java.lang.Long
     * <li>javax.xml.datatype.XMLGregorianCalendar
     * <li>java.sql.Date
     * <li>java.sql.Time
     * <li>java.sql.Timestamp
     * </ul>
     * <li>PassThroughConverter registered for the following (additional) immutable types:<ul>
     * <li>java.net.URL
     * <li>java.net.URI
     * <li>java.util.UUID
     * <li>java.math.BigInteger
     * <li>java.util.Locale
     * <li>java.io.File
     * <li>java.net.Inet4Address
     * <li>java.net.Inet6Address
     * <li>java.net.InetSocketAddress
     * </ul>
     * <li>CloneableConverter registered for the following cloneable types:<ul>
     * <li>java.util.Date
     * <li>java.util.Calendar
     * <li>javax.xml.datatype.XMLGregorianCalendar
     * </ul>
     * </ul>
     * 
     * @param converterFactory the converter factory on which to register the converters
     */
    public static void register(ConverterFactory converterFactory) {
        
        converterFactory.registerConverter(new CopyByReferenceConverter());
//        converterFactory.registerConverter(new WrapperToPrimitiveConverter());
//        converterFactory.registerConverter(new StringToCharacterConverter());
        /*
         * Register converter to instantiate by using a constructor on
         * the destination which takes the source as argument
         */
        converterFactory.registerConverter(new ConstructorConverter());
        
        converterFactory.registerConverter(new EnumConverter());
        
        /*
         * Register to/from string converters
         */
        converterFactory.registerConverter(new FromStringConverter());
        converterFactory.registerConverter(new ToStringConverter());
        
        /*
         * Register common date/time converters
         */
        converterFactory.registerConverter(new DateAndTimeConverters.CalendarToXmlGregorianCalendarConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.DateToCalendarConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.DateToXmlGregorianCalendarConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.LongToCalendarConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.LongToDateConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.LongToXmlGregorianCalendarConverter());
        
        converterFactory.registerConverter(new DateAndTimeConverters.DateToSqlDateConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.DateToTimeConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.SqlDateToDateConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.TimeToDateConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.LongToSqlDateConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.LongToTimeConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.XmlGregorianCalendarToSqlDateConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.XmlGregorianCalendarToTimeConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.CalendarToSqlDateConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.CalendarToTimeConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.XmlGregorianCalendarToTimestampConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.DateToTimestampConverter());
        
        
        
        /*
         * Register numeric type converter
         */
        converterFactory.registerConverter(new NumericConverters.BigDecimalToDoubleConverter());
        converterFactory.registerConverter(new NumericConverters.BigDecimalToFloatConverter());
        converterFactory.registerConverter(new NumericConverters.BigIntegerToIntegerConverter(false));
        converterFactory.registerConverter(new NumericConverters.BigIntegerToLongConverter(false));
        
        converterFactory.registerConverter(new NumericConverters.IntegerToShortConverter(false));
        converterFactory.registerConverter(new NumericConverters.LongToIntegerConverter(false));
        converterFactory.registerConverter(new NumericConverters.LongToShortConverter(false));
        
        converterFactory.registerConverter(new NumericConverters.FloatToShortConverter(false));
        converterFactory.registerConverter(new NumericConverters.FloatToIntegerConverter(false));
        converterFactory.registerConverter(new NumericConverters.FloatToLongConverter(false));
        
        converterFactory.registerConverter(new NumericConverters.DoubleToShortConverter(false));
        converterFactory.registerConverter(new NumericConverters.DoubleToIntegerConverter(false));
        converterFactory.registerConverter(new NumericConverters.DoubleToLongConverter(false));
        
        /*
         * Register additional common "immutable" types
         */
        converterFactory.registerConverter(new PassThroughConverter(
              URL.class,
              URI.class,
              UUID.class,
              BigInteger.class,
              Locale.class,
              File.class,
              Inet4Address.class,
              Inet6Address.class,
              InetSocketAddress.class
                ));
        /*
         * Register additional common "cloneable" types
         */
        converterFactory.registerConverter(new CloneableConverter(
              Date.class,
              Calendar.class,
              XMLGregorianCalendar.class
                ));
    }
}
