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

package ma.glasnost.orika.test.primitives;

import static org.junit.Assert.assertEquals;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class PrimitivesTestCase {

	@Test
	public void testPrimitivesToWrapper() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		MapperFacade mapper = factory.getMapperFacade();

		PrimitiveAttributes source = new PrimitiveAttributes();

		source.setAge(27);
		source.setShortValue((short)27);
		source.setFloatValue(2.5f);
		source.setDoubleValue(22.4567d);
		source.setLongValue(System.currentTimeMillis());
		source.setName("PPPPP");
		source.setSex('H');
		source.setVip(true);

		WrapperAttributes destination = mapper.map(source, WrapperAttributes.class);

		Assert.assertEquals(Integer.valueOf(source.getAge()), destination.getAge());
		Assert.assertEquals(source.getName(), destination.getName());
		Assert.assertEquals(Character.valueOf(source.getSex()), destination.getSex());
		Assert.assertEquals(source.getVip(), destination.getVip());
		Assert.assertEquals(Short.valueOf(source.getShortValue()), destination.getShortValue());
		Assert.assertEquals(Long.valueOf(source.getLongValue()), destination.getLongValue());
		Assert.assertEquals(Float.valueOf(source.getFloatValue()), destination.getFloatValue());
		Assert.assertEquals(Double.valueOf(source.getDoubleValue()), destination.getDoubleValue());

	}

	@Test
	public void testWrapperToPrimitives() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		MapperFacade mapper = factory.getMapperFacade();

		WrapperAttributes source = new WrapperAttributes();

		source.setAge(27);
		source.setShortValue((short)27);
		source.setFloatValue(2.5f);
		source.setDoubleValue(22.4567d);
		source.setLongValue(System.currentTimeMillis());
		source.setName("PPPPP");
		source.setSex('H');
		source.setVip(true);

		PrimitiveAttributes destination = mapper.map(source, PrimitiveAttributes.class);

		Assert.assertEquals(source.getAge(), Integer.valueOf(destination.getAge()));
		Assert.assertEquals(source.getName(), destination.getName());
		Assert.assertEquals(source.getSex(), Character.valueOf(destination.getSex()));
		Assert.assertEquals(source.getVip(), destination.getVip());
		Assert.assertEquals(source.getShortValue(), Short.valueOf(destination.getShortValue()));
		Assert.assertEquals(source.getLongValue(), Long.valueOf(destination.getLongValue()));
		Assert.assertEquals(source.getFloatValue(), Float.valueOf(destination.getFloatValue()));
		Assert.assertEquals(source.getDoubleValue(), Double.valueOf(destination.getDoubleValue()));

	}
	
	@Test
	public void testWrapperToWrapper() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		MapperFacade mapper = factory.getMapperFacade();

		WrapperAttributes source = new WrapperAttributes();

		source.setAge(27);
		source.setShortValue((short)27);
		source.setFloatValue(2.5f);
		source.setDoubleValue(22.4567d);
		source.setLongValue(System.currentTimeMillis());
		source.setName("PPPPP");
		source.setSex('H');
		source.setVip(true);

		OtherWrapperAttributes destination = mapper.map(source, OtherWrapperAttributes.class);

		Assert.assertEquals(source.getAge(), Integer.valueOf(destination.getAge()));
		Assert.assertEquals(source.getName(), destination.getName());
		Assert.assertEquals(source.getSex(), Character.valueOf(destination.getSex()));
		Assert.assertEquals(source.getVip(), destination.getVip());
		Assert.assertEquals(source.getShortValue(), Short.valueOf(destination.getShortValue()));
		Assert.assertEquals(source.getLongValue(), Long.valueOf(destination.getLongValue()));
		Assert.assertEquals(source.getFloatValue(), Float.valueOf(destination.getFloatValue()));
		Assert.assertEquals(source.getDoubleValue(), Double.valueOf(destination.getDoubleValue()));

	}

	
	@Test
	public void int_short() {
	    ShortHolder source = new ShortHolder();
	    source.value = 2;
	    
	    MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
	    
	    IntHolder dest = mapper.map(source, IntHolder.class);
	    assertEquals(source.value, dest.value);
	    
	    ShortHolder mapBack = mapper.map(dest, ShortHolder.class);
	    assertEquals(source.value, mapBack.value);
	}
	
    @Test
    public void long_short() {
        ShortHolder source = new ShortHolder();
        source.value = 2;
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        LongHolder dest = mapper.map(source, LongHolder.class);
        assertEquals(source.value, dest.value);
        
        ShortHolder mapBack = mapper.map(dest, ShortHolder.class);
        assertEquals(source.value, mapBack.value);
    }
    
    @Test
    public void long_int() {
        IntHolder source = new IntHolder();
        source.value = 2;
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        LongHolder dest = mapper.map(source, LongHolder.class);
        assertEquals(source.value, dest.value);
        
        IntHolder mapBack = mapper.map(dest, IntHolder.class);
        assertEquals(source.value, mapBack.value);
    }
	
	
	public static class IntHolder {
	    public int value;
	}
	
	public static class ShortHolder {
	    public short value;
	}
	
	public static class LongHolder {
	    public long value;
	}
	
	public static class FloatHolder {
	    public float value;
	}
	
	public static class DoubleHolder {
	    public double value;
	}
	
	public static class CharHolder {
	    public char value;
	}
	
	public static class PrimitiveAttributes {
		private int age;
		private short shortValue;
		private long longValue;
		private float floatValue;
		private double doubleValue;
		private String name;
		private char sex;
		private boolean vip;
		

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public char getSex() {
			return sex;
		}

		public void setSex(char sex) {
			this.sex = sex;
		}

		public boolean getVip() {
			return vip;
		}

		public void setVip(boolean vip) {
			this.vip = vip;
		}

		public short getShortValue() {
			return shortValue;
		}

		public void setShortValue(short shortValue) {
			this.shortValue = shortValue;
		}

		public long getLongValue() {
			return longValue;
		}

		public void setLongValue(long longValue) {
			this.longValue = longValue;
		}

		public float getFloatValue() {
			return floatValue;
		}

		public void setFloatValue(float floatValue) {
			this.floatValue = floatValue;
		}

		public double getDoubleValue() {
			return doubleValue;
		}

		public void setDoubleValue(double doubleValue) {
			this.doubleValue = doubleValue;
		}

	}

	public static class WrapperAttributes {
		private Integer age;
		private Short shortValue;
		private Long longValue;
		private Float floatValue;
		private Double doubleValue;
		private String name;
		private Character sex;
		private Boolean vip;

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String nom) {
			this.name = nom;
		}

		public Character getSex() {
			return sex;
		}

		public void setSex(Character sex) {
			this.sex = sex;
		}

		public Boolean getVip() {
			return vip;
		}

		public void setVip(Boolean vip) {
			this.vip = vip;
		}

		public Short getShortValue() {
			return shortValue;
		}

		public void setShortValue(Short shortValue) {
			this.shortValue = shortValue;
		}

		public Long getLongValue() {
			return longValue;
		}

		public void setLongValue(Long longValue) {
			this.longValue = longValue;
		}

		public Float getFloatValue() {
			return floatValue;
		}

		public void setFloatValue(Float floatValue) {
			this.floatValue = floatValue;
		}

		public Double getDoubleValue() {
			return doubleValue;
		}

		public void setDoubleValue(Double doubleValue) {
			this.doubleValue = doubleValue;
		}

	}
	
	public static class OtherWrapperAttributes {
		private Integer age;
		private Short shortValue;
		private Long longValue;
		private Float floatValue;
		private Double doubleValue;
		private String name;
		private Character sex;
		private Boolean vip;

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String nom) {
			this.name = nom;
		}

		public Character getSex() {
			return sex;
		}

		public void setSex(Character sex) {
			this.sex = sex;
		}

		public Boolean getVip() {
			return vip;
		}

		public void setVip(Boolean vip) {
			this.vip = vip;
		}

		public Short getShortValue() {
			return shortValue;
		}

		public void setShortValue(Short shortValue) {
			this.shortValue = shortValue;
		}

		public Long getLongValue() {
			return longValue;
		}

		public void setLongValue(Long longValue) {
			this.longValue = longValue;
		}

		public Float getFloatValue() {
			return floatValue;
		}

		public void setFloatValue(Float floatValue) {
			this.floatValue = floatValue;
		}

		public Double getDoubleValue() {
			return doubleValue;
		}

		public void setDoubleValue(Double doubleValue) {
			this.doubleValue = doubleValue;
		}


	}
}
