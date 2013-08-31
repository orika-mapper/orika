package ma.glasnost.orika.impl.mapping.strategy;

import ma.glasnost.orika.metadata.Type;

/**
 * AbstractMappingStrategy provides base MappingStrategy functionality
 */
public abstract class AbstractMappingStrategy implements MappingStrategy {

    /**
     * The source type mapped by this strategy
     */
    protected final Type<Object> sourceType;
    /**
     * The destination type mapped by this strategy
     */
    protected final Type<Object> destinationType;
    
    /**
     * @param sourceType
     * @param destinationType
     */
    public AbstractMappingStrategy(Type<Object> sourceType, Type<Object> destinationType) {
        this.sourceType = sourceType;
        this.destinationType = destinationType;
    }
    
    public Type<Object> getAType() {
        return sourceType;
    }

    public Type<Object> getBType() {
        return destinationType;
    }
    
    public String toString() {
        return getClass().getSimpleName() + "(" + sourceType + ", " + destinationType + ")";
    }
    
}
