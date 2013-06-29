package ma.glasnost.orika.metadata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * FieldSet provides a recipe for how to collect the exact set of fields
 * which are expected to be mapped for a given type, without being directly
 * tied to any particular type.
 * 
 * @author mattdeboer
 */
public class FieldSet {
    
    /**
     * Specifies a FieldSet which includes all fields for the
     * particular type, including nested fields.
     * 
     * @param exceptions
     * @return a FieldSet composed of <em>all</em> fields for the particular type
     */
    public static FieldSet all() {
        return new FieldSet(false, false, true, new String[0]);
    }
    
    /**
     * Specifies a FieldSet which includes all fields for the
     * particular type, <em>not</em> including nested fields.
     * 
     * @param exceptions
     * @return a FieldSet composed of <em>all non-nested</em> fields for the particular type
     */
    public static FieldSet allNonNested() {
        return new FieldSet(false, false, false, new String[0]);
    }
    
    
    /**
     * Specifies a FieldSet which includes all fields for the
     * particular type other than those explicitly specified
     * in the provided list, including nested fields.
     * 
     * @param exceptions
     * @return a FieldSet composed of <em>all fields except</em> those provided
     */
    public static FieldSet allExcept(String...exceptions) {
        return new FieldSet(true, false, true, exceptions);
    }
    
    /**
     * Specifies a FieldSet which includes all fields for the
     * particular type other than those explicitly specified, 
     * <em>not</em> including nested fields.
     * 
     * @param exceptions
     * @return a FieldSet composed of <em>all non-nested fields except</em> those provided
     */
    public static FieldSet allNonNestedExcept(String...exceptions) {
        return new FieldSet(true, false, false, exceptions);
    }
    
    /**
     * Specifies a FieldSet which may include any fields
     * other than those explicitly specified.
     * 
     * @param exclusions
     * @return a FieldSet composed of <em>none of</em> the provided fields
     */
    public static FieldSet noneOf(String...exclusions) {
        return new FieldSet(true, false, true, exclusions);
    }
    
    /**
     * Specifies a FieldSet which may only include the fields
     * explicitly specified.
     * 
     * @param fields
     * @return a FieldSet composed of <em>only</em> the provided fields
     */
    public static FieldSet only(String...fields) {
        return new FieldSet(false, true, true, fields);
    }

    private final boolean exclusive;
    private final boolean strict;
    private final boolean includesNested;
    private final Set<String> fields;
    
    private FieldSet(boolean isExclusive, boolean isStrict, boolean includesNested, String[] fields) {
        this.exclusive = isExclusive;
        this.strict = isStrict;
        this.includesNested = includesNested;
        this.fields = new HashSet<String>(Arrays.asList(fields));
    }
    
    
    /**
     * Produces an Expectation based on the provided properties
     * 
     * @param allProperties
     * @param aToB the direction of evaluation on a given ClassMap
     * @return an Expectation based on the provided set of properties
     */
    public Expectation toExpectation(Map<String, Property> allProperties, boolean aToB) {
        
        Map<String, Property> properties = new HashMap<String, Property>();
        for (Entry<String, Property> entry: allProperties.entrySet()) {
            if (!entry.getValue().hasPath() || includesNested) {
                if (!(exclusive || strict) || fields.contains(entry.getKey())) {
                    properties.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return new Expectation(exclusive, strict, properties, aToB);
    }
    
    
    
}
