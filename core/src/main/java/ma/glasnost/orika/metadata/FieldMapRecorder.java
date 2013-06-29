package ma.glasnost.orika.metadata;

import java.util.Map;

import ma.glasnost.orika.metadata.FieldMapResult.ActionTaken;


/**
 * FieldMapRecorder
 * 
 * @author mattdeboer
 */
public class FieldMapRecorder {
    
    private final StringBuilder logDetails;
    private final boolean isDebugEnabled;
    private final ClassMap<?, ?> classMap;
    private Map<FieldMap, FieldMapResult> results;
    private boolean aToB = true;
    
    /**
     * @param classMap
     * @param aToB
     * @param isDebugEnabled
     */
    public FieldMapRecorder(ClassMap<?,?> classMap, boolean isDebugEnabled) {
        this.logDetails = new StringBuilder();
        this.isDebugEnabled = isDebugEnabled;
        this.classMap = classMap;
        this.results = classMap.getForwardMappingResults();
    }
    
    /**
     * Flip the direction of field mapping results recorded by this recorder
     */
    public void flip() {
        aToB = !aToB;
        this.results = aToB ? classMap.getForwardMappingResults() : classMap.getReverseMappingResults();
    }
    
    /**
     * @param fieldMap
     * @param description
     */
    public void mapWithDescription(FieldMap fieldMap, String description) {
        results.put(fieldMap, new FieldMapResult(fieldMap, ActionTaken.MAPPED, description));
        if (isDebugEnabled) {
            append(getFieldTag(fieldMap) + description);
        }
    }
    
    /**
     * @param fieldMap
     * @param reason
     */
    public void excludeWithReason(FieldMap fieldMap, String reason) {
        results.put(fieldMap, new FieldMapResult(fieldMap, ActionTaken.EXCLUDED, reason));
        if (isDebugEnabled) {
            append(getFieldTag(fieldMap) + "excuding because " + reason);
        }
    }
    
    /**
     * @param fieldMap
     * @param reason
     */
    public void ignoreWithReason(FieldMap fieldMap, String reason) {
        results.put(fieldMap, new FieldMapResult(fieldMap, ActionTaken.IGNORED, reason));
        if (isDebugEnabled) {
            append(getFieldTag(fieldMap) + "excuding because " + reason);
        }
    }
    
    /**
     * @return true if debug logging is enabled for this recorder
     */
    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }
    
    /**
     * @return the logDetails for this recorder
     */
    public StringBuilder getLogDetails() {
        return logDetails;
    }
    
    /**
     * Append the supplied characters to the debug log
     * 
     * @param chars
     */
    public void append(CharSequence chars) {
        logDetails.append(chars);
    }
    
    /**
     * @param fieldMap
     * @return
     */
    private String getFieldTag(FieldMap fieldMap) {
        return "\n\t Field(" + fieldMap.getSource() + ", " + fieldMap.getDestination() + ") : ";
    }
}
