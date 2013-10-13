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
import static ma.glasnost.orika.impl.generator.SourceCodeContext.append;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.impl.generator.MultiOccurrenceVariableRef;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Type;

/**
 * ConvertArrayOrCollectionToCollection handles mapping of an Array or
 * Collection to a Collection using a Converter which has been specified at the
 * element level
 * 
 */
public class ConvertArrayOrCollectionToCollection extends AbstractSpecification {
    
    public boolean appliesTo(FieldMap fieldMap) {
        if (fieldMap.getConverterId() != null) {
            Converter<Object, Object> converter = mapperFactory.getConverterFactory().getConverter(fieldMap.getConverterId());
            
            /*
             * We can only apply this condition if a converter was specified
             * explicitly, and it is not compatible with the collection and/or
             * array types themselves.
             */
            if (fieldMap.getDestination().isCollection() && (fieldMap.getSource().isArray() || fieldMap.getSource().isCollection())) {
                Type<?> sourceElementType = fieldMap.getSource().isArray() ? fieldMap.getAType().getComponentType() : fieldMap.getAType()
                        .getNestedType(0);
                Type<?> destElementType = fieldMap.getBType().getNestedType(0);
                if (converter.getAType().isAssignableFrom(sourceElementType)
                        && converter.getBType().isAssignableFrom(destElementType)
                        && (!converter.getAType().isAssignableFrom(fieldMap.getAType()) || !converter.getBType().isAssignableFrom(
                                fieldMap.getBType()))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (code.isDebugEnabled()) {
            code.debugField(fieldMap, "converting to Collection using " + source.getConverter());
        }
        
        StringBuilder convertCollection = new StringBuilder();
        MultiOccurrenceVariableRef moSource = MultiOccurrenceVariableRef.from(source);
        MultiOccurrenceVariableRef moDest = MultiOccurrenceVariableRef.from(destination);
        
        String assureInstanceExists = format("if((%s)) { \n %s; \n}", destination.isNull(),
                destination.assign(code.newObject(source, destination.type())));
        
        append(convertCollection,
                moSource.declareIterator(),
                "while(" + moSource.iteratorHasNext() + ") {",
                format("%s.add(%s.convert((%s)%s, %s))", moDest, code.usedConverter(source.getConverter()), moSource.elementTypeName(),
                        moSource.nextElement(), code.usedType(destination)), "}");
        
        String mapNull = shouldMapNulls(fieldMap, code) ? format(" else { %s; }", destination.assignIfPossible("null")) : "";
        return format(" %s { %s; %s; } %s", source.ifNotNull(), assureInstanceExists, convertCollection, mapNull);
    }
    
}
