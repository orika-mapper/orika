package ma.glasnost.orika.impl.generator.specification;

import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * AnyTypeToString handles conversion of any non-String type
 * to a String using a toString() or primitive equivalent.
 */
public class AnyTypeToString extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return String.class.equals(fieldMap.getDestination().getType().getRawType());
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return "(\"\" + " + source + ").equals(" + destination +")";
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (source.isPrimitive()) {
            if (code.isDebugEnabled()) {
                code.debug("converting primitive to String");
            }
            
            return statement(destination.assign("\"\"+ %s", source));
        } else {
            if (code.isDebugEnabled()) {
                code.debug("converting " + source.typeName() + " using toString()");
            }
            
            if (shouldMapNulls(fieldMap, code)) {
                return statement("if (" + source.notNull() + ") {" + statement(destination.assign("%s.toString()", source)) + "} else {" + statement(destination.assign("null")) + "}");
            } else {
                return statement(source.ifNotNull() + destination.assign("%s.toString()", source));
            }
        }
    }
}
