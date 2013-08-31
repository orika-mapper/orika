package ma.glasnost.orika.test.converter;

import java.util.Date;

public class FieldLevelConverterClasses {
    
    public static class A {
        private String date;
        
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
    }
    
    // Date format dd/MM/yyyy
    public static class B {
        private Date date;
        
        public Date getDate() {
            return date;
        }
        
        public void setDate(Date date) {
            this.date = date;
        }
    }
    
    // Date format dd-MM-yyyy
    public static class C {
        private Date date;
        
        public Date getDate() {
            return date;
        }
        
        public void setDate(Date date) {
            this.date = date;
        }
    }
}
