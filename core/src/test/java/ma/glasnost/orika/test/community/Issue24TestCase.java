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

package ma.glasnost.orika.test.community;

import org.junit.Assert;
import org.junit.Test;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.MappingUtil;

/**
 * lookupConcreteDestinationType should return the most specific type, not the first that is assignable.
 * <p>
 * 
 * @see <a href="https://code.google.com/archive/p/orika/issues/24">https://code.google.com/archive/p/orika/</a>
 * @author mattdeboer
 *
 */
public class Issue24TestCase {
    
    @Test
    public void testLookupDestinationGetsMostSpecific() {
        MapperFactory factory = MappingUtil.getMapperFactory();

        factory.registerClassMap(factory.classMap(A.class, BSub.class).byDefault().toClassMap());
        factory.registerClassMap(factory.classMap(A.class, B.class).byDefault().toClassMap());
        factory.registerClassMap(factory.classMap(A.class, BSuper.class).byDefault().toClassMap());
        factory.getMapperFacade();

        Type<?> bsuper = factory.lookupConcreteDestinationType(TypeFactory.valueOf(A.class), TypeFactory.valueOf(BSuper.class), null);
        Assert.assertEquals(BSuper.class, bsuper.getRawType());
        Type<?> b = factory.lookupConcreteDestinationType(TypeFactory.valueOf(A.class), TypeFactory.valueOf(B.class), null);
        Assert.assertEquals(B.class, b.getRawType());
        Type<?> bsub = factory.lookupConcreteDestinationType(TypeFactory.valueOf(A.class), TypeFactory.valueOf(BSub.class), null);
        Assert.assertEquals(BSub.class, bsub.getRawType());
    }

    public static class A {

    }

    public static class BSuper extends B {

    }

    public static class B extends BSub {

    }

    public static class BSub {

    }
}
