package ma.glasnost.orika;

import ma.glasnost.orika.converter.ConverterFactory;

/**
 * A utility class for writing out the current state of Reportable
 * mapping components.
 * 
 * @author mattdeboer
 */
public final class StateReporter {

	public static final String DIVIDER = "\n-------------------------------------------------------------";

	private StateReporter() {}
	
	/**
     * Reports the current state of the provided MapperFactory and it's
     * contained component parts.
     * 
     * @param out
     * @param mapperFactory
     */
    public static void reportCurrentState(StringBuilder out, MapperFactory mapperFactory) {
    	ConverterFactory converterFactory = mapperFactory.getConverterFactory();
    	if (converterFactory instanceof Reportable) {
	    	((Reportable)converterFactory).reportCurrentState(out);
    	}
    	if (mapperFactory instanceof Reportable) {
    		((Reportable)mapperFactory).reportCurrentState(out);
    	}
    	MapperFacade mapperFacade = mapperFactory.getMapperFacade();
    	if (mapperFacade instanceof Reportable) {
    		((Reportable)mapperFacade).reportCurrentState(out);
    	}
    }
	
    /**
     * Prints out the size of the specified object(s) in approximate kB of memory used.
     * 
     * @param objects
     * @return
     */
    public static String humanReadableSizeInMemory(Object...objects) {
    	long size = 0;
    	return String.format("%,.1f kB", size/1000.0);
    }
    
	/**
	 * Marks implementations as capable of reporting on their current
	 * state, typically for troubleshooting purposes.
	 * 
	 * @author mattdeboer
	 *
	 */
	public interface Reportable {

		/**
		 * Writes details of the current state of this object to the provided
		 * StringBuilder.
		 * 
		 * @param out
		 */
		void reportCurrentState(StringBuilder out);
	}
	
}
