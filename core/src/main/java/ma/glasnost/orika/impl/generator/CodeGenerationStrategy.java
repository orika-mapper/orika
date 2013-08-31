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
package ma.glasnost.orika.impl.generator;

import java.util.List;

import ma.glasnost.orika.MapperFactory;

/**
 * CodeGenerationStrategy is an encapsulation of the collection of individual 
 * field mapping scenarios for which code is generated. It allows reordering,
 * overriding, adding and removing the scenarios individually as desired. 
 * 
 * @author matt.deboer@gmail.com
 *
 */
public interface CodeGenerationStrategy {
    
    /**
     * Position represents the relative position of a specification
     * in the chain of mapping specifications
     */
    enum Position {
        BEFORE,
        AFTER,
        IN_PLACE_OF,
        FIRST,
        LAST
    }
    
    /**
     * Set the MapperFactory on this CodeGenerationStrategy
     * 
     * @param mapperFactory the MapperFactory to apply
     */
    public void setMapperFactory(MapperFactory mapperFactory);
    
    /**
     * Convenience method to add a specification at a relative position with respect to
     * another Specification, or the list in general
     * 
     * @param spec the specification to add
     * @param relativePosition the relative position
     * @param relativeSpec the other relative spec (for Positions BEFORE, AFTER, or IN_PLACE_OF)
     */
    public void addSpecification(Specification spec, Position relativePosition, Class<Specification> relativeSpec);
    
    /**
     * @return the defined specifications 
     */
    public List<Specification> getSpecifications();
    
    /**
     * Convenience method to add an AggregateSpecification at a relative position with respect to
     * another AggregateSpecification, or the list in general
     * 
     * @param spec the specification to add
     * @param relativePosition the relative position
     * @param relativeSpec the other relative spec (for Positions BEFORE, AFTER, or IN_PLACE_OF)
     */
    public void addAggregateSpecification(AggregateSpecification spec, Position relativePosition, Class<AggregateSpecification> relativeSpec);
    
    /**
     * @return the defined aggregate specifications
     */
    public List<AggregateSpecification> getAggregateSpecifications();
}
