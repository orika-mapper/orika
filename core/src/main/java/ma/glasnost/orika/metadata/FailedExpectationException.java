package ma.glasnost.orika.metadata;

import ma.glasnost.orika.MappingException;

/**
 * FailedExpectationException represents an expectation regarding a mapping
 * result for a ClassMap which was not met.
 * 
 * @author mattdeboer
 */
public class FailedExpectationException extends MappingException {
    
    private static final long serialVersionUID = 1L;
    private final String message;
    /**
     * @param message
     */
    public FailedExpectationException(String message) {
        super(message);
        this.message = message;
    }
    
    public String getLocalizedMessage() {
        
        StringBuilder messageOutput = new StringBuilder(message);
        String inputs = describeInputs();
        if (inputs.length() > 0) {
            messageOutput.append(inputs);
        }
        
        return messageOutput.toString();
    }
    
}
