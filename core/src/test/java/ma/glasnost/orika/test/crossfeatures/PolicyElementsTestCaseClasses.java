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
