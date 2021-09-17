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

package ma.glasnost.orika.converter.builtin;

import java.util.Calendar;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import ma.glasnost.orika.BaseConverter;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * BuiltinConverters is a utility class used to register common built-in
 * converters.
 * 
 * @author mattdeboer
 *
 */
public abstract class BuiltinConverters {
    
    /**
     * Registers a common set of built-in converters which can handle many
     * common conversion situations.<br>
     * Specifically, this includes:
     * <ul>
     * <li>ConstructorConverter: converts from the source type to destination
     * type if there is a constructor available on the destination type which
     * takes the source as a single argument.
     * <li>FromStringConverter: able to convert from a String to enum,
     * primitive, or primitive wrapper.
     * <li>ToStringconverter: able to convert any type to String
     * <li>DateAndTimeConverters: convert between common date/time
     * representations
     * <ul>
     * <li>java.util.Calendar
     * <li>java.util.Date
     * <li>long / java.lang.Long
     * <li>javax.xml.datatype.XMLGregorianCalendar
     * <li>java.sql.Date
     * <li>java.sql.Time
     * <li>java.sql.Timestamp
     * </ul>
     * <li>CloneableConverter registered for the following cloneable types:
     * <ul>
     * <li>java.util.Date
     * <li>java.util.Calendar
     * <li>javax.xml.datatype.XMLGregorianCalendar
     * </ul>
     * </ul>
     * 
     * @param converterFactory
     *            the converter factory on which to register the converters
     */
    public static void register(ConverterFactory converterFactory) {
        
        converterFactory.registerConverter(new CopyByReferenceConverter());
        
        converterFactory.registerConverter(new EnumConverter());
        
        /*
         * Register to/from string converters
         */
        converterFactory.registerConverter(new FromStringConverter());
        converterFactory.registerConverter(new ToStringConverter());
        
        /*
         * Register common date/time converters
         */
        converterFactory.registerConverter(new DateAndTimeConverters.DateToXmlGregorianCalendarConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.DateToTimeConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.CalendarToXmlGregorianCalendarConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.XmlGregorianCalendarToTimestampConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.XmlGregorianCalendarToSqlDateConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.XmlGregorianCalendarToTimeConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.LongToXmlGregorianCalendarConverter());
        
        converterFactory.registerConverter(new DateAndTimeConverters.DateToCalendarConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.CalendarToTimeConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.CalendarToSqlDateConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.LongToCalendarConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.TimestampToCalendarConverter());
        
        converterFactory.registerConverter(new DateAndTimeConverters.DateToSqlDateConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.LongToSqlDateConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.TimeToSqlDateConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.TimestampToSqlDateConverter());
        
        converterFactory.registerConverter(new DateAndTimeConverters.LongToTimeConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.TimestampToTimeConverter());
        
        converterFactory.registerConverter(new DateAndTimeConverters.LongToTimestampConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.DateToTimestampConverter());
        
        converterFactory.registerConverter(new DateAndTimeConverters.LongToDateConverter());
        
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
         * Register additional common "cloneable" types
         */
        converterFactory.registerConverter(makeSimpleConverter(
                XMLGregorianCalendar.class, s -> (XMLGregorianCalendar) s.clone()));
        converterFactory.registerConverter(makeSimpleConverter(
                Calendar.class, s -> (Calendar) s.clone()));
        converterFactory.registerConverter(makeSimpleConverter(
                java.sql.Date.class, s -> (java.sql.Date) s.clone()));
        converterFactory.registerConverter(makeSimpleConverter(Date.class, s -> (Date) s.clone()));

        /*
         * Register converter to instantiate by using a constructor on the
         * destination which takes the source as argument
         */
        converterFactory.registerConverter(new ConstructorConverter());
    }

    static <T> Converter<T, T> makeSimpleConverter(Class<T> cls, SimpleFnConverter<T> fn) {
        Type<T> type = TypeFactory.valueOf(cls);

        return new BaseConverter<T, T>(type, type) {

            @Override
            public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
                return type.isAssignableFrom(sourceType) && destinationType.equals(sourceType);
            }

            @Override
            public T convert(T source, Type<? extends T> destinationType, MappingContext mappingContext) {
                if (source == null) {
                    return null;
                }
                return fn.convert(source);
            }

        };
    }

    interface SimpleFnConverter<T> {
        T convert(T source);
    }
}
