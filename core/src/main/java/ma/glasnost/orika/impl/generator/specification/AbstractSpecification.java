package ma.glasnost.orika.impl.generator.specification;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.Specification;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * AbstractSpecification provides the base implementation for Specification
 */
public abstract class AbstractSpecification implements Specification {
    
    /**
     * 
     */
    protected MapperFactory mapperFactory;
    
    public void setMapperFactory(MapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
    }
    
    /**
     * Tests whether this fieldMap should map nulls;
     * 
     * @param fieldMap
     * @param context
     * @return true if nulls should be mapped for this FeildMap
     */
    public static boolean shouldMapNulls(FieldMap fieldMap, SourceCodeContext context) {
        Boolean mapNull = fieldMap.isDestinationMappedOnNull();
        if (mapNull == null) {
            mapNull = context.shouldMapNulls();
        }
        return mapNull;
    }
    
    public abstract boolean appliesTo(FieldMap fieldMap);
    
    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return source + ".equals(" + destination + ")";
    }
    
    public abstract String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code);
    
}
