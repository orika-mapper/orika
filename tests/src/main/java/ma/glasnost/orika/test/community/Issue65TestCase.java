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
package ma.glasnost.orika.test.community;

import java.util.Date;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class Issue65TestCase {
    
    public static class CustomException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
    
    public static class DomainObject {
        
        private Long value;
        private Date date;
        /**
         * @return the value
         */
        public Long getValue() {
            return value;
        }
        /**
         * @param value the value to set
         */
        public void setValue(Long value) {
            if (value == null || value <= 0) {
                throw new CustomException();
            }
            this.value = value;
        }
        /**
         * @return the date
         */
        public Date getDate() {
            return date;
        }
        /**
         * @param date the date to set
         */
        public void setDate(Date date) {
            this.date = date;
        }
        
    }
    
    public static class DomainObjectDto {
        
        private Long value;
        private Date date;
        /**
         * @return the value
         */
        public Long getValue() {
            return value;
        }
        /**
         * @param value the value to set
         */
        public void setValue(Long value) {
            this.value = value;
        }
        /**
         * @return the date
         */
        public Date getDate() {
            return date;
        }
        /**
         * @param date the date to set
         */
        public void setDate(Date date) {
            this.date = date;
        }
    }
    
    
    @Test(expected=CustomException.class)
    public void throwExceptions() {
        
        MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        MapperFacade mapper = mapperFactory.getMapperFacade();
        
        DomainObjectDto dto = new DomainObjectDto();
        dto.setDate(new Date());
        dto.setValue(-2L);
        
        mapper.map(dto, DomainObject.class); 
    }
}
