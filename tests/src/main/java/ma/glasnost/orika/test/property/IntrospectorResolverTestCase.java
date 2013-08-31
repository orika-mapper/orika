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

package ma.glasnost.orika.test.property;

import java.math.BigDecimal;
import java.util.Map;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.metadata.NestedProperty;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.property.IntrospectorPropertyResolver;
import ma.glasnost.orika.property.PropertyResolverStrategy;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class IntrospectorResolverTestCase {

    private PropertyResolverStrategy propertyResolver = new IntrospectorPropertyResolver();
    
	@Test
	public void testNestedProperty() {
		String np = "start.x";

		NestedProperty p = (NestedProperty) propertyResolver.getProperty(Line.class, np);

		Assert.assertEquals(Integer.TYPE, p.getRawType());
	}
	
	@Test
    public void testGetInvalidNestedProperty() {
        String np = "bogus.x";

        try {
            propertyResolver.getProperty(Line.class, np);
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("could not resolve nested property [" + np + "]"));
        }
    }

	@Test
	public void testBooleanMapping() {
		SpecialCase sc = new SpecialCase();
		sc.setChecked(true);
		sc.totalCost = new BigDecimal("42.50");
		
		MapperFacade mapper = MappingUtil.getMapperFactory().getMapperFacade();
		SpecialCaseDto dto = mapper.map(sc, SpecialCaseDto.class);
		
		Assert.assertEquals(sc.isChecked(), Boolean.valueOf(dto.isChecked()));
		//Assert.assertEquals(sc.totalCost.doubleValue(), dto.getTotalCost(), 0.01d);
	}
	
	@Test
	public void testOverridePropertyDefinition() {
	    
	    Map<String, Property> properties = propertyResolver.getProperties(PostalAddress.class);
	    Property city = properties.get("city");
	    
	    Assert.assertNotNull(city.getSetter());
	}
	
	public static class Point {
		private int x, y;

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}
	}

	public static class Line {
		private Point start;
		private Point end;

		public Point getStart() {
			return start;
		}

		public void setStart(Point start) {
			this.start = start;
		}

		public Point getEnd() {
			return end;
		}

		public void setEnd(Point end) {
			this.end = end;
		}
	}

	public static class LineDTO {
		private int x0, y0, x1, y1;

		public int getX0() {
			return x0;
		}

		public void setX0(int x0) {
			this.x0 = x0;
		}

		public int getY0() {
			return y0;
		}

		public void setY0(int y0) {
			this.y0 = y0;
		}

		public int getX1() {
			return x1;
		}

		public void setX1(int x1) {
			this.x1 = x1;
		}

		public int getY1() {
			return y1;
		}

		public void setY1(int y1) {
			this.y1 = y1;
		}

	}
	
	public static class SpecialCase {
		
		private Boolean checked;
		public BigDecimal totalCost;
		
		
		public Boolean isChecked() {
        	return checked;
        }

		public void setChecked(Boolean checked) {
        	this.checked = checked;
        }
	}
	
	public static class SpecialCaseDto {
		
		private boolean checked;
		private double totalCost;
		
		public boolean isChecked() {
        	return checked;
        }
		public void setChecked(boolean checked) {
        	this.checked = checked;
        }
		public double getTotalCost() {
        	return totalCost;
        }
		public void setTotalCost(double totalCost) {
        	this.totalCost = totalCost;
        }
	}
	
	public static interface Address {
        public String getStreet();
        public String getCity();
        public String getSubnational();
        public String getPostalCode();
        public String getCountry();
    }
	
	public static class PostalAddress implements Address {
	    
	    private String street;
	    private String city;
	    private String subnational;
	    private String postalCode;
	    private String country;
        public String getStreet() {
            return street;
        }
        public void setStreet(String street) {
            this.street = street;
        }
        public String getCity() {
            return city;
        }
        public void setCity(String city) {
            this.city = city;
        }
        public String getSubnational() {
            return subnational;
        }
        public void setSubnational(String subnational) {
            this.subnational = subnational;
        }
        public String getPostalCode() {
            return postalCode;
        }
        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }
        public String getCountry() {
            return country;
        }
        public void setCountry(String country) {
            this.country = country;
        }
	}
	

}
