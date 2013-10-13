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
 * StringToEnum handles the case where the source is a String
 * and the destination is an enumeration.
 *
 */
public class StringToEnum extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getBType().isEnum() && fieldMap.getAType().isString();
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return "(Enum.valueOf(%s.class, \"\"+%s) == " + destination +")";
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (code.isDebugEnabled()) {
            code.debugField(fieldMap, "converting String to enum " + destination.type());
        }
        
        String assignEnum = destination.assign("Enum.valueOf(%s.class, \"\"+%s)", destination.typeName(), source);
        String mapNull = shouldMapNulls(fieldMap, code) ? format(" else {\n %s;\n}", destination.assignIfPossible("null")): "";
        return statement("%s { %s; } %s", source.ifNotNull(), assignEnum, mapNull);
    }
}
