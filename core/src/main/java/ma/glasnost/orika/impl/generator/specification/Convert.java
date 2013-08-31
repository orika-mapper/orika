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
                return format("(%s != null && %s == %s.%Value())", source, destination, source,  source.type().getPrimitiveType().getName());
            } else if (source.type().isPrimitive()) {
                return format("(%s != null && %s.%Value == %s)", destination, destination, destination.type().getPrimitiveType().getName(), source);
            } else {
                return format("(%s != null && %s.equals(%s))", source, source, destination);
            }
            
        } else {
        
            if (destination.type().isPrimitive()) {
                String wrapper = source.asWrapper();
                String primitive = source.type().getPrimitiveType().getName();
                return format("(%s == ((%s)%s.convert(%s, %s)).%Value())", destination, wrapper, code.usedConverter(source.getConverter()), wrapper, code.usedType(destination), primitive);
            } else if (source.type().isPrimitive()) {
                return format("(%s == %s.convert(%s, %s))", destination.asWrapper(), code.usedConverter(source.getConverter()), source.asWrapper(), code.usedType(destination));
            } else {
                return format("(%s != null && %s.equals(%s.convert(%s, %s)))", source, destination, code.usedConverter(source.getConverter()), source.asWrapper(), code.usedType(destination));
            }
        }
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {

        String assureInstanceExists = destination.isNestedProperty() ? (statement(code.assureInstanceExists(destination, source)) + "\n") : "";
        
        String statement;
        boolean canHandleNulls;
        if (source.getConverter() instanceof CopyByReferenceConverter) {
            if (code.isDebugEnabled()) {
                code.debug("copying " + source.type() + " by reference");
            }
            statement = destination.assignIfPossible(source);
            canHandleNulls = true;
        } else {
            if (code.isDebugEnabled()) {
                code.debug("converting using " + source.getConverter());
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
