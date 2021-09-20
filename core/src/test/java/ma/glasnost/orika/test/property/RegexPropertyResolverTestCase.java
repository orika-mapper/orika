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
package ma.glasnost.orika.test.property;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.property.RegexPropertyResolver;
import ma.glasnost.orika.test.property.TestCaseClasses.A;
import ma.glasnost.orika.test.property.TestCaseClasses.Address;
import ma.glasnost.orika.test.property.TestCaseClasses.B;
import ma.glasnost.orika.test.property.TestCaseClasses.C;
import ma.glasnost.orika.test.property.TestCaseClasses.D;
import ma.glasnost.orika.test.property.TestCaseClasses.Name;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 */
public class RegexPropertyResolverTestCase {

    @Test
    public void testRegexResolution() {

        MapperFactory factory =
                new DefaultMapperFactory.Builder()
                        .propertyResolverStrategy(
                                new RegexPropertyResolver(
                                        "readThe([\\w]+)ForThisBean",
                                        "assignThe([\\w]+)",
                                        true, true))
                        .build();
        factory.registerClassMap(
                factory.classMap(A.class, B.class)
                        .field("name.firstName", "givenName")
                        .field("name.lastName", "sirName")
                        .field("address.city", "city")
                        .field("address.street", "street")
                        .field("address.postalCode", "postalCode")
                        .field("address.country", "country")
        );


        MapperFacade mapper = factory.getMapperFacade();

        A a = new A();
        Name name = new Name();
        name.setFirstName("Albert");
        name.setLastName("Einstein");
        a.assignTheName(name);
        Address address = new Address();
        address.city = "Somewhere";
        address.country = "Germany";
        address.postalCode = "A1234FG";
        address.street = "1234 Easy St.";
        a.setAddress(address);


        B b = mapper.map(a, B.class);

        Assert.assertNotNull(b);

        A mapBack = mapper.map(b, A.class);

        Assert.assertEquals(a, mapBack);

    }

    @Test
    public void testRegexResolutionWithSpecifiedCaptureGroupIndex() throws Exception {
        MapperFactory factory =
                new DefaultMapperFactory.Builder()
                        .propertyResolverStrategy(
                                new RegexPropertyResolver(
                                        "(get)(\\w+)", //arbitrarily make the getter regex have two capture groups
                                        "(set|with)(\\w+)", //C.Builder has withXXX methods and D.Builder has setXXX methods
                                        true, true, 2, 2)) //our write method regex has two capture groups, index=2 captures the property name
                        .build();

        factory.registerClassMap(
                factory.classMap(TestCaseClasses.C.class, D.Builder.class)
                        .byDefault());

        factory.registerClassMap(
                factory.classMap(TestCaseClasses.D.class, C.Builder.class)
                        .byDefault());


        MapperFacade mapper = factory.getMapperFacade();

        C c = new C.Builder()
                .withFoo("hello")
                .withBar(33)
                .build();

        D d = mapper.map(c, D.Builder.class).build();

        C c1 = mapper.map(d, C.Builder.class).build();

        Assert.assertEquals(c, c1);
    }
}
