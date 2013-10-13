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
 * StringToStringConvertible handles conversion of String to primitive types
 *
 */
public class StringToStringConvertible extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return String.class.equals(fieldMap.getSource().getType().getRawType())
                && (fieldMap.getDestination().getType().isPrimitive() || fieldMap.getDestination().getType().isPrimitiveWrapper());
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return "(" + source.notNull() + " && " + source + ".equals(\"\" + " + destination +"))";
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (code.isDebugEnabled()) {
            code.debugField(fieldMap, "converting from String to " + destination.type());
        }
        
        String value = source.toString();
        if (String.class.equals(source.rawType()) && (Character.class.equals(destination.rawType()) || char.class.equals(destination.rawType()))) {
            value = value + ".charAt(0)";
        }
        if (destination.isPrimitive()) {
            return statement(destination.assign("%s.valueOf(%s)", destination.wrapperTypeName(), value));
        } else {
            String mapNull = shouldMapNulls(fieldMap, code) ? format(" else { %s; }", destination.assignIfPossible("null")): "";
            return statement(format("%s {\n %s; } %s", source.ifNotNull(), destination.assign("%s.valueOf(%s)", destination.typeName(), value), mapNull));
        }
    }
}
