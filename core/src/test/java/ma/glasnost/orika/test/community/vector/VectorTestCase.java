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

package ma.glasnost.orika.test.community.vector;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class VectorTestCase {
    private final MapperFactory factory = new DefaultMapperFactory.Builder().build();
    
    @Test
    public void test() {
        
        // Property.Builder builder = new Property.Builder();
        // builder.merge(UtilityResolver.getDefaultPropertyResolverStrategy().getProperty(Trade.class,
        // "fees"));
        // Property fees = builder.elementType(TypeFactory.valueOf()).build();
        
        factory.classMap(XTrade.class, Trade.class).fieldMap("fees").bElementType(Fee.class).add().byDefault().register();
        
        XTrade xtrade = new XTrade();
        XFee xfee = new XFee();
        xfee.amount = BigDecimal.valueOf(34.95);
        xfee.description = "ATM Fee";
        xtrade.fees.add(xfee);
        xfee = new XFee();
        xfee.amount = BigDecimal.valueOf(250.00);
        xfee.description = "Cable Bill";
        xtrade.fees.add(xfee);
        
        Trade trade = factory.getMapperFacade().map(xtrade, Trade.class);
        Assert.assertEquals(2, trade.fees.size());
        Assert.assertEquals(xtrade.fees.get(0).amount, ((Fee) trade.fees.get(0)).amount);
        Assert.assertEquals(xtrade.fees.get(0).description, ((Fee) trade.fees.get(0)).description);
        Assert.assertEquals(xtrade.fees.get(1).amount, ((Fee) trade.fees.get(1)).amount);
        Assert.assertEquals(xtrade.fees.get(1).description, ((Fee) trade.fees.get(1)).description);
    }
    
    public static class XTrade {
        public List<XFee> fees = new ArrayList<>();
    }
    
    public static class XFee {
        public BigDecimal amount;
        public String description;
    }
    
    public static class Trade {
        public Vector fees = new Vector();
    }
    
    public static class Fee {
        public BigDecimal amount;
        public String description;
    }
}
