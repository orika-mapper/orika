package ma.glasnost.orika.test.community;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;

import org.junit.Test;

public class Issue98TestCase {
    
    @Test
    public void testMixedPaymentListMapping() throws Exception {
        CardPayment card = new CardPayment();
        card.setAmount(BigDecimal.TEN);
        card.setAuthorization("1234");
        
        CashPayment cash = new CashPayment();
        cash.setAmount(BigDecimal.ONE);
        
        List<Payment> list = new ArrayList<Payment>();
        list.add(card);
        list.add(cash);
        List<PaymentDTO> dtos = new CustomMapper().mapAsList(list, PaymentDTO.class);
        
        int numCardPayments = 0;
        int numCashPayments = 0;
        for (PaymentDTO paymentDTO : dtos) {
            if (paymentDTO instanceof CardPaymentDTO)
                numCardPayments++;
            else if (paymentDTO instanceof CashPaymentDTO)
                numCashPayments++;
        }
        
        assertEquals(1, numCardPayments);
        assertEquals(1, numCashPayments);
        
    }
    
    @Test
    public void testUnmixedPaymentListMapping() throws Exception {
        CardPayment card = new CardPayment();
        card.setAmount(BigDecimal.TEN);
        card.setAuthorization("1234");
        
        CardPayment card2 = new CardPayment();
        card2.setAmount(BigDecimal.ONE);
        card2.setAuthorization("4321");
        
        List<Payment> list = new ArrayList<Payment>();
        list.add(card);
        list.add(card2);
        List<PaymentDTO> dtos = new CustomMapper().mapAsList(list, PaymentDTO.class);
        
        int numCardPayments = 0;
        int numCashPayments = 0;
        for (PaymentDTO paymentDTO : dtos) {
            if (paymentDTO instanceof CardPaymentDTO)
                numCardPayments++;
            else if (paymentDTO instanceof CashPaymentDTO)
                numCashPayments++;
        }
        
        assertEquals(2, numCardPayments);
        assertEquals(0, numCashPayments);
        
    }
    
    public static abstract class Payment implements Serializable {
        /**
                     * 
                     */
        private static final long serialVersionUID = 1L;
        private BigDecimal amount;
        
        public BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
    
    public static class CardPayment extends Payment {
        /**
                     * 
                     */
        private static final long serialVersionUID = 1L;
        private String authorization;
        
        public String getAuthorization() {
            return authorization;
        }
        
        public void setAuthorization(String authorization) {
            this.authorization = authorization;
        }
    }
    
    public static class CashPayment extends Payment {
        /**
                     * 
                     */
        private static final long serialVersionUID = 1L;
        private String description;
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
    }
    
    public static abstract class PaymentDTO implements Serializable {
        /**
                     * 
                     */
        private static final long serialVersionUID = 1L;
        private BigDecimal amount;
        
        public BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
    
    public static class CardPaymentDTO extends PaymentDTO {
        /**
                     * 
                     */
        private static final long serialVersionUID = 1L;
        private String authorization;
        
        public String getAuthorization() {
            return authorization;
        }
        
        public void setAuthorization(String authorization) {
            this.authorization = authorization;
        }
    }
    
    public static class CashPaymentDTO extends PaymentDTO {
        /**
                     * 
                     */
        private static final long serialVersionUID = 1L;
        private String description;
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
    }
    
    public class CustomMapper extends ConfigurableMapper {
        
        @Override
        protected void configure(MapperFactory factory) {
            
            super.configure(factory);
            
            factory.classMap(PaymentDTO.class, Payment.class).byDefault().register();
            
            factory.classMap(CashPaymentDTO.class, CashPayment.class).use(PaymentDTO.class, Payment.class).byDefault().register();
            
            factory.classMap(CardPaymentDTO.class, CardPayment.class).use(PaymentDTO.class, Payment.class).byDefault().register();
            
        }
    }
}
