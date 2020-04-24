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

package ma.glasnost.orika.impl.generator.specification;

import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.TypeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mattdeboer
 *
 */
public class PrimitiveAndObject extends AbstractSpecification {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrimitiveAndObject.class);
    
    public boolean appliesTo(FieldMap fieldMap) {
        return (fieldMap.getSource().getType().isPrimitive() 
                && !TypeFactory.TYPE_OF_OBJECT.equals(fieldMap.getDestination().getType()))
                    || fieldMap.getDestination().getType().isPrimitive();
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return source + " == " + destination;
    }

    @Override
    public String generateMappingCode(FieldMap fieldMap, VariableRef source, String sourceValue, VariableRef destination, SourceCodeContext code) {

        LOGGER.debug("PrimitiveAndObject condition occurred; context: " +
                "\nsrc: " + source.property() +
                "\nsrc.isArrayElement: " + source.property().isArrayElement() +
                "\nsrc.isListElement: " + source.property().isListElement() +
                "\nsrc.isMapKey: " + source.property().isMapKey() +
                "\ndest: " + destination.property() +
                "\ndest.isArrayElement: " + destination.property().isArrayElement() +
                "\ndest.isListElement: " + destination.property().isListElement() +
                "\ndest.isMapKey: " + destination.property().isMapKey()
        );
        throw new MappingException("Encountered mapping of primitive to object (or vise-versa); sourceType="+
                source.type() + ", destinationType=" + destination.type());
    }

}
