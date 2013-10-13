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
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * EnumToEnum handles conversion of one enumeration to another
 */
public class EnumToEnum extends AbstractSpecification {
    
    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getBType().isEnum() && fieldMap.getAType().isEnum();
    }
    
    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return "(Enum.valueOf(%s.class, %s.name()) == " + destination + ")";
    }
    
    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (code.isDebugEnabled()) {
            code.debugField(fieldMap, "converting enum " + source.typeName() + " to enum " + destination.typeName());
        }
        
        String assignEnum = destination.assign("Enum.valueOf(%s.class, %s.name())", destination.typeName(), source);
        String mapNull = shouldMapNulls(fieldMap, code) ? format(" else {\n %s;\n}", destination.assignIfPossible("null")): "";
        return statement("%s { %s; } %s", source.ifNotNull(), assignEnum, mapNull);
    }
}


