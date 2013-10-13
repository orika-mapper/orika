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

import static java.lang.String.format;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * ArrayOrCollectionToArray handles mapping of an Array or Collection to
 * an Array
 */
public class ArrayOrCollectionToArray extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getDestination().isArray() && (fieldMap.getSource().isArray() || fieldMap.getSource().isCollection());
    }


    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        final VariableRef arrayVar = destination.elementRef(destination.name()+"Array__");
        String newArray = format("%s[] %s = new %s[%s]", destination.elementTypeName(), arrayVar.name(), destination.elementTypeName(), source.size());
        
        String mapArray;
        if (destination.elementType().isPrimitive()) {
            if (code.isDebugEnabled()) {
                code.debugField(fieldMap, "mapping to primitive array");
            }
            mapArray = format("mapArray(%s, asList(%s), %s.class, mappingContext)", arrayVar.name(), source, arrayVar.typeName());
        } else {
            if (code.isDebugEnabled()) {
                code.debugField(fieldMap, "mapping to array");
            }
            mapArray = format("mapperFacade.mapAsArray(%s, asList(%s), %s, %s, mappingContext)", arrayVar.name(), source, code.usedType(source.elementType()),
                    code.usedType(destination.elementType()));
        }
        String mapNull = shouldMapNulls(fieldMap, code) ? format(" else { %s; }", destination.assignIfPossible("null")) : "";
        return format(" %s { %s; %s; %s; } %s", source.ifNotNull(), newArray, mapArray, destination.assign(arrayVar), mapNull);
    }
    
}
