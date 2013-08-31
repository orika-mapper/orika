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
package ma.glasnost.orika.impl;

import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.AggregateSpecification;
import ma.glasnost.orika.impl.generator.CodeGenerationStrategy;
import ma.glasnost.orika.impl.generator.Specification;
import ma.glasnost.orika.impl.generator.specification.AnyTypeToString;
import ma.glasnost.orika.impl.generator.specification.ApplyRegisteredMapper;
import ma.glasnost.orika.impl.generator.specification.ArrayOrCollectionToArray;
import ma.glasnost.orika.impl.generator.specification.ArrayOrCollectionToCollection;
import ma.glasnost.orika.impl.generator.specification.ArrayOrCollectionToMap;
import ma.glasnost.orika.impl.generator.specification.Convert;
import ma.glasnost.orika.impl.generator.specification.ConvertArrayOrCollectionToArray;
import ma.glasnost.orika.impl.generator.specification.ConvertArrayOrCollectionToCollection;
import ma.glasnost.orika.impl.generator.specification.CopyByReference;
import ma.glasnost.orika.impl.generator.specification.EnumToEnum;
import ma.glasnost.orika.impl.generator.specification.MapToArray;
import ma.glasnost.orika.impl.generator.specification.MapToCollection;
import ma.glasnost.orika.impl.generator.specification.MapToMap;
import ma.glasnost.orika.impl.generator.specification.MultiOccurrenceElementToObject;
import ma.glasnost.orika.impl.generator.specification.MultiOccurrenceToMultiOccurrence;
import ma.glasnost.orika.impl.generator.specification.ObjectToMultiOccurrenceElement;
import ma.glasnost.orika.impl.generator.specification.ObjectToObject;
import ma.glasnost.orika.impl.generator.specification.PrimitiveAndObject;
import ma.glasnost.orika.impl.generator.specification.StringToEnum;
import ma.glasnost.orika.impl.generator.specification.StringToStringConvertible;
import ma.glasnost.orika.impl.generator.specification.UnmappableEnum;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class DefaultCodeGenerationStrategy implements CodeGenerationStrategy {

    private final ArrayList<Specification> specifications;
    private final ArrayList<AggregateSpecification> aggregateSpecifications;
    
    DefaultCodeGenerationStrategy() {
        
        this.specifications = new ArrayList<Specification>();
        
        specifications.add(new ConvertArrayOrCollectionToArray());
        specifications.add(new ConvertArrayOrCollectionToCollection());
        specifications.add(new Convert());
        specifications.add(new CopyByReference());
        specifications.add(new ApplyRegisteredMapper());
        specifications.add(new EnumToEnum());
        specifications.add(new StringToEnum());
        specifications.add(new UnmappableEnum());
        specifications.add(new ArrayOrCollectionToArray());
        specifications.add(new ArrayOrCollectionToCollection());
        specifications.add(new MapToMap());
        specifications.add(new MapToArray());
        specifications.add(new MapToCollection());
        specifications.add(new ArrayOrCollectionToMap());
        specifications.add(new StringToStringConvertible());
        specifications.add(new AnyTypeToString());
        specifications.add(new MultiOccurrenceElementToObject());
        specifications.add(new ObjectToMultiOccurrenceElement());
        specifications.add(new PrimitiveAndObject());
        specifications.add(new ObjectToObject());
        
        this.aggregateSpecifications = new ArrayList<AggregateSpecification>();
        
        aggregateSpecifications.add(new MultiOccurrenceToMultiOccurrence());
        
    }
    
    public void setMapperFactory(MapperFactory mapperFactory) {
        for (Specification spec: this.specifications) {
            spec.setMapperFactory(mapperFactory);
        }
        for (AggregateSpecification spec: this.aggregateSpecifications) {
            spec.setMapperFactory(mapperFactory);
        }
    }
    
    /* (non-Javadoc)
     * @see ma.glasnost.orika.impl.generator.CodeGenerationStrategy#addSpecification(ma.glasnost.orika.impl.generator.Specification, ma.glasnost.orika.impl.generator.CodeGenerationStrategy.Position, ma.glasnost.orika.impl.generator.Specification)
     */
    public void addSpecification(Specification spec, Position relativePosition, Class<Specification> relativeSpec) {
        addSpec(this.specifications, spec, relativePosition, relativeSpec);
    }

    private static <T> void addSpec(List<T> specifications, T spec, Position relativePosition, Class<T> relativeSpec) {
        
        if (relativePosition == null || relativePosition == Position.LAST) {
            specifications.add(spec);
        } else if (relativePosition == Position.FIRST) {
            specifications.add(0, spec);
        } else {
            for (int i =0, len=specifications.size(); i < len; ++i) { 
                T s = specifications.get(i);
                if (s.getClass().equals(relativeSpec)) {
                    switch(relativePosition) {
                    case IN_PLACE_OF:
                        specifications.remove(i);
                        break;
                    case BEFORE:
                        break;
                    case AFTER:
                        ++i;
                        break;
                    }
                    specifications.add(i, spec);
                    break;
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see ma.glasnost.orika.impl.generator.CodeGenerationStrategy#getSpecifications()
     */
    public List<Specification> getSpecifications() {
        return specifications;
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.impl.generator.CodeGenerationStrategy#addAggregateSpecification(ma.glasnost.orika.impl.generator.AggregateSpecification, ma.glasnost.orika.impl.generator.CodeGenerationStrategy.Position, ma.glasnost.orika.impl.generator.AggregateSpecification)
     */
    public void addAggregateSpecification(AggregateSpecification spec, Position relativePosition, Class<AggregateSpecification> relativeSpec) {
        addSpec(this.aggregateSpecifications, spec, relativePosition, relativeSpec);
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.impl.generator.CodeGenerationStrategy#getAggregateSpecifications()
     */
    public List<AggregateSpecification> getAggregateSpecifications() {
        return aggregateSpecifications;
    }
    
}
