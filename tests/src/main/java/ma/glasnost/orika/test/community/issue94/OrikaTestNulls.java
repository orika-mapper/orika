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
package ma.glasnost.orika.test.community.issue94;

import java.util.Date;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class OrikaTestNulls {

	
	
    public static class ContainerA {
        private Date startDate;
        private Date endDate;

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

    }

    public static class ContainerB {
        private Date startDate;

        private InnerContainer inner;

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public InnerContainer getInner() {
            return inner;
        }

        public void setInner(InnerContainer inner) {
            this.inner = inner;
        }

    }

    public static class InnerContainer {
        private Date endDate;

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

    }

    @Test
    public void mapNestedNulls_atGlobalLevel() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().mapNulls(true).build();
        ClassMapBuilder<ContainerA, ContainerB> classMapBuilder = mapperFactory.classMap(ContainerA.class,
                ContainerB.class);
        classMapBuilder.field("startDate", "startDate");
        classMapBuilder.field("endDate", "inner.endDate");
        classMapBuilder.register();

        MapperFacade facade = mapperFactory.getMapperFacade();

        ContainerA a = new ContainerA();
        ContainerB b = new ContainerB();
        b.setStartDate(new Date());
        
        InnerContainer c = new InnerContainer();
        c.setEndDate(new Date());
        
        b.setInner(c);

        facade.map(a, b);

        Assert.assertNull("StartDate is not null", b.getStartDate());
        Assert.assertNull("EndDate is not null", b.getInner().getEndDate());
    }
    
    @Test
    public void mapNestedNulls_atClassMapLevel() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        ClassMapBuilder<ContainerA, ContainerB> classMapBuilder = mapperFactory.classMap(ContainerA.class,
                ContainerB.class);
        classMapBuilder.mapNulls(true);
        classMapBuilder.field("startDate", "startDate");
        classMapBuilder.field("endDate", "inner.endDate");
        classMapBuilder.register();

        MapperFacade facade = mapperFactory.getMapperFacade();

        ContainerA a = new ContainerA();
        ContainerB b = new ContainerB();
        b.setStartDate(new Date());
        
        InnerContainer c = new InnerContainer();
        c.setEndDate(new Date());
        
        b.setInner(c);

        facade.map(a, b);

        Assert.assertNull("StartDate is not null", b.getStartDate());
        Assert.assertNull("EndDate is not null", b.getInner().getEndDate());
    }

    @Test
    public void mapNestedNulls_atFieldLevel() {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory(true);
        ClassMapBuilder<ContainerA, ContainerB> classMapBuilder = mapperFactory.classMap(ContainerA.class,
                ContainerB.class);
        classMapBuilder.fieldMap("startDate", "startDate").mapNulls(true).add();
        classMapBuilder.fieldMap("endDate", "inner.endDate").mapNulls(true).add();
        classMapBuilder.register();

        MapperFacade facade = mapperFactory.getMapperFacade();

        ContainerA a = new ContainerA();
        ContainerB b = new ContainerB();
        b.setStartDate(new Date());
        
        InnerContainer c = new InnerContainer();
        c.setEndDate(new Date());
        
        b.setInner(c);

        Assert.assertNull(a.getStartDate());
        Assert.assertNull(a.getEndDate());
        facade.map(a, b);

        Assert.assertNull("StartDate is not null", b.getStartDate());
        Assert.assertNull("EndDate is not null", b.getInner().getEndDate());
    }

}
