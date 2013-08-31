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

package ma.glasnost.orika.test.fieldmap;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class NestedExpressionTestCase {
    
    @Test
    public void testNestedProperty() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        factory.registerClassMap(ClassMapBuilder.map(Order.class, OrderDTO.class).field("product.state.type.label", "stateLabel")
                .field("product.name", "productName").toClassMap());
        
        StateType type = new StateType();
        type.setLabel("Open");
        
        State state = new State();
        state.setType(type);
        
        Product product = new Product();
        product.setState(state);
        product.setName("Glasnost Platform");
        
        Order order = new Order();
        order.setProduct(product);
        
        OrderDTO dto = factory.getMapperFacade().map(order, OrderDTO.class);
        
        Assert.assertEquals("Open", dto.getStateLabel());
        
        Order object = factory.getMapperFacade().map(dto, Order.class);
        
        Assert.assertEquals("Open", object.getProduct().getState().getType().getLabel());
        
    }
    
    public static class StateType {
        private String label;
        
        public String getLabel() {
            return label;
        }
        
        public void setLabel(String label) {
            this.label = label;
        }
        
    }
    
    public static class State {
        private StateType type;
        
        public StateType getType() {
            return type;
        }
        
        public void setType(StateType type) {
            this.type = type;
        }
        
    }
    
    public static class Product {
        private State state;
        private String name;
        
        public State getState() {
            return state;
        }
        
        public void setState(State state) {
            this.state = state;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    public static class Order {
        private Product product;
        
        public Product getProduct() {
            return product;
        }
        
        public void setProduct(Product product) {
            this.product = product;
        }
    }
    
    public static class OrderDTO {
        private String stateLabel;
        private String productName;
        
        public String getStateLabel() {
            return stateLabel;
        }
        
        public void setStateLabel(String stateLabel) {
            this.stateLabel = stateLabel;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
        
    }
    
}
