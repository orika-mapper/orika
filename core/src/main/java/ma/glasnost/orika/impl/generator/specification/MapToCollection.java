package ma.glasnost.orika.impl.generator.specification;

import static ma.glasnost.orika.impl.generator.SourceCodeContext.entrySetRef;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * MapToCollection handles mapping of Map to Collection
 *
 */
public class MapToCollection extends ArrayOrCollectionToCollection {

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getSource().isMap() && fieldMap.getDestination().isCollection();
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return source + " == " + destination;
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        if (code.isDebugEnabled()) {
            code.debug("mapping from Map<" + source.type().getNestedType(0) + ", " + 
                    source.type().getNestedType(1) + "> to Collection<" + destination.elementTypeName() + ">");
        }
        
        return super.generateMappingCode(fieldMap, entrySetRef(source), destination, code);
    }
    
}
