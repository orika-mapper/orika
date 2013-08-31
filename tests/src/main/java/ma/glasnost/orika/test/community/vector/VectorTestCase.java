package ma.glasnost.orika.test.community.vector;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;

import org.junit.Test;

public class VectorTestCase {
    private MapperFactory factory = new DefaultMapperFactory.Builder().compilerStrategy(new EclipseJdtCompilerStrategy()).build();
    
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
        public List<XFee> fees = new ArrayList<XFee>();
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
