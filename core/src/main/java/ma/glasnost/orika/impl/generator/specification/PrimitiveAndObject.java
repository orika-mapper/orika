package ma.glasnost.orika.impl.generator.specification;

import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * @author mattdeboer
 *
 */
public class PrimitiveAndObject extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return (fieldMap.getSource().getType().isPrimitive() 
                && !TypeFactory.TYPE_OF_OBJECT.equals(fieldMap.getDestination().getType()))
                    || fieldMap.getDestination().getType().isPrimitive();
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return source + " == " + destination;
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        throw new MappingException("Encountered mapping of primitive to object (or vise-versa); sourceType="+
                source.type() + ", destinationType=" + destination.type());
    }
    
}
