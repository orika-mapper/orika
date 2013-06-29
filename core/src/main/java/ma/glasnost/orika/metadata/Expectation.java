package ma.glasnost.orika.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ma.glasnost.orika.metadata.FieldMapResult.ActionTaken;

/**
 * Expectation represents a strict declaration about a set of fields that should
 * be mapped, which can be evaluated against a ClassMap.
 * 
 * @author mattdeboer
 */
public class Expectation {
    
    private final boolean exclusive;
    private final boolean strict;
    private final Map<String, Property> fieldNames;
    private final boolean aToB;
    
    /**
     * @param exclusive
     * @param strict
     * @param fieldNames
     * @param aToB
     */
    public Expectation(boolean exclusive, boolean strict, Map<String, Property> fieldNames, boolean aToB) {
        this.exclusive = exclusive;
        this.strict = strict;
        this.fieldNames = fieldNames;
        this.aToB = aToB;
    }
    
    /**
     * Verifies whether this expectation is met regarding the mapping of it's
     * configured fields; if any expected field is not mapped, a
     * FailedExpectationException will be thrown.
     * 
     * @param classMap
     *            the ClassMap against which this expectation should be
     *            evaluated
     * @throws FailedExpectationException
     *             if any expected field is not mapped
     */
    public void evaluate(ClassMap<?, ?> classMap) throws FailedExpectationException {
        
        Map<String, Property> remainingFields = new HashMap<String, Property>(fieldNames);
        Map<FieldMap, FieldMapResult> results = aToB ? classMap.getForwardMappingResults() : classMap.getReverseMappingResults();
        for (Entry<FieldMap, FieldMapResult> entry : results.entrySet()) {
            FieldMap fieldMap = entry.getKey();
            FieldMapResult result = entry.getValue();
            if (remainingFields.containsKey(fieldMap.getDestinationExpression())) {
                /*
                 * If field was found, then we expect:
                 * 
                 *   if exclusive, it was not mapped
                 *   if !exclusive, it was mapped
                 */
                if (!exclusive && ActionTaken.MAPPED != result.getActionTaken()) {
                    throw failedExpectation(fieldMap.getDestinationExpression() + " was " + result.getActionTaken().toString().toLowerCase()
                            + " because " + result.getComment(), classMap, fieldMap.getSource(), fieldMap.getDestination());
                } else if (exclusive && ActionTaken.MAPPED == result.getActionTaken()) {
                    throw failedExpectation(fieldMap.getDestinationExpression() + " was " + result.getActionTaken().toString().toLowerCase()
                            + (fieldMap.isByDefault() ? " by-default" : " explicitly") + " by " + result.getComment(), classMap,
                            fieldMap.getSource(), fieldMap.getDestination());
                }
            } else if (strict && !exclusive && ActionTaken.MAPPED == result.getActionTaken()) {
                /*
                 * If field was not found, then we expect:
                 *   if strict && !exclusive => !mapped
                 *   if exclusive, it was not mapped
                 *   if !exclusive, it was mapped
                 */
                throw failedExpectation(fieldMap.getDestinationExpression() + " was " + result.getActionTaken().toString().toLowerCase()
                        + (fieldMap.isByDefault() ? " by-default" : " explicitly") + " by " + result.getComment(), classMap,
                        fieldMap.getSource(), fieldMap.getDestination());
            }
            remainingFields.remove(fieldMap.getDestinationExpression());
        }
        
        if (!exclusive) {
            for (Entry<String, Property> unmapped : remainingFields.entrySet()) {
                throw failedExpectation("expected mapping for: " + classMap.getBType() + "[ " + unmapped.getKey() + " ]"
                        + ", which was not registered to be mapped, either explicitly or by-default ", classMap, null, unmapped.getValue());
            }
        }
    }
    
    /**
     * Constructs a new FailedExpectationException using the provided details
     * 
     * @param message
     * @param classMap
     * @param source
     * @param dest
     * @return
     */
    private FailedExpectationException failedExpectation(String message, ClassMap<?, ?> classMap, Property source, Property dest) {
        FailedExpectationException e = new FailedExpectationException(message);
        e.setDestinationType(classMap.getBType());
        e.setSourceType(classMap.getAType());
        e.setDestinationProperty(dest);
        e.setSourceProperty(source);
        return e;
    }
    
}
