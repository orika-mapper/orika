package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * EnumToEnum handles conversion of one enumeration to another
 */
public class EnumToEnum extends AbstractSpecification {
    
    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getBType().isEnum() && fieldMap.getAType().isEnum();
    }
    
    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return "(Enum.valueOf(%s.class, %s.name()) == " + destination + ")";
    }
    
    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (code.isDebugEnabled()) {
            code.debug("converting enum " + source.typeName() + " to enum " + destination.typeName());
        }
        
        String assignEnum = destination.assign("Enum.valueOf(%s.class, %s.name())", destination.typeName(), source);
        String mapNull = shouldMapNulls(fieldMap, code) ? format(" else {\n %s;\n}", destination.assignIfPossible("null")): "";
        return statement("%s { %s; } %s", source.ifNotNull(), assignEnum, mapNull);
    }
}


