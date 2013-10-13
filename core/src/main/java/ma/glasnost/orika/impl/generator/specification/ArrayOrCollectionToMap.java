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

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.append;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;

import java.util.Map;

import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.impl.generator.MapEntryRef;
import ma.glasnost.orika.impl.generator.MapEntryRef.EntryPart;
import ma.glasnost.orika.impl.generator.MultiOccurrenceVariableRef;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.impl.util.StringUtil;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Type;

/**
 * ArrayOrCollectionToMap handles mapping of an Array or Collection
 * to a Map field
 *
 */
public class ArrayOrCollectionToMap extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getDestination().isMap() && (fieldMap.getSource().isCollection() || fieldMap.getSource().isArray());
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        StringBuilder out = new StringBuilder();
        
        MultiOccurrenceVariableRef d = MultiOccurrenceVariableRef.from(destination);
        MultiOccurrenceVariableRef s = MultiOccurrenceVariableRef.from(source);
        MultiOccurrenceVariableRef newDest = new MultiOccurrenceVariableRef(d.type(), "new_" + d.name());
        
        append(out,
                s.ifNotNull() + " {");
        
        if (d.isAssignable()) {
            out.append(statement(newDest.declare(newDest.newInstance(d.newMap()))));
        } else {
            out.append(statement(newDest.declare(d)));
            out.append(statement("%s.clear()", newDest));
        }
        
        VariableRef element = new VariableRef(s.elementType(), "source" + StringUtil.capitalize(s.name()) + "Element");
        
        @SuppressWarnings("unchecked")
        Type<MapEntry<Object, Object>> entryType = MapEntry.concreteEntryType((Type<? extends Map<Object, Object>>) d.type());
        
        VariableRef newEntry = new VariableRef(entryType, "source" + StringUtil.capitalize(s.name()) + "Entry");
        VariableRef newKey = new MapEntryRef(newEntry.type(), newEntry.name(), EntryPart.KEY);
        VariableRef newVal = new MapEntryRef(newEntry.type(), newEntry.name(), EntryPart.VALUE);
        /*
         * Loop through the individual entries, map key/value and then put them
         * into the destination
         */
        if (s.isArray()) {
            if (code.isDebugEnabled()) {
                code.debugField(fieldMap, "mapping " + s.elementTypeName() + "[] to Map<" + d.type().getNestedType(0) + ", " + d.type().getNestedType(1) + ">");
            }
            append(out,
                    format("for( int entryIndex = 0, entryLen = %s.length; entryIndex < entryLen; ++entryIndex ) {\n", s),
                    element.declare("%s[entryIndex]", s),
                    newEntry.declare("mapperFacade.map(%s, %s, %s, mappingContext)", element, code.usedType(element), code.usedType(newEntry)),
                    "\n",
                    format("%s.put(%s, %s)", newDest, newKey, newVal),
                    "}");
        } else {
            if (code.isDebugEnabled()) {
                code.debugField(fieldMap, "mapping Collection<" + s.elementTypeName() + "> to Map<" + d.type().getNestedType(0) + ", " + d.type().getNestedType(1) + ">");
            }
            append(out,
                    format("for( java.util.Iterator entryIter = %s.iterator(); entryIter.hasNext(); ) {\n", s),
                    element.declare("entryIter.next()"),
                    newEntry.declare("mapperFacade.map(%s, %s, %s, mappingContext)", element, code.usedType(element), code.usedType(newEntry)),
                    "\n",
                    format("%s.put(%s, %s)", newDest, newKey, newVal),
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
