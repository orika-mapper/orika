/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

/**
 * DateToStringConverter provides custom conversion from String values
 * to and from Date instances, based on a provided date format pattern.<br><br>
 * 
 * The format is applied based on the rules defined in {@link java.text.SimpleDateFormat}.
 *
 */
public class DateToStringConverter extends BidirectionalConverter<Date, String> {
    
    private final String pattern;
    private final Locale locale;
    private final ThreadLocal<SimpleDateFormat> dateFormats = new ThreadLocal<SimpleDateFormat>();
    
    /**
     * @return a SimpleDateFormat instance safe for use in the current thread
     */
    private SimpleDateFormat getDateFormat() {
    	SimpleDateFormat formatter = dateFormats.get();
    	if (formatter == null) {
    		formatter = new SimpleDateFormat(pattern, locale);
    		dateFormats.set(formatter);
    	}
    	return formatter;
    }
    
    /**
     * Constructs a new instance of DateToStringConverter capable of
     * parsing and constructing Date strings according to the provided format. 
     * 
     * @param format the format descriptor, processed according to the rules
     * defined in {@link java.text.SimpleDateFormat}
     */
    public DateToStringConverter(final String format) {
        this(format, Locale.getDefault());
    }

    /**
     * Constructs a new instance of DateToStringConverter with given locale
     */
    public DateToStringConverter(final String format, final Locale locale) {
        this.pattern = format;
        this.locale = locale;
    }
    
    @Override
	public String convertTo(Date source, Type<String> destinationType) {
        return getDateFormat().format(source);
    }
    
    @Override
	public Date convertFrom(String source, Type<Date> destinationType) {
        try {
            return getDateFormat().parse(source);
        } catch (ParseException e) {
            return null;
        }
    }    
}
