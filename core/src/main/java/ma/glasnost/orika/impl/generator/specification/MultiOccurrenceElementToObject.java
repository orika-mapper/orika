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


import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * MultiOccurrenceElementToObject handles the case where a multi-occurrence element
 * is of type Object.
 *
 */
public class MultiOccurrenceElementToObject extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return (fieldMap.getSource().isMapKey() || fieldMap.getSource().isArrayElement() || fieldMap.getSource().isListElement())
                && (TypeFactory.TYPE_OF_OBJECT.equals(fieldMap.getSource().getType()));
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (code.isDebugEnabled()) {
            code.debugField(fieldMap, "mapping multi-occurrence element of type Object to object");
        }
        
        return statement(destination.assign(destination.cast(source)));
    }
}
