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
            code.debug("converting String to enum " + destination.type());
        }
        
        String assignEnum = destination.assign("Enum.valueOf(%s.class, \"\"+%s)", destination.typeName(), source);
        String mapNull = shouldMapNulls(fieldMap, code) ? format(" else {\n %s;\n}", destination.assignIfPossible("null")): "";
        return statement("%s { %s; } %s", source.ifNotNull(), assignEnum, mapNull);
    }
}
