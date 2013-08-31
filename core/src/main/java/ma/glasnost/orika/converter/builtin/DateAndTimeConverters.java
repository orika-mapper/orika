package ma.glasnost.orika.converter.builtin;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

/**
 * DateAndTimeConverters provides a set of individual converters
 * for conversion between the below listed enumeration of commonly used data/time
 * representations:
 * <ul>
 * <li>java.util.Date
 * <li>java.util.Calendar
 * <li>java.lang.Long or long
 * <li>javax.xml.datatype.XMLGregorianCalendar
 * </ul>
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class DateAndTimeConverters {

	
	/**
	 * Provides conversion between Date and Calendar
	 * 
	 * @author matt.deboer@gmail.com
	 */
	public static class DateToCalendarConverter extends
			BidirectionalConverter<Date, Calendar> {

		@Override
		public Calendar convertTo(Date source, Type<Calendar> destinationType) {
			return toCalendar(source);
		}

		@Override
		public Date convertFrom(Calendar source, Type<Date> destinationType) {
			return toDate(source);
		}
	}

	/**
     * Provides conversion between Date and java.sql.Date
     * 
     * @author matt.deboer@gmail.com
     */
    public static class DateToSqlDateConverter extends
        CustomConverter<Date, java.sql.Date> {

        /* (non-Javadoc)
         * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
         */
        public java.sql.Date convert(Date source, Type<? extends java.sql.Date> destinationType) {
            return new java.sql.Date(source.getTime());
        }
    }
	
    /**
     * Provides conversion between Date and Time
     * 
     * @author matt.deboer@gmail.com
     */
    public static class DateToTimeConverter extends
            CustomConverter<Date, Time> {

        /* (non-Javadoc)
         * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
         */
        public Time convert(Date source, Type<? extends Time> destinationType) {
            return new Time(source.getTime());
        }
    }
	
    /**
     * Provides conversion between Date and java.sql.Date
     * 
     * @author matt.deboer@gmail.com
     */
    public static class SqlDateToDateConverter extends
        CustomConverter<java.sql.Date, Date> {

        /* (non-Javadoc)
         * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
         */
        public Date convert(java.sql.Date source, Type<? extends Date> destinationType) {
            return new Date(source.getTime());
        }
    }
    
    /**
     * Provides conversion between Date and Time
     * 
     * @author matt.deboer@gmail.com
     */
    public static class TimeToDateConverter extends
            CustomConverter<Time, Date> {

        /* (non-Javadoc)
         * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
         */
        public Date convert(Time source, Type<? extends Date> destinationType) {
            return new Date(source.getTime());
        }
    }
    
    /**
     * Provides conversion between XMLGregorianCalendar and java.sql.Date
     * 
     * @author matt.deboer@gmail.com
     */
    public static class XmlGregorianCalendarToSqlDateConverter extends
        CustomConverter<XMLGregorianCalendar, java.sql.Date> {

        /* (non-Javadoc)
         * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
         */
        public java.sql.Date convert(XMLGregorianCalendar source, Type<? extends java.sql.Date> destinationType) {
            return new java.sql.Date(toLong(source));
        }
    }
    
    /**
     * Provides conversion between XMLGregorianCalendar and Time
     * 
     * @author matt.deboer@gmail.com
     */
    public static class XmlGregorianCalendarToTimeConverter extends
        CustomConverter<XMLGregorianCalendar, Time> {

        /* (non-Javadoc)
         * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
         */
        public Time convert(XMLGregorianCalendar source, Type<? extends Time> destinationType) {
            return new Time(toLong(source));
        }
    }
    
    /**
     * Provides conversion between Calendar and java.sql.Date
     * 
     * @author matt.deboer@gmail.com
     */
    public static class CalendarToSqlDateConverter extends
        CustomConverter<Calendar, java.sql.Date> {

        /* (non-Javadoc)
         * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
         */
        public java.sql.Date convert(Calendar source, Type<? extends java.sql.Date> destinationType) {
            return new java.sql.Date(toLong(source));
        }
    }
    
    /**
     * Provides conversion between Calendar and Time
     * 
     * @author matt.deboer@gmail.com
     */
    public static class CalendarToTimeConverter extends
        CustomConverter<Calendar, Time> {

        /* (non-Javadoc)
         * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
         */
        public Time convert(Calendar source, Type<? extends Time> destinationType) {
            return new Time(toLong(source));
        }
    }
    
	/**
	 * Provides conversion between Date and XMLGregorianCalendar
	 * 
	 * @author matt.deboer@gmail.com
	 */
	public static class DateToXmlGregorianCalendarConverter extends
			BidirectionalConverter<Date, XMLGregorianCalendar> {

	    private DatatypeFactory factory;
        
        {
            try {
                factory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException e) {
                throw new IllegalStateException(e);
            }
        }
	    
		@Override
		public XMLGregorianCalendar convertTo(Date source,
				Type<XMLGregorianCalendar> destinationType) {
			return toXMLGregorianCalendar(source, factory);
		}

		@Override
		public Date convertFrom(XMLGregorianCalendar source,
				Type<Date> destinationType) {
			return toDate(source);
		}
	}

	/**
	 * Provides conversion between Calendar and XMLGregorianCalendar
	 * 
	 * @author matt.deboer@gmail.com
	 */
	public static class CalendarToXmlGregorianCalendarConverter extends
			BidirectionalConverter<Calendar, XMLGregorianCalendar> {

	    private DatatypeFactory factory;
	    
	    {
	        try {
                factory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException e) {
                throw new IllegalStateException(e);
            }
	    }
	    
		@Override
		public XMLGregorianCalendar convertTo(Calendar source,
				Type<XMLGregorianCalendar> destinationType) {
			return toXMLGregorianCalendar(source, factory);
		}

		@Override
		public Calendar convertFrom(XMLGregorianCalendar source,
				Type<Calendar> destinationType) {
			return toCalendar(source);
		}
	}

	/**
	 * Provides conversion between Long and Date
	 * 
	 * @author matt.deboer@gmail.com
	 *
	 */
	public static class LongToDateConverter extends
			BidirectionalConverter<Long, Date> {

		@Override
		public Date convertTo(Long source, Type<Date> destinationType) {
			return toDate(source);
		}

		@Override
		public Long convertFrom(Date source, Type<Long> destinationType) {
			return toLong(source);
		}
	}

	
	/**
     * Provides conversion between Long and Date
     * 
     * @author matt.deboer@gmail.com
     *
     */
    public static class LongToSqlDateConverter extends
            BidirectionalConverter<Long, java.sql.Date> {

        @Override
        public java.sql.Date convertTo(Long source, Type<java.sql.Date> destinationType) {
            return new java.sql.Date(source);
        }

        @Override
        public Long convertFrom(java.sql.Date source, Type<Long> destinationType) {
            return toLong(source);
        }
    }
	
    /**
     * Provides conversion between Long and Date
     * 
     * @author matt.deboer@gmail.com
     *
     */
    public static class LongToTimeConverter extends
            BidirectionalConverter<Long, Time> {

        @Override
        public Time convertTo(Long source, Type<Time> destinationType) {
            return new Time(source);
        }

        @Override
        public Long convertFrom(Time source, Type<Long> destinationType) {
            return toLong(source);
        }
    }
    
	/**
	 * Provides conversion between Long and Calendar
	 * 
	 * @author matt.deboer@gmail.com
	 *
	 */
	public static class LongToCalendarConverter extends
			BidirectionalConverter<Long, Calendar> {

		@Override
		public Calendar convertTo(Long source, Type<Calendar> destinationType) {
			return toCalendar(source);
		}

		@Override
		public Long convertFrom(Calendar source, Type<Long> destinationType) {
			return toLong(source);
		}
	}

	
	/**
     * Provides conversion between Long and Calendar
     * 
     * @author matt.deboer@gmail.com
     *
     */
    public static class LongToXmlGregorianCalendarConverter extends
            BidirectionalConverter<Long, XMLGregorianCalendar> {

        private DatatypeFactory factory;
        
        {
            try {
                factory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException e) {
                throw new IllegalStateException(e);
            }
        }
        
        @Override
        public XMLGregorianCalendar convertTo(Long source, Type<XMLGregorianCalendar> destinationType) {
            return toXMLGregorianCalendar(source, factory);
        }

        @Override
        public Long convertFrom(XMLGregorianCalendar source, Type<Long> destinationType) {
            return toLong(source);
        }
    }
    
    
    /**
     * Provides conversion between Calendar and Time
     * 
     * @author matt.deboer@gmail.com
     */
    public static class XmlGregorianCalendarToTimestampConverter extends
        CustomConverter<XMLGregorianCalendar, Timestamp> {

        /* (non-Javadoc)
         * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
         */
        public Timestamp convert(XMLGregorianCalendar source, Type<? extends Timestamp> destinationType) {
            return new Timestamp(toLong(source));
        }
    }
    
    /**
     * Provides conversion between Calendar and Time
     * 
     * @author matt.deboer@gmail.com
     */
    public static class DateToTimestampConverter extends
        CustomConverter<Date, Timestamp> {

        /* (non-Javadoc)
         * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
         */
        public Timestamp convert(Date source, Type<? extends Timestamp> destinationType) {
            return new Timestamp(toLong(source));
        }
    }
    
    
	private static Date toDate(XMLGregorianCalendar source) {
		return source.toGregorianCalendar().getTime();
	}

	private static Date toDate(Calendar source) {
		return source.getTime();
	}

	private static Date toDate(Long source) {
		return new Date(source);
	}

	private static Calendar toCalendar(XMLGregorianCalendar source) {
		return toCalendar(source.toGregorianCalendar().getTime());
	}

	private static Calendar toCalendar(Date source) {
		Calendar c = Calendar.getInstance();
		c.setTime(source);
		return c;
	}

	private static Calendar toCalendar(Long source) {
		return toCalendar(new Date(source));
	}

	private static XMLGregorianCalendar toXMLGregorianCalendar(
			Calendar source, DatatypeFactory factory) {
		return toXMLGregorianCalendar(source.getTime(), factory);
	}

	private static XMLGregorianCalendar toXMLGregorianCalendar(
			Date source, DatatypeFactory factory) {
		
	    GregorianCalendar c = new GregorianCalendar();
		c.setTime(source);
		
		return factory.newXMLGregorianCalendar(c);
		
	}

	private static XMLGregorianCalendar toXMLGregorianCalendar(
			Long source, DatatypeFactory factory) {
		return toXMLGregorianCalendar(new Date(source), factory);
	}

	private static Long toLong(Date source) {
		return source.getTime();
	}

	private static Long toLong(Calendar source) {
		return toLong(source.getTime());
	}

	private static Long toLong(XMLGregorianCalendar source) {
		return toLong(source.toGregorianCalendar().getTime());
	}
}
