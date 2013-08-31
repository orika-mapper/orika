package ma.glasnost.orika.test.converter;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.CustomConverterBase;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;

public class PrimitiveConversionTestCase {

	@Test
	public void testPrimitiveToWrapper() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new CustomConverterBase<Double, BigDecimal>() {

			public BigDecimal convert(Double source,
			        Class<? extends BigDecimal> destinationType) {
				return BigDecimal.valueOf(source);
			}
		});
		
		factory.getConverterFactory().registerConverter(new CustomConverterBase<BigDecimal, Double>() {

			public Double convert(BigDecimal source,
					Class<? extends Double> destinationType) {
				return source.doubleValue();
			}
		});
		
		factory.registerClassMap(ClassMapBuilder.map(A.class, B.class).byDefault().toClassMap());
		
		
		A source = new A();
		source.setValue(BigDecimal.TEN);
		
		B target = factory.getMapperFacade().map(source, B.class);
		
		Assert.assertTrue(target.getValue() == 10.0);
	}
	
	public static class A {
		private BigDecimal value;

		public BigDecimal getValue() {
			return value;
		}

		public void setValue(BigDecimal value) {
			this.value = value;
		}
		
	}
	
	public static class B {
		private double value;

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}
		
	}
}
