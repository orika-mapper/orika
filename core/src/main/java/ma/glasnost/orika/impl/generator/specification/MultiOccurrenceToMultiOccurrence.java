package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.AggregateSpecification;
import ma.glasnost.orika.impl.generator.Node;
import ma.glasnost.orika.impl.generator.Node.NodeList;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * MultiOccurrenceToMultiOccurrence handles the mapping of one or more
 * multi-occurrence source fields to one or more multi-occurrence destination
 * fields.
 */
public class MultiOccurrenceToMultiOccurrence implements AggregateSpecification {
    
    /**
     * The MapperFactory relevant to this code generation request
     */
    protected MapperFactory mapperFactory;
    
    /**
     * Generates the code to support a (potentially parallel) mapping from one
     * or more multi-occurrence fields in the source type to one or more
     * multi-occurrence fields in the destination type.
     * 
     * @param fieldMappings
     *            the field mappings to be applied
     * @param code 
     * @param logDetails
     *            a StringBuilder to accept debug logging information
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public String fromMultiOccurrenceToMultiOccurrence(List<FieldMap> fieldMappings, SourceCodeContext code) {
        
        StringBuilder out = new StringBuilder();
        while (!fieldMappings.isEmpty()) {
            Set<FieldMap> associated = code.getAssociatedMappings(fieldMappings, fieldMappings.get(0));
            fieldMappings.removeAll(associated);
            
            NodeList sourceNodes = new NodeList();
            NodeList destNodes = new NodeList();
            
            for (FieldMap map : associated) {

                Node.addFieldMap(map, sourceNodes, true);
                Node.addFieldMap(map, destNodes, false);
            }    
              
            registerClassMaps(sourceNodes, destNodes);
            
            out.append(generateMultiOccurrenceMapping(sourceNodes, destNodes, associated, code));
        }
        return out.toString();
    }
    
    /**
     * Generates the code to support a (potentially parallel) mapping from one
     * or more multi-occurrence fields in the source type to one or more
     * multi-occurrence fields in the destination type.
     * @param sourceNodes 
     * @param destNodes 
     * 
     * @param sources
     *            the associated source variables
     * @param destinations
     *            the associated destination variables
     * @param subFields
     *            the nested properties of the individual field maps
     * @param code 
     * @param logDetails
     *            a StringBuilder to accept debug logging information
     * @return a reference to <code>this</code> CodeSourceBuilder
     */
    public String generateMultiOccurrenceMapping(NodeList sourceNodes, NodeList destNodes,
            Set<FieldMap> subFields, SourceCodeContext code) {
        
        StringBuilder out = new StringBuilder();
        
        /*
         * Construct size/length expressions used to limit the parallel iteration
         * of multiple source variables; only keep iterating so long as all variables
         * in a parallel set are non-empty
         */
        List<String> sourceSizes = new ArrayList<String>();
        for (Node ref : sourceNodes) {
            if (!ref.isLeaf()) {
                sourceSizes.add(ref.multiOccurrenceVar.size());
            }
        }
        
        String sizeExpr = join(sourceSizes, ", ");
        if (!"".equals(sizeExpr)) {
            sizeExpr = "min(new int[]{" + sizeExpr + "})";
        }
        
        /*
         * Declare "collector" elements and their iterators; used for aggregating
         * results which are finally assigned/copied back into their final destination(s)
         */
        for (Node destRef : destNodes) {
            
            if (!destRef.isLeaf()) {
                out.append(statement(destRef.newDestination.declare(destRef.newDestination.newInstance(sizeExpr))));
                if (destRef.newDestination.isArray()) {
                    out.append(statement(destRef.newDestination.declareIterator()));
                }
                List<Node> children = new ArrayList<Node>();
                children.add(destRef);
                while (!children.isEmpty()) {
                    Node child = children.remove(0);
                    children.addAll(child.children);
                    if (child.elementRef != null) {
                        out.append(statement(child.elementRef.declare()));
                        if (child.multiOccurrenceVar.isArray()) {
                            out.append(statement(child.multiOccurrenceVar.declareIterator()));
                        }
                        if (child.elementRef.isPrimitive()) {
                            out.append(statement(child.nullCheckFlag.declare("true")));
                        }
                        out.append(statement(child.shouldAddToCollectorFlag.declare("false")));
                    }
                }
            }
        }
        
        // TODO: need to create a flag variable to mark whether destination has been added
        // to it's collector; it should be set to false upon new destination element creation
        
        StringBuilder endWhiles = new StringBuilder();
        StringBuilder addLastElement = new StringBuilder();
        
        iterateSources(sourceNodes, destNodes, out, endWhiles);
        
        LinkedList<Node> stack = new LinkedList<Node>(destNodes);
        while (!stack.isEmpty()) {
            
            Node currentNode = stack.removeFirst();
            stack.addAll(0, currentNode.children);
            Node srcNode = null;
            if (currentNode.value != null) {
                srcNode = Node.findFieldMap(currentNode.value, sourceNodes, true);
            } else {
                FieldMap fieldMap = currentNode.getMap();
                if (fieldMap != null) {
                    srcNode = Node.findFieldMap(fieldMap, sourceNodes, true).parent;
                }
            }
            
            if (!currentNode.isLeaf() && srcNode != null) { 
                /*
                 * Add a comparison for the next source element; if it is "different" than 
                 * it's destination (determined by custom comparator we've generated), then
                 * we create a new element and add it to the destination collector.
                 */
                String currentElementNull = currentNode.elementRef.isPrimitive() ? currentNode.nullCheckFlag.toString() : currentNode.elementRef.isNull();
                String currentElementComparator = code.currentElementComparator(srcNode, currentNode, sourceNodes, destNodes);
                String or = (!"".equals(currentElementNull) && !"".equals(currentElementComparator)) ? " || " : "";
                
                if (mapperFactory.getConverterFactory().canConvert(srcNode.elementRef.type(), currentNode.elementRef.type()) //) {
                        || ClassUtil.isImmutable(currentNode.elementRef.type())) {
                
                    append(out,
                            (currentNode.elementRef.isPrimitive() ? currentNode.nullCheckFlag.assign("false") : ""),
                            currentNode.shouldAddToCollectorFlag.assign("true")
                            );
                    
                
                } else {
                
                    append(out,
                            "if ( " + currentElementNull + or + currentElementComparator + ") {\n",
                            currentNode.elementRef.assign(code.newObject(srcNode.elementRef, currentNode.elementRef.type())),
                            currentNode.shouldAddToCollectorFlag.assign("true"),
                            "}");
                }
            }
            
            if (currentNode.value != null) {
                
                /*
                 * If we have a fieldMap for the current node, attempt to map the fields
                 */
                boolean wasConverted = mapFields(currentNode, srcNode, out, code);
                if (currentNode.parent != null 
                        && currentNode.parent.elementRef != null 
                        && !currentNode.parent.addedToCollector) {
                    
                    String assignNull = (currentNode.parent.elementRef.isPrimitive() ? currentNode.parent.nullCheckFlag.assign("true") : currentNode.parent.elementRef.assign("null"));
                    if (mapperFactory.getConverterFactory().canConvert(srcNode.parent.elementRef.type(), currentNode.parent.elementRef.type())) {
                        append(out,
                                (currentNode.parent.isRoot() ? currentNode.parent.newDestination.add(currentNode.parent.elementRef) : currentNode.parent.multiOccurrenceVar.add(currentNode.parent.elementRef)),
                                assignNull
                                );
                    } else {
                        append(out,
                                format("if (%s) {", currentNode.parent.shouldAddToCollectorFlag),
                                (currentNode.parent.isRoot() ? currentNode.parent.newDestination.add(currentNode.parent.elementRef) : currentNode.parent.multiOccurrenceVar.add(currentNode.parent.elementRef)),
                                currentNode.parent.shouldAddToCollectorFlag.assign("false"),
                                (wasConverted ? assignNull : ""),
                                "}");
                    }
                    currentNode.parent.addedToCollector = true;
                }
            }
        }  
        
        out.append(endWhiles.toString());
        out.append(addLastElement.toString());
        
        /*
         * Finally, we loop over the destination nodes and assign/copy all of the temporary 
         * "collector" variables back into their final destination
         */
        for (Node destRef : destNodes) {
            if (destRef.isRoot() && !destRef.isLeaf()) {
                if (destRef.multiOccurrenceVar.isArray() || destRef.multiOccurrenceVar.isMap()) {
                    /*
                     * We use a List as the temporary collector element for Arrays and Maps
                     */
                    append(out,
                            format("if (%s && %s) {",destRef.newDestination.notNull(), destRef.newDestination.notEmpty()),
                            destRef.multiOccurrenceVar.addAll(destRef.newDestination),
                            "}\n");
                } else {
                    append(out,
                            format("if (%s && %s) {",destRef.newDestination.notNull(), destRef.newDestination.notEmpty()),
                            format("if (%s) {", destRef.multiOccurrenceVar.isNull()),
                            destRef.multiOccurrenceVar.assignIfPossible(destRef.multiOccurrenceVar.newInstance(sizeExpr)),
                            "} else {\n",
                            destRef.multiOccurrenceVar + ".clear()",
                            "}\n",
                            destRef.multiOccurrenceVar.addAll(destRef.newDestination),
                            "}\n");
                }
            }
        }
        
        return out.toString();
    }
    
    private Property innermostElement(final Property p) {
        Property result = p;
        while (result.getElement() != null) {
            result = result.getElement();
        }
        return result;
    }
    
    private boolean mapFields(Node currentNode, Node srcNode, StringBuilder out, SourceCodeContext code) {
        
        String srcName = srcNode.parent != null ? srcNode.parent.elementRef.name() : "source";
        
        Property sp = innermostElement(currentNode.value.getSource());
        Property srcProp = new Property.Builder().merge(sp).expression(innermostElement(currentNode.value.getSource()).getExpression()).build();
        VariableRef s = new VariableRef(srcProp, srcName);
        
        Property dp = innermostElement(currentNode.value.getDestination());
        Property dstProp = new Property.Builder().merge(dp).expression(innermostElement(currentNode.value.getDestination()).getExpression()).build();
        String dstName =  "destination";
        if (currentNode.parent != null ) {
            dstName = currentNode.parent.elementRef.name();
        }
        
        VariableRef d = new VariableRef(dstProp, dstName);
        
        Type<?> destType = currentNode.parent != null ? currentNode.parent.elementRef.type() : null;
        
        out.append(statement(code.mapFields(currentNode.value, s, d, destType, null)));
        
        Type<?> parentElementType = currentNode.parent != null ? currentNode.parent.elementRef.type() : TypeFactory.TYPE_OF_OBJECT;
        
        return d.type().equals(parentElementType) && mapperFactory.getConverterFactory().canConvert(s.type(), d.type());
    }
    
    
    /**
     * Register the ClassMaps needed to map this pair of source and
     * destination nodes.
     * 
     * @param sourceNodes
     * @param destNodes
     */
    private void registerClassMaps(NodeList sourceNodes, NodeList destNodes) {
        /*
         * Register all of the subordinate ClassMaps needed by this multi-occurrence
         * mapping 
         */
        Map<MapperKey, ClassMapBuilder<?,?>> builders = new HashMap<MapperKey, ClassMapBuilder<?,?>>();
        
        LinkedList<Node> stack = new LinkedList<Node>(destNodes);
        while (!stack.isEmpty()) {
            
            Node currentNode = stack.removeFirst();
            stack.addAll(0, currentNode.children);
            Node srcNode = null;
            if (currentNode.value != null) {
                srcNode = Node.findFieldMap(currentNode.value, sourceNodes, true);
            } else {
                FieldMap fieldMap = currentNode.getMap();
                if (fieldMap != null) {
                    srcNode = Node.findFieldMap(fieldMap, sourceNodes, true).parent;
                }
            }
        
            if (srcNode != null 
                    && srcNode.parent != null 
                    && srcNode.parent.elementRef != null
                    && currentNode != null
                    && currentNode.parent != null 
                    && currentNode.parent.elementRef != null) {
                
                MapperKey key = new MapperKey(srcNode.parent.elementRef.type(), currentNode.parent.elementRef.type());
                if (!ClassUtil.isImmutable(key.getAType()) 
                        && !ClassUtil.isImmutable(key.getBType()) 
                        && !mapperFactory.existsRegisteredMapper(key.getAType(), key.getBType(), true)) {
                    ClassMapBuilder<?,?> builder = builders.get(key);
                    if (builder == null) {
                        builder = mapperFactory.classMap(key.getAType(), key.getBType());
                        builders.put(key, builder);
                    }
                    Property sp = innermostElement(currentNode.value.getSource());
                    Property dp = innermostElement(currentNode.value.getDestination()); 
                    builder.fieldMap(sp.getExpression(), dp.getExpression()).add();
                }
            }
        }
        
        
        for (ClassMapBuilder<?,?> builder: builders.values()) {
            builder.register();
        }
    }
    
    /**
     * Creates the looping constructs for nested source variables
     * 
     * @param sourceNodes
     * @param destNodes
     * @param out
     * @param endWhiles
     */
    private void iterateSources(NodeList sourceNodes, NodeList destNodes, StringBuilder out, StringBuilder endWhiles) {
        
        if (!sourceNodes.isEmpty()) {
            for (Node srcRef : sourceNodes) {
                if (!srcRef.isLeaf()) {
                    out.append(srcRef.multiOccurrenceVar.ifNotNull()).append(" {\n");
                    endWhiles.append("\n}");
                    out.append(statement(srcRef.multiOccurrenceVar.declareIterator()));
                }
            }
            
            StringBuilder loopSource = new StringBuilder();
            /*
             * Create while loop for the top level multi-occurrence objects
             */
            loopSource.append("while (");
            Iterator<Node> sourcesIter = sourceNodes.iterator();
            boolean atLeastOneIter = false;
            while (sourcesIter.hasNext()) {
                Node ref = sourcesIter.next();
                if (!ref.isLeaf()) {
                    if (atLeastOneIter) {
                        loopSource.append(" && ");
                    }
                    loopSource.append(ref.multiOccurrenceVar.iteratorHasNext());
                    atLeastOneIter = true;
                }
            }
            loopSource.append(") {");
            
            if (atLeastOneIter) {
                out.append("\n");
                out.append(loopSource.toString());
            }
            for (Node srcRef : sourceNodes) {
                
                if (!srcRef.isLeaf()) {
                    out.append(statement(srcRef.elementRef.declare(srcRef.multiOccurrenceVar.nextElement())));
                    iterateSources(srcRef.children, destNodes, out, endWhiles);
                }
            }
            if (atLeastOneIter) {
                endWhiles.append("}\n");
            }
        }
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.impl.generator.AggregateSpecification#appliesTo(ma.glasnost.orika.metadata.FieldMap)
     */
    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getSource().getContainer() != null || fieldMap.getDestination().getContainer() != null;
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.impl.generator.AggregateSpecification#generateMappingCode(java.util.Set, ma.glasnost.orika.impl.generator.SourceCode)
     */
    public String generateMappingCode(List<FieldMap> fieldMappings, SourceCodeContext code) {
        return this.fromMultiOccurrenceToMultiOccurrence(fieldMappings, code);
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.impl.generator.AggregateSpecification#setMapperFactory(ma.glasnost.orika.MapperFactory)
     */
    public void setMapperFactory(MapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
    }
}
