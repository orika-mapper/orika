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
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * CopyByReference handles mapping of immutable types by reference
 */
public class CopyByReference extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return ClassUtil.isImmutable(fieldMap.getSource().getType())
                && fieldMap.getDestination().isAssignableFrom(fieldMap.getSource());
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        if (source.type().isPrimitive() || source.type().isPrimitiveWrapper()) {
            return source + " == " + destination;
        } else {
            return source + ".equals(" + destination + ")";
        }
        
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (code.isDebugEnabled()) {
            code.debugField(fieldMap, "copying " + source.elementTypeName() + " by reference");
        }
        
        StringBuilder out = new StringBuilder();
        if (!source.isPrimitive()) {
            out.append(source.ifNotNull() + "{");
        }
        out.append(statement(destination.assign(source)));
        if (!source.isPrimitive()) {
            out.append("\n }");
            if (shouldMapNulls(fieldMap, code) && !destination.isPrimitive()) {
                append(out, 
                        " else {",
                        destination.assignIfPossible("null"),
                        "\n }");
            }
        }
        return out.toString();
    }
    
}
