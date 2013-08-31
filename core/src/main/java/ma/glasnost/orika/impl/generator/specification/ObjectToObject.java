package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.impl.generator.MultiOccurrenceVariableRef;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * ObjectToObject
 *
 */
public class ObjectToObject extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return true;
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.impl.generator.specification.AbstractSpecification#generateEqualityTestCode(ma.glasnost.orika.metadata.FieldMap, ma.glasnost.orika.impl.generator.VariableRef, ma.glasnost.orika.impl.generator.VariableRef, ma.glasnost.orika.impl.generator.SourceCodeContext)
     */
    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return "";
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (code.isDebugEnabled()) {
            code.debug("mapping object to object");
        }
        
        String mapNewObject = destination.assign(format("(%s)%s", destination.typeName(), code.callMapper(source, destination.type()), source));
        String mapExistingObject = destination.assign(format("(%s)%s", destination.typeName(), code.callMapper(source, destination)));
        String mapStmt = format(" %s { %s; } else { %s; }", destination.ifNull(), mapNewObject, mapExistingObject);
        
        String ipStmt = "";
        if (fieldMap.getInverse() != null) {
            VariableRef inverse = new VariableRef(fieldMap.getInverse(), destination);
            
            if (inverse.isCollection()) {
                MultiOccurrenceVariableRef inverseCollection = MultiOccurrenceVariableRef.from(inverse);
                ipStmt += inverse.ifNull() + inverse.assign(inverseCollection.newCollection()) + ";";
                ipStmt += format("%s.add(%s);", inverse, destination.owner());
            } else if (inverse.isArray()) {
                ipStmt += "/* TODO Orika source code does not support Arrays */";
            } else {
                ipStmt += statement(inverse.assign(destination.owner()));
            }
        }
        
        String mapNull = shouldMapNulls(fieldMap, code) ? format(" else {\n %s;\n}\n", destination.assign("null")): "";
        return statement("%s { %s;  %s } %s", source.ifNotNull(), mapStmt, ipStmt, mapNull);
        
    }
    
}
