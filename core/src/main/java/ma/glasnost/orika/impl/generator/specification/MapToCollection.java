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

import static ma.glasnost.orika.impl.generator.SourceCodeContext.entrySetRef;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * MapToCollection handles mapping of Map to Collection
 *
 */
public class MapToCollection extends ArrayOrCollectionToCollection {

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getSource().isMap() && fieldMap.getDestination().isCollection();
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (code.isDebugEnabled()) {
            code.debugField(fieldMap, "mapping from Map<" + source.type().getNestedType(0) + ", " + 
                    source.type().getNestedType(1) + "> to Collection<" + destination.elementTypeName() + ">");
        }
        
        return super.generateMappingCode(fieldMap, entrySetRef(source), destination, code);
    }
    
}
