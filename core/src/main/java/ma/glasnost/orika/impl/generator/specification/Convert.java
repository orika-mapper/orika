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
import ma.glasnost.orika.converter.builtin.CopyByReferenceConverter;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * Convert applies the conversion operation between two properties.
 * There is a special shortcut case applied when the converter is a
 * CopyByReferenceConverter -- we applied the code to assign the reference
 * directly rather than making an extra method call.
 * 
 * @author mattdeboer
 *
 */
public class Convert extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getConverterId() != null || mapperFactory.getConverterFactory().canConvert(fieldMap.getAType(), fieldMap.getBType());
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (source.getConverter() instanceof CopyByReferenceConverter) {
            /*
             * We apply a shortcut here if we know it's an immutable conversion --
             * no reason to pass it through an extra function call just to return
             * back the original item again.
             */
            if (destination.type().isPrimitive() && source.type().isPrimitive()) {
                return format("(%s == %s)", destination, source);
            } else if (destination.type().isPrimitive()) {
                return format("(%s != null && %s == %s.%sValue())", source, destination, source,  source.type().getName());
            } else if (source.type().isPrimitive()) {
                return format("(%s != null && %s.%sValue() == %s)", destination, destination, destination.type().getPrimitiveType().getName(), source);
            } else {
                return format("(%s != null && %s.equals(%s))", source, source, destination);
            }
            
        } else {
        
            if (destination.type().isPrimitive()) {
                String wrapper = source.asWrapper();
                String wrapperType = destination.type().getWrapperType().getSimpleName();
                String primitive = destination.type().getName();
                return format("(%s == ((%s)%s.convert(%s, %s)).%sValue())", destination, wrapperType, code.usedConverter(source.getConverter()), wrapper, code.usedType(destination), primitive);
            } else if (source.type().isPrimitive()) {
                return format("(%s == %s.convert(%s, %s))", destination.asWrapper(), code.usedConverter(source.getConverter()), source.asWrapper(), code.usedType(destination));
            } else {
                return format("(%s != null && %s.equals(%s.convert(%s, %s)))", destination, destination, code.usedConverter(source.getConverter()), source.asWrapper(), code.usedType(destination));
            }
        }
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {

        String assureInstanceExists = destination.isNestedProperty() ? (statement(code.assureInstanceExists(destination, source)) + "\n") : "";
        
        String statement;
        boolean canHandleNulls;
        if (source.getConverter() instanceof CopyByReferenceConverter) {
            if (code.isDebugEnabled()) {
                code.debugField(fieldMap, "copying " + source.type() + " by reference");
            }
            statement = destination.assignIfPossible(source);
            canHandleNulls = true;
        } else {
            if (code.isDebugEnabled()) {
                code.debugField(fieldMap, "converting using " + source.getConverter());
            }
            statement = destination.assignIfPossible("%s.convert(%s, %s)", code.usedConverter(source.getConverter()), source.asWrapper(), code.usedType(destination));
            canHandleNulls = false;
        }
        
        boolean shouldSetNull = shouldMapNulls(fieldMap, code) && !destination.isPrimitive();
        String destinationNotNull = destination.ifPathNotNull();
        
        if (!source.isNullPossible() || (canHandleNulls && shouldSetNull && "".equals(destinationNotNull))) {
            return statement(statement);
        } else {
            String elseSetNull = shouldSetNull ? (" else "+ destinationNotNull +"{ \n" + destination.assignIfPossible("null")) + ";\n }" : "";
            return statement(source.ifNotNull() + "{ \n" + assureInstanceExists + statement) + "\n}" + elseSetNull;
        }
    }
}
