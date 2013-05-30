package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.append;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.generator.MultiOccurrenceVariableRef;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;

public class ArrayOrCollectionToCollection extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return (fieldMap.getSource().isArray() || fieldMap.getSource().isCollection()) && fieldMap.getDestination().isCollection();
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        // TODO:
        return "";
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        StringBuilder out = new StringBuilder();
        
        MultiOccurrenceVariableRef s = MultiOccurrenceVariableRef.from(source);
        MultiOccurrenceVariableRef d = MultiOccurrenceVariableRef.from(destination);
        
        final Class<?> destinationElementClass = d.elementType().getRawType();
        
        if (destinationElementClass == null) {
        	final String dc = destination.getOwner() == null ? "null" : destination.getOwner().rawType().getName();
            throw new MappingException("cannot determine runtime type of destination collection " + dc + "." + d.name());
        }
        
        // Start check if source property ! = null
        out.append(s.ifNotNull() + " {\n");
        
        /*
         *  TODO: migrate this to create a new destination variable first; 
         *  fill it, and then assign it to the destination using the setter. 
         */
       
        MultiOccurrenceVariableRef newDest = new MultiOccurrenceVariableRef(d.type(), "new_" + d.name());
        if (d.isAssignable()) {
            out.append(statement(newDest.declare(d.newInstance(source.size()))));
        } else {
            out.append(statement(newDest.declare(""+d)));
            out.append(statement("%s.clear()", newDest));
        }
        
        if (s.isArray()) {
            if (code.isDebugEnabled()) {
                code.debug("mapping " + s.elementTypeName() + "[] to Collection<" + d.elementTypeName() + ">");
            }
            
            if (s.elementType().isPrimitive()) {
                out.append("\n");
                out.append(statement("%s.addAll(asList(%s));", newDest, s));
            } else {
                out.append("\n");
                out.append(statement("%s.addAll(mapperFacade.mapAsList(asList(%s), %s.class, mappingContext));", newDest, s, d.elementTypeName()));
            }
        } else {
            if (code.isDebugEnabled()) {
                code.debug("mapping Collection<" + s.elementTypeName() + "> to Collection<" + d.elementTypeName() + ">");
            }
            append(out,
                    "\n",
                    format("%s.addAll(mapperFacade.mapAs%s(%s, %s, %s, mappingContext))", newDest, d.collectionType(), s,
                            code.usedType(s.elementType()), code.usedType(d.elementType())));
        }
        if (fieldMap.getInverse() != null) {
            final MultiOccurrenceVariableRef inverse = new MultiOccurrenceVariableRef(fieldMap.getInverse(), "orikaCollectionItem");
            
            if (fieldMap.getInverse().isCollection()) {
                append(out,
                          format("for (java.util.Iterator orikaIterator = %s.iterator(); orikaIterator.hasNext();) { ", newDest),
                          format("    %s orikaCollectionItem = (%s) orikaIterator.next();", d.elementTypeName(), d.elementTypeName()),
                          format("    %s { %s; }", inverse.ifNull(), inverse.assignIfPossible(inverse.newCollection())),
                          format("    %s.add(%s)", inverse, d.owner()),
                          "}");
                
            } else if (fieldMap.getInverse().isArray()) {
                out.append(" // TODO support array");
            } else {
                append(out,
                        format("for (java.util.Iterator orikaIterator = %s.iterator(); orikaIterator.hasNext();) { ", newDest),
                        format("    %s orikaCollectionItem = (%s) orikaIterator.next();", d.elementTypeName(), d.elementTypeName()),
                        inverse.assign(d.owner()),
                        "}");
                
            }
        }
        // End check if source property ! = null
        if (d.isAssignable()) {
            out.append(statement(d.assign(newDest)));
        }
        
        String mapNull = shouldMapNulls(fieldMap, code) ? format(" else {\n %s;\n}", d.assignIfPossible("null")): "";
        
        append(out, "}" + mapNull);
        
        return out.toString();
    }
    
}
