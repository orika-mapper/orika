package ma.glasnost.orika.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;

/**
 * Support for JSR-310 (Date and Time).
 * <p>
 * 
 * @see <a href="https://github.com/orika-mapper/orika/issues/170">https://github.com/orika-mapper/orika/issues/170</a>
 * @see <a href="https://github.com/orika-mapper/orika/issues/96">https://github.com/orika-mapper/orika/issues/96</a>
 */
public class JavaDateApiTest {
    
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JavaDateApiTest.class);
    
    @Test
    public void testJavaDateApiMappings() {
        DefaultMapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        
        // prepare input
        A a = new A();
        a.setInstant(Instant.parse("2007-12-03T10:15:30.00Z"));
        a.setDuration(Duration.parse("-PT6H3M"));
        a.setLocalDate(LocalDate.parse("2007-12-03"));
        a.setLocalTime(LocalTime.parse("10:15:30.00"));
        a.setLocalDateTime(LocalDateTime.parse("2007-12-03T10:15:30.00"));
        a.setZonedDateTime(ZonedDateTime.parse("2007-12-03T10:15:30.00+02:00[Europe/Vienna]"));
        a.setDayOfWeek(DayOfWeek.MONDAY);
        a.setMonth(Month.JULY);
        a.setMonthDay(MonthDay.parse("--12-03"));
        a.setOffsetDateTime(OffsetDateTime.parse("2007-12-03T10:15:30.00+02:00"));
        a.setOffsetTime(OffsetTime.parse("10:15:30.00+02:00"));
        a.setPeriod(Period.parse("-P1Y2M"));
        a.setYear(Year.parse("2007"));
        a.setYearMonth(YearMonth.parse("2007-12"));
        a.setZoneOffset(ZoneOffset.of("+02:00"));
        
        // run Test:
        A mappedA = mapperFactory.getMapperFacade().map(a, A.class);
        
        // validate result
        assertThat(mappedA, notNullValue());
        assertThat(mappedA.getInstant(), is(a.getInstant()));
        assertThat(mappedA.getDuration(), is(a.getDuration()));
        assertThat(mappedA.getLocalDate(), is(a.getLocalDate()));
        assertThat(mappedA.getLocalTime(), is(a.getLocalTime()));
        assertThat(mappedA.getLocalDateTime(), is(a.getLocalDateTime()));
        assertThat(mappedA.getZonedDateTime(), is(a.getZonedDateTime()));
        assertThat(mappedA.getDayOfWeek(), is(a.getDayOfWeek()));
        assertThat(mappedA.getMonth(), is(a.getMonth()));
        assertThat(mappedA.getMonthDay(), is(a.getMonthDay()));
        assertThat(mappedA.getOffsetDateTime(), is(a.getOffsetDateTime()));
        assertThat(mappedA.getOffsetTime(), is(a.getOffsetTime()));
        assertThat(mappedA.getOffsetTime(), is(a.getOffsetTime()));
        assertThat(mappedA.getPeriod(), is(a.getPeriod()));
        assertThat(mappedA.getYear(), is(a.getYear()));
        assertThat(mappedA.getYearMonth(), is(a.getYearMonth()));
        assertThat(mappedA.getZoneOffset(), is(a.getZoneOffset()));
        
    }
    
    @Test
    public void testJavaDateApiMappings_withCustomConverter_shouldOverwriteDefaultBehavior() {
        DefaultMapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory.getConverterFactory().registerConverter(new CustomConverter<Instant, Instant>() {
            public Instant convert(Instant source, Type<? extends Instant> destType, MappingContext mappingContext) {
                if (source == null) {
                    return null;
                }
                // TestCase: add 28 days during Mapping
                return source.plus(Period.parse("P28D"));
            }
        });
        
        // prepare input
        A a = new A();
        a.setInstant(Instant.parse("2007-03-04T10:15:30.00Z"));
        
        // run Test:
        A mappedA = mapperFactory.getMapperFacade().map(a, A.class);
        
        // validate result
        assertThat(mappedA, notNullValue());
        assertThat(mappedA.getInstant(), is(Instant.parse("2007-04-01T10:15:30.00Z")));
        
    }
    
    public static class A {

        private Instant instant;
        private Duration duration;
        private LocalDate localDate;
        private LocalTime localTime;
        private LocalDateTime localDateTime;
        private ZonedDateTime zonedDateTime;
        private DayOfWeek dayOfWeek;
        private Month month;
        private MonthDay monthDay;
        private OffsetDateTime offsetDateTime;
        private OffsetTime offsetTime;
        private Period period;
        private Year year;
        private YearMonth yearMonth;
        private ZoneOffset zoneOffset;
        
        public Instant getInstant() {
            return instant;
        }
        
        public void setInstant(Instant instant) {
            this.instant = instant;
        }
        
        public Duration getDuration() {
            return duration;
        }
        
        public void setDuration(Duration duration) {
            this.duration = duration;
        }
        
        public LocalDate getLocalDate() {
            return localDate;
        }
        
        public void setLocalDate(LocalDate localDate) {
            this.localDate = localDate;
        }
        
        public LocalTime getLocalTime() {
            return localTime;
        }
        
        public void setLocalTime(LocalTime localTime) {
            this.localTime = localTime;
        }
        
        public LocalDateTime getLocalDateTime() {
            return localDateTime;
        }
        
        public void setLocalDateTime(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
        }
        
        public ZonedDateTime getZonedDateTime() {
            return zonedDateTime;
        }
        
        public void setZonedDateTime(ZonedDateTime zonedDateTime) {
            this.zonedDateTime = zonedDateTime;
        }
        
        public DayOfWeek getDayOfWeek() {
            return dayOfWeek;
        }
        
        public void setDayOfWeek(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }
        
        public Month getMonth() {
            return month;
        }
        
        public void setMonth(Month month) {
            this.month = month;
        }
        
        public MonthDay getMonthDay() {
            return monthDay;
        }
        
        public void setMonthDay(MonthDay monthDay) {
            this.monthDay = monthDay;
        }
        
        public OffsetDateTime getOffsetDateTime() {
            return offsetDateTime;
        }
        
        public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
            this.offsetDateTime = offsetDateTime;
        }
        
        public OffsetTime getOffsetTime() {
            return offsetTime;
        }
        
        public void setOffsetTime(OffsetTime offsetTime) {
            this.offsetTime = offsetTime;
        }
        
        public Period getPeriod() {
            return period;
        }
        
        public void setPeriod(Period period) {
            this.period = period;
        }
        
        public Year getYear() {
            return year;
        }
        
        public void setYear(Year year) {
            this.year = year;
        }
        
        public YearMonth getYearMonth() {
            return yearMonth;
        }
        
        public void setYearMonth(YearMonth yearMonth) {
            this.yearMonth = yearMonth;
        }
        
        public ZoneOffset getZoneOffset() {
            return zoneOffset;
        }
        
        public void setZoneOffset(ZoneOffset zoneOffset) {
            this.zoneOffset = zoneOffset;
        }

    }

}
