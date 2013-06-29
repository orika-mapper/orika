package ma.glasnost.orika.metadata;

/**
 * FieldMapResult represents the details regarding how the mapping of a given
 * FieldMap was handled during the mapping object generation process.
 * 
 * @author mattdeboer
 */
public class FieldMapResult {
    
    /**
     * ActionTaken represents the action taken in generating mapping code for
     * the associated FieldMap
     * 
     */
    public enum ActionTaken {
        /**
         * The pair of fields were mapped
         */
        MAPPED,
        /**
         * The field mapping was excluded
         */
        EXCLUDED,
        /**
         * The field mapping was ignored
         */
        IGNORED
    }
    
    private final FieldMap fieldMap;
    private final ActionTaken actionTaken;
    private final String comment;
    
    /**
     * @param fieldMap
     * @param actionTaken
     * @param comment
     */
    public FieldMapResult(FieldMap fieldMap, ActionTaken actionTaken, String comment) {
        super();
        this.fieldMap = fieldMap;
        this.actionTaken = actionTaken;
        this.comment = comment;
    }
    
    /**
     * @return the fieldMap
     */
    public FieldMap getFieldMap() {
        return fieldMap;
    }
    
    /**
     * @return the actionTaken
     */
    public ActionTaken getActionTaken() {
        return actionTaken;
    }
    
    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }
    
    public String toString() {
        return "'" + fieldMap.getDestination() + "' was " + getActionTaken().toString().toLowerCase()
                + (ActionTaken.MAPPED == getActionTaken() ? " by " : " because ") + getComment();
    }
    
}
