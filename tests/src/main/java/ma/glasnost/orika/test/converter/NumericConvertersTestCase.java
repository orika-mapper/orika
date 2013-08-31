package ma.glasnost.orika.test.converter;

import java.math.BigDecimal;
import java.math.BigInteger;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.converter.builtin.NumericConverters.BigDecimalToDoubleConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.BigDecimalToFloatConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.BigIntegerToIntegerConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.BigIntegerToLongConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.DoubleToIntegerConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.DoubleToLongConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.DoubleToShortConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.FloatToIntegerConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.FloatToLongConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.FloatToShortConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.IntegerToShortConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.LongToIntegerConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.LongToShortConverter;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * DateAndTimeConverters provides a set of individual converters for conversion
 * between the below listed enumeration of commonly used data/time
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
public class NumericConvertersTestCase {
    
    private static final double DELTA = 0.000000001;
    
    @Test
    public void testBigDecimalToDoubleConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new BigDecimalToDoubleConverter());
        MapperFacade mapper = factory.getMapperFacade();
        
        BigDecimal bd = new BigDecimal("5423.51478");
        Double db = mapper.map(bd, Double.class);
        Assert.assertEquals(bd.doubleValue(), db.doubleValue(), 0.00001d);
        
        BigDecimal reverse = mapper.map(db, BigDecimal.class);
        Assert.assertEquals(bd.doubleValue(), reverse.doubleValue(), 0.00001d);
    }
    
    @Test
    public void testBigDecimalToFloatConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new BigDecimalToFloatConverter());
        MapperFacade mapper = factory.getMapperFacade();
        
        BigDecimal bd = new BigDecimal("5423.51");
        Float ft = mapper.map(bd, Float.class);
        Assert.assertEquals(bd.floatValue(), ft.floatValue(), 0.01d);
        
        BigDecimal reverse = mapper.map(ft, BigDecimal.class);
        Assert.assertEquals(bd.doubleValue(), reverse.doubleValue(), 0.01d);
    }
    
    @Test
    public void testBigIntegerToLongConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new BigIntegerToLongConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        BigInteger bi = new BigInteger("" + Long.MAX_VALUE);
        Long lg = mapper.map(bi, Long.class);
        Assert.assertEquals(bi.longValue(), lg.longValue());
        
        BigInteger reverse = mapper.map(lg, BigInteger.class);
        Assert.assertEquals(bi.longValue(), reverse.longValue());
    }
    
    @Test
    public void testBigIntegerToIntegerConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new BigIntegerToIntegerConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        BigInteger bi = new BigInteger("" + Integer.MAX_VALUE);
        Integer i = mapper.map(bi, Integer.class);
        Assert.assertEquals(bi.longValue(), i.longValue());
        
        BigInteger reverse = mapper.map(i, BigInteger.class);
        Assert.assertEquals(bi.longValue(), reverse.longValue());
    }
    
    @Test(expected = MappingException.class)
    public void testBigIntegerToLongConverter_Overflow() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new BigIntegerToLongConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        BigInteger bi = new BigInteger("1" + Long.MAX_VALUE);
        Long lg = mapper.map(bi, Long.class);
        Assert.assertEquals(bi.longValue(), lg.longValue());
        
        BigInteger reverse = mapper.map(lg, BigInteger.class);
        Assert.assertEquals(bi.longValue(), reverse.longValue());
    }
    
    @Test(expected = MappingException.class)
    public void testBigIntegerToIntegerConverter_Overflow() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new BigIntegerToIntegerConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        BigInteger bi = new BigInteger("1" + Long.MAX_VALUE);
        Integer i = mapper.map(bi, Integer.class);
        Assert.assertEquals(bi.longValue(), i.longValue());
        
        BigInteger reverse = mapper.map(i, BigInteger.class);
        Assert.assertEquals(bi.longValue(), reverse.longValue());
    }
    
    @Test
    public void testLongToShortConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new LongToShortConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Long value = (long) Short.MAX_VALUE;
        Short result = mapper.map(value, Short.class);
        Assert.assertEquals(value.longValue(), result.longValue());
        
        Long reverse = mapper.map(result, Long.class);
        Assert.assertEquals(result.longValue(), reverse.longValue());
    }
    
    @Test
    public void testLongToIntegerConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new LongToIntegerConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Long value = (long) Integer.MAX_VALUE;
        Integer result = mapper.map(value, Integer.class);
        Assert.assertEquals(value.longValue(), result.longValue());
        
        Long reverse = mapper.map(result, Long.class);
        Assert.assertEquals(result.longValue(), reverse.longValue());
    }
    
    @Test
    public void testIntegerToShortConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new IntegerToShortConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Integer value = (int) Short.MAX_VALUE;
        Short result = mapper.map(value, Short.class);
        Assert.assertEquals(value.intValue(), result.intValue());
        
        Integer reverse = mapper.map(result, Integer.class);
        Assert.assertEquals(result.intValue(), reverse.intValue());
    }
    
    @Test
    public void testDoubleToShortConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new DoubleToShortConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Double value = (double) Short.MAX_VALUE;
        Short result = mapper.map(value, Short.class);
        Assert.assertEquals(value.doubleValue(), result.doubleValue(), DELTA);
        
        Double reverse = mapper.map(result, Double.class);
        Assert.assertEquals(result.doubleValue(), reverse.doubleValue(), DELTA);
    }
    
    @Test
    public void testDoubleToIntegerConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new DoubleToIntegerConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Double value = (double) Integer.MAX_VALUE;
        Integer result = mapper.map(value, Integer.class);
        Assert.assertEquals(value.doubleValue(), result.doubleValue(), DELTA);
        
        Double reverse = mapper.map(result, Double.class);
        Assert.assertEquals(result.doubleValue(), reverse.doubleValue(), DELTA);
    }
    
    @Test
    public void testDoubleToLongConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new DoubleToLongConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Double value = (double) Long.MAX_VALUE;
        Long result = mapper.map(value, Long.class);
        Assert.assertEquals(value.doubleValue(), result.doubleValue(), DELTA);
        
        Double reverse = mapper.map(result, Double.class);
        Assert.assertEquals(result.doubleValue(), reverse.doubleValue(), DELTA);
    }
    
    @Test
    public void testFloatToShortConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new FloatToShortConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Float value = (float) Short.MAX_VALUE;
        Short result = mapper.map(value, Short.class);
        Assert.assertEquals(value.floatValue(), result.floatValue(), DELTA);
        
        Float reverse = mapper.map(result, Float.class);
        Assert.assertEquals(result.floatValue(), reverse.floatValue(), DELTA);
    }
    
    @Test
    public void testFloatToIntegerConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new FloatToIntegerConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Float value = (float) Integer.MAX_VALUE;
        Integer result = mapper.map(value, Integer.class);
        Assert.assertEquals(value.floatValue(), result.floatValue(), DELTA);
        
        Float reverse = mapper.map(result, Float.class);
        Assert.assertEquals(result.floatValue(), reverse.floatValue(), DELTA);
    }
    
    @Test
    public void testFloatToLongConverter() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new FloatToLongConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Float value = (float) Long.MAX_VALUE;
        Long result = mapper.map(value, Long.class);
        Assert.assertEquals(value.floatValue(), result.floatValue(), DELTA);
        
        Float reverse = mapper.map(result, Float.class);
        Assert.assertEquals(result.floatValue(), reverse.floatValue(), DELTA);
    }
    
    // ~ overflow exceptions
    
    @Test(expected = MappingException.class)
    public void testLongToShortConverter_overflow() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new LongToShortConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Long value = (long) Short.MAX_VALUE + 1;
        Short result = mapper.map(value, Short.class);
        Assert.assertEquals(value.longValue(), result.longValue());
        
        Long reverse = mapper.map(result, Long.class);
        Assert.assertEquals(result.longValue(), reverse.longValue());
    }
    
    @Test(expected = MappingException.class)
    public void testLongToIntegerConverter_overflow() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new LongToIntegerConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Long value = (long) Integer.MAX_VALUE + 1;
        Integer result = mapper.map(value, Integer.class);
        Assert.assertEquals(value.longValue(), result.longValue());
        
        Long reverse = mapper.map(result, Long.class);
        Assert.assertEquals(result.longValue(), reverse.longValue());
    }
    
    @Test(expected = MappingException.class)
    public void testIntegerToShortConverter_overflow() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new IntegerToShortConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Integer value = Short.MAX_VALUE + 1;
        Short result = mapper.map(value, Short.class);
        Assert.assertEquals(value.intValue(), result.intValue());
        
        Integer reverse = mapper.map(result, Integer.class);
        Assert.assertEquals(result.intValue(), reverse.intValue());
    }
    
    @Test(expected = MappingException.class)
    public void testDoubleToShortConverter_overflow() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new DoubleToShortConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Double value = (double) Short.MAX_VALUE + 1;
        Short result = mapper.map(value, Short.class);
        Assert.assertEquals(value.doubleValue(), result.doubleValue(), DELTA);
        
        Double reverse = mapper.map(result, Double.class);
        Assert.assertEquals(result.doubleValue(), reverse.doubleValue(), DELTA);
    }
    
    @Test(expected = MappingException.class)
    public void testDoubleToIntegerConverter_overflow() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new DoubleToIntegerConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Double value = (double) Integer.MAX_VALUE + 1;
        Integer result = mapper.map(value, Integer.class);
        Assert.assertEquals(value.doubleValue(), result.doubleValue(), DELTA);
        
        Double reverse = mapper.map(result, Double.class);
        Assert.assertEquals(result.doubleValue(), reverse.doubleValue(), DELTA);
    }
    
    @Test(expected = MappingException.class)
    public void testDoubleToLongConverter_overflow() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new DoubleToLongConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Double value = Long.MAX_VALUE + 10000.0;
        Long result = mapper.map(value, Long.class);
        Assert.assertEquals(value.doubleValue(), result.doubleValue(), DELTA);
        
        Double reverse = mapper.map(result, Double.class);
        Assert.assertEquals(result.doubleValue(), reverse.doubleValue(), DELTA);
    }
    
    @Test(expected = MappingException.class)
    public void testFloatToShortConverter_overflow() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new FloatToShortConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Float value = (Short.MAX_VALUE) * 1.1f;
        Short result = mapper.map(value, Short.class);
        Assert.assertEquals(value.floatValue(), result.floatValue(), DELTA);
        
        Float reverse = mapper.map(result, Float.class);
        Assert.assertEquals(result.floatValue(), reverse.floatValue(), DELTA);
    }
    
    @Test(expected = MappingException.class)
    public void testFloatToIntegerConverter_overflow() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new FloatToIntegerConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Float value = (Integer.MAX_VALUE) * 1.1f;
        Integer result = mapper.map(value, Integer.class);
        Assert.assertEquals(value.floatValue(), result.floatValue(), DELTA);
        
        Float reverse = mapper.map(result, Float.class);
        Assert.assertEquals(result.floatValue(), reverse.floatValue(), DELTA);
    }
    
    @Test(expected = MappingException.class)
    public void testFloatToLongConverter_overflow() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.getConverterFactory().registerConverter(new FloatToLongConverter(false));
        MapperFacade mapper = factory.getMapperFacade();
        
        Float value = (Long.MAX_VALUE) * 1.1f;
        Long result = mapper.map(value, Long.class);
        Assert.assertEquals(value.floatValue(), result.floatValue(), DELTA);
        
        Float reverse = mapper.map(result, Float.class);
        Assert.assertEquals(result.floatValue(), reverse.floatValue(), DELTA);
    }
}
