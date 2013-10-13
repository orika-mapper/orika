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

import static ma.glasnost.orika.impl.generator.SourceCodeContext.append;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * ObjectToMultiOccurrenceElement handles the case where the destination is
 * a multi-occurrence object of type Object
 *
 */
public class ObjectToMultiOccurrenceElement extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        
        return (fieldMap.getDestination().isMapKey() || fieldMap.getDestination().isArrayElement() || fieldMap.getDestination().isListElement())
                && (TypeFactory.TYPE_OF_OBJECT.equals(fieldMap.getDestination().getType()));
        
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (code.isDebugEnabled()) {
            code.debugField(fieldMap, "mapping object to multi-occurrence element of type Object");
        }
        
        StringBuilder out = new StringBuilder();
        if (!source.isPrimitive()) {
            out.append(source.ifNotNull() + "{");
        }
        out.append(statement(destination.assign(source)));
        if (!source.isPrimitive()) {
            out.append("}");
            if (shouldMapNulls(fieldMap, code) && !destination.isPrimitive()) {
                append(out, 
                        " else {\n",
                        destination.assignIfPossible("null"),
                        "}\n");
            }
        }
        return out.toString();
    }

}
