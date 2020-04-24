/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.glasnost.orika.impl.generator.specification;

import ma.glasnost.orika.impl.generator.MapEntryRef;
import ma.glasnost.orika.impl.generator.MapEntryRef.EntryPart;
import ma.glasnost.orika.impl.generator.MultiOccurrenceVariableRef;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.impl.util.StringUtil;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.FieldMapBuilder;
import ma.glasnost.orika.metadata.TypeFactory;

import java.util.Map;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.append;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;

/**
 * MapToMap handles conversion of a Map to another Map
 *
 */
public class MapToMap extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getSource().isMap() && fieldMap.getDestination().isMap();
    }

    @Override
    public String generateMappingCode(FieldMap fieldMap, VariableRef source, String sourceValue, VariableRef destination, SourceCodeContext code) {

        MultiOccurrenceVariableRef d = MultiOccurrenceVariableRef.from(destination);
        MultiOccurrenceVariableRef s = MultiOccurrenceVariableRef.from(source);

        if (code.isDebugEnabled()) {
            code.debugField(fieldMap, "mapping from Map<" + source.type().getNestedType(0) + ", " +
                    source.type().getNestedType(1) + "> to Map<" + destination.type().getNestedType(0) + ", " +
                    destination.type().getNestedType(1) + ">");
        }

        StringBuilder out = new StringBuilder();

        out.append(s.ifNotNull(sourceValue));
        out.append("{\n");

        MultiOccurrenceVariableRef newDest = new MultiOccurrenceVariableRef(destination.type(), "new_" + destination.name());
        if (d.isAssignable()) {
            out.append(statement(newDest.declare(d.newMap())));
        } else {
            out.append(statement(newDest.declare(d)));
            out.append(statement("%s.clear()", newDest));
        }

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
                format("for( %s; %s; ) { \n", s.declareIterator(sourceValue), s.iteratorHasNext()),
                entry.declare(s.nextElement()),
                newKey.declare(),
                newVal.declare(),
                code.mapFields(FieldMapBuilder.mapKeys(s.mapKeyType(), d.mapKeyType()), sourceKey, newKey),
                code.mapFields(FieldMapBuilder.mapValues(s.mapValueType(), d.mapValueType()), sourceVal, newVal),
                format("%s.put(%s, %s)", newDest, newKey, newVal),
                "\n",
                "}");

        if (d.isAssignable()) {
            out.append(statement(d.assign(newDest)));
        }

        String mapNull = shouldMapNulls(fieldMap, code) ? format(" else {\n %s;\n}", d.assignIfPossible("null")): "";
        append(out, "}" + mapNull);

        return out.toString();
    }
    
}
