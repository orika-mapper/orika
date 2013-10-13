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

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;

/**
 * ApplyRegisteredMapper looks for a registered mapper which supports
 * mapping the provided elements.
 */
public class ApplyRegisteredMapper extends ObjectToObject {

    public boolean appliesTo(FieldMap fieldMap) {
        return mapperFactory.existsRegisteredMapper(fieldMap.getAType(), fieldMap.getBType(), false);
    }
    
    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (code.isDebugEnabled()) {
            Mapper<Object, Object> mapper = mapperFactory.lookupMapper(
                    new MapperKey(source.type(), destination.type()));
            Type<?> sourceType;
            Type<?> destType;
            if (mapper.getAType().isAssignableFrom(source.type())) {
                sourceType = mapper.getAType();
                destType = mapper.getBType();
            } else {
                sourceType = mapper.getBType();
                destType = mapper.getAType();
            }
            code.debugField(fieldMap, "mapping using registered Mapper<" + sourceType + "," +
                    destType + ">");
        }
        
        return super.generateMappingCode(fieldMap, source, destination, code);
    }
}
