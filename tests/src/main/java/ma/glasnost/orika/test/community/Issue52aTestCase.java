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

import junit.framework.Assert;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class Issue52aTestCase {
    
    @Test
    public void test() {

        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        factory.classMap(Parent.class, ParentDto.class).byDefault().register();
        factory.classMap(Child1.class, ChildDto1.class).byDefault().register();
        factory.classMap(Child2.class, ChildDto2.class).byDefault().register();
        factory.classMap(Child3.class, ChildDto3.class).byDefault().register();
        factory.classMap(ChildDto11.class, Child11.class).byDefault().register();
        factory.classMap(ChildDto12.class, Child12.class).byDefault().register();
        factory.classMap(ChildDto111.class, Child111.class).byDefault().register();

        ChildDto11 dto11 = factory.getMapperFacade().map(new Child11(), ChildDto11.class);
        ChildDto12 dto12 = factory.getMapperFacade().map(new Child12(), ChildDto12.class);
        ChildDto111 dto111 = factory.getMapperFacade().map(new Child111(), ChildDto111.class);
        ChildDto3 dto3 = factory.getMapperFacade().map(new Child3(), ChildDto3.class);
        ChildDto2 dto2 = factory.getMapperFacade().map(new Child2(), ChildDto2.class);
        ChildDto1 dto1 = factory.getMapperFacade().map(new Child1(), ChildDto1.class);

        Assert.assertNotNull(dto11);
        Assert.assertNotNull(dto12);
        Assert.assertNotNull(dto111);
        Assert.assertNotNull(dto3);
        Assert.assertNotNull(dto2);
        Assert.assertNotNull(dto1);
    }
   
    
    public static class Parent {

    }
    public static class Child1 extends Parent {

    }
    public static class Child2 extends Parent {

    }
    public static class Child3 extends Parent {

    }
    public static class Child11 extends Child1 {

    }
    public static class Child12 extends Child1 {

    }
    public static class Child111 extends Child11 {

    }


    public static class ParentDto {

    }
    public static class ChildDto1 extends ParentDto {

    }
    public static class ChildDto2 extends ParentDto {

    }
    public static class ChildDto3 extends ParentDto {

    }
    public static class ChildDto11 extends ChildDto1 {

    }
    public static class ChildDto12 extends ChildDto1 {

    }
    public static class ChildDto111 extends ChildDto11 {

    }
}

