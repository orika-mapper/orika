package ma.glasnost.orika.impl;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingContextFactory;
import ma.glasnost.orika.metadata.Type;

/**
 * @author mattdeboer
 *
 */
public class NonCyclicMappingContext extends MappingContext {

    /**
     * Factory constructs instances of the base MappingContext
     */
    public static class Factory implements MappingContextFactory {
        
        final LinkedBlockingQueue<MappingContext> contextQueue = new LinkedBlockingQueue<MappingContext>();
        final Map<Object, Object> globalProperties;
        
        /**
         * @param globalProperties
         */
        public Factory(Map<Object, Object> globalProperties) {
            this.globalProperties = globalProperties;
        }
        
        public MappingContext getContext() {
            MappingContext context = contextQueue.poll();
            if (context == null) {
                context = new NonCyclicMappingContext(globalProperties);
            }
            context.containsCycle(false);
            return context;
        }
        
        public void release(MappingContext context) {
            context.reset();
            contextQueue.offer(context);
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see ma.glasnost.orika.MappingContextFactory#getGlobalProperties()
         */
        public Map<Object, Object> getGlobalProperties() {
            return globalProperties;
        }
    }
    
    
    /**
     * @param globalProperties
     */
    protected NonCyclicMappingContext(Map<Object, Object> globalProperties) {
        super(globalProperties);
    }

    @Override
    public <S, D> void cacheMappedObject(S source, Type<Object> destinationType, D destination) {
        // NO-OP
    }

    @Override
    public <D> D getMappedObject(Object source, Type<?> destinationType) {
        return null;
    }
}
