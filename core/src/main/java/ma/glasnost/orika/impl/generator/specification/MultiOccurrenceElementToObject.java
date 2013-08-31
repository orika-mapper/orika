package ma.glasnost.orika.impl.generator.specification;


import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * MultiOccurrenceElementToObject handles the case where a multi-occurrence element
 * is of type Object.
 *
 */
public class MultiOccurrenceElementToObject extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return (fieldMap.getSource().isMapKey() || fieldMap.getSource().isArrayElement() || fieldMap.getSource().isListElement())
                && (TypeFactory.TYPE_OF_OBJECT.equals(fieldMap.getSource().getType()));
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return source + " == " + destination;
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (code.isDebugEnabled()) {
            code.debug("mapping multi-occurrence element of type Object to object");
        }
        
        return statement(destination.assign(destination.cast(source)));
    }
}
