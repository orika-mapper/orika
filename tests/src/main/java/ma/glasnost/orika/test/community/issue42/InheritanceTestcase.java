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

package ma.glasnost.orika.test.community.issue42;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import org.junit.Assert;
import org.junit.Test;

public class InheritanceTestcase {
    
    @Test
    public void testInheritance() {
        
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        
        mapperFactory.registerClassMap(mapperFactory.classMap(BaseEntity.class, BaseEntityDto.class).byDefault().toClassMap());
        
        ClassMapBuilder<BaseUser, BaseUserDto> userClassMapBuilder = mapperFactory.classMap(BaseUser.class, BaseUserDto.class);
        userClassMapBuilder.customize(new CustomMapper<BaseUser, BaseUserDto>() {
            @Override
            public void mapAtoB(BaseUser a, BaseUserDto b, MappingContext mappingContext) {
                b.setName(a.getName() + " [mapped from BaseUser to BaseUserDto]");
            }
            
            @Override
            public void mapBtoA(BaseUserDto b, BaseUser a, MappingContext mappingContext) {
                a.setName(b.getName() + " [mapped from BaseUserDto to BaseUser]");
            }
        });
        mapperFactory.registerClassMap(userClassMapBuilder.use(BaseEntity.class, BaseEntityDto.class).byDefault().toClassMap());
        
        mapperFactory.registerClassMap(mapperFactory.classMap(Customer.class, CustomerDto.class)
                .use(BaseUser.class, BaseUserDto.class)
                .byDefault()
                .toClassMap());
                
        MapperFacade mapperFacade = mapperFactory.getMapperFacade();
        
        Customer customer = new Customer();
        customer.setId(1234L);
        customer.setName("Customer Name");
        customer.setEmail("test@test.org");
        
        CustomerDto customerDto = mapperFacade.map(customer, CustomerDto.class);
        
        Customer c = mapperFacade.map(customerDto, Customer.class);
        
        Assert.assertEquals(customer.getEmail(), c.getEmail());
        Assert.assertEquals(customer.getId(), c.getId());
        
    }
    
    public static class BaseEntity {
        protected Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
    }
    
    public static class BaseUser extends BaseEntity {
        protected String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class Customer extends BaseUser {
        protected String email;
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    public static class BaseEntityDto {
        protected Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
    }
    
    public static class BaseUserDto extends BaseEntityDto {
        protected String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class CustomerDto extends BaseUserDto {
        protected String email;
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
}
