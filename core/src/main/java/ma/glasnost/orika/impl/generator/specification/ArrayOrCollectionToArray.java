package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * ArrayOrCollectionToArray handles mapping of an Array or Collection to
 * an Array
 */
public class ArrayOrCollectionToArray extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getDestination().isArray() && (fieldMap.getSource().isArray() || fieldMap.getSource().isCollection());
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return "";
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        final VariableRef arrayVar = destination.elementRef(destination.name());
        String newArray = format("%s[] %s = new %s[%s]", destination.elementTypeName(), destination.name(), destination.elementTypeName(), source.size());
        
        String mapArray;
        if (destination.elementType().isPrimitive()) {
            code.mapWithDescription(fieldMap, "mapping to primitive array");
            mapArray = format("mapArray(%s, asList(%s), %s.class, mappingContext)", arrayVar, source, arrayVar.typeName());
        } else {
            code.mapWithDescription(fieldMap, "mapping to array");
            mapArray = format("mapperFacade.mapAsArray(%s, asList(%s), %s, %s, mappingContext)", destination.name(), source, code.usedType(source.elementType()),
                    code.usedType(destination.elementType()));
        }
        String mapNull = shouldMapNulls(fieldMap, code) ? format(" else { %s; }", destination.assignIfPossible("null")) : "";
        return format(" %s { %s; %s; %s; } %s", source.ifNotNull(), newArray, mapArray, destination.assign(arrayVar), mapNull);
    }
    
}
