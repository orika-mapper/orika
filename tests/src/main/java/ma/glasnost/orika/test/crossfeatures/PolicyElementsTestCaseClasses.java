package ma.glasnost.orika.test.crossfeatures;

import java.util.Set;

import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

public class PolicyElementsTestCaseClasses {
    
    public static class Policy {
        
        private Set<PolicyElement> elements;
        
        public Set<PolicyElement> getElements() {
            return elements;
        }
        
        public void setElements(Set<PolicyElement> elements) {
            this.elements = elements;
        }
        
    }
    
    public static abstract class PolicyElement {
        
    }
    
    public static class CustomerElement extends PolicyElement {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class ProductElement extends PolicyElement {
        
    }
    
    public static class OtherElement extends PolicyElement {
        
    }
    
    public static class OneOtherElement extends PolicyElement {
        
    }
    
    public static class PolicyDTO {
        
        private Set<PolicyElementDTO> elements;
        
        public Set<PolicyElementDTO> getElements() {
            return elements;
        }
        
        public void setElements(Set<PolicyElementDTO> elements) {
            this.elements = elements;
        }
        
    }
    
    public static abstract class PolicyElementDTO {
        
    }
    
    public static class CustomerElementDTO extends PolicyElementDTO {
        
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    public static class ProductElementDTO extends PolicyElementDTO {
        
    }
    
    public static class OtherElementDTO extends PolicyElementDTO {
        
    }
    
    public static class OneOtherElementDTO extends PolicyElementDTO {
        
    }
    
    // Hibernate will generate a proxy for PolicyElement class and not
    // for childs classes
    public static class PolicyElementProxy extends PolicyElement {
        private final PolicyElement target;
        
        public PolicyElementProxy(PolicyElement target) {
            super();
            this.target = target;
        }
        
        public Type<?> getTargetClass() {
            return TypeFactory.valueOf(target.getClass());
        }
        
        public Object getTarget() {
            return target;
        }
    }
    
}
