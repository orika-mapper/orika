package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.append;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;

import java.util.Map;

import ma.glasnost.orika.impl.generator.MapEntryRef;
import ma.glasnost.orika.impl.generator.MapEntryRef.EntryPart;
import ma.glasnost.orika.impl.generator.MultiOccurrenceVariableRef;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.impl.util.StringUtil;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.FieldMapBuilder;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * MapToMap handles conversion of a Map to another Map
 *
 */
public class MapToMap extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getSource().isMap() && fieldMap.getDestination().isMap();
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return source + " == " + destination;
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        MultiOccurrenceVariableRef d = MultiOccurrenceVariableRef.from(destination);
        MultiOccurrenceVariableRef s = MultiOccurrenceVariableRef.from(source);
        
        if (code.isDebugEnabled()) {
            code.debug("mapping from Map<" + source.type().getNestedType(0) + ", " + 
            source.type().getNestedType(1) + "> to Map<" + destination.type().getNestedType(0) + ", " + 
            destination.type().getNestedType(1) + ">");
        }
        
        StringBuilder out = new StringBuilder();
        
        out.append(s.ifNotNull());
        out.append("{\n");
        
        MultiOccurrenceVariableRef newDest = new MultiOccurrenceVariableRef(destination.type(), "new_" + destination.name());
        if (d.isAssignable()) {
            out.append(statement(newDest.declare(d.newMap())));
        } else {
            out.append(statement(newDest.declare(d)));
            out.append(statement("%s.clear()", newDest));
        }
        
        
        if (d.mapKeyType().equals(s.mapKeyType()) && d.mapValueType().equals(s.mapValueType())) {
            /*
             * Simple map-to-map case: both key and value types are identical
             */
            out.append(statement("%s.putAll(mapperFacade.mapAsMap(%s, %s, %s, mappingContext));", newDest, s, code.usedType(s.type()), code.usedType(d.type())));
        } else {
            VariableRef newKey = new VariableRef(d.mapKeyType(), "new" + StringUtil.capitalize(d.name()) + "Key");
            VariableRef newVal = new VariableRef(d.mapValueType(), "new" + StringUtil.capitalize(d.name()) + "Val");
            VariableRef entry = new VariableRef(TypeFactory.valueOf(Map.Entry.class), "source" + StringUtil.capitalize(d.name()) + "Entry");
            VariableRef sourceKey = new MapEntryRef(s.mapKeyType(), entry.name(), EntryPart.KEY);
            VariableRef sourceVal = new MapEntryRef(s.mapValueType(), entry.name(), EntryPart.VALUE);
            /*
             * Loop through the individual entries, map key/value and then put
             * them into the destination
             */
            append(out,
                    format("for( java.util.Iterator entryIter = %s.entrySet().iterator(); entryIter.hasNext(); ) {\n", s),
                    entry.declare("entryIter.next()"),
                    newKey.declare(),
                    newVal.declare(),
                    code.mapFields(FieldMapBuilder.mapKeys(s.mapKeyType(), d.mapKeyType()), sourceKey, newKey, null, null),
                    code.mapFields(FieldMapBuilder.mapValues(s.mapValueType(), d.mapValueType()), sourceVal, newVal, null, null),
                    format("%s.put(%s, %s)", newDest, newKey, newVal),
                    "\n",
                    "}");
        }
        
        if (d.isAssignable()) {
            out.append(statement(d.assign(newDest)));
        }
        
        String mapNull = shouldMapNulls(fieldMap, code) ? format(" else {\n %s;\n}", d.assignIfPossible("null")): "";
        append(out, "}" + mapNull);
        
        return out.toString();
    }
    
}
