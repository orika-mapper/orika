package ma.glasnost.orika.test.jdk8;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
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
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Assert;
import org.junit.Test;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

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
    
    @Test
    public void testJdk8Type_shouldBeImmutable() throws Exception {
        // 1. find all SubClasses of TemporalAccessor, TemporalAmount
        List<Class<?>> jdkClasses = new ArrayList<>();
        jdkClasses.addAll(getJdkClasses("java.time", TemporalAccessor.class));
        jdkClasses.addAll(getJdkClasses("java.time", TemporalAmount.class));
        
        // 2. filter Classes which should be immutable (TemporalAccessor and TemporalAmount should be implemented as immutable):
        List<Class<?>> immutableJdkClasses = jdkClasses.stream()
                .filter(this::immutableRequirements)
                .collect(Collectors.toList());
        
        LOG.info("Found the following immutable classes: ");
        immutableJdkClasses.forEach((type) -> LOG.info("\t {}", type.getName()));
        
        // validation: collect classes which are not identified as immutable (there should be none):
        List<Class<?>> missingTypes = immutableJdkClasses.stream()
                .filter((type) -> !TypeFactory.valueOf(type).isImmutable())
                .collect(Collectors.toList());
        
        // throw Validation Exception if there are some classes.
        // This can happen if the JDK introduces new Classes which most likely must be added to ma.glasnost.orika.metadata.Type.
        if (!missingTypes.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Add the following immutable Types to ma.glasnost.orika.metadata.Type:\n");
            missingTypes.forEach((type) -> {
                sb.append(statement(type));
                sb.append('\n');
            });
            LOG.warn(sb.toString());
            Assert.fail(sb.toString());
        }
    }

    private String statement(Class<?> subType) {
        return "        addClassIfExists(tmpImmutableJdk8Types, \"" + subType.getName() + "\");";
    }
    
    private boolean immutableRequirements(Class<?> subType) {
        return Modifier.isFinal(subType.getModifiers()) // Immutable Classes are typical final
                && Modifier.isPublic(subType.getModifiers()) // We are not interested in non-public classes
                && !subType.isEnum(); // We don't need register Enums in ma.glasnost.orika.metadata.Type (they always are immutable)
    }
    
    
    @SuppressWarnings("unchecked")
    private <T> List<Class<T>> getJdkClasses(String rootPackage, final Class<T> type) throws Exception {
        
        String rootPackagePath = rootPackage.replace('.', '/');
        String path = type.getName().replace('.', '/') + ".class";
        URL resource = ClassLoader.getSystemClassLoader().getResource(path);
        if (resource == null) {
            throw new RuntimeException("Unexpected problem: No resource for " + path);
        }
        String filePath = resource.getFile();
        File jarFile = getJarFile(filePath);
        
        List<Class<T>> subTypes = new ArrayList<>();
        
        try (ZipFile zipFile = new ZipFile(jarFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.getName().startsWith(rootPackagePath)) {
                    String className = zipEntry.getName();
                    if (className.endsWith(".class")) {
                        className = className.substring(0, className.length() - ".class".length());
                        className = className.replace('/', '.');
                        Class<?> clazz = Class.forName(className);
                        if (type.isAssignableFrom(clazz)) {
                            subTypes.add((Class<T>) clazz);
                        }
                    }
                }
            }
        }
        Collections.sort(subTypes, (type1, type2) -> type1.getName().compareTo(type2.getName()));
        
        return subTypes;
    }

    private File getJarFile(String filePath) throws Exception {
        String jarFilePath = filePath;
        jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
        if (jarFilePath.startsWith("file:")) {
            jarFilePath = jarFilePath.substring(jarFilePath.lastIndexOf("file:") + "file:".length(), jarFilePath.length());
        }
        if (jarFilePath.contains(".jar!")) {
        	jarFilePath = jarFilePath.substring(0, jarFilePath.lastIndexOf(".jar!"))+ ".jar";
        }
        return new File(jarFilePath);
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
